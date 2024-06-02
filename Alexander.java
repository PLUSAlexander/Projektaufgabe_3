import java.sql.*;

public class Alexander {

    private static Connection con;
    private static String url = "jdbc:postgresql://localhost/postgres";
    private static String user = "postgres";
    private static String pwd = "1234";
    public static void main (String[] args) throws SQLException {
        con = DriverManager.getConnection(url, user, pwd);


        con.close();
    }
}
