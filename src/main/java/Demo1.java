import com.mysql.cj.xdevapi.Collection;
import com.mysql.cj.xdevapi.Schema;
import com.mysql.cj.xdevapi.Session;
import com.mysql.cj.xdevapi.SessionFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Demo1 {

    public static void main(String[] args) throws IOException {

        Properties prop = new Properties();
        prop.load(Files.newBufferedReader(Path.of("xdev.properties")));

        SessionFactory sf = new SessionFactory();
        Session session = sf.getSession(prop.getProperty("URL"));

        // 1. Show all schemas
        System.out.println("SHOW DATABASES;");

        // 2. Create schema
        Schema schema = session.createSchema(prop.getProperty("SCHEMA"), true);

        // 3. Show all schemas again
        System.out.println("SHOW DATABASES;");

        try {
            // 4. Create Collection "Contents"
            Collection contents = schema.createCollection("contents", true);

            // 5. Show "Contents"
            System.out.println(String.format("SHOW TABLES FROM %s;", schema.getName()));
            System.out.println(String.format("DESC %s.%s;", schema.getName(), contents.getName()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            session.dropSchema(schema.getName());
            session.close();
        }
    }
}
