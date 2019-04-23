import com.mysql.cj.xdevapi.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Demo2 {

    public static void main(String[] args) throws IOException {

        Properties prop = new Properties();
        prop.load(Files.newBufferedReader(Path.of("xdev.properties")));

        SessionFactory sf = new SessionFactory();
        Session session = sf.getSession(prop.getProperty("URL"));
        Schema schema = session.createSchema(prop.getProperty("SCHEMA"), true);

        try {
            // 1. Add a contents into collection
            final Collection contents = schema.createCollection("contents", true);
            DbDoc doc = new DbDocImpl().add("title", new JsonString().setValue("MySQL Document store")).add("area", new JsonNumber().setValue("1")).add("user", new JsonString().setValue("00005c96cc54000000000000c8d7")).add("text", new JsonString().setValue("Cool!"));
            contents.add(doc).execute();

            // 2. Show all contents
            System.out.println(String.format("SELECT * FROM %s.contents;", schema.getName()));

            // 3. Find all contents
            contents.find().execute().forEach(d -> System.out.println(String.format("| %s | %s |", d.get("_id"), d.get("title"))));

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            session.dropSchema(schema.getName());
            session.close();
        }
    }
}
