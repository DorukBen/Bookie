package com.karambit.bookie.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import com.karambit.bookie.R;

/**
 * Created by orcan on 2/26/17.
 */
public class GenrePickerDialog extends AlertDialog {

    public static final String TAG = GenrePickerDialog.class.getSimpleName();

    private OnOkClickListener mOkClickListener;

    public GenrePickerDialog(@NonNull Context context, String[] genreTypes, int selectedGenre) {
        super(context);

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.genre_picker_dialog, null);

        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(genreTypes.length - 1);
        numberPicker.setDisplayedValues(genreTypes);
        if (selectedGenre > 0) {
            numberPicker.setValue(selectedGenre);
        }

        Button selectGenre = (Button) dialogView.findViewById(R.id.selectGenreButton);

        selectGenre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOkClickListener != null) {
                    mOkClickListener.onOkClicked(numberPicker.getValue());
                }
            }
        });

        setView(dialogView);
    }

    public interface OnOkClickListener {
        void onOkClicked(int selectedGenre);
    }

    public OnOkClickListener getOkClickListener() {
        return mOkClickListener;
    }

    public void setOkClickListener(OnOkClickListener okClickListener) {
        mOkClickListener = okClickListener;
    }
}
