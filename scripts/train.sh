mvn dependency:copy-dependencies
mvn lbjava:clean; mvn lbjava:compile; mvn compile
java -cp target/classes:target/dependency/* edu.illinois.cs.cogcomp.quant.driver.Quantifier