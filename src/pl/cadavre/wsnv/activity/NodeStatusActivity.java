
package pl.cadavre.wsnv.activity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import pl.cadavre.wsnv.DatabaseConstants;
import pl.cadavre.wsnv.PreferencesConstants;
import pl.cadavre.wsnv.R;
import pl.cadavre.wsnv.dialog.OKDialogFragment.OnOKClickListener;
import pl.cadavre.wsnv.entity.Health;
import pl.cadavre.wsnv.entity.Node;
import pl.cadavre.wsnv.entity.Result;
import pl.cadavre.wsnv.network.JDBCConnection;
import pl.cadavre.wsnv.type.LightType;
import pl.cadavre.wsnv.type.MoveType;
import android.annotation.SuppressLint;
import android.app.ActionBar;
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
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Connects to database, obtains node data and makes autoconfig
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
@SuppressLint("DefaultLocale")
public class NodeStatusActivity extends BaseActivity {

    SharedPreferences preferences;

    Timer timer;

    ArrayList<Node> nodes = new ArrayList<Node>();

    ArrayList<Result> results = new ArrayList<Result>();

    ArrayList<Health> healths = new ArrayList<Health>();

    int nodeCount = 0;

    MenuItem miProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_status);

        this.preferences = getSharedPreferences(PreferencesConstants.NODES_PREFERENCES_NAME,
                MODE_PRIVATE);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        new GetLasetsResultsFromTableTask().execute(getApp().connParams);
    }

    @Override
    protected void onResume() {

        super.onResume();
        TimerTask refreshTask = new TimerTask() {

            @Override
            public void run() {

                runOnUiThread(new Runnable() {

                    public void run() {

                        miProgress.expandActionView();
                    }
                });
                new GetLasetsResultsFromTableTask().execute(getApp().connParams);
            }
        };

        timer = new Timer("resultRefresher", true);
        timer.schedule(refreshTask, 10000, 10000);
    }

    @Override
    protected void onPause() {

        timer.cancel();
        super.onPause();
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
        this.miProgress = menu.findItem(R.id.miProgress);
        this.miProgress.setEnabled(false);
        this.miProgress.expandActionView();

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

    private void setNodesList() {

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout llNodesList = (LinearLayout) findViewById(R.id.llNodesList);

        for (int i = 0; i < this.nodeCount; i++) {
            LinearLayout addon = (LinearLayout) inflater.inflate(R.layout.node_status_entry, null);
            Result result = this.results.get(i);
            Node node = this.nodes.get(i);

            Calendar sunrise = getApp().sunCalc.getOfficialSunriseCalendarForDate(result.getTime());
            Calendar sunset = getApp().sunCalc.getOfficialSunsetCalendarForDate(result.getTime());

            // fill node name
            TextView tvName = (TextView) addon.findViewById(R.id.tvName);
            tvName.setText(this.preferences.getString("nodeID:" + node.getId(), ""));

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
            addon.findViewById(R.id.tvBattery).setTag("battery,id:" + node.getId());

            // set divider margin
            LinearLayout.LayoutParams lp = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 25, 0, 0);
            addon.setLayoutParams(lp);

            llNodesList.addView(addon);
        }
    }

    private void clearList() {

        LinearLayout llNodesList = (LinearLayout) findViewById(R.id.llNodesList);
        llNodesList.removeAllViews();

        this.nodes.clear();
        this.results.clear();
        this.healths.clear();
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

            clearList();

            setReadData((ResultSet) results);
            setNodesList();

            // new GetLasetsHealthFromTableTask().execute(getApp().connParams);

            miProgress.collapseActionView();
        }
    }
}
