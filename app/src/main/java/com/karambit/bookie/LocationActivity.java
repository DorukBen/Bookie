package com.karambit.bookie;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;

public class LocationActivity extends AppCompatActivity {

    public static final String TAG = LocationActivity.class.getSimpleName();

    public static final int RESULT_LOCATION_UPDATED = 1;

    private double mLatitude;
    private double mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            SpannableString s = new SpannableString(getString(R.string.select_location));
            s.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            float elevation = getResources().getDimension(R.dimen.actionbar_title_size);
            actionBar.setElevation(elevation);
        }

        final Button okButton = (Button) findViewById(R.id.locationOKButton);
        okButton.setEnabled(false);
        okButton.setTextColor(ContextCompat.getColor(LocationActivity.this, R.color.secondaryTextColor));
        okButton.setAlpha(0.5f);

        final SupportPlaceAutocompleteFragment autocompleteFragment = (SupportPlaceAutocompleteFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
                .build();

        autocompleteFragment.setFilter(typeFilter);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mLatitude = place.getLatLng().latitude;
                mLongitude = place.getLatLng().longitude;
                okButton.setEnabled(true);
                okButton.setTextColor(ContextCompat.getColor(LocationActivity.this, R.color.colorAccent));
                okButton.setAlpha(1f);
            }

            @Override
            public void onError(Status status) {
            }
        });

        if (autocompleteFragment.getView() != null){
            autocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText("");
                    view.setVisibility(View.GONE);

                    okButton.setEnabled(false);
                    okButton.setTextColor(ContextCompat.getColor(LocationActivity.this, R.color.secondaryTextColor));
                    okButton.setAlpha(0.5f);
                }
            });
        }


        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBHandler dbHandler = DBHandler.getInstance(LocationActivity.this);
                dbHandler.updateCurrentUserLocation(mLatitude, mLongitude);
                SessionManager.updateCurrentUserFromDB(LocationActivity.this);
                getIntent().putExtra("latitude", mLatitude);
                getIntent().putExtra("longitude", mLongitude);
                setResult(RESULT_LOCATION_UPDATED, getIntent());
                finish();
            }
        });


    }
}
