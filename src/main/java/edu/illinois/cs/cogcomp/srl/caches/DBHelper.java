package edu.illinois.cs.cogcomp.srl.caches;

import edu.illinois.cs.cogcomp.core.io.IOUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

class DBHelper {

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

	public static void createTable(String dbFile, String tableName,
			String tableDefinition) throws SQLException {

		checkConnection(dbFile);

		Connection connection = getConnection(dbFile);

		Statement statement = connection.createStatement();
		String sql = "create table " + tableName + " (" + tableDefinition + ")";

		System.out.println(sql);
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

	public static String getDBURL(String dbFile) {
		// return "jdbc:h2:tcp://austen.cs.illinois.edu:9092/" + dbFile
		// + ";MODE=MySQL";
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
		if (!IOUtils.exists(dbFile + ".h2.db"))
			create = true;
		return create;
	}

	public static Connection getConnection(String dbFile) {
		checkConnection(dbFile);
		return connections.get(dbFile);
	}
}