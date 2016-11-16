package com.karambit.bookie;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.karambit.bookie.helper.FileNameGenerator;
import com.karambit.bookie.helper.ImagePicker;
import com.karambit.bookie.helper.ImageScaler;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.helper.UploadFileTask;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookApi;
import com.karambit.bookie.rest_api.BookieClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddBookActivity extends AppCompatActivity {

    private static final String TAG = AddBookActivity.class.getSimpleName();

    private static final String UPLOAD_IMAGE_URL = "upload_book_image.php";
    private static final String UPLOAD_THUMBNAIL_URL = "upload_book_thumbnail.php";

    public static final String SERVER_BOOK_IMAGES_FOLDER = "http://46.101.171.117/bookie/book_images/";
    public static final String SERVER_BOOK_THUMBNAILS_FOLDER = "http://46.101.171.117/bookie/book_thumbnails/";

    private static final int CUSTOM_PERMISSIONS_REQUEST_CODE = 123;

    private int mScreenHeight;
    private int mScreenWidth;
    private String[] mGenreTypes;

    private EditText mNameEditText;
    private EditText mAuthorEditText;

    private int mSelectedGenre = -1;

    private File mSavedBookImageFile;

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

                    Log.i(TAG, "Permissions OK!");
                } else {
                    String[] permissions = {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                    };

                    ActivityCompat.requestPermissions(AddBookActivity.this, permissions, CUSTOM_PERMISSIONS_REQUEST_CODE);

                    Log.i(TAG, "Permissions NOT OK!");
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
                Bitmap croppedBitmap = ImageScaler.cropImage(bitmap, 72 / 96f);

                mSavedBookImageFile = saveBitmap(croppedBitmap);

                findViewById(R.id.addImageButton).setVisibility(View.GONE);
                findViewById(R.id.cameraIconImageView).setVisibility(View.GONE);
                findViewById(R.id.infoTextLayout).setVisibility(View.GONE);
                ((ImageView) findViewById(R.id.bookImageImageView)).setImageBitmap(croppedBitmap);

                findViewById(R.id.parallaxLayout).getLayoutParams().height = mScreenWidth * 96 / 72;
                findViewById(R.id.parallaxLayout).requestLayout();
            }
        }
    }

    private void attemptUploadBook() {

        // Upload Book image
        String path = mSavedBookImageFile.getPath();
        String imageUrlString = BookieClient.BASE_URL + UPLOAD_IMAGE_URL;
        String name = mSavedBookImageFile.getName();

        final UploadFileTask uftImage = new UploadFileTask(path, imageUrlString, name);

        uftImage.setUploadProgressListener(new UploadFileTask.UploadProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                Log.i(TAG, "Book image upload progress => " + progress + " / 100");
            }

            @Override
            public void onProgressCompleted() {

                Log.w(TAG, "Book image upload is OK");

                // Upload Thumbnail of image
                Bitmap bookImageBitmap = BitmapFactory.decodeFile(mSavedBookImageFile.getAbsolutePath(), new BitmapFactory.Options());
                Bitmap thumbnailBitmap = Bitmap.createScaledBitmap(bookImageBitmap, 288, 384, true);
                final File thumbnailFile = saveThumbnail(thumbnailBitmap, mSavedBookImageFile.getName());

                String thumbnailUrlString = BookieClient.BASE_URL + UPLOAD_THUMBNAIL_URL;

                UploadFileTask uftThumbnail = new UploadFileTask(thumbnailFile.getPath(), thumbnailUrlString, thumbnailFile.getName());

                uftThumbnail.setUploadProgressListener(new UploadFileTask.UploadProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        Log.i(TAG, "Thumbnail Upload Progress = " + progress + " / 100");
                    }

                    @Override
                    public void onProgressCompleted() {
                        Log.w(TAG, "Thumbnail upload is OK");

                        attemptInsertBook(mSavedBookImageFile.getName(), thumbnailFile.getName());
                    }
                });

                uftThumbnail.execute();
            }
        });

        uftImage.execute();

    }

    private void attemptInsertBook(String bookImageName, String thumbnailName) {
        User.Details userDetails = SessionManager.getCurrentUserDetails(this);

        BookApi bookApi = BookieClient.getClient().create(BookApi.class);

        Call<ResponseBody> call = bookApi.insertBook(userDetails.getEmail(), userDetails.getPassword(), mNameEditText.getText().toString(),
                /*TODO*/ 1, mAuthorEditText.getText().toString(), mSelectedGenre, userDetails.getUser().getID(),
                SERVER_BOOK_IMAGES_FOLDER + bookImageName,
                SERVER_BOOK_THUMBNAILS_FOLDER + thumbnailName);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(AddBookActivity.this, "Allahina gurban", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
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

        if (! (mSavedBookImageFile != null && mSavedBookImageFile.exists())) {
            ok = false;
            Toast.makeText(this, R.string.pick_image_for_book, Toast.LENGTH_SHORT).show();
        }

        return ok;
    }

    private File saveBitmap(Bitmap bitmap) {

        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + getString(R.string.app_name) + File.separator;

        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }

        String imageName = FileNameGenerator.generateBookImageName(SessionManager.getCurrentUserDetails(this).getEmail());

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

    private File saveThumbnail(Bitmap thumbnail, String bookImageName) {
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator +
                getString(R.string.app_name) + File.separator + ".thumbnails" + File.separator;

        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }

        String thumbnailName = FileNameGenerator.generateBookThumbnailName(bookImageName);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path + thumbnailName);
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
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

        return new File(path + thumbnailName);
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
