/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 *
 */
package edu.illinois.cs.cogcomp.core.utilities;

import javax.swing.table.DefaultTableModel;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This is a utility class that exists exclusively for generating Org-Mode tables. It is probably
 * not the most efficient way of doing what it does, though.
 *
 * Update 09/17/2016 Zhili Feng: add method that generate text-formatted table.
 *
 * @author Vivek Srikumar
 *         <p>
 *         Nov 24, 2008
 */
@SuppressWarnings("serial")
public class Table extends DefaultTableModel {

    Set<Integer> separators = new HashSet<>();

    /**
     * This function creates a text-formatted table
     * @return String
     */
    public String toTextTable() {
        StringBuilder buffer = new StringBuilder();

        int numCols = this.getColumnCount();
        int numRows = this.getRowCount();

        List<String> formatterList = new ArrayList<>();
        List<ArrayList<String>> allTableRows = new ArrayList<>();
        ArrayList<String> currTableRow = new ArrayList<>();

        // This is used to determine the space between  two '|' in the table
        int longestWordSize = 10;

        for (int i = 0; i < 2 * numCols + 1; i++) {
            if (i % 2 == 0) {
                formatterList.add("%-2s ");
                currTableRow.add("|");
            }
            else {
                longestWordSize = Math.max(this.getColumnName(i/2).length(), longestWordSize);
                formatterList.add("%-10s ");
                currTableRow.add(this.getColumnName(i/2));
            }
        }
        allTableRows.add(currTableRow);
        formatterList.set(formatterList.size()-1, "%-2s%n");

        for (int row = 0; row < numRows; row++) {
            currTableRow = new ArrayList<>();
            for (int i = 0; i < numCols * 2 + 1; i++) {
                if (i % 2 == 0) {
                    currTableRow.add("|");
                }
                else {
                    longestWordSize = Math.max(this.getValueAt(row, i/2).toString().length(), longestWordSize);
                    currTableRow.add(this.getValueAt(row, i/2).toString());
                }
            }
            allTableRows.add(currTableRow);
        }

        // Change the space between '|' if the longest word has length longer than 10(default)
        if (longestWordSize > 10) {
            for (int i = 1; i < formatterList.size(); i += 2) {
                // All columns will have same width of Math.max(10, longest word)
                formatterList.set(i, "%-" + longestWordSize + "s ");
            }
        }

        StringBuilder formatterString = new StringBuilder();
        for (int i = 0; i < formatterList.size(); i ++) {
            formatterString.append(formatterList.get(i));
        }

        int lenColSep = 0;
        for (int i = 0; i < allTableRows.size(); i ++) {
            // Add this line to separate data from their column names
            if (i == 1) {
                // 4 characters include two '|'s, and 2 padding spaces of the trailing '|'
                char[] colSepArr = new char[lenColSep-4];
                Arrays.fill(colSepArr, '-');
                String colSep = new String(colSepArr);
                buffer.append("|");
                buffer.append(colSep);
                buffer.append("|\n");
            }
            String appendStr = String.format(formatterString.toString(), (allTableRows.get(i)).toArray());
            buffer.append(appendStr);
            if (i == 0) {
                lenColSep = appendStr.length();
            }
        }

        return buffer.toString();

    }

    public String toOrgTable() {
        StringBuilder buffer = new StringBuilder();

        int numCols = this.getColumnCount();
        int numRows = this.getRowCount();

        buffer.append("|");
        for (int i = 0; i < numCols; i++) {
            buffer.append(this.getColumnName(i)).append(" | ");
        }
        buffer.append("\n|-----\n");

        for (int row = 0; row < numRows; row++) {

            if (this.separators.contains(row)) {
                buffer.append("|-----\n");
            }
            buffer.append("| ");
            for (int i = 0; i < numCols; i++) {
                buffer.append(this.getValueAt(row, i)).append(" | ");
            }
            buffer.append("\n");
        }

        return buffer.toString();

    }

    public String toHTMLTable() {
        StringBuffer buffer = new StringBuffer();

        int numCols = this.getColumnCount();
        int numRows = this.getRowCount();

        buffer.append("<table style=\"border-width:1px; border-style:solid; border-color:#000000; border-collapse:collapse; \">\n");
        buffer.append("<tr>");

        for (int i = 0; i < numCols; i++) {
            buffer.append(
                    "<th style=\"border-width:1px; border-style:inset; border-color:#000000;\">")
                    .append(this.getColumnName(i)).append("</th>");
        }
        buffer.append("</tr>\n");

        for (int row = 0; row < numRows; row++) {

            String color;
            String trh = "td";
            if (this.separators.contains(row)) {
                color = "background-color: #ccccff;";
                trh = "th";
            } else if (row % 2 == 0)
                color = "background-color: #cccccc;";
            else
                color = "background-color: #ffffff;";

            buffer.append("<tr>");
            for (int i = 0; i < numCols; i++) {
                buffer.append("<")
                        .append(trh)
                        .append(" style=\"border-width:1px; border-style:inset; border-color:#000000; ")
                        .append(color).append(" \">").append(this.getValueAt(row, i)).append(" </")
                        .append(trh).append(">");
            }
            buffer.append("</tr>\n");
        }
        buffer.append("</table>\n");

        return buffer.toString();

    }

    public String getFormattedString(double d, int numDecimalPlaces) {
        StringBuilder sb = new StringBuilder();
        sb.append("##.");

        for (int i = 0; i < numDecimalPlaces; i++)
            sb.append("#");

        DecimalFormat df = new DecimalFormat(sb.toString());
        return df.format(d);
    }

    public void addSeparator() {
        separators.add(this.getRowCount());
    }

}
