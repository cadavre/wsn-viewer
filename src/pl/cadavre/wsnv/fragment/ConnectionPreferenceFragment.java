
package pl.cadavre.wsnv.fragment;

import java.util.ArrayList;
import java.util.List;

import pl.cadavre.wsnv.PreferencesConstants;
import pl.cadavre.wsnv.R;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

/**
 * Fragment for connection settings
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class ConnectionPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.conn_settings);

        WifiManager wifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> wifiNetworks = wifiMgr.getConfiguredNetworks();

        ArrayList<CharSequence> ssids = new ArrayList<CharSequence>();
        for (WifiConfiguration wifiConfiguration : wifiNetworks) {
            ssids.add(wifiConfiguration.SSID);
        }
        CharSequence[] output = (CharSequence[]) ssids.toArray(new CharSequence[ssids.size()]);

        ListPreference ssidPref = (ListPreference) findPreference(PreferencesConstants.LOCAL_WIFI_SSID);
        ssidPref.setEntries(output);
        ssidPref.setEntryValues(output);
    }
}
