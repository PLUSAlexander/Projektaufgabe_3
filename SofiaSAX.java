import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.util.*;
import java.sql.*;


public class SofiaSAX {
    private static Connection con;
    private static String url = "jdbc:postgresql://localhost/postgres";
    private static String user = "postgres";
    private static String pwd = "1234";
    private static int id = 0;
    private static int toCounter = 1;
    private static List<Integer> toCount = new ArrayList<>();
    private static int fromCounter = 1;
    private static List<Integer> fromCount = new ArrayList<>();

    private static Map<String, Integer> venues = new HashMap<>();
    private static Map<String, Integer> years = new HashMap<>();


    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, SQLException {
        con = DriverManager.getConnection(url, user, pwd);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        BibHandler bibHandler = new BibHandler();
        saxParser.parse("/C://Users//Startklar//Dokumente//Projektaufgabe_3//toy_example.txt/", bibHandler);
        //System.out.println(bibHandler.getXML());

        createEdgeModel();
        bibHandler.nodeInserter();
        edgeInserter();

        xPathAncestor("Nikolaus Augsten");
    }



    //Phase 1

    //parse XML
    public static class BibHandler extends DefaultHandler {
        private Bib xmlDoc;
        private StringBuilder elementValue;

        //element types ->
        private static final String BIB = "bib";
        private static final String ARTICLE = "article";
        private static final String INPROCEEDINGS = "inproceedings";
        private static final String AUTHOR = "author";
        private static final String TITLE = "title";
        private static final String PAGES = "pages";
        private static final String YEAR = "year";
        private static final String VOLUME = "volume";
        private static final String JOURNAL = "journal";
        private static final String NUMBER = "number";
        private static final String EE = "ee";
        private static final String URL = "url";
        private static final String BOOKTITLE = "booktitle";
        private static final String CROSSREF = "crossref";


        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (elementValue == null) {
                elementValue = new StringBuilder();
            } else {
                elementValue.append(ch, start, length);
            }
        }


        @Override
        public void startDocument() throws SAXException {
            xmlDoc = new Bib();
        }


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case BIB -> xmlDoc.entryList = new ArrayList<>();
                case ARTICLE, INPROCEEDINGS -> {
                    xmlDoc.entryList.add(new BibEntry());
                    String key = attributes.getValue("key");
                    latestEntry().setKey("key = " + key);
                    latestEntry().setType(qName);
                }
                case CROSSREF, BOOKTITLE, AUTHOR, TITLE, PAGES, YEAR, VOLUME, JOURNAL, NUMBER, URL -> {
                    elementValue = new StringBuilder();
                    elementValue.append(qName).append(":  ");
                }
                case EE -> {
                    elementValue = new StringBuilder();
                    elementValue.append(qName);
                    String eeType = attributes.getValue("type");
                    if (eeType != null) elementValue.append("(type = ").append(eeType).append(")");
                    elementValue.append(":  ");
                }
            }
        }


        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case TITLE -> latestEntry().setTitle(elementValue.toString());
                case PAGES -> latestEntry().setPages(elementValue.toString());
                case YEAR -> latestEntry().setYear(elementValue.toString());
                case VOLUME -> latestEntry().setVolume(elementValue.toString());
                case JOURNAL -> latestEntry().setJournal(elementValue.toString());
                case AUTHOR -> latestEntry().setAuthor(elementValue.toString());
                case NUMBER -> latestEntry().setNumber(elementValue.toString());
                case EE -> latestEntry().setEe(elementValue.toString());
                case URL -> latestEntry().setUrl(elementValue.toString());
                case BOOKTITLE -> latestEntry().setBookTitle(elementValue.toString());
                case CROSSREF -> latestEntry().setCrossRef(elementValue.toString());
            }
        }


        private BibEntry latestEntry() {
            List<BibEntry> entryList = xmlDoc.getEntryList();
            int latestArticleIndex = entryList.size() - 1;
            return entryList.get(latestArticleIndex);
        }


        public Bib getXML() {
            return xmlDoc;
        }


        //insert data into node-table from Edge-Model
        public void nodeInserter() throws SQLException {
            List<BibEntry> entryList = xmlDoc.getEntryList();

            for (BibEntry entry : entryList) {

                String titleA = entry.getTitle();
                String pagesA = entry.getPages();
                String volumeA = entry.getVolume();
                String journalA = entry.getJournal();
                String numberA = entry.getNumber();
                String urlA = entry.getUrl();
                String booktitleA = entry.getBookTitle();
                String crossrefA = entry.getCrossRef();
                String type = entry.getType();
                List<String> ee = entry.getEE();
                List<String> author = entry.getAuthors();

                String yearA = entry.getYear();
                String[] partsY = yearA.split(":  ");
                String year = partsY[1];

                String key = entry.getKey();
                String[] parts = key.split("/");
                String venue = parts[1];
                String name = parts[2];

                Statement stmInsert = con.createStatement();
                StringBuilder strInsert = new StringBuilder("insert into node(id, s_id, type, content) VALUES ");


                //group after venue and year ->

                String venueToGroup = venue;   //for pacmmod = sigmod; group both under sigmod

                if(venue.equals("pacmmod")) {
                    venueToGroup = "sigmod";
                }

                if(!venues.containsKey(venueToGroup)){
                    id++;
                    strInsert.append("(").append(id).append(", '").append(venue).append("', 'venue', ").append("null), ");
                    toCount.add(toCounter);
                    fromCount.add(0);
                    fromCounter = toCounter;
                    toCounter++;
                    venues.put(venue, id);
                }


                String checkYearInVenue = year + venueToGroup;
                if(!years.containsKey(checkYearInVenue)) {
                    id++;
                    strInsert.append("(").append(id).append(", '").append(venueToGroup).append("_").append(year).append("', 'year', ").append("null), ");
                    toCount.add(toCounter);
                    toCounter++;
                    fromCount.add(venues.get(venueToGroup));
                    years.put(checkYearInVenue, id);
                }


                id++;
                strInsert.append("(").append(id).append(", '").append(name).append("', '").append(type).append("', ").append("null), ");
                toCount.add(toCounter);
                fromCounter = toCounter;
                toCounter++;
                fromCount.add(years.get(checkYearInVenue));


                //add attributes ->

                if(!author.isEmpty()) {
                    for (String auth : author) {
                        String[] partsAu = auth.split(":  ");
                        String a = partsAu[1];
                        id++;
                        strInsert.append("(").append(id).append(", null, 'author', '").append(a).append("'), ");
                        toCount.add(toCounter);
                        toCounter++;
                        fromCount.add(fromCounter);
                    }
                }

                if(titleA != null) {
                    String[] partsT = titleA.split(":  ");
                    String title = partsT[1];
                    id++;
                    strInsert.append("(").append(id).append(", null, 'title', '").append(title.replace("'", "''")).append("'), ");
                    toCount.add(toCounter);
                    toCounter++;
                    fromCount.add(fromCounter);
                }

                if(pagesA != null) {
                    String[] partsP = pagesA.split(":  ");
                    String pages = partsP[1];
                    id++;
                    strInsert.append("(").append(id).append(", null, 'pages', '").append(pages).append("'), ");
                    toCount.add(toCounter);
                    toCounter++;
                    fromCount.add(fromCounter);
                }

                if(year != null) {
                    id++;
                    strInsert.append("(").append(id).append(", null, 'year', '").append(year).append("'), ");
                    toCount.add(toCounter);
                    toCounter++;
                    fromCount.add(fromCounter);
                }

                if(booktitleA != null) {
                    String[] partsB = booktitleA.split(":  ");
                    String booktitle = partsB[1];
                    id++;
                    strInsert.append("(").append(id).append(", null, 'booktitle', '").append(booktitle).append("'), ");
                    toCount.add(toCounter);
                    toCounter++;
                    fromCount.add(fromCounter);
                }

                if(volumeA != null) {
                    String[] partsV = volumeA.split(":  ");
                    String volume = partsV[1];
                    id++;
                    strInsert.append("(").append(id).append(", null, 'volume', '").append(volume).append("'), ");
                    toCount.add(toCounter);
                    toCounter++;
                    fromCount.add(fromCounter);
                }

                if(journalA != null) {
                    String[] partsJ = journalA.split(":  ");
                    String journal = partsJ[1];
                    id++;
                    strInsert.append("(").append(id).append(", null, 'journal', '").append(journal).append("'), ");
                    toCount.add(toCounter);
                    toCounter++;
                    fromCount.add(fromCounter);
                }

                if(numberA != null) {
                    String[] partsN = numberA.split(":  ");
                    String number = partsN[1];
                    id++;
                    strInsert.append("(").append(id).append(", null, 'number', '").append(number).append("'), ");
                    toCount.add(toCounter);
                    toCounter++;
                    fromCount.add(fromCounter);
                }

                if(!ee.isEmpty()) {
                    for (String e : ee) {
                        String[] partsEE = e.split(":  ");
                        String eee = partsEE[1];
                        id++;
                        if(e.contains("(type"))
                            strInsert.append("(").append(id).append(", null, 'ee (type = \"oa\")', '").append(eee).append("'), ");
                        else
                            strInsert.append("(").append(id).append(", null, 'ee', '").append(eee).append("'), ");
                        toCount.add(toCounter);
                        toCounter++;
                        fromCount.add(fromCounter);
                    }
                }

                if(crossrefA != null) {
                    String[] partsC = crossrefA.split(":  ");
                    String crossref = partsC[1];
                    id++;
                    strInsert.append("(").append(id).append(", null, 'crossref', '").append(crossref).append("'), ");
                    toCount.add(toCounter);
                    toCounter++;
                    fromCount.add(fromCounter);
                }

                if(urlA != null) {
                    String[] partsU = urlA.split(":  ");
                    String url = partsU[1];
                    id++;
                    strInsert.append("(").append(id).append(", null, 'url', '").append(url).append("')");
                    toCount.add(toCounter);
                    toCounter++;
                    fromCount.add(fromCounter);
                }


                strInsert.append(";");
                System.out.println(strInsert);
                stmInsert.execute(strInsert.toString());
            }
        }
    }



    //list of all entries (articles or inproceedings)
    public static class Bib {
        private List<BibEntry> entryList;

        public List<BibEntry> getEntryList() {
            return this.entryList;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('\n').append("bib").append('\n');
            for (BibEntry entry : entryList) {
                sb.append("   ").append(entry).append("\n");
            }
            return sb.toString();
        }
    }



    //entry (article or inproceeding)
    public static class BibEntry {
        private String key, title, pages, year, volume, journal, number, url, bookTitle, crossRef, type;
        private List<String> authors = new ArrayList<>();
        private List<String> eeType = new ArrayList<>();

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getKey(){
            return key;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle(){
            return title;
        }

        public void setAuthor(String author) {
            authors.add(author);
        }

        public List<String> getAuthors(){
            return authors;
        }

        public void setPages(String pages) {
            this.pages = pages;
        }

        public String getPages() {
            return pages;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getYear() {
            return year;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        public String getVolume() {
            return volume;
        }

        public void setJournal(String journal) {
            this.journal = journal;
        }

        public String getJournal(){
            return journal;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getNumber(){
            return number;
        }

        public void setEe(String ee) {
            eeType.add(ee);
        }

        public List<String> getEE(){
            return eeType;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrl(){
            return url;
        }

        public void setBookTitle(String bookTitle) {
            this.bookTitle = bookTitle;
        }

        public String getBookTitle(){
            return bookTitle;
        }

        public void setCrossRef(String crossRef) {
            this.crossRef = crossRef;
        }

        public String getCrossRef(){
            return crossRef;
        }


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(type).append(", ");
            if (key != null)
                sb.append(key).append('\n');
            if (!authors.isEmpty())
                for (String author : authors) {
                    sb.append("      ").append(author).append('\n');
                }
            if (title != null)
                sb.append("      ").append(title).append('\n');
            if (pages != null)
                sb.append("      ").append(pages).append('\n');
            if (year != null)
                sb.append("      ").append(year).append('\n');
            if (volume != null)
                sb.append("      ").append(volume).append('\n');
            if (journal != null)
                sb.append("      ").append(journal).append('\n');
            if (number != null)
                sb.append("      ").append(number).append('\n');
            if (bookTitle != null)
                sb.append("      ").append(bookTitle).append('\n');
            if (!eeType.isEmpty())
                for (String ee : eeType) {
                    sb.append("      ").append(ee).append('\n');
                }
            if (crossRef != null)
                sb.append("      ").append(crossRef).append('\n');
            if (url != null)
                sb.append("      ").append(url).append('\n');

            return sb.toString();
        }
    }



    //create schema for Edge-Model
    public static void createEdgeModel() throws SQLException {
        Statement stmDropNode = con.createStatement();
        String dropNode = "DROP TABLE IF EXISTS NODE;";
        stmDropNode.execute(dropNode);

        Statement stmCreateNode = con.createStatement();
        String createNode = "CREATE TABLE NODE (id int, s_id varchar, type varchar, content varchar);";
        stmCreateNode.execute(createNode);

        Statement stmDropEdge = con.createStatement();
        String dropEdge = "DROP TABLE IF EXISTS EDGE;";
        stmDropEdge.execute(dropEdge);

        Statement stmCreateEdge = con.createStatement();
        String createEdge = "CREATE TABLE EDGE (from_ int, to_ int);";
        stmCreateEdge.execute(createEdge);

        //root element
        String strInsert = "insert into node(id, s_id, type, content) VALUES (0, 'bib', 'bib', null);";
        Statement st = con.createStatement();
        st.execute(strInsert);
    }



    //fill edge-table of Edge-Model
    public static void edgeInserter() throws SQLException {
        Statement st = con.createStatement();
        StringBuilder sbEdgeInsert = new StringBuilder("insert into edge (from_, to_) VALUES ");
        for (int i = 0; i < toCount.size() && i < fromCount.size(); i++) {
            if (i == toCount.size() - 1) {
                sbEdgeInsert.append("(").append(fromCount.get(i)).append(", ").append(toCount.get(i)).append(");");
            } else {
                sbEdgeInsert.append("(").append(fromCount.get(i)).append(", ").append(toCount.get(i)).append("), ");
            }
        }

        st.execute(sbEdgeInsert.toString());
    }


    //TO DO: Implementing the XPath-axes in the Edge-Model

    public static void xPathAncestor(String input) throws SQLException {
        String recursiveQuery = """
        WITH RECURSIVE AncestorCTE AS (
            SELECT id as start_node, from_ AS ancestor
            FROM edge
            JOIN node ON node.id = edge.to_
            WHERE node.s_id = ? OR node.content = ?
            UNION ALL
            SELECT a.start_node, e.from_ AS ancestor
            FROM edge e
            INNER JOIN AncestorCTE a ON e.to_ = a.ancestor
        )
        SELECT DISTINCT start_node, ancestor FROM AncestorCTE
        ORDER BY start_node, ancestor;
        """;

        try (PreparedStatement pstmt = con.prepareStatement(recursiveQuery)) {
            pstmt.setString(1, input);
            pstmt.setString(2, input);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nAncestors of nodes matching: " + input);
            Statement st = con.createStatement();
            while (rs.next()) {
                //System.out.println("Start Node: " + rs.getString("start_node") + ", Ancestor: " + rs.getString("ancestor"));
                String ancestorVal = "select s_id from node where id = " + rs.getString("ancestor") + ";";
                ResultSet rs1 = st.executeQuery(ancestorVal);
                while (rs1.next())
                    System.out.println("Ancestors of " + input + ": " + rs1.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


/*
    public static void xPathAncestorNotInDB(String input) throws SQLException{
        System.out.println("\n \n \n");
        System.out.println("Ancestors of: " + input);
        List<Integer> resultIDs = new ArrayList<>();
        Statement st = con.createStatement();
        String getContentID = "select id from node where s_id = '" + input + "' OR content = '" + input + "';"; // hole ID
        ResultSet rs = st.executeQuery(getContentID);
        while (rs.next()) {
            resultIDs.add(Integer.valueOf(rs.getString(1)));
        }


        List<Set<Integer>> resultSets = new ArrayList<>();
        for (Integer i : resultIDs)
             resultSets.add(getRecursiveAncestor(new HashSet<>(), i));


        for (Set<Integer> s : resultSets) {
            for (Integer i : s) {
                String getAncestorVal = "select s_id from node where id = " + i + ";";
                ResultSet rs2 = st.executeQuery(getAncestorVal);
                while (rs2.next())
                    System.out.println(rs2.getString(1));
            }
            System.out.println("\n");
        }
    }

    public static Set<Integer> getRecursiveAncestor (Set<Integer> ancestors, int to) throws SQLException {
        Statement st = con.createStatement();
        int i = 0;

        String getAncestorID = "select from_ from edge where to_ = " + to + ";";
        ResultSet rs1 = st.executeQuery(getAncestorID);
        while (rs1.next()) {
            //System.out.println(rs1.getString(1));
            ancestors.add(Integer.valueOf(rs1.getString(1)));
            i = Integer.valueOf(rs1.getString(1));
        }
        if (i != 0)
            getRecursiveAncestor(ancestors, i);

        return ancestors;
    }

*/

    //Phase 2
}
