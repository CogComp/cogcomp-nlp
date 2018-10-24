/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.io.caches;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class TextAnnotationDBHandler implements TextAnnotationCache {
    private final String dbFile;
    private final String[] datasetNames;
    private Logger log = LoggerFactory.getLogger(TextAnnotationDBHandler.class);

    public TextAnnotationDBHandler(String dbFile, String[] datasetNames) {
        this.dbFile = dbFile;
        this.datasetNames = datasetNames;

        boolean create = DBHelper.dbFileExists(dbFile);

        log.info("Sentence cache {}found", create ? "not " : "");

        DBHelper.initializeConnection(dbFile);
        log.info("Initialized connection to {}", dbFile);

        if (create) {
            createDatabase(dbFile);
        }
    }

    private void createDatabase(String dbFile) {
        try {
            log.info("Creating sentence cache database at " + dbFile);

            DBHelper.createTable(dbFile, "datasets",
                    "id int not null auto_increment, name varchar(20)");

            DBHelper.createTable(dbFile, "sentences", "id int not null, ta blob, primary key(id)");

            DBHelper.createTable(dbFile, "sentencesToDataset", "sentenceId int not null, "
                    + "datasetId int not null, "
                    + "foreign key (sentenceId) references sentences(id), "
                    + "foreign key (datasetId) references datasets(id), "
                    + "primary key(sentenceId, datasetId)");

            initializeDatasets(dbFile);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void initializeDatasets(String dbFile) {
        Connection connection = DBHelper.getConnection(dbFile);
        for (String dataset : datasetNames) {
            PreparedStatement stmt;
            try {
                stmt = connection.prepareStatement("select * from datasets where name = ?");
                stmt.setString(1, dataset);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    stmt = connection.prepareStatement("insert into datasets(name) values (?)");
                    stmt.setString(1, dataset);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                log.error("Error with database access", e);
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public void addTextAnnotation(String dataset, TextAnnotation ta) {
        try {
            Connection connection = DBHelper.getConnection(dbFile);

            PreparedStatement stmt =
                    connection.prepareStatement("select id from datasets where name = ?");
            stmt.setString(1, dataset);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int datasetId = rs.getInt("id");

            stmt.close();

            int id = ta.getTokenizedText().hashCode();

            stmt = connection.prepareStatement("select * from sentences where id = ?");
            stmt.setInt(1, id);

            rs = stmt.executeQuery();
            if (!rs.next()) {

                stmt = connection.prepareStatement("insert into sentences (id, ta) values (?,?)");

                byte[] bytes = serialize(ta);

                stmt.setInt(1, id);
                stmt.setBytes(2, bytes);
                stmt.executeUpdate();
            }

            stmt =
                    connection
                            .prepareStatement("select * from sentencesToDataset where sentenceId = ? and datasetId = ?");
            stmt.setInt(1, id);
            stmt.setInt(2, datasetId);

            rs = stmt.executeQuery();
            if (!rs.next()) {

                stmt =
                        connection
                                .prepareStatement("insert into sentencesToDataset (sentenceId, datasetId) values(?,?)");
                stmt.setInt(1, id);
                stmt.setInt(2, datasetId);
                stmt.executeUpdate();
            } else {
                log.debug("Repeated in {} : {}", dataset, ta);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateTextAnnotation(TextAnnotation ta) {

        try {
            Connection connection = DBHelper.getConnection(dbFile);

            int id = ta.getTokenizedText().hashCode();
            byte[] bytes = serialize(ta);
            PreparedStatement stmt =
                    connection.prepareStatement("update sentences set ta = ? where id = ?");
            stmt.setBytes(1, bytes);
            stmt.setInt(2, id);

            stmt.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] serialize(TextAnnotation ta) {
        try {
            return SerializationHelper.serializeTextAnnotationToBytes(ta);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private TextAnnotation deserialize(byte[] bytes) {
        try {
            return SerializationHelper.deserializeTextAnnotationFromBytes(bytes);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public IResetableIterator<TextAnnotation> getDataset(String dataset) {
        try {
            Connection connection = DBHelper.getConnection(dbFile);
            PreparedStatement stmt;

            String base =
                    "select sentences.ta, datasets.name from sentences, datasets, sentencesToDataset "
                            + "where " + "sentencesToDataset.datasetId = datasets.id and "
                            + "sentences.id = sentencesToDataset.sentenceId ";

            stmt =
                    connection.prepareStatement(base + " and datasets.name = ?",
                            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            stmt.setString(1, dataset);

            final ResultSet rs = stmt.executeQuery();

            return new IResetableIterator<TextAnnotation>() {

                @Override
                public void remove() {}

                @Override
                public TextAnnotation next() {
                    try {

                        byte[] bytes = rs.getBytes(1);
                        return deserialize(bytes);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public boolean hasNext() {
                    try {

                        return rs.next();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void reset() {
                    try {
                        rs.beforeFirst();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean contains(TextAnnotation ta) {
        int id = ta.getTokenizedText().hashCode();
        Connection connection = DBHelper.getConnection(dbFile);

        PreparedStatement stmt;
        try {
            stmt = connection.prepareStatement("select * from sentences where id = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void removeTextAnnotation(TextAnnotation ta) {
        try {
            Connection connection = DBHelper.getConnection(dbFile);

            int id = ta.getTokenizedText().hashCode();

            PreparedStatement stmt =
                    connection
                            .prepareStatement("delete from sentencesToDataset where sentenceId = ?");
            stmt.setInt(1, id);
            stmt.execute();
            stmt.close();

            stmt = connection.prepareStatement("delete from sentences where id = ?");
            stmt.setInt(1, id);
            stmt.execute();
            stmt.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TextAnnotation getTextAnnotation(TextAnnotation ta) {
        log.error("Not implemented." );
        return null;
    }
}
