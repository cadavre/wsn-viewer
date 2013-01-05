
package pl.zeman.iqh.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pl.zeman.iqh.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Dashboard Activity class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class DashboardActivity extends BaseActivity implements OnItemClickListener {

    private final static int ACTION_BUTTON_CONN_PREF = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (!getApp().hasNecessaryPreferences()) {
            Intent connSettingIntent = new Intent(this, ConnectionPreferenceActivity.class);
            startActivity(connSettingIntent);
        } else if (getApp().hasInternetConnection()) {
            getApp().setConnectionPreferences();
        }

        ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
        Map<String, String> item = new HashMap<String, String>();
        item.put("title", "Plan budynku");
        item.put("summary", "Poka¿ plan budynku");
        data.add(item);
        item = new HashMap<String, String>();
        item.put("title", "Status budynku");
        item.put("summary", "Poka¿ aktualne odczyty z czujników");
        data.add(item);
        item = new HashMap<String, String>();
        item.put("title", "Status sieci WSN");
        item.put("summary", "Poka¿ status sieci czujników");
        data.add(item);
        item = new HashMap<String, String>();
        item.put("title", "Konfiguracja motów");
        item.put("summary", "Dokonaj rekonfiguracji motów");
        data.add(item);

        SimpleAdapter adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2,
                new String[] { "title", "summary" }, new int[] { android.R.id.text1,
                        android.R.id.text2 });

        ListView lvMainMenu = (ListView) findViewById(R.id.lvMainMenu);

        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(android.R.layout.simple_list_item_1, null);
        TextView tvHeader = (TextView) header.findViewById(android.R.id.text1);
        tvHeader.setText("Menu g³ówne");
        lvMainMenu.addHeaderView(tvHeader);

        lvMainMenu.setAdapter(adapter);
        lvMainMenu.setOnItemClickListener(this);

        ImageView ivIPKM = (ImageView) findViewById(R.id.ivIPKM);
        ivIPKM.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://ipkm.polsl.pl/")));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, ACTION_BUTTON_CONN_PREF, 0, R.string.wizard)
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

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        @SuppressWarnings("rawtypes")
        Class destination = null;
        switch (position) {
            case 1:
                destination = HouseActivity.class;
                break;
            case 2:
                destination = NodeStatusActivity.class;
                break;
            case 3:
                destination = SystemStatusActivity.class;
                break;
            case 4:
                destination = NodeAutoconfigActivity.class;
                break;
            default:
                return;
        }
        startActivity(new Intent(DashboardActivity.this, destination));
    }

}
