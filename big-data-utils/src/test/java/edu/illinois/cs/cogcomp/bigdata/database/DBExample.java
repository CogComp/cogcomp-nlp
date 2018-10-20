/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.bigdata.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.bigdata.database.DBHelper;
import edu.illinois.cs.cogcomp.bigdata.database.DBHelper.Column;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Running this will create a db for you at the location specified by dbFile
 * Once you are done populating, you can view the db by connecting to it by
 * running java -cp target/dependency/h2-1.4.182.jar org.h2.tools.Server -web
 * -webPort 9090 and connecting to the dbURL. The username is blank, dbURL
 * should have absolute system path (eg.
 * "jdbc:h2:/Users/Shyam/java_code/wikiutils/database").
 * 
 * @author upadhya3
 *
 */

// this is an example database created using DBHelper
public class DBExample {
	private String dbFile;
	private String tableName;
	private static PreparedStatement insertSt;
	private static final Logger logger = LoggerFactory
			.getLogger(DBExample.class);

	// make sure we only have one instance of the db
	private static DBExample instance;

	/***
	 * if db does not exist, it will create one!
	 * 
	 * @param dbFile
	 * @param tableName
	 * @return
	 */
	public static DBExample getInstance(String dbFile, String tableName) {
		if (instance == null) {
			instance = new DBExample(dbFile, tableName);

		}
		return instance;
	}

	private DBExample(String dbFile, String tableName) {
		this.dbFile = dbFile;
		this.tableName = tableName;
		logger.info("Checking for database at " + dbFile);
		boolean create = DBHelper.dbFileExists(dbFile);
		// if not found create one!
		logger.info("cache {} found", create ? "not " : "");

		DBHelper.initializeConnection(dbFile);

		if (create)
			createDatabase();

		try {
			setupPreparedStatements();
		} catch (Exception ex) {
			logger.error("Unable to prepare SQL statements", ex);
		}

	}

	private void setupPreparedStatements() throws SQLException {
		Connection connection = DBHelper.getConnection(dbFile);
		prepareInsert(connection);

	}

	private void prepareInsert(Connection connection) throws SQLException {
		String insert = "insert into "
				+ tableName
				+ " (task, dataset, input, numVars, constraintsId, solutionId) "
				+ "values (?, ?, ?, ?, ?, ?)";

		insertSt = connection.prepareStatement(insert);
	}

	private void createDatabase() {
		try {
			logger.info("Creating ILP cache database at " + dbFile);

			List<Column> columns = new ArrayList<DBHelper.Column>();
			columns.add(new Column("task int not null", true));
			columns.add(new Column("dataset int not null", true));
			columns.add(new Column("input int not null", true));
			columns.add(new Column("numVars int not null", false));
			columns.add(new Column("constraintsId int not null", true));
			columns.add(new Column("solutionId int not null", false));

			DBHelper.createTable(dbFile, tableName, columns);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void addItem(SomeItem item) throws SQLException {
		insertSt.clearParameters();
		insertSt.setInt(1, item.task);
		insertSt.setInt(2, item.dataset);
		insertSt.setInt(3, item.input);
		insertSt.setInt(4, item.numVars);
		insertSt.setInt(5, item.constraintsId);
		insertSt.setInt(6, item.solutionId);
		insertSt.executeUpdate();

	}

}
