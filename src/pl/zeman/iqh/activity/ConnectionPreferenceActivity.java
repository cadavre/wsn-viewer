
package pl.zeman.iqh.activity;

import java.sql.SQLException;

import pl.zeman.iqh.R;
import pl.zeman.iqh.dialog.OKDialogFragment.OnOKClickListener;
import pl.zeman.iqh.network.JDBCConnection;
import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;

/**
 * Dashboard Activity class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class ConnectionPreferenceActivity extends BaseActivity {

    MenuItem miTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conn_pref);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                goToDashboard();
                return true;
            case R.id.miTest:
                getApp().setConnectionPreferences(true);
                if (getApp().hasNecessaryPreferences()) {
                    new TestConnectionTask().execute(getApp().connParams);
                } else {
                    showOKDialog(R.string.error, R.string.error_not_necessary_settings,
                            new OnOKClickListener() {

                                public void onOKClicked() {

                                    miTest.collapseActionView();
                                }
                            });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // inflate Menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conn_pref_activity, menu);

        // set ProgressBar gravity to right
        ProgressBar pb = (ProgressBar) menu.findItem(R.id.miTest).getActionView();
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(Gravity.RIGHT);
        pb.setLayoutParams(lp);

        // save element for further collapse/expand
        miTest = menu.findItem(R.id.miTest);

        return true;
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

    private class TestConnectionTask extends AsyncTask<Bundle, String, Boolean> {

        @Override
        protected Boolean doInBackground(Bundle... params) {

            try {
                JDBCConnection conn = JDBCConnection.get();
                conn.openConnection(params[0], true);
                conn.closeConnection();

                return true;
            } catch (SQLException e) {
                return false;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean successful) {

            if (successful) {
                showOKDialog(R.string.success, R.string.success_done_db_connection,
                        new OnOKClickListener() {

                            public void onOKClicked() {

                                Intent autoConfigIntent = new Intent(
                                        ConnectionPreferenceActivity.this,
                                        NodeAutoconfigActivity.class);
                                startActivity(autoConfigIntent);
                                finish();
                            }
                        });
            } else {
                showOKDialog(R.string.error, R.string.error_no_db_connection);
            }

            miTest.collapseActionView();
        }
    }
}
