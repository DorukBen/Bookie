package com.karambit.bookie;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;

import com.karambit.bookie.helper.ImagePicker;
import com.karambit.bookie.helper.ImageScaler;
import com.karambit.bookie.helper.TypefaceSpan;

public class AddBookActivity extends AppCompatActivity {

    private int mScreenHeight;
    private int mScreenWidth;
    private String[] mGenreTypes;

    private int mSelectedGenre = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "autograf.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan(120), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(s);
        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Resources res = getResources();
        mGenreTypes = res.getStringArray(R.array.genre_types);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenHeight = size.y;
        mScreenWidth = size.x;

        View parallaxLayout = findViewById(R.id.parallaxLayout);
        parallaxLayout.getLayoutParams().height = mScreenHeight / 5 * 3;
        parallaxLayout.requestLayout();

        findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.doneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.genreButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog genrePicker = new Dialog(AddBookActivity.this);
                genrePicker.setContentView(R.layout.genre_picker_dialog);
                genrePicker.setTitle("");

                final NumberPicker numberPicker = (NumberPicker) genrePicker.findViewById(R.id.numberPicker);
                numberPicker.setMinValue(0);
                numberPicker.setMaxValue(mGenreTypes.length-1);
                numberPicker.setDisplayedValues(mGenreTypes);
                if(mSelectedGenre > 0){
                    numberPicker.setValue(mSelectedGenre);
                }

                Button selectGenre  = (Button) genrePicker.findViewById(R.id.selectGenreButton);

                selectGenre.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSelectedGenre = numberPicker.getValue();
                        ((Button)findViewById(R.id.genreButton)).setText(mGenreTypes[numberPicker.getValue()]);
                        genrePicker.dismiss();
                    }
                });

                genrePicker.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = ImagePicker.getImageFromResult(getBaseContext(), resultCode, data);
                findViewById(R.id.addImageButton).setVisibility(View.GONE);
                findViewById(R.id.cameraIconImageView).setVisibility(View.GONE);
                findViewById(R.id.infoTextLayout).setVisibility(View.GONE);
                ((ImageView) findViewById(R.id.bookImageImageView)).setImageBitmap(ImageScaler.cropImage(bitmap, 72 / 96f));

                findViewById(R.id.parallaxLayout).getLayoutParams().height = mScreenWidth * 96 / 72;
                findViewById(R.id.parallaxLayout).requestLayout();
            }
        }
    }

    public void onClickAddBookImage(View view) {
        startActivityForResult(ImagePicker.getPickImageIntent(getBaseContext()), 1);
    }
}
