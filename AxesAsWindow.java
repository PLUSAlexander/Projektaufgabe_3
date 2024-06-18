import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AxesAsWindow {

    public static void calculateAncestor(int id, Connection con) throws SQLException {

        int preVal = 0;
        int postVal = 0;

        try (Statement st = con.createStatement()) {
            // Fetching pre and post values for the given id
            ResultSet rsPrePost = st.executeQuery("SELECT pre, post FROM prepostvalues WHERE id = " + id);
            if (rsPrePost.next()) {
                preVal = rsPrePost.getInt("pre");
                postVal = rsPrePost.getInt("post");
            }

            // Fetching possible ancestors based on pre and post values
            List<Integer> ancestors = new ArrayList<>();
            String queryAncestors = "SELECT id FROM prepostvalues WHERE pre < " + preVal + " AND post > " + postVal;
            ResultSet rsAncestors = st.executeQuery(queryAncestors);
            while (rsAncestors.next()) {
                ancestors.add(rsAncestors.getInt("id"));
            }

            // Outputting the ancestors
            for (Integer ancestor : ancestors) {
                System.out.println(ancestor);
            }
        }
    }

    public static void calculateDescendants(int id, Connection con) throws SQLException {
        int preVal = 0;
        int postVal = 0;

        try (Statement st = con.createStatement()) {
            // Fetching pre and post values for the given id
            ResultSet rsPrePost = st.executeQuery("SELECT pre, post FROM prepostvalues WHERE id = " + id);
            if (rsPrePost.next()) {
                preVal = rsPrePost.getInt("pre");
                postVal = rsPrePost.getInt("post");
            }

            // Fetching possible descendants based on pre and post values
            List<Integer> descendants = new ArrayList<>();
            String queryDescendants = "SELECT id FROM prepostvalues WHERE pre > " + preVal + " AND post < " + postVal;
            ResultSet rsDescendants = st.executeQuery(queryDescendants);
            while (rsDescendants.next()) {
                descendants.add(rsDescendants.getInt("id"));
            }

            // Outputting the descendants
            for (Integer descendant : descendants) {
                System.out.println(descendant);
            }
        }
    }

    public static void calculateFollowingSibling(int id, Connection con) throws SQLException {
        int preVal = 0;
        int postVal = 0;
        int parentVal = 0;

        try (Statement st = con.createStatement()) {
            // Fetching parent value for the given id from the accel table
            ResultSet rsParent = st.executeQuery("SELECT parent FROM accel WHERE id = " + id);
            if (rsParent.next()) {
                parentVal = rsParent.getInt("parent");
            }

            // Fetching pre and post values for the given id from the prepostvalues table
            ResultSet rsPrePost = st.executeQuery("SELECT pre, post FROM prepostvalues WHERE id = " + id);
            if (rsPrePost.next()) {
                preVal = rsPrePost.getInt("pre");
                postVal = rsPrePost.getInt("post");
            }

            // Fetching the following siblings based on pre, post, and parent values using SQL JOIN
            List<Integer> followingSiblings = new ArrayList<>();
            String queryFollowingSibling = "SELECT p.id FROM prepostvalues p JOIN accel a ON p.id = a.id " +
                    "WHERE p.pre > " + preVal + " AND p.post > " + postVal + " AND a.parent = " + parentVal;
            ResultSet rsFollowingSibling = st.executeQuery(queryFollowingSibling);
            while (rsFollowingSibling.next()) {
                followingSiblings.add(rsFollowingSibling.getInt("id"));
            }

            // Outputting the following siblings
            for (Integer sibling : followingSiblings) {
                System.out.println(sibling);
            }
        }
    }

    public static void calculatePrecedingSibling(int id, Connection con) throws SQLException {
        int preVal = 0;
        int postVal = 0;
        int parentVal = 0;

        try (Statement st = con.createStatement()) {
            // Fetching parent value for the given id from the accel table
            ResultSet rsParent = st.executeQuery("SELECT parent FROM accel WHERE id = " + id);
            if (rsParent.next()) {
                parentVal = rsParent.getInt("parent");
            }

            // Fetching pre and post values for the given id from the prepostvalues table
            ResultSet rsPrePost = st.executeQuery("SELECT pre, post FROM prepostvalues WHERE id = " + id);
            if (rsPrePost.next()) {
                preVal = rsPrePost.getInt("pre");
                postVal = rsPrePost.getInt("post");
            }

            // Fetching the preceding siblings based on pre, post, and parent values using SQL JOIN
            List<Integer> precedingSiblings = new ArrayList<>();
            String queryPrecedingSibling = "SELECT p.id FROM prepostvalues p JOIN accel a ON p.id = a.id " +
                    "WHERE p.pre < " + preVal + " AND p.post < " + postVal + " AND a.parent = " + parentVal;
            ResultSet rsPrecedingSibling = st.executeQuery(queryPrecedingSibling);
            while (rsPrecedingSibling.next()) {
                precedingSiblings.add(rsPrecedingSibling.getInt("id"));
            }

            // Outputting the preceding siblings
            for (Integer sibling : precedingSiblings) {
                System.out.println(sibling);
            }
        }
    }
}
