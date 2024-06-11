import java.util.*;
import java.sql.*;

public class XPathAxes {


    //ancestors ->
    public static void xPathAncestor(String input, Connection con) throws SQLException {
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
        SELECT DISTINCT ancestor FROM AncestorCTE
        ORDER BY ancestor;
        """;

        Statement st = con.createStatement();
        StringBuilder sbInsert = new StringBuilder("insert into AncestorResult VALUES ");

        try (PreparedStatement pstmt = con.prepareStatement(recursiveQuery)) {
            pstmt.setString(1, input);
            pstmt.setString(2, input);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nAncestors of nodes matching " + input + " -> ");
            while (rs.next()) {
                int ancestorId = rs.getInt("ancestor");
                //System.out.println(ancestorId);
                //System.out.println("Ancestor: " + rs.getString("ancestor"));
                String ancestorVal = "select s_id from node where id = " + rs.getString("ancestor") + ";";
                ResultSet rs1 = st.executeQuery(ancestorVal);
                while (rs1.next()) {
                    System.out.println("Ancestor of " + input + ": " + rs1.getString(1));
                    sbInsert.append("(" + ancestorId + ", '" + rs1.getString(1) + "'), ");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String dropAncestorTable = "drop table if exists AncestorResult;";
        st.execute(dropAncestorTable);

        String createAncestorTable = "create table AncestorResult (id integer, " + input.replaceAll(" ", "") + " varchar(255));";
        st.execute(createAncestorTable);

        sbInsert.replace(sbInsert.length() - 2, sbInsert.length(), ";");
        if (sbInsert.toString().contains(");"))
            st.execute(sbInsert.toString());
    }


    //descendants ->
    public static void xPathDescendant(String input, Connection con) throws SQLException {
        String recursiveQuery = """
        WITH RECURSIVE DescendantCTE AS (
            SELECT id as start_node, to_ AS descendant
            FROM edge
            JOIN node ON node.id = edge.from_
            WHERE node.s_id = ? OR node.content = ?
            UNION ALL
            SELECT d.start_node, e.to_ AS descendant
            FROM edge e
            INNER JOIN DescendantCTE d ON e.from_ = d.descendant
        )
        SELECT DISTINCT start_node, descendant FROM DescendantCTE
        ORDER BY start_node, descendant;
        """;

        StringBuilder sbInsert = new StringBuilder("insert into DescendantResult VALUES ");

        try (PreparedStatement pstmt = con.prepareStatement(recursiveQuery)) {
            pstmt.setString(1, input);
            pstmt.setString(2, input);
            ResultSet rs = pstmt.executeQuery();

            // Fetch additional details for each descendant
            System.out.println("\nDescendants of nodes matching " + input + " ->");
            while (rs.next()) {
                int descendantId = rs.getInt("descendant");
                String getDescendantDetail = "SELECT COALESCE(s_id, content) AS identifier FROM node WHERE id = ?";
                try (PreparedStatement pstmt2 = con.prepareStatement(getDescendantDetail)) {
                    pstmt2.setInt(1, descendantId);
                    ResultSet rs1 = pstmt2.executeQuery();
                    if (rs1.next()) {
                        String identifier = rs1.getString("identifier");
                        System.out.println("Descendant of " + input + ": " + (identifier != null ? identifier : "No identifier available"));
                        sbInsert.append("(" + descendantId + ", '" + rs1.getString(1) + "'), ");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Statement st = con.createStatement();

        String dropDescendantTable = "drop table if exists DescendantResult;";
        st.execute(dropDescendantTable);

        String createDescendantTable = "create table DescendantResult (id integer, " + input.replaceAll(" ", "") + " varchar(255));";
        st.execute(createDescendantTable);

        sbInsert.replace(sbInsert.length() - 2, sbInsert.length(), ";");
        //System.out.println(sbInsert);
        if (sbInsert.toString().contains(");"))
            st.execute(sbInsert.toString());
    }



    //sibling-preceding ->
    public static void xPathSiblingPreceding(String input, Connection con) throws SQLException {
        List<Integer> inputIDS = new ArrayList<>();
        Statement st = con.createStatement();
        String getInputID = "select id from node where s_id = '" + input + "' or content = '" + input + "';";
        ResultSet rs = st.executeQuery(getInputID);
        while (rs.next())
            inputIDS.add(Integer.valueOf(rs.getString(1)));

        List<Integer> preceedingIDs = new ArrayList<>();
        for (Integer inputID : inputIDS) {
            int parentID = 0;
            String getParentID = "select from_ from edge where to_ = " + inputID + ";";
            ResultSet rsParent = st.executeQuery(getParentID);
            while (rsParent.next())
                parentID = Integer.valueOf(rsParent.getString(1));

            for (int i = inputID - 1; i > 0; i--) {
                String getParentOfSiblingID = "select from_ from edge where to_ = " + i + ";";
                ResultSet rsParentOfSibling = st.executeQuery(getParentOfSiblingID);
                while (rsParentOfSibling.next())
                    if (Integer.valueOf(rsParentOfSibling.getString(1)) == parentID && !preceedingIDs.contains(i)) {
                        preceedingIDs.add(i);
                    } else {
                        break;
                    }
            }
        }

        StringBuilder sbInsert = new StringBuilder("insert into PreSiblingResult VALUES ");

        System.out.println("\nSibling-Preceding of nodes matching " + input + " ->");
        if(preceedingIDs.isEmpty())
            System.out.println("no Sibling-Preceding nodes.");
        for (Integer i : preceedingIDs) {
            String getNodeDetails = "SELECT COALESCE(s_id, content) AS identifier FROM node WHERE id = ?";
            try (PreparedStatement pstmt2 = con.prepareStatement(getNodeDetails)) {
                pstmt2.setInt(1, i);
                ResultSet rs1 = pstmt2.executeQuery();
                if (rs1.next()) {
                    String identifier = rs1.getString("identifier");
                    System.out.println("Sibling-Preceding of " + input + ": " + (identifier != null ? identifier : "No identifier available"));
                    if (identifier != null)
                        sbInsert.append("(" + i + ", '" + identifier + "'), ");
                }
            }
        }

        String dropPreSiblingTable = "drop table if exists PreSiblingResult;";
        st.execute(dropPreSiblingTable);

        String createDescendantTable = "create table PreSiblingResult (id integer, " + input.replaceAll(" ", "") + " varchar(255));";
        st.execute(createDescendantTable);

        sbInsert.replace(sbInsert.length() - 2, sbInsert.length(), ";");
        //System.out.println(sbInsert);
        if (sbInsert.toString().contains(");"))
            st.execute(sbInsert.toString());
    }



    //sibling-following ->
    public static void xPathSiblingFollowing(String input, Connection con) throws SQLException {
        Statement st = con.createStatement();
        int maxID = 0;
        String getMaxID = "SELECT MAX(id) FROM node;";
        ResultSet rsMax = st.executeQuery(getMaxID);
        while (rsMax.next())
            maxID = Integer.valueOf(rsMax.getString(1));

        List<Integer> inputIDS = new ArrayList<>();
        String getInputID = "select id from node where s_id = '" + input + "' or content = '" + input + "';";
        ResultSet rs = st.executeQuery(getInputID);
        while (rs.next())
            inputIDS.add(Integer.valueOf(rs.getString(1)));

        List<Integer> followingIDs = new ArrayList<>();
        for (Integer inputID : inputIDS) {
            int parentID = 0;
            String getParentID = "select from_ from edge where to_ = " + inputID + ";";
            ResultSet rsParent = st.executeQuery(getParentID);
            while (rsParent.next())
                parentID = Integer.valueOf(rsParent.getString(1));


            for (int i = inputID + 1; i <= maxID; i++) {
                String getParentOfSiblingID = "select from_ from edge where to_ = " + i + ";";
                ResultSet rsParentOfSibling = st.executeQuery(getParentOfSiblingID);
                while (rsParentOfSibling.next())
                    if (Integer.valueOf(rsParentOfSibling.getString(1)) == parentID && !followingIDs.contains(i)) {
                        followingIDs.add(i);
                    } else {
                        break;
                    }
            }
        }

        StringBuilder sbInsert = new StringBuilder("insert into FolSiblingResult VALUES ");

        System.out.println("\nSibling-Following of nodes matching " + input + " ->");
        if(followingIDs.isEmpty())
            System.out.println("no Sibling-Following nodes.");
        for (Integer i : followingIDs) {
            String getNodeDetails = "SELECT COALESCE(s_id, content) AS identifier FROM node WHERE id = ?";
            try (PreparedStatement pstmt2 = con.prepareStatement(getNodeDetails)) {
                pstmt2.setInt(1, i);
                ResultSet rs1 = pstmt2.executeQuery();
                if (rs1.next()) {
                    String identifier = rs1.getString("identifier");
                    System.out.println("Sibling-Following of " + input + ": " + (identifier != null ? identifier : "No identifier available"));
                    if (identifier != null)
                        sbInsert.append("(" + i + ", '" + identifier + "'), ");
                }
            }
        }

        String dropFolSiblingTable = "drop table if exists FolSiblingResult;";
        st.execute(dropFolSiblingTable);

        String createDescendantTable = "create table FolSiblingResult (id integer, " + input.replaceAll(" ", "") + " varchar(255));";
        st.execute(createDescendantTable);

        sbInsert.replace(sbInsert.length() - 2, sbInsert.length(), ";");
        //System.out.println(sbInsert);
        if (sbInsert.toString().contains(");"))
            st.execute(sbInsert.toString());
    }
}