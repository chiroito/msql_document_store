import com.mysql.cj.xdevapi.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Demo4 {

    public static void main(String[] args) throws IOException {

        Properties prop = new Properties();
        prop.load(Files.newBufferedReader(Path.of("xdev.properties")));

        SessionFactory sf = new SessionFactory();
        Session session = sf.getSession(prop.getProperty("URL"));
        Schema schema = session.createSchema(prop.getProperty("SCHEMA"), true);

        try {
            // 1. Insert 50 area
            session.sql(String.format("CREATE TABLE %s.area (id INT NOT NULL PRIMARY KEY, name VARCHAR(32) NOT NULL)", schema.getName())).execute();
            Table area = schema.getTable("area");
            IntStream.range(0, 50).mapToObj(id -> "" + id)
                    .forEach(id -> area.insert("id", "name").values(id, "area" + id).execute());
            session.sql(String.format("ANALYZE TABLE %s.%s;", schema.getName(), area.getName()));

            // 1. Add 100 user
            Collection users = schema.createCollection("users", true);
            List<String> userIds = IntStream.range(0, 100)
                    .mapToObj(num -> new DbDocImpl().add("name", new JsonString().setValue("user" + num)))
                    .map(d -> users.add(d).execute().getGeneratedIds().get(0)).collect(Collectors.toList());
            session.sql(String.format("ANALYZE TABLE %s.%s;", schema.getName(), users.getName()));

            // 1. Add 1000 content
            final Collection contents = schema.createCollection("contents", true);
            for (int i = 0; i < 1000; i++) {
                JsonString title = new JsonString().setValue("Title");
                JsonNumber areaField = new JsonNumber().setValue("" + (i % 50));
                JsonString user = new JsonString().setValue(userIds.get(i % userIds.size()));
                JsonString text = new JsonString().setValue("xxx");
                DbDoc d = new DbDocImpl().add("title", title).add("area", areaField).add("user", user).add("text", text);
                contents.add(d).execute();
            }
            session.sql(String.format("ANALYZE TABLE %s.%s;", schema.getName(), contents.getName()));

            // 2. Show explain plan of joining a collection and a table
            System.out.println(String.format("EXPLAIN SELECT c.doc->>'$.title' AS title, a.name AS area FROM %s.contents c LEFT OUTER JOIN %s.area a ON c.doc->>'$.area' = a.id;", schema.getName(), schema.getName()));

            // 3. Show explain plan of joining two collections using doc->>'$._id'
            System.out.println(String.format("EXPLAIN SELECT c.doc->>'$.title' AS title, u.doc->>'$.name' AS user FROM %s.contents c LEFT OUTER JOIN %s.users u ON c.doc->>'$.user' = u.doc->>'$._id';", schema.getName(), schema.getName()));

            // 4. Show explain plan of joining two collections using _id
            System.out.println(String.format("EXPLAIN SELECT c.doc->>'$.title' AS title, u.doc->>'$.name' AS user FROM %s.contents c LEFT OUTER JOIN %s.users u ON c.doc->>'$.user' = u._id;", schema.getName(), schema.getName()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            session.dropSchema(schema.getName());
            session.close();
        }
    }
}
