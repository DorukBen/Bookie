package com.karambit.bookie;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.helper.ComfortableProgressDialog;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.UserApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationActivity extends AppCompatActivity {

    public static final String TAG = LocationActivity.class.getSimpleName();

    public static final int RESULT_LOCATION_UPDATED = 1;

    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";

    private double mLatitude;
    private double mLongitude;

    private DBManager mDbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            SpannableString s = new SpannableString(getString(R.string.select_location));
            s.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            float elevation = getResources().getDimension(R.dimen.actionbar_starting_elevation);
            actionBar.setElevation(elevation);
        }

        mDbManager = new DBManager(this);
        mDbManager.open();

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

                final ComfortableProgressDialog progressDialog = new ComfortableProgressDialog(LocationActivity.this);
                progressDialog.setMessage(R.string.please_wait);
                progressDialog.setCancelable(false);
                progressDialog.show();

                User.Details currentUserDetails = SessionManager.getCurrentUserDetails(LocationActivity.this);
                String email = currentUserDetails.getEmail();
                String password = currentUserDetails.getPassword();

                updateLocation(email, password, mLatitude, mLongitude, progressDialog);
            }
        });


    }

    private void updateLocation(String email, String password, final double latitude, final double longitude, final ComfortableProgressDialog progressDialog) {
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        Call<ResponseBody> uploadUserLocation = userApi.updateUserLocation(email, password, latitude, longitude);

        uploadUserLocation.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                mDbManager.getUserDataSource().updateUserLocation(latitude, longitude);
                                SessionManager.updateCurrentUserFromDB(LocationActivity.this);
                                SessionManager.setLocationText(null);
                                getIntent().putExtra(EXTRA_LATITUDE, latitude);
                                getIntent().putExtra(EXTRA_LONGITUDE, longitude);
                                setResult(RESULT_LOCATION_UPDATED, getIntent());
                                finish();
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Location Page Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Location Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Location Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Location Page Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                Toast.makeText(LocationActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Current User Settings Page Error)");
                            Toast.makeText(LocationActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Current User Settings Page Error)");
                        Toast.makeText(LocationActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(LocationActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(LocationActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Location onFailure: " + t.getMessage());
            }
        });
    }
}
