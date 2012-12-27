
package pl.cadavre.wsnv.activity;

import pl.cadavre.wsnv.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Dashboard Activity class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class DashboardActivity extends BaseActivity {

    private final static int ACTION_BUTTON_CONN_PREF = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        if (!getApp().hasNecessaryPreferences()) {
            Intent connSettingIntent = new Intent(this, ConnectionPreferenceActivity.class);
            startActivity(connSettingIntent);
            finish();
        } else if (getApp().hasInternetConnection()) {
            getApp().setConnectionPreferences();
        }

        Button test = (Button) findViewById(R.id.test);
        test.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {

                startActivity(new Intent(DashboardActivity.this, HouseActivity.class));
            }
        });
        
        Button test2 = (Button) findViewById(R.id.test2);
        test2.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {

                startActivity(new Intent(DashboardActivity.this, NodeStatusActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, ACTION_BUTTON_CONN_PREF, 0, R.string.settings)
                .setIcon(R.drawable.ic_monitor)
                .setShowAsAction(
                        MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case ACTION_BUTTON_CONN_PREF:
                Intent go = new Intent(this, ConnectionPreferenceActivity.class);
                startActivity(go);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
