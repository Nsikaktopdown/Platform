package com.pies.platform.admin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pies.platform.Login;
import com.pies.platform.PermissionUtils;
import com.pies.platform.R;
import com.pies.platform.admin.model.*;
import com.pies.platform.teachersActivity.model.teacher_data;

import java.security.Provider;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    private GoogleMap mMap;

    protected static final String TAG = "MainActivity";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    protected String mLatitudeLabel;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean mPermissionDenied = false;

    protected String mLongitudeLabel;

    LocationManager locationManager;
    GoogleMap.OnMyLocationChangeListener mLocationListener;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    //  private static final String TAG = "EmailPassword";
    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    ;
    private FirebaseUser mFirebaseUser;
    // [END declare_database_ref]
    ProgressDialog progressDialog;
    private EditText hName, hAddress, hPhone;
    private Button add;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    Location location;
    Criteria criteria;
    double lat = 0;
    double longt = 0;
    AlertDialog alert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        alert = builder.create();


        hName = (EditText) findViewById(R.id.input_home_name);
        hAddress = (EditText) findViewById(R.id.input_home_address);
        hPhone = (EditText) findViewById(R.id.input_home_phone);
        progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setMessage("Updating Profile    m...");
        progressDialog.setCancelable(false);
        add = (Button) findViewById(R.id.btn_add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = hName.getText().toString();
                String address = hAddress.getText().toString();
                String phone = hPhone.getText().toString();

                Toast.makeText(MapsActivity.this, lat + " " + longt, Toast.LENGTH_SHORT).show();
                if (lat == 0 || longt == 0 ) {
                    Toast.makeText(MapsActivity.this, "Current location not gotten..Enable GPS", Toast.LENGTH_SHORT).show();
                }
                else if( name.isEmpty() || address.isEmpty() || phone.isEmpty()){
                    Toast.makeText(MapsActivity.this, "Incomplete information", Toast.LENGTH_SHORT).show();

                }  else{
                    createHome(lat, longt, name, address, phone);
                }

            }
        });


        progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Adding Home...");
        progressDialog.setCancelable(false);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // Creating an empty criteria object
        if(mMap != null){
            mMap.setMyLocationEnabled(true);
        }


        //   locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alert.show();
        } else {
            alert.hide();
            // Getting the name of the provider that meets the criteria
            String provider = locationManager.getBestProvider(criteria, false);

            location = locationManager.getLastKnownLocation(provider);
            if (location != null) {

                lat = location.getLatitude();

                longt = location.getLongitude();
                // Toast.makeText(MapsActivity.this, "long:" + d + "lat:" + c, Toast.LENGTH_SHORT).show();


            } else {
//                locationManager.requestLocationUpdates(provider, 1000, 0, (android.location.LocationListener) this);
            }


        }


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
       // client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;


        }
        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(lat, longt);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        mMap.setMyLocationEnabled(true);
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();


        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        locationManager.removeUpdates((android.location.LocationListener) this);

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
        checklocation();
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public void checklocation() {
        //   locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
           alert.show();
        } else {
            alert.hide();
            // Getting the name of the provider that meets the criteria
            String provider = locationManager.getBestProvider(criteria, false);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
            lat = location.getLatitude();

            longt = location.getLongitude();
            // Toast.makeText(MapsActivity.this, "long:" + d + "lat:" + c, Toast.LENGTH_SHORT).show();



        }}
}
    // [START write_fan_out]
    private void createHome(double lat, double longt, String name, String address, String phone) {
        progressDialog.show();
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("Homes-Added").push().getKey();
        Add_Home_item home = new Add_Home_item(lat, longt, name, address, phone,"");
        Map<String, Object> postValues = home.toAddHome();

        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/Homes-Added/" + key, postValues);
        childUpdates.put("/Homes-Added-toAssign/" + name, postValues);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MapsActivity.this, "Home not Added",
                            Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setMessage("Home Added")
                            .setTitle("Successfully")
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //FirebaseAuth.getInstance().signOut();


                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            progressDialog.hide();

            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.v(TAG,"home added");
            }
        });

    }


    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        if(mMap != null){
            mMap.addMarker(new MarkerOptions().position(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }

        double c = location.getLatitude();

        double d = location.getLongitude();
        Toast.makeText(MapsActivity.this, "long:" + d + "lat:" + c, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //   locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        alert.show();
        } else {
            alert.hide();
            // Getting the name of the provider that meets the criteria
            String provider = locationManager.getBestProvider(criteria, false);


            if (location != null) {
                lat = location.getLatitude();

                longt = location.getLongitude();



            } else {
//                locationManager.requestLocationUpdates(provider, 1000, 0, (android.location.LocationListener) this);
            }


        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //   locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
    alert.show();
        } else {
            alert.hide();
            // Getting the name of the provider that meets the criteria
            String provider = locationManager.getBestProvider(criteria, false);


            if (location != null) {
                lat = location.getLatitude();
                longt = location.getLongitude();
               // Toast.makeText(MapsActivity.this, "long:" + d + "lat:" + c, Toast.LENGTH_SHORT).show();



            } else {
//                locationManager.requestLocationUpdates(provider, 1000, 0, (android.location.LocationListener) this);
            }


        }


    }
}
