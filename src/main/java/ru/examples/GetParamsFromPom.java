package ru.examples;

import javafx.application.Application;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;

public class GetParamsFromPom {
    public static void main(String[] args) throws IOException, XmlPullParserException {

//        String pathToPom = "../webapps/CheckingSkillsWeb/META-INF/maven/ru.utils/CheckingSkillsWeb/pom.xml";
        String pathToPom = "pom.xml";

        System.out.println(Application.class.getResourceAsStream(pathToPom));

        if ((new File(pathToPom)).exists()) {
            Model model = null;
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(pathToPom));) {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                model = reader.read(bufferedReader);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (model != null) {
                System.out.println(model.getId());
                System.out.println(model.getGroupId());
                System.out.println(model.getArtifactId());
                System.out.println(model.getVersion());            }
        }


/*
            model = reader.read(
                    new InputStreamReader(
                            Application.class.getResourceAsStream(
                                    "/META-INF/maven/ru.utils/CheckingSkillsWeb/pom.xml"
                            )
                    ));
*/
    }
}
