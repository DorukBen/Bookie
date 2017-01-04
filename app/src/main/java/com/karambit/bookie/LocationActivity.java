package com.karambit.bookie;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;

public class LocationActivity extends AppCompatActivity {

    public static final String TAG = LocationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(R.string.select_location);
            getSupportActionBar().setElevation(0);
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
                //TODO: set user location here place.getLat()
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
                finish();
            }
        });


    }
}
