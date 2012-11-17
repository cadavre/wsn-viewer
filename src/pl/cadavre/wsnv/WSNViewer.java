
package pl.cadavre.wsnv;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * WSN Viewer application Class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class WSNViewer extends Application {

    public static final String TAG = "WSNV";

    public static final int NETWORK_WIFI = 1;

    public static final int NETWORK_MOBILE = 2;
    
    public static final String HEALTH_TABLE = "node_health";
    
    public static final String RESULTS_TABLE = "xbw_da100_results";

    public Bundle connParams;

    private boolean areConnParamsSet = false;

    private ConnectivityManager connMgr;

    private NetworkInfo networkInfo;

    private SharedPreferences preferences;

    @Override
    public void onCreate() {

        super.onCreate();
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    /**
     * Check if Internet connection is accessible
     * 
     * @return boolean
     */
    public boolean hasInternetConnection() {

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
    public int getNetworkType() {

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
    public boolean hasNecessaryPreferences() {

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
     * Set database connection preferences from SharedPreferences according to current connection state
     * 
     * @param force Force setting new preferences even if already set
     */
    public void setConnectionPreferences(boolean force) {

        if (!areConnParamsSet || force) {

            connParams = new Bundle();
            connParams.putString(PreferencesConstants.DB_HOST,
                    preferences.getString(PreferencesConstants.DB_HOST, null));
            connParams.putString(PreferencesConstants.DB_PORT,
                    preferences.getString(PreferencesConstants.DB_PORT, "5432"));
            connParams.putString(PreferencesConstants.DB_DATABASE,
                    preferences.getString(PreferencesConstants.DB_DATABASE, null));
            connParams.putString(PreferencesConstants.DB_USER,
                    preferences.getString(PreferencesConstants.DB_USER, null));
            connParams.putString(PreferencesConstants.DB_PASSWORD,
                    preferences.getString(PreferencesConstants.DB_PASSWORD, null));

            // if current network type is Wi-Fi
            if (getNetworkType() == NETWORK_WIFI) {

                WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiConnection = wifiMgr.getConnectionInfo();

                // if SSID fits to local_wifi_ssid and db_localhost is set
                if (preferences.getString(PreferencesConstants.LOCAL_WIFI_SSID, "").equals(
                        "\"" + wifiConnection.getSSID() + "\"")
                        && preferences.contains(PreferencesConstants.DB_LOCALHOST)) {
                    connParams.putString(PreferencesConstants.DB_HOST,
                            preferences.getString(PreferencesConstants.DB_LOCALHOST, null));
                }
            }

            areConnParamsSet = true;
        }
    }

    /**
     * Set connection preferences from SharedPreferences
     */
    public void setConnectionPreferences() {

        setConnectionPreferences(false);
    }
}
