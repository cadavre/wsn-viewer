
package pl.cadavre.wsnv.activity;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import pl.cadavre.wsnv.DatabaseConstants;
import pl.cadavre.wsnv.R;
import pl.cadavre.wsnv.WSNViewer;
import pl.cadavre.wsnv.entity.Node;
import pl.cadavre.wsnv.entity.Result;
import pl.cadavre.wsnv.entity.TypeMap;
import pl.cadavre.wsnv.entity.UniqueResultSet;
import pl.cadavre.wsnv.exception.NonUniqueException;
import pl.cadavre.wsnv.network.JDBCConnection;
import pl.cadavre.wsnv.type.Type;
import pl.cadavre.wsnv.type.TypeFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Connects to database, obtains node data and makes autoconfig
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class NodeAutoconfigActivity extends BaseActivity {

    TypeMap types = new TypeMap();

    ArrayList<Node> nodes = new ArrayList<Node>();

    int nodeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        Button btn = (Button) findViewById(R.id.test);
        btn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                new GetLasetsResultsFromTableTask().execute(getApp().connParams,
                        WSNViewer.RESULTS_TABLE);
            }
        });
    }

    protected void setGuessTypes(ResultSet results) {

        try {
            // 1. get Types from RESULTS_TABLE by columns
            ResultSetMetaData metaData = results.getMetaData();
            int count = metaData.getColumnCount();
            for (int i = 1; i <= count; i++) {
                String name = metaData.getColumnName(i);
                Type type = TypeFactory.get(name);
                if (type != null) {
                    this.types.put(i, type);
                }
            }

            // 2. get Node objects
            while (results.next()) {
                int id = results.getInt(DatabaseConstants.Results.ID);

                Node node = new Node();
                node.setId(id);

                // 3. fill latest Result with data
                UniqueResultSet resultSet = new UniqueResultSet();

                for (Type type : this.types.getAvailableTypes()) {
                    Result result = new Result(
                            results.getDouble(this.types.getColumnForType(type)), type,
                            results.getDate(DatabaseConstants.Results.TIMESTAMP));
                    resultSet.addResult(result);
                }

                node.setResults(resultSet);
                this.nodes.add(node);

                // 4. increase Node counter
                nodeCount++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NonUniqueException e) {
            e.printStackTrace();
        }

        LayoutInflater inflater = getLayoutInflater();
        View nodeView = inflater.inflate(R.layout.node_autoconfig_entry, null);

        for (Node node : nodes) {
            TextView tvResultsTime = (TextView) nodeView.findViewById(R.id.tvResultsTime);
            
        }
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

            setGuessTypes((ResultSet) results);
        }
    }
}
