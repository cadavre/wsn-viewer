
package pl.zeman.iqh.activity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import pl.zeman.iqh.DatabaseConstants;
import pl.zeman.iqh.PreferencesConstants;
import pl.zeman.iqh.R;
import pl.zeman.iqh.dialog.OKDialogFragment.OnOKClickListener;
import pl.zeman.iqh.entity.Health;
import pl.zeman.iqh.entity.Node;
import pl.zeman.iqh.entity.Result;
import pl.zeman.iqh.entity.Utils;
import pl.zeman.iqh.network.JDBCConnection;
import pl.zeman.iqh.type.LightType;
import pl.zeman.iqh.type.MoveType;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Connects to database, obtains node data and makes autoconfig
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class NodeAutoconfigActivity extends BaseActivity {

    private static final int PICK_POINT_REQUEST = 1;

    SharedPreferences preferences;

    AsyncTask currentTask;

    ArrayList<Node> nodes = new ArrayList<Node>();

    ArrayList<Result> results = new ArrayList<Result>();

    ArrayList<Health> healths = new ArrayList<Health>();

    int nodeCount = 0;

    MenuItem miProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_autoconfig);

        this.preferences = getSharedPreferences(PreferencesConstants.NODES_PREFERENCES_NAME,
                MODE_PRIVATE);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Button btnConfigure = (Button) findViewById(R.id.btnConfigure);
        btnConfigure.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                miProgress.expandActionView();
                currentTask = new GetLasetsResultsFromTableTask().execute(getApp().connParams);
            }
        });
    }

    @Override
    protected void onPause() {

        if (this.currentTask != null) {
            this.currentTask.cancel(true);
            this.currentTask = null;
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        if (findViewById(R.id.btnConfigure).isEnabled()) {
            // inflate Menu
            inflater.inflate(R.menu.just_progress, menu);

            // set ProgressBar gravity to right
            ProgressBar pb = (ProgressBar) menu.findItem(R.id.miProgress).getActionView();
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(Gravity.RIGHT);
            pb.setLayoutParams(lp);

            // save element for further collapse/expand
            this.miProgress = menu.findItem(R.id.miProgress);
            this.miProgress.setEnabled(false);
        } else {
            // inflate Menu
            inflater.inflate(R.menu.just_save, menu);

            menu.findItem(R.id.miSave).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {

                    saveNodesData();
                    finish();

                    return true;
                }
            });
        }

        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_POINT_REQUEST) {
            if (resultCode == RESULT_OK) {
                int id = data.getIntExtra("nodeId", 0);
                String schema = data.getStringExtra("schema");
                int x = data.getIntExtra("x", 0);
                int y = data.getIntExtra("y", 0);
                Log.d(TAG, "gotX:" + x);
                Log.d(TAG, "gotY:" + y);
                Log.d(TAG, "gotID:" + id);
                Log.d(TAG, "gotSchema:" + schema);

                saveNodeLocation(id, x, y, schema);
            }
        }
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

        if (getApp().hasNecessaryPreferences()) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            showOKDialog(R.string.error, R.string.error_not_necessary_settings);
        }
    }

    /**
     * Set Nodes and Results
     * 
     * @param results
     */
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

    /**
     * Set Health data
     * 
     * @param results
     */
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

    /**
     * Fill Nodes list on screen
     */
    private void setNodesList() {

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout llNodesList = (LinearLayout) findViewById(R.id.llNodesList);

        for (int i = 0; i < this.nodeCount; i++) {
            LinearLayout addon = (LinearLayout) inflater.inflate(R.layout.node_autoconfig_entry,
                    null);
            Result result = this.results.get(i);
            Node node = this.nodes.get(i);

            Calendar sunrise = getApp().sunCalc.getOfficialSunriseCalendarForDate(result.getTime());
            Calendar sunset = getApp().sunCalc.getOfficialSunsetCalendarForDate(result.getTime());

            // write Node ID
            TextView tvNodeID = (TextView) addon.findViewById(R.id.tvNodeID);
            tvNodeID.setText("ID: " + node.getId());

            // fill ETs if already set
            EditText etName = (EditText) addon.findViewById(R.id.etName);
            etName.setText(this.preferences.getString("nodeID:" + node.getId(), ""));

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

            result.getTime().add(Calendar.HOUR, 6);
            if (result.getTime().before(Calendar.getInstance(Locale.getDefault()))) {
                tvResultsTime.setTextColor(Color.RED);
            }
            result.getTime().add(Calendar.HOUR, -6);

            // write last readings
            TextView tvRTemp = (TextView) addon.findViewById(R.id.tvRTemp);
            TextView tvRLight = (TextView) addon.findViewById(R.id.tvRLight);
            TextView tvRMove = (TextView) addon.findViewById(R.id.tvRMove);

            tvRTemp.setText(result.getConvertedTemperature());

            Drawable lightCompound = null;
            String lightName = "";
            switch (result.getLightLevel()) {
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
                    if (result.getTime().compareTo(sunrise) < 0
                            || result.getTime().compareTo(sunset) > 0) {
                        lightName = getString(R.string.light_bright);
                    } else {
                        lightName = getString(R.string.light_sunny);
                    }
                    break;
            }
            tvRLight.setText(lightName);
            tvRLight.setCompoundDrawablesWithIntrinsicBounds(lightCompound, null, null, null);

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
            tvRMove.setText(moveName);

            // set proper tags
            addon.findViewById(R.id.etName).setTag("label,id:" + node.getId());
            addon.findViewById(R.id.btnSetLocation).setTag("location,id:" + node.getId());
            addon.findViewById(R.id.tvBattery).setTag("battery,id:" + node.getId());

            // set location butons listeners
            addon.findViewById(R.id.btnSetLocation).setOnClickListener(new OnClickListener() {

                public void onClick(View v) {

                    saveNodesData();

                    Intent intent = new Intent(NodeAutoconfigActivity.this, HouseActivity.class);
                    intent.putExtra("nodeId", ((String) v.getTag()).replace("location,id:", ""));
                    startActivityForResult(intent, PICK_POINT_REQUEST);
                }
            });

            // set divider margin
            LinearLayout.LayoutParams lp = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 25, 0, 0);
            addon.setLayoutParams(lp);

            llNodesList.addView(addon);
        }
    }

    /**
     * Update Nodes list with Health data
     */
    private void updateNodesList() {

        for (int i = 0; i < this.nodeCount; i++) {
            Health health = this.healths.get(i);
            Node node = this.nodes.get(i);
            TextView tvBattery = (TextView) findViewById(android.R.id.content).findViewWithTag(
                    "battery,id:" + node.getId());
            tvBattery.setText(getString(R.string.battery) + ": " + health.getConvertedBattery());
            if (health.getBattery() < 20) {
                tvBattery.setTextColor(Color.RED);
            }
        }
    }

    /**
     * Save Nodes data to SharedPreferences
     */
    private void saveNodesData() {

        SharedPreferences.Editor prefEditor = this.preferences.edit();

        for (int i = 0; i < this.nodeCount; i++) {
            Node node = this.nodes.get(i);
            EditText et = (EditText) findViewById(android.R.id.content).findViewWithTag(
                    "label,id:" + node.getId());

            prefEditor.putString("nodeID:" + node.getId(), et.getText().toString());
        }

        prefEditor.putInt("nodeCount", this.nodeCount);
        prefEditor.commit();

        return;
    }

    /**
     * Save single Node location to SharedPreferences
     * 
     * @param id Node ID
     * @param x X coordinate
     * @param y Y coordinate
     * @param schema Z coordinate
     */
    private void saveNodeLocation(int id, int x, int y, String schema) {

        SharedPreferences.Editor prefEditor = this.preferences.edit();
        prefEditor.putInt(id + "x", x);
        prefEditor.putInt(id + "y", y);
        prefEditor.putString(id + "schema", schema);
        prefEditor.commit();
    }

    /**
     * Cleanup Views after proper autoconfig
     */
    private void cleanupViews() {

        miProgress.collapseActionView(); // hide progress
        ((Button) findViewById(R.id.btnConfigure)).setEnabled(false); // disable config button

        invalidateOptionsMenu();
    }

    /**
     * Asynchronical task for loading Results from database
     * 
     * @author Seweryn Zeman <seweryn.zeman@gmail.com>
     */
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

            currentTask = new GetLasetsHealthFromTableTask().execute(getApp().connParams);

            cleanupViews();
        }

        @Override
        protected void onCancelled() {

            Log.d(TAG, "ASyncTask cancelled succesfully");
            super.onCancelled();
        }
    }

    /**
     * Asynchronical task for loading Results from database
     * 
     * @author Seweryn Zeman <seweryn.zeman@gmail.com>
     */
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
            
            currentTask = null;
        }

        @Override
        protected void onCancelled() {

            Log.d(TAG, "ASyncTask cancelled succesfully");
            super.onCancelled();
        }
    }
}
