/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.pmi;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class CountsCache {
    private static Logger logger = LoggerFactory.getLogger(CountsCache.class); 
    
    public static final String sqlDriver = "org.h2.Driver";

    private static Connection connection;

    private final String dbFile;

    static {
        try {
            Class.forName(sqlDriver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void createTable(String dbFile, String tableName, String tableDefinition)
            throws SQLException {

        checkConnection(dbFile);

        Connection connection = getConnection(dbFile);

        Statement statement = connection.createStatement();
        String sql = "create table " + tableName + " (" + tableDefinition + ")";

        logger.info(sql);
        statement.executeUpdate(sql);

        statement.close();
    }

    private synchronized static void initializeConnection(String dbFile) {
        try {
            connection = DriverManager.getConnection(getDBURL(dbFile));

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private static String getDBURL(String dbFile) {
        return "jdbc:h2:" + dbFile + ";MODE=MySQL";
    }

    private static void checkConnection(String dbFile) {
        try {
            if (connection == null || connection.isClosed())
                initializeConnection(dbFile);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean dbFileExists(String dbFile) {
        boolean create = false;
        if (!IOUtils.exists(dbFile + ".h2.db"))
            create = true;
        return create;
    }

    private static Connection getConnection(String dbFile) {
        checkConnection(dbFile);
        return connection;
    }

    public CountsCache(String dbFile) {
        this.dbFile = dbFile;
        boolean create = dbFileExists(dbFile);
        logger.info("Cache {}found", create ? "not " : "");
        initializeConnection(dbFile);

        if (create) {
            logger.info("Creating database cache at " + dbFile);
            try {
                createTable(dbFile, "counts",
                        "item varchar not null, value bigint not null, primary key (item)");
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    public void add(String item, long count) {
        item = item.trim();

        String sql = "insert into counts (item, value) values(?,?)";

        try {
            Connection c = getConnection(dbFile);
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setString(1, item);
            stmt.setLong(2, count);
            stmt.executeUpdate();
        } catch (Exception ex) {
            System.err.println("Cannot add (" + item + ", " + count + ")");
            throw new RuntimeException(ex);
        }

    }

    public boolean hasKey(String item) {
        item = item.trim();

        String sql = "select value from counts where item = ?";

        try {
            Connection c = getConnection(dbFile);
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setString(1, item);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public long get(String item) {
        item = item.trim();

        String sql = "select value from counts where item = ?";

        try {
            Connection c = getConnection(dbFile);
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setString(1, item);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            } else {
                return -1;
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public int getStatistics() {
        String sql = "select count(*) from counts";
        try {
            Connection c = getConnection(dbFile);
            PreparedStatement stmt = c.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            rs.next();
            return rs.getInt(1);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
}
