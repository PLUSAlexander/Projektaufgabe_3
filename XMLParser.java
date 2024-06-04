import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class XMLParser {
    public static void invokeParser(String path) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        XMLHandler handler = new XMLHandler();
        saxParser.parse(path, handler);
        handler.printResults();
    }

    public static class XMLHandler extends DefaultHandler {
        private List<Node> nodes = new ArrayList<>();
        private List<Edge> edges = new ArrayList<>();
        private Stack<Integer> contextStack = new Stack<>();
        private StringBuilder currentValue = new StringBuilder();
        private int nodeId = 0;

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            currentValue.append(ch, start, length);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentValue.setLength(0);
            Node newNode = new Node(nodeId++, qName, null);
            nodes.add(newNode);

            if (!contextStack.isEmpty()) {
                edges.add(new Edge(contextStack.peek(), newNode.id));
            }
            contextStack.push(newNode.id);

            if (qName.equalsIgnoreCase("article")) {
                String key = attributes.getValue("key");
                if (key != null) {
                    String venue = extractVenue(key);
                    if (venue != null) {
                        Node venueNode = new Node(nodeId++, "venue", venue);
                        nodes.add(venueNode);
                        edges.add(new Edge(contextStack.peek(), venueNode.id));
                    }
                }
            }
        }

        private String extractVenue(String key) {
            String[] parts = key.split("/");
            if (parts.length > 1) {
                return parts[1]; // The venue is typically the second part in the key
            }
            return null;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equalsIgnoreCase("author") || qName.equalsIgnoreCase("title") || qName.equalsIgnoreCase("pages") || qName.equalsIgnoreCase("year") || qName.equalsIgnoreCase("volume") || qName.equalsIgnoreCase("journal") || qName.equalsIgnoreCase("number")) {
                Node node = nodes.get(nodes.size() - 1);
                node.content = currentValue.toString().trim();
            }

            contextStack.pop();
        }

        public void printResults() {
            for (Node node : nodes) {
                System.out.println("Node ID: " + node.id + ", Type: " + node.type + ", Content: " + node.content);
            }
            for (Edge edge : edges) {
                System.out.println("Edge from Node ID: " + edge.from + " to Node ID: " + edge.to);
            }
        }
    }

    public static class Node {
        int id;
        String type;
        String content;

        public Node(int id, String type, String content) {
            this.id = id;
            this.type = type;
            this.content = content;
        }
    }

    public static class Edge {
        int from;
        int to;

        public Edge(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }
}
