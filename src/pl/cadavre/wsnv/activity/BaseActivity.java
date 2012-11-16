
package pl.cadavre.wsnv.activity;

import pl.cadavre.wsnv.PreferencesConstants;
import pl.cadavre.wsnv.R;
import pl.cadavre.wsnv.dialog.OKDialogFragment;
import pl.cadavre.wsnv.dialog.OKDialogFragment.OnOKClickListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Main Activity with Network handling
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class BaseActivity extends Activity {

    public static final String TAG = "WSNV";

    public static final int NETWORK_WIFI = 1;

    public static final int NETWORK_MOBILE = 2;

    ConnectivityManager connMgr;

    NetworkInfo networkInfo;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!getClass().getCanonicalName().equals(
                "pl.cadavre.wsnv.activity.ConnectionPreferenceActivity")
                && !hasNecessaryPreferences()) {
            Intent connSettingIntent = new Intent(this, ConnectionPreferenceActivity.class);
            startActivity(connSettingIntent);
            finish();
        }
    }

    @Override
    protected void onStart() {

        super.onStart();
        if (!hasInternetConnection()) {
            OnOKClickListener onOK = new OnOKClickListener() {

                public void onOKClicked() {

                    startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    finish();
                }
            };
            showOKDialog(R.string.error, R.string.error_no_internet_connection, onOK);
        } else {
            getNetworkType();
        }
    }

    /**
     * Check if Internet connection is accessible
     * 
     * @return boolean
     */
    protected boolean hasInternetConnection() {

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get network connection type - WiFi or Mobile data
     * 
     * @return int
     */
    protected int getNetworkType() {

        NetworkInfo currentNetwork = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = currentNetwork.isConnected();
        currentNetwork = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = currentNetwork.isConnected();

        return isWifiConn ? NETWORK_WIFI : (isMobileConn ? NETWORK_MOBILE : null);
    }

    /**
     * Check if SharedPreferences contains data necessary to connect to database
     * 
     * @return boolean
     */
    protected boolean hasNecessaryPreferences() {

        if (preferences.contains(PreferencesConstants.DB_HOST)
                && preferences.contains(PreferencesConstants.DB_PORT)
                && preferences.contains(PreferencesConstants.DB_DATABASE)
                && preferences.contains(PreferencesConstants.DB_USER)
                && preferences.contains(PreferencesConstants.DB_PASSWORD)) {
            return true;
        }

        return false;
    }

    /**
     * Display OK dialog with resulting Activity.finish()
     * 
     * @param title
     * @param content
     */
    void showOKDialog(int title, int content) {

        showOKDialog(title, content, null);
    }

    /**
     * Display OK dialog with listener callback
     * 
     * @param title
     * @param content
     * @param listener
     */
    void showOKDialog(int title, int content, OnOKClickListener listener) {

        OKDialogFragment newFragment = OKDialogFragment.newInstance(title, content);
        newFragment.setOnOKClickListener(listener);
        newFragment.show(getFragmentManager(), "okdialog");
    }

}
