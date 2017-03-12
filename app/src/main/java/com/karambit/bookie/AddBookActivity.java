package com.karambit.bookie;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.text.InputType;
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
import android.widget.Toast;

import com.karambit.bookie.helper.FileNameGenerator;
import com.karambit.bookie.helper.GenrePickerDialog;
import com.karambit.bookie.helper.ImagePicker;
import com.karambit.bookie.helper.ImageScaler;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.helper.UploadFileTask;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddBookActivity extends AppCompatActivity {

    private static final String TAG = AddBookActivity.class.getSimpleName();

    public static final int TAB_INDEX = 2;
    public static final String TAB_SPEC = "tab_add_book";
    public static final String TAB_INDICATOR = "tab2";

    private static final String UPLOAD_IMAGE_URL = "BookCreate";

    public static final int RESULT_BOOK_CREATED = 1;
    private static final int REQUEST_CODE_CUSTOM_PERMISSIONS = 2;

    private int mScreenHeight;
    private int mScreenWidth;
    private String[] mGenreTypes;

    private EditText mNameEditText;
    private EditText mAuthorEditText;

    private int mSelectedGenre = -1;

    private Book.State mSelectedState;

    private File mSavedBookImageFile;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, MainActivity.FONT_APP_NAME_TITLE), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        float titleSize = getResources().getDimension(R.dimen.actionbar_app_name_title_size);
        s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(s);
        }

        mNameEditText = (EditText) findViewById(R.id.bookNameEditText);
        mNameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mAuthorEditText = (EditText) findViewById(R.id.authorEditText);
        mAuthorEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

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

                    // State prompt dialog
                    final Dialog statePromptDialog = new Dialog(AddBookActivity.this);
                    statePromptDialog.setContentView(R.layout.book_state_prompt_dialog);

//                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//                    Window window = statePromptDialog.getWindow();
//
//                    lp.copyFrom(window.getAttributes());
//                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//
//                    window.setAttributes(lp);

                    Button stateReadingButton = (Button) statePromptDialog.findViewById(R.id.stateReadingButton);
                    Button stateOpenButton = (Button) statePromptDialog.findViewById(R.id.stateOpenToShareButton);
                    Button stateClosedButton = (Button) statePromptDialog.findViewById(R.id.stateClosedToShareButton);

                    stateReadingButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            statePromptDialog.dismiss();
                            mSelectedState = Book.State.READING;
                            attemptUploadBook(mNameEditText.getText().toString(), mAuthorEditText.getText().toString(), mSelectedState.getStateCode(), mSelectedGenre);
                        }
                    });

                    stateOpenButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            statePromptDialog.dismiss();
                            mSelectedState = Book.State.OPENED_TO_SHARE;
                            attemptUploadBook(mNameEditText.getText().toString(), mAuthorEditText.getText().toString(), mSelectedState.getStateCode(), mSelectedGenre);
                        }
                    });

                    stateClosedButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            statePromptDialog.dismiss();
                            mSelectedState = Book.State.CLOSED_TO_SHARE;
                            attemptUploadBook(mNameEditText.getText().toString(), mAuthorEditText.getText().toString(), mSelectedState.getStateCode(), mSelectedGenre);
                        }
                    });

                    statePromptDialog.show();
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

                    ActivityCompat.requestPermissions(AddBookActivity.this, permissions, REQUEST_CODE_CUSTOM_PERMISSIONS);

                    Log.i(TAG, "Permissions NOT OK!");
                }
            }
        });

        findViewById(R.id.genreButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final GenrePickerDialog genrePickerDialog = new GenrePickerDialog(AddBookActivity.this, mGenreTypes, mSelectedGenre);

                genrePickerDialog.setOkClickListener(new GenrePickerDialog.OnOkClickListener() {
                    @Override
                    public void onOkClicked(int selectedGenre) {

                        mSelectedGenre = selectedGenre;
                        ((Button) findViewById(R.id.genreButton)).setText(mGenreTypes[selectedGenre]);
                        genrePickerDialog.dismiss();
                    }
                });

                genrePickerDialog.show();
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

    private void attemptUploadBook(String bookName, String author, int bookState, int genreCode) {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.creating_book));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();

        // Upload Book image
        String path = mSavedBookImageFile.getPath();
        String name = mSavedBookImageFile.getName();

        bookName = bookName.trim();
        author = author.trim();
        bookName = upperCaseString(bookName);
        author = upperCaseString(author);
        bookName = bookName.replace(" ","_");
        author = author.replace(" ","_");

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String serverArgsString = "?email=" + currentUserDetails.getEmail()
                + "&password=" + currentUserDetails.getPassword()
                + "&imageName=" + name
                + "&bookName=" + bookName
                + "&author=" + author
                + "&bookState=" + bookState
                + "&genreCode=" + genreCode;
        String imageUrlString = BookieClient.BASE_URL + UPLOAD_IMAGE_URL + serverArgsString;


        final UploadFileTask uftImage = new UploadFileTask(path, imageUrlString, name);

        uftImage.setUploadProgressListener(new UploadFileTask.UploadProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                Log.i(TAG, "Book image upload progress => " + progress + " / 100");
            }

            @Override
            public void onProgressCompleted() {
                Log.w(TAG, "Book image upload is OK");
                mProgressDialog.dismiss();

                setResult(RESULT_BOOK_CREATED);
                finish();
            }

            @Override
            public void onProgressError() {
                mProgressDialog.dismiss();
                Log.e(TAG, "Image upload ERROR");
                Toast.makeText(AddBookActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            }
        });

        uftImage.execute();
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

        if (!(mSavedBookImageFile != null && mSavedBookImageFile.exists())) {
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

    private String upperCaseString(String input){
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        if (words[0].length() > 0) {
            sb.append(Character.toUpperCase(words[0].charAt(0)) + words[0].subSequence(1, words[0].length()).toString().toLowerCase());
            for (int i = 1; i < words.length; i++) {
                sb.append(" ");
                sb.append(Character.toUpperCase(words[i].charAt(0)) + words[i].subSequence(1, words[i].length()).toString().toLowerCase());
            }
        }
        return sb.toString();
    }

    private boolean checkPermissions() {

        return !(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CUSTOM_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startActivityForResult(ImagePicker.getPickImageIntent(getBaseContext()), 1);

                } else {
                    Toast.makeText(this, R.string.permission_request_message, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}
