package com.karambit.bookie.helper;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.karambit.bookie.R;

/**
 * Created by orcan on 2/16/17.
 */

public class ComfortableProgressDialog extends Dialog {

    private TextView mMessage;
    private ProgressBar mProgressBar;

    public ComfortableProgressDialog(Context context) {
        super(context);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.comfortable_progress_dialog, null);

        mMessage = (TextView) dialogView.findViewById(R.id.dialogText);
        mProgressBar = (ProgressBar) dialogView.findViewById(R.id.dialogProgressbar);

        setContentView(dialogView);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);
    }

    public void setMessage(CharSequence message) {
        mMessage.setText(message);
    }

    public void setMessage(@StringRes int stringRes) {
        mMessage.setText(stringRes);
    }

    public TextView getMessageTextView() {
        return mMessage;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }
}