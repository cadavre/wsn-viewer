
package pl.zeman.iqh.network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import pl.zeman.iqh.PreferencesConstants;
import android.os.Bundle;
import android.util.Log;

/**
 * Network Utilities
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class JDBCConnection {

    public static final String TAG = "WSNV";

    private static JDBCConnection instance = null;

    private Connection connection;

    public static JDBCConnection get() throws ClassNotFoundException {

        if (instance == null) {
            instance = new JDBCConnection();
        }

        return instance;
    }

    public static void getJDBC() throws ClassNotFoundException {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "(1)JDBC error: " + e.getMessage());
            throw e;
        }
    }

    public void openConnection(Bundle params, boolean force) throws SQLException {

        if (connection != null && !force) {
            return;
        }

        try {
            String address = "jdbc:postgresql://" + params.getString(PreferencesConstants.DB_HOST)
                    + ":" + params.getString(PreferencesConstants.DB_PORT) + "/"
                    + params.getString(PreferencesConstants.DB_DATABASE);
            Log.i(TAG, "Connecting to " + address);
            DriverManager.setLoginTimeout(15);
            connection = DriverManager.getConnection(address,
                    params.getString(PreferencesConstants.DB_USER),
                    params.getString(PreferencesConstants.DB_PASSWORD));
            connection.setAutoCommit(false);

            return;
        } catch (SQLException e) {
            Log.e(TAG, "(2)JDBC error: " + e.getMessage());
            throw e;
        }
    }

    public void openConnection(Bundle params) throws SQLException {

        openConnection(params, false);
    }

    public void closeConnection() throws SQLException {

        try {
            connection.close();
        } catch (SQLException e) {
            Log.e(TAG, "(3)JDBC error: " + e.getMessage());
            throw e;
        }
    }

    public ResultSet getResults(String $sql) throws SQLException {

        try {
            Statement st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet results = st.executeQuery($sql);

            return results;
        } catch (SQLException e) {
            Log.e(TAG, "(4)JDBC error: " + e.getMessage());
            throw e;
        }
    }

}