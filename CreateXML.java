import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateXML {

    public static void mainMethod(String text) {
        String largeString = text;
        convertStringToXML(largeString, "my_small_bib.xml");
    }

    public static void convertStringToXML(String data, String outputFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            Element rootElement = doc.createElement("bib");
            doc.appendChild(rootElement);

            Pattern pattern = Pattern.compile("(article|incollection|inproceedings), key = (.*?)\\s+(.*?)(?=\\s+(article|incollection|inproceedings), key =|$)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(data);

            while (matcher.find()) {
                String entryType = matcher.group(1);
                Element entry = doc.createElement(entryType);
                rootElement.appendChild(entry);
                entry.setAttribute("key", matcher.group(2).trim());

                processAttributes(matcher.group(3), entry, doc);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(outputFile));
            transformer.transform(source, result);

            StreamResult consoleResult = new StreamResult(System.out);
            transformer.transform(source, consoleResult);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processAttributes(String attributes, Element entry, Document doc) {
        Pattern attrPattern = Pattern.compile("(author|title|pages|year|booktitle|journal|number|ee|url|crossref):\\s*(.*?)(?=\\s+(author|title|pages|year|booktitle|journal|number|ee|url|crossref):|$)");
        Matcher attrMatcher = attrPattern.matcher(attributes);

        while (attrMatcher.find()) {
            String attrName = attrMatcher.group(1);
            String attrValue = attrMatcher.group(2).trim();

            if ("ee".equals(attrName)) {
                String[] ees = attrValue.split("\\s+");
                for (String ee : ees) {
                    Element eeElement = doc.createElement(attrName);
                    eeElement.appendChild(doc.createTextNode(ee.trim()));
                    entry.appendChild(eeElement);
                }
            } else {
                Element attrElement = doc.createElement(attrName);
                attrElement.appendChild(doc.createTextNode(attrValue));
                entry.appendChild(attrElement);
            }
        }
    }
}