
package pl.zeman.iqh.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.zeman.iqh.PreferencesConstants;
import pl.zeman.iqh.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Fragment for connection settings
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class ConnectionPreferenceFragment extends PreferenceFragment implements
        OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.conn_settings);

        WifiManager wifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> wifiNetworks = wifiMgr.getConfiguredNetworks();

        // get list of saved Wi-Fi SSIDs
        ArrayList<CharSequence> ssids = new ArrayList<CharSequence>();
        CharSequence[] output = null;
        if (wifiNetworks != null && wifiNetworks.size() != 0) {
            for (WifiConfiguration wifiConfiguration : wifiNetworks) {
                ssids.add(wifiConfiguration.SSID);
            }
            output = (CharSequence[]) ssids.toArray(new CharSequence[ssids.size()]);
        }

        // set obtained list if any SSID exsists
        ListPreference ssidPref = (ListPreference) findPreference(PreferencesConstants.LOCAL_WIFI_SSID);
        if (output != null) {
            ssidPref.setEntries(output);
            ssidPref.setEntryValues(output);
        } else {
            ssidPref.setEnabled(false);
        }

        // set current preferences values as summaries
        Map<String, ?> prefs = getPreferenceScreen().getSharedPreferences().getAll();
        Set<String> keys = prefs.keySet();
        for (String key : keys) {
            String curentVal = (String) prefs.get(key);
            Preference pref = (Preference) findPreference(key);
            if (curentVal != null) {
                pref.setSummary(curentVal);
            }
        }
    }

    @Override
    public void onResume() {

        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
        super.onPause();
    }

    /**
     * Listener of changed Preferences
     */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        String curentVal = sharedPreferences.getString(key, null);
        if (curentVal != null && curentVal.isEmpty()) {
            sharedPreferences.edit().remove(key).commit();
        } else {
            Preference pref = (Preference) findPreference(key);
            pref.setSummary(curentVal);
        }
    }
}
