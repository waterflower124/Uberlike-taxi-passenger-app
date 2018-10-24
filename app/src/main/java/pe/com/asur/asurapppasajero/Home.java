package pe.com.asur.asurapppasajero;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import io.paperdb.Paper;
import pe.com.asur.asurapppasajero.BottomSheetRiderFragment;
import pe.com.asur.asurapppasajero.CallDriver;
import pe.com.asur.asurapppasajero.Common.Common;
import pe.com.asur.asurapppasajero.Helper.CustomInfoWindow;
import pe.com.asur.asurapppasajero.Home_;
import pe.com.asur.asurapppasajero.MainActivity;
import pe.com.asur.asurapppasajero.Model.Token;
import pe.com.asur.asurapppasajero.Model.User;
import pe.com.asur.asurapppasajero.R;
import pe.com.asur.asurapppasajero.Remote.IFCMService;
/*
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;*/
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener{

    SupportMapFragment mapFragment;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    //location
    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference drivers;
    GeoFire geoFire;

    Marker mUserMarker, markerDestination;

    //Bottomsheet
    ImageView imgExpandable;
    BottomSheetRiderFragment mBottomSheet;
    Button btnPickupRequest;


    int radius = 1; //1km
    int distance = 1; //3km
    private static final int LIMIT = 3;

    //send alert
    IFCMService mService;

    //presense system
    DatabaseReference driverAvailable;

    PlaceAutocompleteFragment place_location, place_destination;
    AutocompleteFilter typeFilter;

    String mPlaceLocation, mPlaceDestination;

    //New update information
    CircleImageView imageAvatar;
    TextView txtRiderName, txtStars;

    //Declare FireStorage to upload avatar
    FirebaseStorage storage;
    StorageReference storageReference;

    public static ProgressDialog progressDialog;

    boolean old_order = false;
//
//    Timer timer = new Timer();
//    TimerTask timerBody = new TimerTask() {
//        int count = 0;
//        public void run() {
//
//            count ++;
////            Toast.makeText(getApplicationContext(), count, Toast.LENGTH_LONG).show();
////            btnPickupRequest.setText(String.valueOf(count));
//            if(count == 2)
//                Toast.makeText(getApplicationContext(), "ddddd", Toast.LENGTH_LONG).show();
//                this.cancel();
//        }
//
//    };
    int send_request_count = 0;

    public static GeoQuery geoQuery_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        timer.schedule(timerBody, 0, 1000);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = Common.getFCMService();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Init Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Add findViewById for imageAvatar txtRiderName....here
        View navigationHeaderView = navigationView.getHeaderView(0);
        txtRiderName = navigationHeaderView.findViewById(R.id.txtRiderName);
        txtRiderName.setText(String.format("%s", Common.currentUser.getName()));
        txtStars = navigationHeaderView.findViewById(R.id.txtStars);
        txtStars.setText(String.format("%s", Common.currentUser.getRates()));
        imageAvatar = navigationHeaderView.findViewById(R.id.imageAvatar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait for Accepting");
        progressDialog.setCancelable(false);



        //Load Avatar
        if (Common.currentUser.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentUser.getAvatarUrl())) {
            Picasso.with(this)
                    .load(Common.currentUser.getAvatarUrl())
                    .into(imageAvatar);
        }


        //maps
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //init view
        imgExpandable = (ImageView) findViewById(R.id.imgExpandable);

        //
//        FirebaseDatabase db = FirebaseDatabase.getInstance();
//        final DatabaseReference current_orderRef = db.getReference(Common.register_current_order);
//
//        current_orderRef
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if(dataSnapshot.exists()) {
//                            old_order = true;
//                            current_orderRef
//                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                    .child("end_position")
//                                    .addListenerForSingleValueEvent(new ValueEventListrner() {
//                                        @Override
//                                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                            if(dataSnapshot.exists())
//                                                Common.order_canceled = false;
//                                            else
//                                                Common.order_canceled = true;
//                                        }
//                                        @Override
//                                        public void onCancelled(DatabaseError databaseError) {
//
//                                        }
//                                    });
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });


        btnPickupRequest = (Button) findViewById(R.id.btnPickupRequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(Common.old_order) {
//                    Toast.makeText(getApplicationContext(), "current order is exist", Toast.LENGTH_LONG).show();
                    new AlertDialog.Builder(Home.this)
                            .setTitle("Notice")
                            .setMessage("Your previous order is remained. Please cancel or finish your order.")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(Common.order_canceled) {
//                                        Common.old_order = false;
//                                        Toast.makeText(getApplicationContext(), "sdfsdf: " + Common.old_order_driverID, Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(Home.this, ViewDriverInfo.class);
                                        intent.putExtra("driverID", Common.old_order_driverID);
                                        intent.putExtra("state", "old_order");
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(Home.this, RateActivity.class);
                                        intent.putExtra("driverID", Common.old_order_driverID);
                                        intent.putExtra("state", "old_order");
                                        startActivity(intent);
                                    }
                                }
                            }).create().show();



                } else {
                    if (Common.driverId.isEmpty()) {

                        progressDialog.show();
//                        driverSearch = true;
                        Common.appoint = true;

                        requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    } else {
                        Toast.makeText(getApplicationContext(), "driver ID already allocate", Toast.LENGTH_SHORT).show();
                    }
                }






             //   if (!Common.isDriverFound) {
//                    if (!Common.isDriverFound) {
//                        requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                    }
//                    else {
//                        //agus prube co esta linea Common.sendRequestToDriver(Common.driverId,mService,getBaseContext(),mLastLocation);
//                        //Common.sendRequestToDriver(Common.driverId,mService,getBaseContext(),mLastLocation);
//
//
//
//
//                        Common.sendRequestToDriver(Common.driverId, mService, getBaseContext(), Common.mLastLocation);
//                        Common.isDriverFound = false;
//
//
//
//
////                        Toast.makeText(getApplicationContext(), "driverID:" + Common.driverId, Toast.LENGTH_LONG).show();
//                    }

              //  }
                   /* AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            requestPickupHere(account.getId());
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {

                        }
                    });
                } else
                    Common.sendRequestToDriver(Common.driverId,mService,getBaseContext(),mLastLocation);*/
            }
        });

        place_destination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_destination);
        place_location = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_location);
        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

        //Event
        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceLocation = place.getAddress().toString();
                //remove old marker
                mMap.clear();

                //Add marker at new location
                mUserMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                        .title("yo"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
            }

            @Override
            public void onError(Status status) {

            }
        });
        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceDestination = place.getAddress().toString();

                //Add new destination marker
                mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                        .title("Destination"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

                //Show information in bottom
                BottomSheetRiderFragment mBottomSheet = BottomSheetRiderFragment.newInstance(mPlaceLocation, mPlaceDestination, false);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());

            }

            @Override
            public void onError(Status status) {

            }
        });

        setUpLocation();

        updateFirebaseToken();

    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
//        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("sssss");
      /*  AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference tokens = db.getReference(Common.token_tbl);

                Token token = new Token(FirebaseInstanceId.getInstance().getToken());
                tokens.child(account.getId())
                        .setValue(token);
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });*/
    }



    private void requestPickupHere(String uid) {
//        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
//        GeoFire mGeoFire = new GeoFire(dbRequest);
//        mGeoFire.setLocation(uid, new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));

        if (mUserMarker.isVisible())
            mUserMarker.remove();
        //add new marker
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Pickup Here")
                .snippet("")
                .position(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mUserMarker.showInfoWindow();

        btnPickupRequest.setText("Pedir Servicio...");

        findDriver();

    }

    private void findDriver() {

//        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        final DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire gfDrivers = new GeoFire(drivers);

        geoQuery_request = gfDrivers.queryAtLocation(new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()),
                Common.radius);
        geoQuery_request.removeAllListeners();
        geoQuery_request.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {

                   // Common.isDriverFound = true;-------------------------------->> le quite este para que entre varias veces segun encuentre
//                    DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
//                //if found
////                if (!Common.isDriverFound) {
//                    DatabaseReference acceptprof = tokens.child(key).child("accept");
//                    acceptprof.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            String accept_state = dataSnapshot.getValue(String.class);
//                            if(accept_state.equals("false")) {
////                                Common.isDriverFound = true;
////                                Common.driverId = key;
////                                btnPickupRequest.setText("PEDIR CARRO");
////                                Toast.makeText(getApplicationContext(), "driver:::" + key, Toast.LENGTH_SHORT).show();
////                                Common.sendRequestToDriver(Common.driverId, mService, getBaseContext(), Common.mLastLocation);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });

                if(!Common.old_order && Common.driverId.isEmpty() && Common.appoint) {
                    Common.send_request_count ++;
                    if(Common.send_request_count == 1) {
                        new CountDownTimer(20000, 1000) {
                            public void onTick(long millisUtilFinished) {
//                            Toast.makeText(getApplicationContext(), "ddd: " + String.valueOf(count), Toast.LENGTH_SHORT).show();
//                            count ++;
                            }

                            public void onFinish() {
                                if (Common.driverId.isEmpty()) {
                                    Common.appoint = false;
                                    Common.send_request_count = 0;
                                    //delete firebase
                                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                                    final DatabaseReference pick_request = db.getReference(Common.pickup_request_tbl);
                                    pick_request.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Nobody accept your request. Please reorder.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }.start();
                    }


                    DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
                    GeoFire mGeoFire = new GeoFire(dbRequest);
                    mGeoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));

                    Common.isDriverFound = true;
//                    Common.driverId = key;
                    btnPickupRequest.setText("PEDIR CARRO");
                    Common.sendRequestToDriver(key, mService, getBaseContext(), Common.mLastLocation, "");

//                    Toast.makeText(getApplicationContext(), "driver ID::: " + key, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //if still not found driver increase distance
//                if (!Common.isDriverFound && radius < LIMIT) {
//                    radius++;
//                    findDriver();
//                } else {
//                    if (!Common.isDriverFound) {
//                        Toast.makeText(Home.this, "No hay Chofer Cerca", Toast.LENGTH_SHORT).show();
//                        btnPickupRequest.setText("Recojerme");
//                        geoQuery.removeAllListeners();
//                    }
//                }
                if(!Common.isDriverFound) {
                    progressDialog.dismiss();
                    Toast.makeText(Home.this, "No hay Chofer Cerca", Toast.LENGTH_SHORT).show();
                    Common.appoint = false;
//                    geoQuery.removeGeoQueryEventListener(this);
//                    driverSearch = false;
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    setUpLocation();

                }
                break;
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE
            }, MY_PERMISSION_REQUEST_CODE);
        } else {

            buildLocationCallBack();
            createLocationRequest();
            displayLocation();
        }
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Common.mLastLocation = locationResult.getLocations().get(locationResult.getLocations().size() - 1);
                displayLocation();
            }
        };
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Common.mLastLocation = location;
                if (Common.mLastLocation != null) {


                    //create latlng from mlaslocation and this is center point
                    LatLng center = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
                    //distance in metters
                    //heading 0 is northSide 90is east 180 is south and 270 is west
                    //base on compact
                    LatLng northSide = SphericalUtil.computeOffset(center, 100000, 0);
                    LatLng southSide = SphericalUtil.computeOffset(center, 100000, 180);

                    LatLngBounds bounds = LatLngBounds.builder()
                            .include(northSide)
                            .include(southSide)
                            .build();

                    place_location.setBoundsBias(bounds);
                    place_location.setFilter(typeFilter);

                    place_destination.setBoundsBias(bounds);
                    place_location.setFilter(typeFilter);


                    //presense System
                    driverAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
                    driverAvailable.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //if have any change from Drivers table we will reload all drivers available
                            loadAllAvailableDriver(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    final double latitude = Common.mLastLocation.getLatitude();
                    final double longitude = Common.mLastLocation.getLongitude();


                    loadAllAvailableDriver(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));


                    Log.d("RAPICITY", String.format("Your location was changed : %f / %f", latitude, longitude));
                } else
                    Log.d("RAPICITY", "Cannot get your location");
            }
        });

    }

    private void loadAllAvailableDriver(final LatLng location) {

        //add Marker
        //here we will clear all map to delete old position of driver
        mMap.clear();
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .position(location)
                .title(String.format("yo")));
        //move camera to this postion
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));


        //Load all available Driver  in distance 3km
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire gf = new GeoFire(driverLocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.latitude, location.longitude), distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                //use key to get email from table users
                //table users is table when driver register account and update information
                //just open your driver to check this table name
                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //because rider and user model is same properties
                                //so we can use rider model to get user here
                                User rider = dataSnapshot.getValue(User.class);

                                //add driver to map
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .flat(true)
                                        .title(rider.getName())
                                       // .snippet("Driver ID : " + dataSnapshot.getKey())
                                        .snippet(dataSnapshot.getKey())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance <= LIMIT)  //distance just find for 3km
                {
                    distance++;
                    loadAllAvailableDriver(location);
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
    /*    if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_signOut) {
            signOut();
        } else if (id == R.id.nav_updateInformation) {
            showUpdateInformationDialog();
        } else if (id == R.id.current_order) {

            Intent intent = new Intent(getBaseContext(), ViewDriverInfo.class);
            intent.putExtra("driverID", Common.old_order_driverID);
            intent.putExtra("state", "old_order");

            startActivity(intent);
        }

        return true;
    }

    private void showUpdateInformationDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Subiendo Informacion");
        alertDialog.setMessage("Plase use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View update_info_layout = inflater.inflate(R.layout.layout_update_information, null);

        final MaterialEditText edtName = update_info_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = update_info_layout.findViewById(R.id.edtPhone);
        final ImageView imgAvatar = update_info_layout.findViewById(R.id.imgAvatar);


        alertDialog.setView(update_info_layout);

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImageAndUpload();
            }
        });
        alertDialog.setView(update_info_layout);

        //setButton
        alertDialog.setPositiveButton("ACTUALIZAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                final AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                String name = edtName.getText().toString();
                String phone = edtPhone.getText().toString();

                Map<String, Object> update = new HashMap<>();
                if (!TextUtils.isEmpty(name))
                    update.put("name", name);
                if (!TextUtils.isEmpty(phone))
                    update.put("phone", phone);

                //Update
                DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl);
                riderInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        waitingDialog.dismiss();
                        if (task.isSuccessful())
                            Toast.makeText(Home.this, "Informacion subida", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(Home.this, "Informacion No subida", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        alertDialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        //show Dialog
        alertDialog.show();

/*
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Subiendo Informacion");
        alertDialog.setMessage("Plase use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View update_info_layout = inflater.inflate(R.layout.layout_update_information, null);

        final MaterialEditText edtName = update_info_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = update_info_layout.findViewById(R.id.edtPhone);
        final ImageView imgAvatar = update_info_layout.findViewById(R.id.imgAvatar);


        alertDialog.setView(update_info_layout);

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImageAndUpload();
            }
        });
        alertDialog.setView(update_info_layout);

        //setButton
        alertDialog.setPositiveButton("ACTUALIZAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();


            }
        });

        alertDialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        final AlertDialog waitingDialog = new SpotsDialog(Home.this);
                        waitingDialog.show();

                        String name = edtName.getText().toString();
                        String phone = edtPhone.getText().toString();

                        Map<String, Object> update = new HashMap<>();
                        if (!TextUtils.isEmpty(name))
                            update.put("name", name);
                        if (!TextUtils.isEmpty(phone))
                            update.put("phone", phone);

                        //Update
                        DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl);
                        riderInformation.child(account.getId())
                                .updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                waitingDialog.dismiss();
                                if (task.isSuccessful())
                                    Toast.makeText(Home.this, "Informacion subida", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(Home.this, "Informacion No subida", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });
            }
        });

        //show Dialog
        alertDialog.show();*/
    }

    private void chooseImageAndUpload() {
        //Start intent to choose image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecionar Foto"), Common.PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri saveUri = data.getData();
            if (saveUri != null) {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Subiendo...");
                progressDialog.show();

                String imageName = UUID.randomUUID().toString(); //Random name image upload
                final StorageReference imageFolder = storageReference.child("images/" + imageName);
                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();

                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        //Update this url to avatar property of User
                                        //first you need add avatar property on user model
                                        Map<String, Object> avatarUpdate = new HashMap<>();
                                        avatarUpdate.put("avatarUrl", uri.toString());

                                        DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl);
                                        riderInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .updateChildren(avatarUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                    Toast.makeText(Home.this, "Subida !", Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(Home.this, "Subida Error!", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                });
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @SuppressWarnings("VisibleForTests")
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("Subida " + progress + "%");
                            }
                        });
            }
        }
    }


    private void signOut() {
        //Reset Remember Value
        Paper.init(this);
        Paper.book().destroy();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Home.this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.mapa_style_map)
            );

            if (!isSuccess)
                Log.e("ERROR", "Map style load failed !!!");
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }


        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //firt check markerDestination
                //if is not null just remove available marker
                if (markerDestination != null)
                    markerDestination.remove();
                markerDestination = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                        .position(latLng)
                        .title("Destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

                //Show bottom sheet// corregido clic mapa
                BottomSheetRiderFragment mBottomSheet = BottomSheetRiderFragment.newInstance(String.format("%f,%f", Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()),
                        String.format("%f,%f", latLng.latitude, latLng.longitude),
                        true);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }
        });

        mMap.setOnInfoWindowClickListener(this);

        if (ActivityCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        //If marker info windows is your location don't apply this event
        if (!marker.getTitle().equals("yo"))
        {
            //Call to new activity CallDriver
            Intent intent = new Intent(Home.this,CallDriver.class);
            //Send information to new activity
            //intent.putExtra("driverId",marker.getSnippet().replaceAll("\\D+","")); para facebok creo
            intent.putExtra("driverId",marker.getSnippet());
            intent.putExtra("lat",Common.mLastLocation.getLatitude());
            intent.putExtra("lng",Common.mLastLocation.getLongitude());
            startActivity(intent);
        }

    }
}
