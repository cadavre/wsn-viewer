
package pl.cadavre.wsnv.activity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import pl.cadavre.wsnv.DatabaseConstants;
import pl.cadavre.wsnv.PreferencesConstants;
import pl.cadavre.wsnv.R;
import pl.cadavre.wsnv.dialog.OKDialogFragment.OnOKClickListener;
import pl.cadavre.wsnv.entity.Health;
import pl.cadavre.wsnv.entity.Node;
import pl.cadavre.wsnv.entity.Result;
import pl.cadavre.wsnv.entity.Utils;
import pl.cadavre.wsnv.network.JDBCConnection;
import pl.cadavre.wsnv.type.LightType;
import pl.cadavre.wsnv.type.MoveType;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Connects to database, obtains node data and makes autoconfig
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class SystemStatusActivity extends BaseActivity {

    SharedPreferences preferences;

    ArrayList<Node> nodes = new ArrayList<Node>();

    ArrayList<Result> results = new ArrayList<Result>();

    ArrayList<Health> healths = new ArrayList<Health>();

    int nodeCount = 0;

    MenuItem miProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_status);

        this.preferences = getSharedPreferences(PreferencesConstants.NODES_PREFERENCES_NAME,
                MODE_PRIVATE);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        new GetLasetsResultsFromTableTask().execute(getApp().connParams);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        // inflate Menu
        inflater.inflate(R.menu.just_progress, menu);

        // set ProgressBar gravity to right
        ProgressBar pb = (ProgressBar) menu.findItem(R.id.miProgress).getActionView();
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(Gravity.RIGHT);
        pb.setLayoutParams(lp);

        // save element for further collapse/expand
        miProgress = menu.findItem(R.id.miProgress);
        miProgress.setEnabled(false);
        miProgress.expandActionView();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                goToDashboard();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        goToDashboard();
    }

    /**
     * Go to Dashboard Activity clearing top Activites stack
     */
    private void goToDashboard() {

        /*Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);*/
        finish();

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

        this.nodes.ensureCapacity(this.nodeCount);
        this.results.ensureCapacity(this.nodeCount);

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

    private void setHealthData(ResultSet results) {

        this.healths.ensureCapacity(this.nodeCount);

        try {
            results.beforeFirst();
            while (results.next()) {
                int nodeId = results.getInt(DatabaseConstants.Health.ID);
                Node node = Utils.getNode(this.nodes, nodeId);

                Health health = new Health(node, results);
                this.healths.add(health);
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
            LinearLayout addon = (LinearLayout) inflater
                    .inflate(R.layout.system_status_entry, null);
            Result result = this.results.get(i);
            Node node = this.nodes.get(i);

            Calendar sunrise = getApp().sunCalc.getOfficialSunriseCalendarForDate(result.getTime());
            Calendar sunset = getApp().sunCalc.getOfficialSunsetCalendarForDate(result.getTime());

            // fill node name and ID if already set
            TextView tvNodeID = (TextView) addon.findViewById(R.id.tvNodeID);
            tvNodeID.setText("ID: " + node.getId());
            TextView tvName = (TextView) addon.findViewById(R.id.tvName);
            tvName.setText(this.preferences.getString("nodeID:" + node.getId(), ""));

            // write last reading time
            TextView tvResultsTime = (TextView) addon.findViewById(R.id.tvResultsTime);
            int year = result.getTime().get(Calendar.YEAR);
            int month = result.getTime().get(Calendar.MONTH);
            int day = result.getTime().get(Calendar.DAY_OF_MONTH);
            int hour = result.getTime().get(Calendar.HOUR_OF_DAY);
            int minute = result.getTime().get(Calendar.MINUTE);
            int second = result.getTime().get(Calendar.SECOND);
            String stringTime = String.format("%s: %02d.%02d.%02d %02d:%02d:%02d",
                    getString(R.string.last_time), day, month, year, hour, minute, second);
            tvResultsTime.setText(stringTime);

            ImageView ivTime = (ImageView) addon.findViewById(R.id.ivTime);
            Calendar now = Calendar.getInstance(Locale.getDefault());
            result.getTime().add(Calendar.HOUR, 6);
            if (result.getTime().before(now)) {
                ivTime.setImageResource(R.drawable.ic_time_medium);
            }
            result.getTime().add(Calendar.HOUR, 18);
            if (result.getTime().before(now)) {
                tvResultsTime.setTextColor(Color.RED);
                ivTime.setImageResource(R.drawable.ic_time_old);
            }
            result.getTime().add(Calendar.HOUR, -24);

            // write last readings
            TextView tvRTemp = (TextView) addon.findViewById(R.id.tvRTemp);
            TextView tvRLight = (TextView) addon.findViewById(R.id.tvRLight);
            TextView tvRMove = (TextView) addon.findViewById(R.id.tvRMove);

            tvRTemp.setText(result.getTemperature() + " (" + result.getConvertedTemperature() + ")");

            String lightName = "";
            switch (result.getLightLevel()) {
                case LightType.LEVEL_DARK:
                    lightName = getString(R.string.light_dark);
                    break;
                case LightType.LEVEL_DUSK:
                    lightName = getString(R.string.light_dusk);
                    break;
                case LightType.LEVEL_BRIGHT:
                case LightType.LEVEL_SUNNY:
                    if (result.getTime().compareTo(sunrise) < 0
                            || result.getTime().compareTo(sunset) > 0) {
                        lightName = getString(R.string.light_bright);
                    } else {
                        lightName = getString(R.string.light_sunny);
                    }
                    break;
            }
            tvRLight.setText(result.getLight() + " (" + lightName + ")");

            String moveName = "";
            switch (result.getMoveStatus()) {
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
            tvRMove.setText(result.getMove() + " (" + moveName + ")");

            // set proper tags for another DB readings
            addon.findViewById(R.id.tvBattery).setTag("batteryTV,id:" + node.getId());
            addon.findViewById(R.id.ivBattery).setTag("batteryIV,id:" + node.getId());
            addon.findViewById(R.id.tvNodePkts).setTag("nodePkts,id:" + node.getId());
            addon.findViewById(R.id.tvHealthPkts).setTag("healthPkts,id:" + node.getId());
            addon.findViewById(R.id.tvParent).setTag("parent,id:" + node.getId());

            // set divider margin
            LinearLayout.LayoutParams lp = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 25, 0, 0);
            addon.setLayoutParams(lp);

            llNodesList.addView(addon);
        }
    }

    private void updateNodesList() {

        for (int i = 0; i < this.nodeCount; i++) {
            Health health = this.healths.get(i);
            Node node = this.nodes.get(i);

            TextView tvBattery = (TextView) findViewById(android.R.id.content).findViewWithTag(
                    "batteryTV,id:" + node.getId());
            ImageView ivBattery = (ImageView) findViewById(android.R.id.content).findViewWithTag(
                    "batteryIV,id:" + node.getId());
            tvBattery.setText(getString(R.string.battery) + ": " + health.getBattery() + " ("
                    + health.getConvertedBattery() + ")");
            if (health.getBattery() < 20) {
                tvBattery.setTextColor(Color.RED);
                ivBattery.setImageResource(R.drawable.ic_battery_empty);
            } else if (health.getBattery() < 22 && health.getBattery() >= 20) {
                ivBattery.setImageResource(R.drawable.ic_battery_half);
            }

            TextView tvNodePkts = (TextView) findViewById(android.R.id.content).findViewWithTag(
                    "nodePkts,id:" + node.getId());
            tvNodePkts.setText(getString(R.string.node_packets) + ": " + health.getNodePktsCount());
            TextView tvHealthPkts = (TextView) findViewById(android.R.id.content).findViewWithTag(
                    "healthPkts,id:" + node.getId());
            tvHealthPkts.setText(getString(R.string.health_packets) + ": "
                    + health.getHealthPktsCount());

            TextView tvParent = (TextView) findViewById(android.R.id.content).findViewWithTag(
                    "parent,id:" + node.getId());
            if (health.getParentId() > 0) {
                tvParent.setText(getString(R.string.parent) + ": " + getString(R.string.yes)
                        + " (ID: " + health.getParentId() + ")");
            } else {
                tvParent.setText(getString(R.string.parent) + ": " + getString(R.string.none));
            }

        }
    }

    private class GetLasetsResultsFromTableTask extends AsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {

            try {
                JDBCConnection conn = JDBCConnection.get();
                conn.openConnection((Bundle) params[0], true);

                String sql = "SELECT t1.* FROM (SELECT nodeid, max(result_time) as max_time FROM "
                        + DatabaseConstants.RESULTS_TABLE
                        + " GROUP BY nodeid) t2 JOIN "
                        + DatabaseConstants.RESULTS_TABLE
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
                showOKDialog(R.string.error, R.string.error_unknown, new OnOKClickListener() {

                    public void onOKClicked() {

                        miProgress.collapseActionView();
                    }
                });

                return;
            }

            setReadData((ResultSet) results);
            setNodesList();

            new GetLasetsHealthFromTableTask().execute(getApp().connParams);
        }
    }

    private class GetLasetsHealthFromTableTask extends AsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {

            try {
                JDBCConnection conn = JDBCConnection.get();
                conn.openConnection((Bundle) params[0], true);

                String sql = "SELECT t1.* FROM (SELECT nodeid, max(result_time) as max_time FROM "
                        + DatabaseConstants.HEALTH_TABLE
                        + " GROUP BY nodeid) t2 JOIN "
                        + DatabaseConstants.HEALTH_TABLE
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
                showOKDialog(R.string.error, R.string.error_unknown, new OnOKClickListener() {

                    public void onOKClicked() {

                        miProgress.collapseActionView();
                    }
                });

                return;
            }

            setHealthData((ResultSet) results);
            updateNodesList();

            miProgress.collapseActionView();
        }
    }
}
