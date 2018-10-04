/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.bigdata.database;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * utils class which helps in creating a database using h2.
 * @author cogcomp
 *
 */
public class DBHelper {

	private static final String sqlDriver = "org.h2.Driver";
	private static final Logger logger = LoggerFactory.getLogger(DBHelper.class);

	static {
		try {
			Class.forName(sqlDriver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private final static Map<String, Connection> connections = new HashMap<String, Connection>();

	public static class Column {
		String name;
		boolean primaryKey;

		public Column(String name, boolean primaryKey) {
			this.name = name;
			this.primaryKey = primaryKey;
		}
	}

	public static void createTable(String dbFile, String tableName,
			List<Column> tableFields) throws SQLException {

		checkConnection(dbFile);

		Connection connection = getConnection(dbFile);

		Statement statement = connection.createStatement();

		StringBuffer sb = new StringBuffer();
		StringBuffer primaryKey = new StringBuffer();
		for (Column s : tableFields) {
			sb.append(s.name + ", ");
			if (s.primaryKey)
				primaryKey.append(s.name.split(" +")[0] + " , ");
		}

		String pk = primaryKey.substring(0, primaryKey.lastIndexOf(",")).trim();

		String def = sb.toString();
		String tableDefinition = def + " primary key (" + pk + " ) ";

		String sql = "create table " + tableName + " ( " + tableDefinition
				+ " )";

		logger.info(sql);
		statement.executeUpdate(sql);

		statement.close();
	}

	public synchronized static void initializeConnection(String dbFile) {
		try {
			Connection connection = DriverManager
					.getConnection(getDBURL(dbFile));

			connections.put(dbFile, connection);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}

	public synchronized static void terminateConnection(String dbFile) {
		try {
			Connection conn = connections.get(dbFile);
			conn.close();
			connections.remove(dbFile);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static String getDBURL(String dbFile) {
//		return "jdbc:h2:tcp://localhost:9099/" + dbFile + ";MODE=MySQL";
		return "jdbc:h2:" + dbFile + ";MODE=MySQL";

	}

	private static void checkConnection(String dbFile) {
		try {
			Connection connection = connections.get(dbFile);
			logger.info("This is "+connection);
			if (connection.isClosed())
				initializeConnection(dbFile);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean dbFileExists(String dbFile) {
		boolean create = false;

		if (!IOUtils.exists(dbFile + ".mv.db"))
			create = true;
		return create;
	}

	public static Connection getConnection(String dbFile) {
		checkConnection(dbFile);
		return connections.get(dbFile);
	}
}
