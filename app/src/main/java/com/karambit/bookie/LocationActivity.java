package com.karambit.bookie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;
import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.helper.ComfortableProgressDialog;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.UserApi;
import com.karambit.bookie.service.BookieIntentFilters;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationActivity extends AppCompatActivity {

    public static final String TAG = LocationActivity.class.getSimpleName();

    private LatLng mLatLng;

    private DBManager mDbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            SpannableString s = new SpannableString(getString(R.string.select_location));
            s.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            float elevation = getResources().getDimension(R.dimen.actionbar_max_elevation);
            actionBar.setElevation(elevation);
            float titleSize = getResources().getDimension(R.dimen.actionbar_title_size);
            s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            ((TextView) toolbar.findViewById(R.id.toolbarTitle)).setText(s);

            toolbar.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            toolbar.findViewById(R.id.doneButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doneLocation();
                }
            });
        }

        mDbManager = new DBManager(this);
        mDbManager.open();

        final SupportPlaceAutocompleteFragment autocompleteFragment = (SupportPlaceAutocompleteFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
                .build();

        autocompleteFragment.setFilter(typeFilter);


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mLatLng = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(LocationActivity.this, R.string.unknown_error, Toast.LENGTH_LONG).show();
            }
        });

        if (autocompleteFragment.getView() != null){

            ((TextView) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextColor(ContextCompat.getColor(this, R.color.primaryTextColor));

            autocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText("");

                    mLatLng = null;
                }
            });
        }

        Button moreInfoButton = (Button) findViewById(R.id.locationMoreInfo);
        moreInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationActivity.this, InfoActivity.class);
                intent.putExtra(InfoActivity.EXTRA_INFO_CODES, new int[]{
                    InfoActivity.INFO_CODE_LOCATION
                });
                startActivity(intent);
            }
        });
    }

    private void doneLocation() {
        if (BookieApplication.hasNetwork()) {
            if (mLatLng != null && getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment).getView() != null) {

                final ComfortableProgressDialog progressDialog = new ComfortableProgressDialog(LocationActivity.this);
                progressDialog.setMessage(R.string.please_wait);
                progressDialog.setCancelable(false);
                progressDialog.show();

                User.Details currentUserDetails = SessionManager.getCurrentUserDetails(LocationActivity.this);
                String email = currentUserDetails.getEmail();
                String password = currentUserDetails.getPassword();

                updateLocation(email, password, mLatLng.latitude, mLatLng.longitude, progressDialog);

            } else {
                Toast.makeText(LocationActivity.this, R.string.select_location_warning, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLocation(String email, String password, final double latitude, final double longitude, final ComfortableProgressDialog progressDialog) {
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        Call<ResponseBody> uploadUserLocation = userApi.updateUserLocation(email, password, latitude, longitude);

        Logger.d("getHomePageBooks() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password +
                     ", \n\tlatitude=" + latitude + ", \n\tlongitude=" + longitude);


        uploadUserLocation.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {

                                Logger.d("Location updated");

                                mDbManager.Threaded(mDbManager.getUserDataSource().cUpdateUserLocation(latitude, longitude));
                                SessionManager.updateCurrentUserFromDB(LocationActivity.this);
                                SessionManager.setLocationText(null);

                                Intent intent = new Intent(BookieIntentFilters.INTENT_FILTER_LOCATION_UPDATED);
                                intent.putExtra(BookieIntentFilters.EXTRA_LOCATION, new LatLng(latitude, longitude));

                                LocalBroadcastManager.getInstance(LocationActivity.this).sendBroadcast(intent);

                                finish();
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);

                                Toast.makeText(LocationActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Logger.e("Response body is null. (Current User Settings Page Error)");
                            Toast.makeText(LocationActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Logger.e("Response object is null. (Current User Settings Page Error)");
                        Toast.makeText(LocationActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());

                    Toast.makeText(LocationActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(LocationActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                Logger.e("Location onFailure: " + t.getMessage());
            }
        });
    }
}
