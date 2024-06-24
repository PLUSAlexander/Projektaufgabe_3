import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AxesAsWindow {


    public static void mainMethode(String axes, int id, Connection con) throws SQLException {
        switch (axes) {
            case "ancestor" -> calculateAncestor(id, con);
            case "descendant" -> calculateDescendants(id, con);
            case "followingsibling" -> calculateFollowingSibling(id, con);
            case  "precedingsibling" -> calculatePrecedingSibling(id, con);
        }

    }

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


            List<Integer> ancestors = new ArrayList<>();
            String getAncestors = "select pre from accel where pre < " + preVal + " AND post > " + postVal + ";";
            ResultSet rsAncestors = st.executeQuery(getAncestors);
            while (rsAncestors.next()) {
                ancestors.add(rsAncestors.getInt("pre"));
            }

            for (Integer ancestorPre : ancestors) {
                String getId = "select id from prepostvalues where pre = " + ancestorPre + ";";
                ResultSet rs1 = st.executeQuery(getId);
                while (rs1.next())
                    System.out.println(rs1.getString(1));
            }


        }
    }

    public static void calculateDescendants(int id, Connection con) throws SQLException {
        int preVal = 0;
        int postVal = 0;

        try (Statement st = con.createStatement()) {
            // Fetching pre and post values for the given id from the prepostvalues table
            ResultSet rsPrePost = st.executeQuery("SELECT pre, post FROM prepostvalues WHERE id = " + id);
            if (rsPrePost.next()) {
                preVal = rsPrePost.getInt("pre");
                postVal = rsPrePost.getInt("post");
            }

            List<Integer> descendants = new ArrayList<>();
            // Select descendants from the accel table where their 'pre' is greater than 'preVal' and 'post' is less than 'postVal'
            String getDescendants = "SELECT pre FROM accel WHERE pre > " + preVal + " AND post < " + postVal + ";";
            ResultSet rsDescendants = st.executeQuery(getDescendants);
            while (rsDescendants.next()) {
                descendants.add(rsDescendants.getInt("pre"));
            }

            for (Integer descendantPre : descendants) {
                // Fetching the 'id' for each descendant based on 'pre' from prepostvalues table
                String getId = "SELECT id FROM prepostvalues WHERE pre = " + descendantPre + ";";
                ResultSet rs1 = st.executeQuery(getId);
                while (rs1.next()) {
                    System.out.println(rs1.getInt("id")); // Changed from getString to getInt, assuming id is an integer
                }
            }
        }
    }

    public static void calculateFollowingSibling(int id, Connection con) throws SQLException {
    //<(pre(v),infinity), (post(v),infinity), par(v), *, *>
        int preVal = 0;
        int postVal = 0;
        int parentVal = 0;

        try (Statement st = con.createStatement()) {
            // Fetching pre and post values for the given id from the prepostvalues table
            ResultSet rsPrePost = st.executeQuery("SELECT pre, post FROM prepostvalues WHERE id = " + id + ";");
            if (rsPrePost.next()) {
                preVal = rsPrePost.getInt("pre");
                postVal = rsPrePost.getInt("post");
            }

            ResultSet rsPar = st.executeQuery("select parent from accel where pre = " + preVal);
            while (rsPar.next())
                parentVal = rsPar.getInt(1);

            ResultSet rsPres = st.executeQuery("select pre from accel where pre > + " +  preVal + " and post > " + postVal + " and parent = " + parentVal + ";");

            ArrayList<Integer> fSibPre = new ArrayList<>();
            while (rsPres.next())
                fSibPre.add(Integer.valueOf(rsPres.getString(1)));


            for (Integer i : fSibPre) {
                ResultSet rsIDs = st.executeQuery("select id from prepostvalues where pre = " + i + ";");
                while (rsIDs.next())
                    System.out.println(rsIDs.getString(1));
            }
        }

    }


    public static void calculatePrecedingSibling(int id, Connection con) throws SQLException {
        int preVal = 0;
        int postVal = 0;
        int parentVal = 0;

        try (Statement st = con.createStatement()) {
            // Fetching pre and post values for the given id from the prepostvalues table
            ResultSet rsPrePost = st.executeQuery("SELECT pre, post FROM prepostvalues WHERE id = " + id);
            if (rsPrePost.next()) {
                preVal = rsPrePost.getInt("pre");
                postVal = rsPrePost.getInt("post");
            }

            // Finding the parent value from the accel table using pre of the given node
            ResultSet rsPar = st.executeQuery("SELECT parent FROM accel WHERE pre <= " + preVal + " AND post >= " + postVal + " ORDER BY pre DESC LIMIT 1");
            if (rsPar.next()) {
                parentVal = rsPar.getInt("parent");
            }

            // Finding all preceding siblings that have both a lower pre and post value than the given node and the same parent
            String queryPrecedingSiblings = "SELECT pre FROM accel WHERE pre < " + preVal + " AND post < " + postVal + " AND parent = " + parentVal + ";";
            ResultSet rsPres = st.executeQuery(queryPrecedingSiblings);

            ArrayList<Integer> precedingSibPres = new ArrayList<>();
            while (rsPres.next()) {
                precedingSibPres.add(rsPres.getInt("pre"));
            }

            for (Integer i : precedingSibPres) {
                // Fetching the id for each preceding sibling based on the pre value from the prepostvalues table
                ResultSet rsIDs = st.executeQuery("SELECT id FROM prepostvalues WHERE pre = " + i + ";");
                while (rsIDs.next()) {
                    System.out.println(rsIDs.getString("id"));
                }
            }
        }
    }

}
