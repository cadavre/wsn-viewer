
package pl.zeman.iqh.activity;

import pl.zeman.iqh.R;
import pl.zeman.iqh.WSNViewer;
import pl.zeman.iqh.dialog.OKDialogFragment;
import pl.zeman.iqh.dialog.OKDialogFragment.OnOKClickListener;
import pl.zeman.iqh.network.JDBCConnection;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Main Activity with Network handling
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class BaseActivity extends Activity {

    public static final String TAG = "WSNV";

    private WSNViewer application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        application = (WSNViewer) getApplicationContext();

        try {
            JDBCConnection.getJDBC();
        } catch (ClassNotFoundException e) {
            showOKDialog(R.string.error, R.string.error_loading_jdbc, new OnOKClickListener() {

                public void onOKClicked() {

                    finish();
                }
            });
        }

    }

    @Override
    protected void onStart() {

        super.onStart();
        if (!getApp().hasInternetConnection()) {
            OnOKClickListener onOK = new OnOKClickListener() {

                public void onOKClicked() {

                    startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    finish();
                }
            };
            showOKDialog(R.string.error, R.string.error_no_internet_connection, onOK);
        }
    }

    /**
     * Get application Context
     * 
     * @return WSNViewer
     */
    public WSNViewer getApp() {

        return application;
    }

    /**
     * Display OK dialog with resulting Activity.finish()
     * 
     * @param title
     * @param content
     */
    public void showOKDialog(int title, int content) {

        showOKDialog(title, content, null);
    }

    /**
     * Display OK dialog with listener callback
     * 
     * @param title
     * @param content
     * @param listener
     */
    public void showOKDialog(int title, int content, OnOKClickListener listener) {

        OKDialogFragment newFragment = OKDialogFragment.newInstance(title, content);
        newFragment.setOnOKClickListener(listener);
        newFragment.show(getFragmentManager(), "okdialog");
    }

}
