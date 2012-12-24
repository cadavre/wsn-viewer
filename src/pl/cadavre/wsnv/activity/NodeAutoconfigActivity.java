
package pl.cadavre.wsnv.activity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import pl.cadavre.wsnv.DatabaseConstants;
import pl.cadavre.wsnv.PreferencesConstants;
import pl.cadavre.wsnv.R;
import pl.cadavre.wsnv.WSNViewer;
import pl.cadavre.wsnv.dialog.OKDialogFragment.OnOKClickListener;
import pl.cadavre.wsnv.entity.Node;
import pl.cadavre.wsnv.entity.Result;
import pl.cadavre.wsnv.network.JDBCConnection;
import pl.cadavre.wsnv.type.LightType;
import pl.cadavre.wsnv.type.MoveType;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

    SharedPreferences preferences;

    ArrayList<Node> nodes = new ArrayList<Node>();

    ArrayList<Result> results = new ArrayList<Result>();

    int nodeCount = 0;

    MenuItem miProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_autoconfig);

        this.preferences = getSharedPreferences(PreferencesConstants.NODES_PREFERENCES_NAME,
                MODE_PRIVATE);

        Button btnConfigure = (Button) findViewById(R.id.btnConfigure);
        btnConfigure.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                miProgress.expandActionView();
                new GetLasetsResultsFromTableTask().execute(getApp().connParams,
                        WSNViewer.RESULTS_TABLE);
            }
        });
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
            Node node = this.nodes.get(i);

            Calendar sunrise = getApp().sunCalc.getOfficialSunriseCalendarForDate(result.getTime());
            Calendar sunset = getApp().sunCalc.getOfficialSunsetCalendarForDate(result.getTime());

            // write Node ID
            TextView tvNodeID = (TextView) addon.findViewById(R.id.tvNodeID);
            tvNodeID.setText("ID: " + node.getId());

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
            addon.findViewById(R.id.etName).setTag("et,id:" + node.getId());
            addon.findViewById(R.id.btnSetLocation).setTag("btn,id:" + node.getId());

            // set divider margin
            LinearLayout.LayoutParams lp = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 25, 0, 0);
            addon.setLayoutParams(lp);

            llNodesList.addView(addon);
        }
    }

    private void saveNodesData() {

        SharedPreferences.Editor prefEditor = this.preferences.edit();

        for (int i = 0; i < this.nodeCount; i++) {
            Node node = this.nodes.get(i);
            EditText et = (EditText) findViewById(android.R.id.content).findViewWithTag(
                    "et,id:" + node.getId());

            prefEditor.putString("nodeID:" + node.getId(), et.getText().toString());
        }

        prefEditor.putInt("nodeCount", this.nodeCount);
        prefEditor.commit();

        return;
    }

    private void cleanupViews() {

        miProgress.collapseActionView(); // hide progress
        ((Button) findViewById(R.id.btnConfigure)).setEnabled(false); // disable config button

        invalidateOptionsMenu();
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
                showOKDialog(R.string.error, R.string.error_unknown, new OnOKClickListener() {

                    public void onOKClicked() {

                        miProgress.collapseActionView();
                    }
                });

                return;
            }

            setReadData((ResultSet) results);
            setNodesList();
            cleanupViews();
        }
    }
}
