
package pl.cadavre.wsnv.activity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import pl.cadavre.wsnv.DatabaseConstants;
import pl.cadavre.wsnv.R;
import pl.cadavre.wsnv.WSNViewer;
import pl.cadavre.wsnv.entity.Node;
import pl.cadavre.wsnv.entity.Result;
import pl.cadavre.wsnv.network.JDBCConnection;
import pl.cadavre.wsnv.type.LightType;
import pl.cadavre.wsnv.type.MoveType;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Connects to database, obtains node data and makes autoconfig
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class NodeAutoconfigActivity extends BaseActivity {

    ArrayList<Node> nodes = new ArrayList<Node>();

    ArrayList<Result> results = new ArrayList<Result>();

    int nodeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_autoconfig);

        Button btnConfigure = (Button) findViewById(R.id.btnConfigure);
        btnConfigure.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                new GetLasetsResultsFromTableTask().execute(getApp().connParams,
                        WSNViewer.RESULTS_TABLE);
            }
        });
    }

    private void setReadData(ResultSet results) {

        int size = 0;
        try {
            results.beforeFirst();
            results.last();
            size = results.getRow();
        } catch (SQLException e) {
            Log.e(TAG, "Error: reading size");
            e.printStackTrace();
        }
        this.nodeCount = size;
        this.nodes.ensureCapacity(size);
        this.results.ensureCapacity(size);

        try {
            results.beforeFirst();
            while (results.next()) {
                Node node = new Node();
                node.setId(results.getInt(DatabaseConstants.Results.ID));
                this.nodes.add(node);

                Result result = new Result(node, results);
                this.results.add(result);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error: reading nodes and results");
            e.printStackTrace();
        }
    }

    private void setNodesList() {

        LayoutInflater inflater = getLayoutInflater();

        LinearLayout llNodesList = (LinearLayout) findViewById(R.id.llNodesList);

        for (int i = 0; i < this.nodeCount; i++) {
            LinearLayout addon = (LinearLayout) inflater.inflate(R.layout.node_autoconfig_entry,
                    null);
            Result result = this.results.get(i);

            // write Node ID
            TextView tvNodeID = (TextView) addon.findViewById(R.id.tvNodeID);
            tvNodeID.setText("ID: " + this.nodes.get(i).getId());

            // write last reading time
            TextView tvResultsTime = (TextView) addon.findViewById(R.id.tvResultsTime);
            int year = result.getTime().get(Calendar.YEAR);
            String month = result.getTime().getDisplayName(Calendar.MONTH, Calendar.LONG,
                    Locale.getDefault());
            int day = result.getTime().get(Calendar.DAY_OF_MONTH);
            int hour = result.getTime().get(Calendar.HOUR_OF_DAY);
            int minute = result.getTime().get(Calendar.MINUTE);
            int second = result.getTime().get(Calendar.SECOND);
            String stringTime = String.format("%s: %02d %s %02d %02d:%02d:%02d",
                    getString(R.string.last_reading), day, month, year, hour, minute, second);
            tvResultsTime.setText(stringTime);

            // write last readings
            TextView tvRTemp = (TextView) addon.findViewById(R.id.tvRTemp);
            TextView tvRLight = (TextView) addon.findViewById(R.id.tvRLight);
            TextView tvRMove = (TextView) addon.findViewById(R.id.tvRMove);

            tvRTemp.setText(this.results.get(i).getConvertedTemperature());

            Drawable lightCompound = null;
            String lightName = "";
            switch (this.results.get(i).getLightLevel()) {
                case LightType.LEVEL_DARK:
                    lightCompound = getResources().getDrawable(R.drawable.ic_brightness_low);
                    lightName = getString(R.string.light_dark);
                    break;
                case LightType.LEVEL_DUSK:
                    lightCompound = getResources().getDrawable(R.drawable.ic_brightness_medium);
                    lightName = getString(R.string.light_dusk);
                    break;
                case LightType.LEVEL_BRIGHT:
                case LightType.LEVEL_SUNNY:
                    lightCompound = getResources().getDrawable(R.drawable.ic_brightness_high);
                    lightName = getString(R.string.light_bright);
                    break;
            }
            tvRLight.setText(lightName);
            tvRLight.setCompoundDrawablesWithIntrinsicBounds(lightCompound, null, null, null);

            String moveName = "";
            switch (this.results.get(i).getMoveStatus()) {
                case MoveType.MOVE_NONE:
                    moveName = getString(R.string.move_none);
                    break;
                case MoveType.MOVE_PRESENT:
                    moveName = getString(R.string.move_present);
                    break;
                case MoveType.UNKNOWN:
                    moveName = getString(R.string.unknown);
                    break;
            }
            tvRMove.setText(moveName);

            // set proper tags
            addon.findViewById(R.id.etName).setTag("id:" + this.nodes.get(i).getId());
            addon.findViewById(R.id.btnSetLocation).setTag("id:" + this.nodes.get(i).getId());

            // set divider margin
            LinearLayout.LayoutParams lp = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 25, 0, 0);
            addon.setLayoutParams(lp);

            llNodesList.addView(addon);
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

            setReadData((ResultSet) results);
            setNodesList();
        }
    }
}
