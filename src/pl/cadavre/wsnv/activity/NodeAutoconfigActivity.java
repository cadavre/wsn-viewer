
package pl.cadavre.wsnv.activity;

import java.sql.ResultSet;
import java.sql.SQLException;

import pl.cadavre.wsnv.R;
import pl.cadavre.wsnv.WSNViewer;
import pl.cadavre.wsnv.network.JDBCConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Connects to database, obtains node data and makes autoconfig
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class NodeAutoconfigActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
    }

    @Override
    protected void onStart() {

        super.onStart();
        new GetLasetsResultsFromTableTask().execute(getApp().connParams, WSNViewer.HEALTH_TABLE);
    }

    private class GetLasetsResultsFromTableTask extends AsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {

            try {
                JDBCConnection conn = JDBCConnection.get();
                conn.openConnection((Bundle) params[0], true);

                String tableName = (String) params[1];
                String sql = "SELECT t1.* FROM (SELECT nodeid, max(result_time) as max_time FROM "
                        + tableName
                        + " GROUP BY nodeid) t2 JOIN "
                        + tableName
                        + " t1 ON t2.nodeid = t1.nodeid AND t2.max_time = t1.result_time ORDER BY nodeid ASC";
                ResultSet results = conn.getResults(sql);

                conn.closeConnection();

                return results;
            } catch (SQLException e) {
                return false;
            } catch (ClassNotFoundException e) {
                return false;
            }

        }

        @Override
        protected void onPostExecute(Object results) {

            if (results.getClass() == Boolean.class) {
                Toast.makeText(NodeAutoconfigActivity.this, "errrur", Toast.LENGTH_SHORT).show();

                return;
            }

            try {
                while (((ResultSet) results).next()) {
                    Log.i(TAG, ((ResultSet) results).getString("nodeid"));
                }
            } catch (SQLException e) {
                Toast.makeText(NodeAutoconfigActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
