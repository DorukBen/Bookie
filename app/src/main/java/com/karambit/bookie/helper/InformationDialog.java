package com.karambit.bookie.helper;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.karambit.bookie.R;

/**
 * This Dialog class used to show important info to user. It contains More Info button and Ok button as default.
 * The extra button can be set with new listener.
 *
 * Make sure you think the cancelable option.
 *
 * Created by orcan on 3/22/17.
 */

public class InformationDialog extends Dialog {

    private ImageView mInfoImage;
    private TextView mPrimaryMessage;
    private TextView mSecondaryMessage;
    private Button mOkButton;
    private Button mMoreInfoButton;
    private Button mExtraButton;

    private DefaultClickListener mDefaultClickListener;
    private ExtraButtonClickListener mExtraButtonClickListener;

    public InformationDialog(@NonNull Context context) {
        super(context);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_information, null);

        mInfoImage = (ImageView) dialogView.findViewById(R.id.dialogInformationInfoImage);
        mPrimaryMessage = (TextView) dialogView.findViewById(R.id.dialogInformationMessagePrimary);
        mSecondaryMessage = (TextView) dialogView.findViewById(R.id.dialogInformationMessageSecondary);
        mOkButton = (Button) dialogView.findViewById(R.id.dialogInformationOk);
        mMoreInfoButton = (Button) dialogView.findViewById(R.id.dialogInformationMoreInfo);
        mExtraButton = (Button) dialogView.findViewById(R.id.dialogInformationExtraButton);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDefaultClickListener != null) {
                    mDefaultClickListener.onOkClick();
                }
            }
        });

        mMoreInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDefaultClickListener != null) {
                    mDefaultClickListener.onMoreInfoClick();
                }
            }
        });

        mExtraButton.setVisibility(View.GONE);
        mSecondaryMessage.setVisibility(View.GONE);

        setContentView(dialogView);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);
    }

    public void setPrimaryMessage(CharSequence primaryMessage) {
        mPrimaryMessage.setText(primaryMessage);
    }

    public void setPrimaryMessage(@StringRes int stringRes) {
        mPrimaryMessage.setText(stringRes);
    }

    public void setSecondaryMessage(CharSequence primaryMessage) {
        mSecondaryMessage.setText(primaryMessage);
        mSecondaryMessage.setVisibility(View.VISIBLE);
    }

    public void setSecondaryMessage(@StringRes int stringRes) {
        mSecondaryMessage.setText(stringRes);
        mSecondaryMessage.setVisibility(View.VISIBLE);
    }

    public void setExtraButtonText(String text) {
        mExtraButton.setText(text);
    }

    public void setExtraButtonText(@StringRes int stringRes) {
        mExtraButton.setText(stringRes);
    }

    public ImageView getInfoImage() {
        return mInfoImage;
    }

    public interface DefaultClickListener {
        void onOkClick();
        void onMoreInfoClick();
    }

    public DefaultClickListener getDefaultClickListener() {
        return mDefaultClickListener;
    }

    public void setDefaultClickListener(DefaultClickListener defaultClickListener) {
        mDefaultClickListener = defaultClickListener;
    }

    public interface ExtraButtonClickListener {
        void onExtraButtonClick();
    }

    public ExtraButtonClickListener getExtraButtonClickListener() {
        return mExtraButtonClickListener;
    }

    public void setExtraButtonClickListener(String text, ExtraButtonClickListener extraButtonClickListener) {
        mExtraButtonClickListener = extraButtonClickListener;
        mExtraButton.setVisibility(View.VISIBLE);
        mExtraButton.setText(text);
        mExtraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mExtraButtonClickListener != null) {
                    mExtraButtonClickListener.onExtraButtonClick();
                }
            }
        });
    }

    public void setExtraButtonClickListener(@StringRes int stringRes, ExtraButtonClickListener extraButtonClickListener) {
        mExtraButtonClickListener = extraButtonClickListener;
        mExtraButton.setVisibility(View.VISIBLE);
        mExtraButton.setText(stringRes);
        mExtraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mExtraButtonClickListener != null) {
                    mExtraButtonClickListener.onExtraButtonClick();
                }
            }
        });
    }

    public void setTopSectionColor(int color) {
        findViewById(R.id.dialogInformationTopSection).setBackgroundColor(color);
    }

    public void setButtonColor(int color) {
        mOkButton.setTextColor(color);
        mMoreInfoButton.setTextColor(color);
        mExtraButton.setTextColor(color);
    }
}
