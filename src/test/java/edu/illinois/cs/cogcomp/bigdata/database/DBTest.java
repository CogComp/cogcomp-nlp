package edu.illinois.cs.cogcomp.bigdata.database;

import java.sql.SQLException;
import java.util.Random;

import junit.framework.TestCase;

public class DBTest extends TestCase {

	void testDBExample() throws SQLException {
		DBExample db = DBExample.getInstance("./database", "myTable");
		Random random = new Random(0);
		for (int i = 0; i < 10; i++)
			db.addItem(new SomeItem(random.nextInt(), random.nextInt(), random
					.nextInt(), random.nextInt(), random.nextInt(), random
					.nextInt()));
	}
	
	
}
