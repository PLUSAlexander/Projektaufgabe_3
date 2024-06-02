import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.List;

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
        private Node currentNode;
        private Edge currentEdge;
        private StringBuilder currentValue = new StringBuilder();
        private int nodeId = 0;

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            currentValue.append(ch, start, length);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentValue.setLength(0);
            if (qName.equalsIgnoreCase("article")) {
                currentNode = new Node(nodeId++, "article", null);
                nodes.add(currentNode);
            } else if (qName.equalsIgnoreCase("author")) {
                currentNode = new Node(nodeId++, "author", null);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equalsIgnoreCase("author")) {
                currentNode.content = currentValue.toString().trim();
                nodes.add(currentNode);
            } else if (qName.equalsIgnoreCase("title") || qName.equalsIgnoreCase("pages") || qName.equalsIgnoreCase("year") || qName.equalsIgnoreCase("volume")) {
                currentNode = new Node(nodeId++, qName, currentValue.toString().trim());
                nodes.add(currentNode);
            }
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
