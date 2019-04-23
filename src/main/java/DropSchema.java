import com.mysql.cj.xdevapi.Session;
import com.mysql.cj.xdevapi.SessionFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DropSchema {

    public static void main(String[] args) throws IOException {

        Properties prop = new Properties();
        prop.load(Files.newBufferedReader(Path.of("xdev.properties")));

        SessionFactory sf = new SessionFactory();
        Session session = sf.getSession(prop.getProperty("URL"));
        session.dropSchema(prop.getProperty("SCHEMA"));
        session.close();
    }
}
