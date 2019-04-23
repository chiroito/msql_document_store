import com.mysql.cj.xdevapi.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Demo3 {

    public static void main(String[] args) throws IOException {

        Properties prop = new Properties();
        prop.load(Files.newBufferedReader(Path.of("xdev.properties")));

        SessionFactory sf = new SessionFactory();
        Session session = sf.getSession(prop.getProperty("URL"));
        Schema schema = session.createSchema(prop.getProperty("SCHEMA"), true);

        try {
            // 1. Add 1,000 contents into collection
            final Collection contents = schema.createCollection("contents", true);
            for (int i = 0; i < 1000; i++) {
                JsonString title = new JsonString().setValue("Title");
                JsonNumber area = new JsonNumber().setValue("" + (i % 50));
                JsonString user = new JsonString().setValue("00005c96cc54000000000000c8d7");
                JsonString text = new JsonString().setValue("xxx");
                DbDoc d = new DbDocImpl().add("title", title).add("area", area).add("user", user).add("text", text);
                contents.add(d).execute();
            }

            // 2. Show explain plan
            System.out.println(String.format("EXPLAIN SELECT doc FROM `%s`.`contents` WHERE (JSON_EXTRACT(doc,'$.area') = 1);", schema.getName()));

            // 3. Add index by "area" field in collection
            contents.createIndex("areaIndex", "{\"fields\": [{\"field\":\"$.area\", \"type\":\"INTEGER\"}]}");
            session.sql(String.format("ANALYZE TABLE %s.%s;", schema.getName(), contents.getName()));

            // 4. Show index
            System.out.println(String.format("SHOW INDEX FROM %s.%s;", schema.getName(), contents.getName()));

            // 5. Show explain plan again
            System.out.println(String.format("EXPLAIN SELECT doc FROM `%s`.`contents` WHERE (JSON_EXTRACT(doc,'$.area') = 1);", schema.getName()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            session.dropSchema(schema.getName());
            session.close();
        }
    }
}
