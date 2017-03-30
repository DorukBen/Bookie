package com.karambit.bookie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.LayoutUtils;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.UserApi;
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

    public static final int RESULT_LOCATION_UPDATED = 1;

    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";

    private LatLng mLatLng;

    private DBManager mDbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            SpannableString s = new SpannableString(getString(R.string.select_location));
            s.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setElevation(LayoutUtils.DP * ElevationScrollListener.ACTIONBAR_ELEVATION_DP);
            actionBar.setTitle(s);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_primary_text_color);
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
                // TODO Put related header extras array
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.done_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            case R.id.action_done: {

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
                    Toast.makeText(this, R.string.select_location_warning, Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
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

                                mDbManager.getUserDataSource().updateUserLocation(latitude, longitude);
                                SessionManager.updateCurrentUserFromDB(LocationActivity.this);
                                SessionManager.setLocationText(null);
                                getIntent().putExtra(EXTRA_LATITUDE, latitude);
                                getIntent().putExtra(EXTRA_LONGITUDE, longitude);
                                setResult(RESULT_LOCATION_UPDATED, getIntent());
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
