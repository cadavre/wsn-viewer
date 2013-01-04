
package pl.zeman.iqh.dialog;

import pl.zeman.iqh.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Dialog displaying popup with title, short content and OK button
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class OKDialogFragment extends DialogFragment {

    OnOKClickListener onOKClickListener = null;

    /**
     * Set on OK button pressed listener
     * 
     * @param listener Provided listener
     */
    public void setOnOKClickListener(OnOKClickListener listener) {

        onOKClickListener = listener;
    }

    /**
     * Get new OKDialog instance
     * 
     * @param title Resource ID for title
     * @param content Resource ID for content
     * @return OKDialogFragment
     */
    public static OKDialogFragment newInstance(int title, int content) {

        OKDialogFragment dialog = new OKDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("content", content);
        dialog.setArguments(args);
        dialog.setCancelable(false);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int title = getArguments().getInt("title");
        int content = getArguments().getInt("content");

        return new AlertDialog.Builder(getActivity()).setIcon(R.drawable.ic_error).setTitle(title)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        if (onOKClickListener != null) {
                            onOKClickListener.onOKClicked();
                        }
                    }
                }).setMessage(content).create();
    }

    /**
     * Interface for on OK press listener
     * 
     * @author Seweryn Zeman <seweryn.zeman@gmail.com>
     */
    public interface OnOKClickListener {

        public abstract void onOKClicked();
    }

}
