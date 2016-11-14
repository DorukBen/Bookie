package com.karambit.bookie;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.FileNameGenerator;
import com.karambit.bookie.helper.ImagePicker;
import com.karambit.bookie.helper.ImageScaler;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.helper.UploadFileTask;
import com.karambit.bookie.rest_api.BookieClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddBookActivity extends AppCompatActivity {

    private static final String TAG = AddBookActivity.class.getSimpleName();

    private static final String UPLOAD_IMAGE_URL = "upload_book_image.php";

    private static final int CUSTOM_PERMISSIONS_REQUEST_CODE = 123;

    private int mScreenHeight;
    private int mScreenWidth;
    private String[] mGenreTypes;

    private EditText mNameEditText;
    private EditText mAuthorEditText;

    private int mSelectedGenre = -1;

    private DBHandler mDBHandler;

    private File mSavedBitmapFile;

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(s);
        }

        mDBHandler = new DBHandler(this);

        mNameEditText = (EditText) findViewById(R.id.bookNameEditText);
        mAuthorEditText = (EditText) findViewById(R.id.authorEditText);

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
                if (areAllInputsValid()) {
                    attemptUploadBook();
                }
            }
        });

        findViewById(R.id.bookImageImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    startActivityForResult(ImagePicker.getPickImageIntent(getBaseContext()), 1);

                    Log.d(TAG, "Permissions OK!");
                } else {
                    String[] permissions = {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                    };

                    ActivityCompat.requestPermissions(AddBookActivity.this, permissions, CUSTOM_PERMISSIONS_REQUEST_CODE);

                    Log.d(TAG, "Permissions NOT OK!");
                }
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
                numberPicker.setMaxValue(mGenreTypes.length - 1);
                numberPicker.setDisplayedValues(mGenreTypes);
                if (mSelectedGenre > 0) {
                    numberPicker.setValue(mSelectedGenre);
                }

                Button selectGenre = (Button) genrePicker.findViewById(R.id.selectGenreButton);

                selectGenre.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSelectedGenre = numberPicker.getValue();
                        ((Button) findViewById(R.id.genreButton)).setText(mGenreTypes[numberPicker.getValue()]);
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

                mSavedBitmapFile = saveBitmap(bitmap);

                findViewById(R.id.addImageButton).setVisibility(View.GONE);
                findViewById(R.id.cameraIconImageView).setVisibility(View.GONE);
                findViewById(R.id.infoTextLayout).setVisibility(View.GONE);
                ((ImageView) findViewById(R.id.bookImageImageView)).setImageBitmap(ImageScaler.cropImage(bitmap, 72 / 96f));

                findViewById(R.id.parallaxLayout).getLayoutParams().height = mScreenWidth * 96 / 72;
                findViewById(R.id.parallaxLayout).requestLayout();
            }
        }
    }

    private void attemptUploadBook() {
        String path = mSavedBitmapFile.getPath();
        String urlString = BookieClient.BASE_URL + UPLOAD_IMAGE_URL;
        String name = mSavedBitmapFile.getName();

        UploadFileTask uft = new UploadFileTask(path, urlString, name);

        uft.setUploadProgressListener(new UploadFileTask.UploadProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                Log.i(TAG, "progress = " + progress);
            }

            @Override
            public void onProgressCompleted() {
                Toast.makeText(AddBookActivity.this, "OK", Toast.LENGTH_SHORT).show();
            }
        });

        uft.execute();
    }

    private boolean areAllInputsValid() {
        boolean ok = true;

        if (TextUtils.isEmpty(mNameEditText.getText())) {
            ok = false;
            mNameEditText.setError(getString(R.string.empty_field_message));
        }

        if (TextUtils.isEmpty(mAuthorEditText.getText())) {
            ok = false;
            mAuthorEditText.setError(getString(R.string.empty_field_message));
        }

        if (mSelectedGenre == -1) {
            ok = false;
            Toast.makeText(this, getText(R.string.select_genre), Toast.LENGTH_SHORT).show();
        }

        if (mSavedBitmapFile == null) {
            ok = false;
            Toast.makeText(this, R.string.pick_image_for_book, Toast.LENGTH_SHORT).show();
        }

        return ok;
    }

    private File saveBitmap(Bitmap bitmap) {

        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + getString(R.string.app_name) + File.separator;

        Log.w(TAG, "saveBitmap: path = " + path);

        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }

        String imageName = FileNameGenerator.generateBookImageName(mDBHandler.getCurrentUserDetails().getEmail());

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path + imageName);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new File(path + imageName);
    }

    private boolean checkPermissions() {

        return !(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CUSTOM_PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startActivityForResult(ImagePicker.getPickImageIntent(getBaseContext()), 1);

                } else {
                    finish();
                }
            }
        }
    }
}
