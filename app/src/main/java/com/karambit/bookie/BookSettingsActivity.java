package com.karambit.bookie;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karambit.bookie.helper.CircleImageView;
import com.karambit.bookie.helper.ComfortableProgressDialog;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.GenrePickerDialog;
import com.karambit.bookie.helper.NetworkChecker;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;

public class BookSettingsActivity extends AppCompatActivity {

    private static final String TAG = BookSettingsActivity.class.getSimpleName();

    public static final int RESULT_LOST = 123;
    public static final int RESULT_BOOK_UPDATED = 111;

    private static final int REPORT_BOOK_WRONG_NAME = 1;
    private static final int REPORT_BOOK_WRONG_AUTHOR = 2;
    private static final int REPORT_BOOK_WRONG_GENRE = 3;
    private static final int REPORT_BOOK_WRONG_PHOTO = 4;
    private static final int REPORT_BOOK_TOO_DAMAGED = 5;
    private static final int REPORT_BOOK_OTHER = 6;

    private Book mBook;
    private boolean mIsAdder;
    private EditText mReportEditText;
    private Button mSendReportButton;
    private RadioGroup mReportRadioGroup;
    private EditText mBookNameEditText;
    private EditText mBookAuthorEditText;
    private int mSelectedGenre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_settings);

        mBook = getIntent().getParcelableExtra("book");
        mIsAdder = getIntent().getBooleanExtra("is_adder", false);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
            SpannableString s = new SpannableString(mBook.getName());
            s.setSpan(new TypefaceSpan(this, "montserrat_regular.ttf"), 0, s.length(),
                      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setTitle(s);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_primary_text_color);
        }

        final ScrollView scrollView = (ScrollView) findViewById(R.id.settingsScrollView);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                if (actionBar != null) {
                    actionBar.setElevation(ElevationScrollListener.getActionbarElevation(scrollY));
                }
            }
        });

        if (NetworkChecker.isNetworkAvailable(this)) {

            User currentUser = SessionManager.getCurrentUser(this);

            if (mIsAdder && mBook.getOwner().equals(currentUser)) {
                findViewById(R.id.bookEditContainer).setVisibility(View.VISIBLE);

                mBookNameEditText = (EditText) findViewById(R.id.bookNameEditText);
                mBookAuthorEditText = (EditText) findViewById(R.id.bookAuthorEditText);
                CircleImageView bookPicture = (CircleImageView) findViewById(R.id.bookPictureImageView);

                Glide.with(this)
                     .load(mBook.getThumbnailURL())
                     .asBitmap()
                     .placeholder(R.drawable.placeholder_88dp)
                     .error(R.drawable.error_88dp)
                     .centerCrop()
                     .into(bookPicture);

                // TODO Change Book picture

                mBookNameEditText.setText(mBook.getName());
                mBookAuthorEditText.setText(mBook.getAuthor());

                final Button genreButton = (Button) findViewById(R.id.bookGenreButton);

                final String[] genres = getResources().getStringArray(R.array.genre_types);
                mSelectedGenre = mBook.getGenreCode();
                genreButton.setText(genres[mSelectedGenre]);

                genreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final GenrePickerDialog genrePickerDialog = new GenrePickerDialog(BookSettingsActivity.this, genres, mSelectedGenre);

                        genrePickerDialog.setOkClickListener(new GenrePickerDialog.OnOkClickListener() {
                            @Override
                            public void onOkClicked(int selectedGenre) {
                                genreButton.setText(genres[selectedGenre]);
                                mSelectedGenre = selectedGenre;
                                genrePickerDialog.dismiss();
                            }
                        });

                        genrePickerDialog.show();
                    }
                });

            } else {
                findViewById(R.id.bookEditContainer).setVisibility(View.GONE);
            }

            mReportEditText = (EditText) findViewById(R.id.reportBookEditText);

            mReportRadioGroup = (RadioGroup) findViewById(R.id.reportBookRadioGroup);

            mSendReportButton = (Button) findViewById(R.id.reportSendButton);

            mReportEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean focused) {
                    if (focused) {
                        if (mReportRadioGroup.getCheckedRadioButtonId() == -1) {
                            mReportRadioGroup.check(R.id.reportUserOther);
                        }
                    } else {
                        if (mReportEditText.length() == 0) {
                            mReportRadioGroup.clearCheck();
                            mSendReportButton.setClickable(false);
                            mSendReportButton.setTextColor(ContextCompat.getColor(BookSettingsActivity.this, R.color.secondaryTextColor));
                        }
                    }
                }
            });

            mReportEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() > 0 || mReportRadioGroup.getCheckedRadioButtonId() != R.id.reportUserOther) {
                        mSendReportButton.setClickable(true);
                        mSendReportButton.setTextColor(ContextCompat.getColor(BookSettingsActivity.this, R.color.colorAccent));
                    } else {
                        mSendReportButton.setClickable(false);
                        mSendReportButton.setTextColor(ContextCompat.getColor(BookSettingsActivity.this, R.color.secondaryTextColor));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });

            mReportRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int id) {
                    switch (id) {
                        case R.id.reportBookWrongName: {

                            mSendReportButton.setClickable(true);
                            mSendReportButton.setTextColor(ContextCompat.getColor(BookSettingsActivity.this, R.color.colorAccent));

                            mReportEditText.setHint(getString(R.string.report_hint_book_additional_info));

                            break;
                        }

                        case R.id.reportBookWrongAuthor: {

                            mSendReportButton.setClickable(true);
                            mSendReportButton.setTextColor(ContextCompat.getColor(BookSettingsActivity.this, R.color.colorAccent));

                            mReportEditText.setHint(getString(R.string.report_hint_book_additional_info));

                            break;
                        }

                        case R.id.reportBookWrongGenre: {

                            mSendReportButton.setClickable(true);
                            mSendReportButton.setTextColor(ContextCompat.getColor(BookSettingsActivity.this, R.color.colorAccent));

                            mReportEditText.setHint(getString(R.string.report_hint_book_additional_info));

                            break;
                        }

                        case R.id.reportBookWrongPhoto: {

                            mSendReportButton.setClickable(true);
                            mSendReportButton.setTextColor(ContextCompat.getColor(BookSettingsActivity.this, R.color.colorAccent));

                            mReportEditText.setHint(getString(R.string.report_hint_book_additional_info));

                            break;
                        }

                        case R.id.reportBookTooDamaged: {

                            mSendReportButton.setClickable(true);
                            mSendReportButton.setTextColor(ContextCompat.getColor(BookSettingsActivity.this, R.color.colorAccent));

                            mReportEditText.setHint(getString(R.string.report_hint_book_additional_info));

                            break;
                        }

                        case R.id.reportBookOther: {

                            if (mReportEditText.length() > 0) {
                                mSendReportButton.setClickable(true);
                                mSendReportButton.setTextColor(ContextCompat.getColor(BookSettingsActivity.this, R.color.colorAccent));
                            } else {
                                mSendReportButton.setClickable(false);
                                mSendReportButton.setTextColor(ContextCompat.getColor(BookSettingsActivity.this, R.color.secondaryTextColor));
                            }

                            mReportEditText.setHint(getString(R.string.report_hint_book_other, mBook.getName()));

                            mReportEditText.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(mReportEditText, InputMethodManager.SHOW_IMPLICIT);

                            break;
                        }
                    }
                }
            });

            RadioButton tooDamaged = (RadioButton) findViewById(R.id.reportBookTooDamaged);

            if (mBook.getOwner().equals(currentUser)) {
                tooDamaged.setVisibility(View.VISIBLE);
            } else {
                tooDamaged.setVisibility(View.GONE);
            }

            mSendReportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(BookSettingsActivity.this)
                        .setMessage(getString(R.string.send_report_prompt, mBook.getName()))
                        .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                mReportEditText.setText("");

                                sendReport();

                                mReportRadioGroup.clearCheck();
                                mSendReportButton.setClickable(false);
                                mSendReportButton.setTextColor(ContextCompat.getColor(BookSettingsActivity.this, R.color.secondaryTextColor));
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show();
                }
            });

            View lostContainer = findViewById(R.id.lostContainer);

            if (mBook.getOwner().equals(currentUser)) {
                lostContainer.setVisibility(View.VISIBLE);
                Button lostButton = (Button) findViewById(R.id.lostButton);
                lostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        new AlertDialog.Builder(BookSettingsActivity.this)
                            .setMessage(getString(R.string.lost_prompt, mBook.getName()))
                            .setPositiveButton(R.string.lost, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    lostProcesses();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create()
                            .show();
                    }
                });
            } else {
                lostContainer.setVisibility(View.GONE);
            }

        } else {
            scrollView.setVisibility(View.GONE);

            View noConnectionView = findViewById(R.id.noConnectionView);
            noConnectionView.setVisibility(View.VISIBLE);
            ((TextView) noConnectionView.findViewById(R.id.emptyStateTextView)).setText(R.string.no_internet_connection);
        }
    }

    private void lostProcesses() {

        // TODO Server

        setResult(RESULT_LOST);
        finish();
    }

    private void sendReport() {
        ComfortableProgressDialog progressDialog = new ComfortableProgressDialog(this);
        progressDialog.setMessage(R.string.please_wait);
        progressDialog.setCancelable(false);
        // TODO progressDialog.show();

        String reportInfo = mReportEditText.getText().toString();

        int checkedRadioButtonId = mReportRadioGroup.getCheckedRadioButtonId();
        int reportCode = getCheckedReportCode(checkedRadioButtonId);

        if (!TextUtils.isEmpty(reportInfo)) {

            // TODO reportInfo can be empty

        }


        // TODO Server

    }

    private int getCheckedReportCode(int id) {
        switch (id) {
            case R.id.reportBookWrongName:
                return REPORT_BOOK_WRONG_NAME;
            case R.id.reportBookWrongAuthor:
                return REPORT_BOOK_WRONG_AUTHOR;
            case R.id.reportBookWrongGenre:
                return REPORT_BOOK_WRONG_GENRE;
            case R.id.reportBookWrongPhoto:
                return REPORT_BOOK_WRONG_PHOTO;
            case R.id.reportBookTooDamaged:
                return REPORT_BOOK_TOO_DAMAGED;
            case R.id.reportUserOther:
                return REPORT_BOOK_OTHER;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (NetworkChecker.isNetworkAvailable(this) && mIsAdder) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.done_menu, menu);
            return super.onCreateOptionsMenu(menu);
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_done:
                boolean ok = true;

                String newBookName = mBookNameEditText.getText().toString().trim();
                String newAuthor = mBookAuthorEditText.getText().toString().trim();

                if (TextUtils.isEmpty(newBookName)) {
                    mBookNameEditText.setError(getString(R.string.empty_field_message));
                    ok = false;
                }
                if (TextUtils.isEmpty(newAuthor)) {
                    mBookAuthorEditText.setError(getString(R.string.empty_field_message));
                    ok = false;
                }

                if (ok) {
                    saveChanges();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveChanges() {
        String bookName = mBookNameEditText.getText().toString();
        String author = mBookAuthorEditText.getText().toString();

        bookName = upperCaseString(bookName);
        author = upperCaseString(author);
        bookName = bookName.replace(" ","_");
        author = author.replace(" ","_");

        ComfortableProgressDialog progressDialog = new ComfortableProgressDialog(this);
        progressDialog.setMessage(R.string.please_wait);
        progressDialog.setCancelable(false);
        // TODO progressDialog.show();

        // TODO Server

        setResult(RESULT_BOOK_UPDATED);

        finish();
    }

    private String upperCaseString(String input){
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        if (words[0].length() > 0) {
            sb.append(Character.toUpperCase(words[0].charAt(0)));
            sb.append(words[0].subSequence(1, words[0].length()).toString().toLowerCase());
            for (int i = 1; i < words.length; i++) {
                sb.append(" ");
                sb.append(Character.toUpperCase(words[i].charAt(0)));
                sb.append(words[i].subSequence(1, words[i].length()).toString().toLowerCase());
            }
        }
        return sb.toString();
    }
}
