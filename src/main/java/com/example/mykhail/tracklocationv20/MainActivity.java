package com.example.mykhail.tracklocationv20;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener, AdapterView.OnItemClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, CompoundButton.OnCheckedChangeListener {
    private NavigationView mNavigationView;
    private TextView mNavigationViewHeaderNumber;
    private View mNavigationViewHeaderView;
    private AutoCompleteTextView mAutoCompleteTextView;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private SharedPreferences mSharedPreferences;

    private String mUserPhoneNumber;
    private FirebaseManager mFirebaseManager;
    private Firebase mFirebaseRef;
    public static DataSnapshot mDataSnapshot;

    private List<String> mUserGroups;
    private List<String> mUserFriendList;
    private List<String> mUserFriendListGroup;

    private SupportMapFragment mMapFragment;
    private LocationManager mLocationManager;
    private ToggleButton mTrackMyLocation;
    private GoogleMap mGoogleMap;
    public static GoogleApiClient mGoogleApiClient;
    private GooglePlacesAutocompleteAdapter mAdapter;
    private String mBestProvider;
    private ProgressDialog mProgressDialog;
    public  AlertDialog alert;
    public  boolean mStopThread;
    public static boolean inetOn;
    public static Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        Firebase.setAndroidContext(getApplicationContext());
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (mNavigationView!=null)
        mNavigationViewHeaderView = mNavigationView.getHeaderView(0);
        mNavigationViewHeaderNumber = (TextView) mNavigationViewHeaderView.findViewById(R.id.textViewHeaderName);
        mTrackMyLocation = (ToggleButton) mNavigationViewHeaderView.findViewById(R.id.toggleButton_navigationview);
        mTrackMyLocation.setOnCheckedChangeListener(this);
        mNavigationView.setNavigationItemSelectedListener(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mFirebaseManager = new FirebaseManager(getApplicationContext());
        mFirebaseRef = new Firebase(Constants.DATABASE_URL);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.GoogleMapFragment);
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autocompletetextview);


        mProgressDialog = new ProgressDialog(this, R.style.full_screen_dialog) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.custom_progressdialog);
                getWindow().setLayout(DrawerLayout.LayoutParams.MATCH_PARENT,
                        DrawerLayout.LayoutParams.MATCH_PARENT);
            }
        };
        mProgressDialog.setCancelable(false);
        mStopThread = false;

    }

    @Override
    public void onResume() {
        super.onResume();

        mProgressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mStopThread) {
                    try {
                        Thread.sleep(100);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                    }
                });
            }
        }).start();

        if (isNeedLogin()) {
            if (!checkInternetStatus() || !checkGPS()){

                if (!checkGPS()) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setMessage(getString(R.string.gps_info));
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Включить ", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            }
                    );
                    alertDialogBuilder.setNegativeButton("Выход", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

                    alert = alertDialogBuilder.create();
                    alert.show();
                }
                if (!checkInternetStatus()) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setMessage(getString(R.string.internet_info));
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Включить", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                                }
                            }
                    );
                    alertDialogBuilder.setNegativeButton("Выход", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

                    alert = alertDialogBuilder.create();
                    alert.show();
                }
            }
            else {
                mStopThread = true;
                startActivityForResult(new Intent(this, Login_activity.class),1);
            }
        } else {
            mSharedPreferences = getPreferences(MODE_PRIVATE);
            mUserPhoneNumber = mSharedPreferences.getString("numberPhone", "");
            Singleton.getInstance().setUserPhone(mUserPhoneNumber);
        }

        if (mUserPhoneNumber != null) {

            if (checkInternetStatus() && checkGPS()) {
                mGoogleApiClient = new GoogleApiClient
                        .Builder(this)
                        .addApi(Places.GEO_DATA_API)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addApi(Places.PLACE_DETECTION_API)
                        .build();
                mGoogleApiClient.connect();


                mAutoCompleteTextView.setThreshold(4);
                mAdapter = new GooglePlacesAutocompleteAdapter(this, mGoogleApiClient, null, null);
                mAutoCompleteTextView.setAdapter(mAdapter);
                mAutoCompleteTextView.setOnItemClickListener(this);
                mMapFragment.getMapAsync(this);

                mFirebaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mDataSnapshot = dataSnapshot;
                        mNavigationViewHeaderNumber.setText(mUserPhoneNumber);
                        mStopThread = true;
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }
            else {

                if (!checkGPS()) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setMessage(getString(R.string.gps_info));
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Включить ", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            }
                    );
                    alertDialogBuilder.setNegativeButton("Выход", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

                    alert = alertDialogBuilder.create();
                    alert.show();
                }
                if (!checkInternetStatus()) {
                    if (!inetOn) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setMessage(getString(R.string.internet_info));
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setPositiveButton("Включить", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                                        inetOn  =true;
                                    }
                                }
                        );
                        alertDialogBuilder.setNegativeButton("Выход", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });

                        alert = alertDialogBuilder.create();
                        alert.show();
                    }
                }
            }
            if(mTrackMyLocation.isChecked()){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
                Date date = new Date();
                mFirebaseRef.child(mUserPhoneNumber).child(Constants.currentTime).setValue(dateFormat.format(date).toString());
            }
            mFirebaseRef.child(mUserPhoneNumber).child(Constants.STATUS).setValue("online");
            mUserGroups = mFirebaseManager.getUserGroups(mUserPhoneNumber);
            mUserFriendList = mFirebaseManager.getUserFriendList(mUserPhoneNumber);
            Singleton.getInstance().setmUserFriendList(mUserFriendList);
            mUserFriendListGroup = mFirebaseManager.getUserFriendListGroup(mUserPhoneNumber);
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_FINE);
            mBestProvider = mLocationManager.getBestProvider(crit, false);
        }

    }

    @Override
    public void onStop() {
        if (mUserPhoneNumber != null ) {
            if( mTrackMyLocation.isChecked()) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
                Date date = new Date();
                mFirebaseRef.child(mUserPhoneNumber).child(Constants.currentTime).setValue(dateFormat.format(date).toString());
            }
            mFirebaseRef.child(mUserPhoneNumber).child(Constants.STATUS).setValue("offline");
        }
        super.onStop();
    }

    public void reloadMap() {
        mMapFragment.getMapAsync(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        mDrawerLayout.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()) {
            case R.id.navigation_item_contacts:
                Singleton.getInstance().setUserPhone(mUserPhoneNumber);
                prepareFragment(FriendListFragment.newInstance(mUserPhoneNumber
                        , mUserGroups
                        , mUserFriendList
                        , mUserFriendListGroup));
                break;
            case R.id.navigation_item_home:
                getSupportFragmentManager().popBackStack();
                break;
            case R.id.navigation_item_logout:
                getSupportFragmentManager().popBackStack();
                mNavigationView.getMenu().getItem(0).setChecked(true);
                mSharedPreferences = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean("login", true);
                editor.putString("numberPhone", "");
                editor.apply();
                Singleton.getInstance().clearAll();
                if (mGoogleMap != null)
                    mGoogleMap.clear();
                mUserPhoneNumber = null;
                startActivityForResult(new Intent(this, Login_activity.class), 1);
                break;
            case R.id.navigation_item_changePassword:
                mNavigationView.getMenu().getItem(0).setChecked(true);
                ResetPasswordFragment resetPasswordFragment = ResetPasswordFragment.newInstance();
                resetPasswordFragment.show(getFragmentManager().beginTransaction(), "dialog");
                break;
            case R.id.navigation_item_addFriend:
                mNavigationView.getMenu().getItem(0).setChecked(true);
                AddFriendFragment addFriendFragment = AddFriendFragment.newInstance(mUserPhoneNumber, mUserGroups);
                addFriendFragment.show(getFragmentManager().beginTransaction(), "dialog");
                break;
        }

        return true;
    }

    private boolean checkCallingPermission() {
        return !(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (checkCallingPermission()) {
            mGoogleMap.setMyLocationEnabled(true);

            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        }
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);

        if (Singleton.getInstance().getSelectedUsers() != null && !mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mFirebaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    List<String> mSelectedUsers = Singleton.getInstance().getSelectedUsers();
                    mGoogleMap.clear();
                    for (int i = 0; i < mSelectedUsers.size(); i++) {

                        Marker selectedUser = mGoogleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(getUserLocation(mSelectedUsers.get(i), dataSnapshot).getLatitude(), getUserLocation(mSelectedUsers.get(i), dataSnapshot).getLongitude()))
                                .title(mSelectedUsers.get(i))
                                .snippet(getString(R.string.deleteMarket)));
                        selectedUser.showInfoWindow();
                        mGoogleMap.animateCamera(CameraUpdateFactory
                                .newLatLng(new LatLng(getUserLocation(mSelectedUsers.get(i), dataSnapshot).getLatitude(), getUserLocation(mSelectedUsers.get(i), dataSnapshot).getLongitude())));
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        mGoogleMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                Singleton.getInstance().getSelectedUsers().remove(marker.getTitle());
                marker.remove();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mTrackMyLocation.isChecked()) {
            mFirebaseRef.child(mUserPhoneNumber).child("first").setValue(location.getLatitude());
            mFirebaseRef.child(mUserPhoneNumber).child("second").setValue(location.getLongitude());
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = new Date();
            mFirebaseRef.child(mUserPhoneNumber).child(Constants.currentTime).setValue(dateFormat.format(date).toString());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        mMenu = menu;
        return true;
    }

    private Menu mMenu;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.search:
                mAutoCompleteTextView.setVisibility(View.VISIBLE);
                mAutoCompleteTextView.requestFocus();
                mMenu.findItem(R.id.clear).setVisible(true);
                break;
            case R.id.clear:
                mAutoCompleteTextView.setVisibility(View.GONE);
                mAutoCompleteTextView.setText("");
                mMenu.findItem(R.id.clear).setVisible(false);
                hideKeyboard(getApplicationContext(), mAutoCompleteTextView);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if (resultCode==RESULT_OK) {
                    mSharedPreferences = getPreferences(MODE_PRIVATE);
                    mUserPhoneNumber = data.getStringExtra("number");
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putBoolean("login", false);
                    editor.putString("numberPhone", mUserPhoneNumber);
                    editor.apply();
                }
                else
                    finish();
                break;
            case 1000:
                if (resultCode == RESULT_OK) {
                    if (checkCallingPermission()) {
                        Location mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        if (mLocation != null)
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 14.0f));
                    }
                }
                break;

        }

    }

    private void prepareFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.GoogleMapFragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    private boolean isNeedLogin() {
        mSharedPreferences = getPreferences(MODE_PRIVATE);
        return mSharedPreferences.getBoolean("login", true);
    }

    private Location getUserLocation(String userName, DataSnapshot dataSnapshot) {
        Location location = new Location("");
        if (dataSnapshot != null) {
            location.setLatitude(Double.parseDouble(dataSnapshot.child(userName).child("first").getValue().toString()));
            location.setLongitude(Double.parseDouble(dataSnapshot.child(userName).child("second").getValue().toString()));
        }
        return location;
    }





    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        mGoogleMap.clear();
        final AutocompletePrediction item = mAdapter.getItem(position);
        final String placeId = item.getPlaceId();
        final CharSequence primaryText = item.getPrimaryText(null);
        Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId).setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                final Place myPlace = places.get(0);
                if (myPlace.getPlaceTypes().get(0) == Place.TYPE_COUNTRY)
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace.getLatLng(), 5.0f));
                else if (myPlace.getPlaceTypes().get(0) == Place.TYPE_CITY_HALL)
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace.getLatLng(), 8.0f));
                else if (myPlace.getPlaceTypes().get(0) == Place.TYPE_CAFE)
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace.getLatLng(), 14.0f));
                else if (myPlace.getPlaceTypes().get(0) == Place.TYPE_STREET_ADDRESS)
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace.getLatLng(), 12.0f));
                else
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace.getLatLng(), 14.0f));
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(myPlace.getLatLng())
                        .title(myPlace.getName().toString())
                );

            }
        });
        hideKeyboard(getApplicationContext(), mAutoCompleteTextView);
        Log.i(Constants.LOG_TAG, "Autocomplete item selected: " + primaryText);
        Log.i(Constants.LOG_TAG, placeId);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void hideKeyboard(Context context, View view) {
        InputMethodManager keyboardManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboardManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkCallingPermission()) {
            mLocationManager.requestLocationUpdates(mBestProvider, 1000, 1, this);
            if (mTrackMyLocation.isChecked() && LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) != null) {
                mFirebaseRef.child(mUserPhoneNumber).child("first")
                        .setValue(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLatitude());
                mFirebaseRef.child(mUserPhoneNumber).child("second")
                        .setValue(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLongitude());
                mGoogleMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(LocationServices.FusedLocationApi
                                .getLastLocation(mGoogleApiClient).getLatitude(),
                                LocationServices.FusedLocationApi
                                        .getLastLocation(mGoogleApiClient).getLongitude()),14.0f));

            }
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(
                                    MainActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!isChecked) {
            if(checkCallingPermission())
            mGoogleMap.setMyLocationEnabled(false);
        }
        else {
            if(checkCallingPermission())
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    private boolean checkInternetStatus() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return true;
            }
        } else {
            return false;
        }
     return false;
    }
    private boolean checkGPS (){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            return false;
        }
        else
            return true;
    }



}
