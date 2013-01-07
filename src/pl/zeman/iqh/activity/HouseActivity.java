
package pl.zeman.iqh.activity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import pl.zeman.iqh.DatabaseConstants;
import pl.zeman.iqh.PreferencesConstants;
import pl.zeman.iqh.R;
import pl.zeman.iqh.dialog.OKDialogFragment.OnOKClickListener;
import pl.zeman.iqh.entity.Node;
import pl.zeman.iqh.entity.Result;
import pl.zeman.iqh.entity.Utils;
import pl.zeman.iqh.network.JDBCConnection;
import pl.zeman.iqh.type.LightType;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * House Activity class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class HouseActivity extends BaseActivity {

    ViewPager vpSchemaContainer;

    OnPageChangeListener pageListener;

    AssetsPagerAdapter adapter;

    AsyncTask currentTask;

    ArrayList<Node> nodes = new ArrayList<Node>();

    ArrayList<Result> results = new ArrayList<Result>();

    int nodeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house);

        ArrayList<String> assets = new ArrayList<String>();
        assets.add("parter.png");
        assets.add("pietro.png");
        this.adapter = new AssetsPagerAdapter(getApplicationContext(), assets);
        this.vpSchemaContainer = (ViewPager) findViewById(R.id.vpSchemaContainer);
        this.vpSchemaContainer.setAdapter(adapter);
        this.pageListener = new OnPageChangeListener();
        this.vpSchemaContainer.setOnPageChangeListener(pageListener);

        currentTask = new GetLasetsResultsFromTableTask().execute(getApp().connParams);
    }

    @Override
    protected void onPause() {

        if (this.currentTask != null) {
            this.currentTask.cancel(true);
            this.currentTask = null;
        }
        super.onPause();
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
     * Adapter for schemas in ViewPager
     * 
     * @author Seweryn Zeman <seweryn.zeman@gmail.com>
     */
    private class AssetsPagerAdapter extends PagerAdapter {

        private List<String> assets;

        private Context context;

        private ArrayList<Boolean> initialized = new ArrayList<Boolean>();

        public AssetsPagerAdapter(Context context, List<String> assets) {

            this.context = context;
            this.assets = assets;
            initialized.add(false);
            initialized.add(false);
        }

        @Override
        public int getCount() {

            return assets.size();
        }

        @Override
        public Object instantiateItem(View collection, int position) {

            RelativeLayout view = new RelativeLayout(context);
            RelativeLayout.LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            view.setLayoutParams(lp);
            view.setTag(assets.get(position).replace(".png", ""));
            Log.d(TAG, view.getTag() + " is a tag");

            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(lp);
            try {
                Drawable d = Drawable
                        .createFromStream(getAssets().open(assets.get(position)), null);
                imageView.setImageDrawable(d);
            } catch (Exception e) {
                e.printStackTrace();
            }

            imageView.setOnTouchListener(new OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    if (getCallingActivity() != null) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            Vibrator vibrator = (Vibrator) HouseActivity.this
                                    .getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(200);

                            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
                            am.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f);

                            int x = (int) event.getX() - 20;
                            int y = (int) event.getY() - 20;

                            Intent intent = new Intent();
                            intent.putExtra("x", x);
                            intent.putExtra("y", y);
                            intent.putExtra("nodeId",
                                    Integer.parseInt(getIntent().getExtras().getString("nodeId")));
                            intent.putExtra("schema", assets.get(pageListener.getCurrentPage())
                                    .replace(".png", ""));
                            setResult(RESULT_OK, intent);

                            ImageView view = new ImageView(HouseActivity.this);
                            view.setImageResource(R.drawable.ic_pinpoint);
                            RelativeLayout.LayoutParams lp = new LayoutParams(
                                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                            lp.setMargins(x, y, 0, 0);
                            view.setLayoutParams(lp);

                            RelativeLayout rlSchema = (RelativeLayout) findViewById(R.id.rlSchema);
                            int page = HouseActivity.this.pageListener.getCurrentPage();
                            if (page == 0) {
                                RelativeLayout rlRoot = (RelativeLayout) rlSchema
                                        .findViewWithTag("parter");
                                rlRoot.addView(view);
                            } else {
                                RelativeLayout rlRoot = (RelativeLayout) rlSchema
                                        .findViewWithTag("pietro");
                                rlRoot.addView(view);
                            }

                            showOKDialog(R.string.location_set, R.string.success_location_set,
                                    new OnOKClickListener() {

                                        public void onOKClicked() {

                                            finish();
                                        }
                                    });
                        }
                    }

                    return true;
                }
            });

            view.addView(imageView);
            ((ViewPager) collection).addView(view);

            if (!initialized.get(position)) {
                Log.d(TAG, "" + position);
                // get Prefs
                SharedPreferences preferences = getSharedPreferences(
                        PreferencesConstants.NODES_PREFERENCES_NAME, MODE_PRIVATE);

                int nodeCount = preferences.getInt("nodeCount", 0);
                for (int i = 0; i < nodeCount; i++) {
                    int id = i + 1;
                    int x = preferences.getInt(id + "x", 0);
                    int y = preferences.getInt(id + "y", 0);
                    String schema = preferences.getString(id + "schema", "");
                    String label = preferences.getString("nodeID:" + id, "");

                    if (x != 0 && y != 0 && !schema.isEmpty()
                            && schema.equals(assets.get(position).replace(".png", ""))) {
                        // TextView with a compound drawable to draw
                        TextView pinView = new TextView(HouseActivity.this);
                        pinView.setTextColor(Color.WHITE);
                        pinView.setShadowLayer(2.0f, 0, 0, Color.BLACK);
                        pinView.setText("ID: " + id + "\n" + label);
                        Drawable pin = getResources().getDrawable(R.drawable.ic_pinpoint);
                        pinView.setCompoundDrawablesWithIntrinsicBounds(null, pin, null, null);
                        RelativeLayout.LayoutParams pinLP = new LayoutParams(
                                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        // setting proper margins to position ImageView pin
                        pinLP.setMargins(x, y, 0, 0);

                        if (!HouseActivity.this.results.isEmpty()) {

                            Result result = Utils.getResultForID(HouseActivity.this.results, id);
                            Calendar sunrise = getApp().sunCalc
                                    .getOfficialSunriseCalendarForDate(result.getTime());
                            Calendar sunset = getApp().sunCalc
                                    .getOfficialSunsetCalendarForDate(result.getTime());

                            pinView.append("\n" + result.getConvertedTemperature());
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
                            pinView.append("\n" + lightName);

                            result.getTime().add(Calendar.HOUR, 6);
                            if (result.getTime().before(Calendar.getInstance(Locale.getDefault()))) {
                                pinView.setTextColor(Color.DKGRAY);
                            }
                            result.getTime().add(Calendar.HOUR, -6);
                        }

                        pinView.setLayoutParams(pinLP);

                        // add pin to proper View
                        RelativeLayout rlSchema = (RelativeLayout) findViewById(R.id.rlSchema);
                        RelativeLayout rlRoot = (RelativeLayout) rlSchema.findViewWithTag(schema);
                        Log.d(TAG, "tag: " + schema);
                        rlRoot.addView(pinView);
                    }
                }

                initialized.set(position, true);
            }

            return view;
        }

        @Override
        public void destroyItem(View collection, int position, Object view) {

            ((ViewPager) collection).removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {

            return view == object;
        }

        @Override
        public Parcelable saveState() {

            return null;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public void startUpdate(View arg0) {

        }

        @Override
        public void finishUpdate(View arg0) {

        }
    }

    /**
     * Listener for changing schemas
     * 
     * @author Seweryn Zeman <seweryn.zeman@gmail.com>
     */
    public class OnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        private int currentPage;

        @Override
        public void onPageSelected(int position) {

            currentPage = position;
        }

        public int getCurrentPage() {

            return currentPage;
        }
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
                showOKDialog(R.string.error, R.string.error_unknown);

                return;
            }

            setReadData((ResultSet) results);
            ArrayList<String> assets = new ArrayList<String>();
            assets.add("parter.png");
            assets.add("pietro.png");
            HouseActivity.this.adapter = new AssetsPagerAdapter(getApplicationContext(), assets);
            vpSchemaContainer.setAdapter(HouseActivity.this.adapter);
            
            currentTask = null;
        }

        @Override
        protected void onCancelled() {

            Log.d(TAG, "ASyncTask cancelled succesfully");
            super.onCancelled();
        }
    }
}
