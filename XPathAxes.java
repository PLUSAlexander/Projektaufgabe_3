import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.util.*;
import java.sql.*;


public class XPathAxes {

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

        try (PreparedStatement pstmt = con.prepareStatement(recursiveQuery)) {
            pstmt.setString(1, input);
            pstmt.setString(2, input);
            ResultSet rs = pstmt.executeQuery();

            // Fetch additional details for each descendant
            System.out.println("\nDescendants of nodes matching: " + input);
            while (rs.next()) {
                int descendantId = rs.getInt("descendant");
                String getDescendantDetail = "SELECT COALESCE(s_id, content) AS identifier FROM node WHERE id = ?";
                try (PreparedStatement pstmt2 = con.prepareStatement(getDescendantDetail)) {
                    pstmt2.setInt(1, descendantId);
                    ResultSet rs1 = pstmt2.executeQuery();
                    if (rs1.next()) {
                        String identifier = rs1.getString("identifier");
                        System.out.println("Descendant of " + input + ": " + (identifier != null ? identifier : "No identifier available"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void xPathPreceeding(String input, Connection con) throws SQLException {
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
                String getParentofSiblingID = "select from_ from edge where to_ = " + i + ";";
                ResultSet rsParentOfSibling = st.executeQuery(getParentofSiblingID);
                while (rsParentOfSibling.next())
                    if (Integer.valueOf(rsParentOfSibling.getString(1)) == parentID) {
                        preceedingIDs.add(i);
                    } else {
                        break;
                    }
            }
        }

        for (Integer i : preceedingIDs)
            System.out.println(i);
    }

    public static void xPathFollowing(String input, Connection con) throws SQLException {
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

        List<Integer> preceedingIDs = new ArrayList<>();
        for (Integer inputID : inputIDS) {
            int parentID = 0;
            String getParentID = "select from_ from edge where to_ = " + inputID + ";";
            ResultSet rsParent = st.executeQuery(getParentID);
            while (rsParent.next())
                parentID = Integer.valueOf(rsParent.getString(1));


            for (int i = inputID + 1; i <= maxID; i++) {
                String getParentofSiblingID = "select from_ from edge where to_ = " + i + ";";
                ResultSet rsParentOfSibling = st.executeQuery(getParentofSiblingID);
                while (rsParentOfSibling.next())
                    if (Integer.valueOf(rsParentOfSibling.getString(1)) == parentID) {
                        preceedingIDs.add(i);
                    } else {
                        break;
                    }
            }
        }

        for (Integer i : preceedingIDs)
            System.out.println(i);
    }



}
