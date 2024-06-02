import java.sql.*;

public class Alexander {

    private static Connection con;
    private static String url = "jdbc:postgresql://localhost/postgres";
    private static String user = "postgres";
    private static String pwd = "1234";
    public static void main (String[] args) throws SQLException {
        con = DriverManager.getConnection(url, user, pwd);
        createEdgeModel();
        XMLParser.invokeParser("/C://Users//Startklar//Dokumente//Projektaufgabe_3//toy_example.txt/");

        con.close();
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
