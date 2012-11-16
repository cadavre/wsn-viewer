
package pl.cadavre.wsnv.activity;

import pl.cadavre.wsnv.PreferencesConstants;
import pl.cadavre.wsnv.R;
import android.os.Bundle;
import android.util.Log;

/**
 * Dashboard Activity class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class DashboardActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

    }

    @Override
    protected void onStart() {

        super.onStart();
        String dbConnURL = "http://"
                + preferences.getString(PreferencesConstants.DB_HOST, "localhost") + ":"
                + preferences.getString(PreferencesConstants.DB_PORT, "5432") + "/"
                + preferences.getString(PreferencesConstants.DB_DATABASE, "pgsql");
        Log.d(TAG, "Will try to connect with " + dbConnURL);
        Log.d(TAG, "Username " + preferences.getString(PreferencesConstants.DB_USER, "root"));
        Log.d(TAG, "Password " + preferences.getString(PreferencesConstants.DB_PASSWORD, "root"));
    }

}
