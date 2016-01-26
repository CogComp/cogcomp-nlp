/**
 *
 */
package edu.illinois.cs.cogcomp.core.utilities;

import javax.swing.table.DefaultTableModel;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a utility class that exists exclusively for generating Org-Mode tables. It is probably
 * not the most efficient way of doing what it does, though.
 *
 * @author Vivek Srikumar
 *         <p/>
 *         Nov 24, 2008
 */
@SuppressWarnings("serial")
public class Table extends DefaultTableModel {

    Set<Integer> separators = new HashSet<>();

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
            buffer.append("<th style=\"border-width:1px; border-style:inset; border-color:#000000;\">").append(this.getColumnName(i)).append("</th>");
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
                buffer.append("<").append(trh).append(" style=\"border-width:1px; border-style:inset; border-color:#000000; ").append(color).append(" \">").append(this.getValueAt(row, i)).append(" </").append(trh).append(">");
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
