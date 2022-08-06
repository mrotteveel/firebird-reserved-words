package nl.lawinegevaar.firebird.reservedwords.sql;

import nl.lawinegevaar.firebird.reservedwords.KeywordLoader;
import nl.lawinegevaar.firebird.reservedwords.KeywordProcessingException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.stream.Stream;

abstract class AbstractSqlKeywordLoader implements KeywordLoader {

    public final void loadKeywords(DataSource dataSource) throws KeywordProcessingException {
        try (Stream<SqlKeyword> keywordStream = getKeywordStream();
             Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (var keywordBatch = new SqlKeywordBatchStatement(createStatement(connection))) {
                keywordStream.forEach(keywordBatch::addBatch);
                keywordBatch.executeBatch();
                connection.commit();
            } catch (SQLException | KeywordProcessingException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new KeywordProcessingException("Data-access exception", e);
        }
    }

    private PreparedStatement createStatement(Connection connection) {
        try {
            return connection.prepareStatement(getStatement());
        } catch (SQLException e) {
            throw new KeywordProcessingException("Could not prepare statement", e);
        }
    }

    /**
     * @return A prepared statement string with 3 parameters: VARCHAR(50), SMALLINT and BOOLEAN.
     */
    abstract String getStatement();

    abstract Stream<SqlKeyword> getKeywordStream();

    private static class SqlKeywordBatchStatement implements AutoCloseable {

        private final PreparedStatement preparedStatement;

        SqlKeywordBatchStatement(PreparedStatement preparedStatement) {
            this.preparedStatement = preparedStatement;
            try {
                ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();
                if (parameterMetaData.getParameterCount() != 3) {
                    throw new KeywordProcessingException("Expected prepared statement with 3 parameters");
                }
                if (parameterMetaData.getParameterType(1) != Types.VARCHAR && parameterMetaData.getPrecision(2) < 50) {
                    throw new KeywordProcessingException("Expected first parameter of type VARCHAR(50) or larger");
                }
                if (parameterMetaData.getParameterType(2) != Types.SMALLINT) {
                    throw new KeywordProcessingException("Expected second parameter of type SMALLINT");
                }
                if (parameterMetaData.getParameterType(3) != Types.BOOLEAN) {
                    throw new KeywordProcessingException("Expected third parameter of type BOOLEAN");
                }
            } catch (SQLException e) {
                throw new KeywordProcessingException("Invalid prepared statement", e);
            }
        }

        void addBatch(SqlKeyword sqlKeyword) {
            try {
                preparedStatement.setString(1, sqlKeyword.word());
                preparedStatement.setInt(2, sqlKeyword.sqlVersion());
                preparedStatement.setBoolean(3, sqlKeyword.reserved());

                preparedStatement.addBatch();
            } catch (SQLException e) {
                throw new KeywordProcessingException("Could not add keyword to batch: " + sqlKeyword, e);
            }
        }

        void executeBatch() {
            try {
                preparedStatement.executeBatch();
            } catch (SQLException e) {
                throw new KeywordProcessingException("Could not execute batch", e);
            }
        }

        @Override
        public void close() {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                throw new KeywordProcessingException("Could not close prepared statement", e);
            }
        }
    }

}
