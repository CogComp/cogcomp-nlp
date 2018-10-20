/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.io.caches;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DBHelper {
    private static Logger logger = LoggerFactory.getLogger(DBHelper.class);

    private static final String sqlDriver = "org.h2.Driver";

    static {
        try {
            Class.forName(sqlDriver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private final static Map<String, Connection> connections = new HashMap<>();

    public static void createTable(String dbFile, String tableName, String tableDefinition)
            throws SQLException {
        checkConnection(dbFile);

        Connection connection = getConnection(dbFile);

        Statement statement = connection.createStatement();
        String sql = "create table " + tableName + " (" + tableDefinition + ")";

        logger.info(sql);
        statement.executeUpdate(sql);

        statement.close();
    }

    public synchronized static void initializeConnection(String dbFile) {
        try {
            Connection connection = DriverManager.getConnection(getDbURL(dbFile));

            connections.put(dbFile, connection);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public static String getDbURL(String dbFile) {
        return "jdbc:h2:" + dbFile + ";MODE=MySQL;FILE_LOCK=NO";
    }

    private static void checkConnection(String dbFile) {
        try {
            Connection connection = connections.get(dbFile);

            if (connection.isClosed())
                initializeConnection(dbFile);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean dbFileExists(String dbFile) {
        boolean create = false;
        if (!IOUtils.exists(dbFile + ".h2.db") && !IOUtils.exists(dbFile + ".mv.db"))
            create = true;
        return create;
    }

    public static Connection getConnection(String dbFile) {
        checkConnection(dbFile);
        return connections.get(dbFile);
    }
}
