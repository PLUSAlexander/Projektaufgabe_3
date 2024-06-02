import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;

public class XMLParser {

    public static void main(String[] args) {

        try {
            // Initialize a document builder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML file
            Document document = builder.parse(new File("/C://Users//Startklar//Dokumente//Projektaufgabe_3//toy_example.txt/"));

            // Normalize the XML structure
            document.getDocumentElement().normalize();

            // Here we choose to parse both article and inproceedings elements
            parseElements(document, "article");
            parseElements(document, "inproceedings");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseElements(Document document, String tag) {
        // Get all elements by tag name
        NodeList list = document.getElementsByTagName(tag);

        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                // Extract elements like author, title, year, ee, url
                System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                System.out.println("Year: " + element.getElementsByTagName("year").item(0).getTextContent());
                System.out.println("Pages: " + element.getElementsByTagName("pages").item(0).getTextContent());
                System.out.println("URL: " + element.getElementsByTagName("url").item(0).getTextContent());

                // Getting authors
                NodeList authors = element.getElementsByTagName("author");
                for (int count = 0; count < authors.getLength(); count++) {
                    Node nodeAuthor = authors.item(count);
                    if (nodeAuthor.getNodeType() == Node.ELEMENT_NODE) {
                        Element author = (Element) nodeAuthor;
                        System.out.println("Author: " + author.getTextContent());
                    }
                }

                // Print electronic edition URLs (if available)
                NodeList eeList = element.getElementsByTagName("ee");
                for (int eeCount = 0; eeCount < eeList.getLength(); eeCount++) {
                    Node eeNode = eeList.item(eeCount);
                    if (eeNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element ee = (Element) eeNode;
                        System.out.println("Electronic Edition (EE): " + ee.getTextContent());
                    }
                }

                System.out.println("\n");
            }
        }
    }
}
