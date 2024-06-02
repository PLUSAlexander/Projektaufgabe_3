import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Alexander {
    private static Connection con;
    private static String url = "jdbc:postgresql://localhost/postgres";
    private static String user = "postgres";
    private static String pwd = "1234";
    private static int id = 1;
    public static void main(String[] args) {
        try {
            con = DriverManager.getConnection(url, user, pwd);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            createEdgeModel();
            String strInsert = "insert into node(id, s_id, type, content) VALUES (0, 'bib', 'bib', null);";
            Statement st = con.createStatement();
            st.execute(strInsert);
            Document document = builder.parse("/C://Users//Startklar//Dokumente//Projektaufgabe_3//toy_example.txt/");

            processEntries(document, "article");
            processEntries(document, "inproceedings");

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void processEntries(Document doc, String tagName) throws SQLException {
        NodeList entries = doc.getElementsByTagName(tagName);
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            String key = entry.getAttribute("key");
            String[] parts = key.split("/");
            String journal = parts.length > 1 ? parts[1] : "No Journal";  // Handling in case of missing parts
            String name = parts.length > 2 ? parts[2] : "No Name";

            System.out.println(tagName.toUpperCase() + " Entry:");
            System.out.println("Journal part: " + journal); // VENUE + VENUE_YEAR
            System.out.println("Name: " + name);

            String year = null;
            List<String> authors = printElementData(entry, "author");
            List<String> title = printElementData(entry, "title");
            List<String>  pages = printElementData(entry, "pages");
            List<String> years = printElementData(entry, "year");
            List<String>  vol = printElementData(entry, "volume");
            List<String>  journals = printElementData(entry, "journal");
            List<String>  booktitles = printElementData(entry, "booktitle");
            List<String>  numbers = printElementData(entry, "number");
            List<String> dois = printDOILinks(entry);
            List<String>  urls = printElementData(entry, "url");

            for (String s : years)
                year = s;


            Statement stInsert = con.createStatement();
            StringBuilder strInsert = new StringBuilder("insert into node(id, s_id, type, content) VALUES ");
            strInsert.append("(" + id + ", '" + journal + "', 'venue', " + "null), ");

            id++;
            strInsert.append("(" + id + ", '" + journal + "_" + year + "', 'year', " + "null), ");
            id++;
            strInsert.append("(" + id + ", '" + name + "', '" + tagName + "', " + "null), ");

            if (true) {
                for (String s : authors) {
                    id++;
                    strInsert.append("(" + id + ", null, 'author', '" + s + "'), ");
                }

                for (String s : title) {
                    id++;
                    strInsert.append("(" + id + ", null, 'title', '" + s.replace("'", "''") + "'), ");
                }

                for (String s : pages) {
                    id++;
                    strInsert.append("(" + id + ", null, 'pages', '" + s + "'), ");
                }

                for (String s : years) {
                    id++;
                    strInsert.append("(" + id + ", null, 'year', '" + s + "'), ");
                }

                for (String s : vol) {
                    id++;
                    strInsert.append("(" + id + ", null, 'volume', '" + s + "'), ");
                }

                for (String s : journals) {
                    id++;
                    strInsert.append("(" + id + ", null, 'journal', '" + s + "'), ");
                }

                for (String s : booktitles) {
                    id++;
                    strInsert.append("(" + id + ", null, 'booktitle', '" + s + "'), ");
                }

                for (String s : numbers) {
                    id++;
                    strInsert.append("(" + id + ", null, 'number', '" + s + "'), ");
                }

                for (String s : dois) {
                    id++;
                    strInsert.append("(" + id + ", null, 'ee', '" + s + "'), ");
                }

                for (String s : urls) {
                    id++;
                    strInsert.append("(" + id + ", null, 'url', '" + s + "');");
                }
                id++;
            } // INSERT

            System.out.println(strInsert); // Add a space between entries for better readability
            System.out.println();
            stInsert.execute(strInsert.toString());
        }


    }


    public static List<String> printElementData(Element parent, String tagName) {
        List<String> entries = new ArrayList<>();
        NodeList elements = parent.getElementsByTagName(tagName);
        if (elements.getLength() > 0) {
            //System.out.println(tagName.toUpperCase() + "s:");
            for (int i = 0; i < elements.getLength(); i++) {
                Element el = (Element) elements.item(i);
                entries.add(el.getTextContent().trim());
                //System.out.println(el.getTextContent().trim());
            }
        }

        return entries;
    }

    public static List<String> printDOILinks(Element parent) {
        List<String> url = new ArrayList<>();
        NodeList eeElements = parent.getElementsByTagName("ee");
        if (eeElements.getLength() > 0) {
            //System.out.println("DOI LINKS:");
            for (int i = 0; i < eeElements.getLength(); i++) {
                Element ee = (Element) eeElements.item(i);
                String eeText = ee.getTextContent();
                if (eeText.startsWith("https://doi.org/")) {
                    url.add(eeText);
                    //System.out.println(eeText);
                }
            }
        }
        return url;
    }

    public static void createEdgeModel() throws SQLException {
        Statement stdropNode = con.createStatement();
        String dropNode = "DROP TABLE IF EXISTS NODE;";
        stdropNode.execute(dropNode);

        Statement stCreateNode = con.createStatement();
        String createNode = "CREATE TABLE NODE (id int, s_id varchar, type varchar, content varchar);";
        stCreateNode.execute(createNode);
        //////////////////////////////////////////////////////////////////////////////////////////////////
        Statement stdropEdge = con.createStatement();
        String dropEdge = "DROP TABLE IF EXISTS EDGE;";
        stdropEdge.execute(dropEdge);

        Statement stCreateEdge = con.createStatement();
        String createEdge = "CREATE TABLE EDGE (fr_om int, t_o int);";
        stCreateEdge.execute(createEdge);
    }

}
