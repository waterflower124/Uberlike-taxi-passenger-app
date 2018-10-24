package pe.com.asur.asurapppasajero;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import pe.com.asur.asurapppasajero.Common.Common;
import pe.com.asur.asurapppasajero.Model.DataMessage;
import pe.com.asur.asurapppasajero.Model.FCMResponse;
import pe.com.asur.asurapppasajero.Model.Rate;
import pe.com.asur.asurapppasajero.Model.Token;
import pe.com.asur.asurapppasajero.Model.User;
import pe.com.asur.asurapppasajero.Remote.IFCMService;
import pe.com.asur.asurapppasajero.Remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;


public class ViewDriverInfo extends AppCompatActivity {


    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    Button ok_order, cancel_order;

    String driverID;
    String state = "";
    String radioTagValue;

    TextView driverName, driverPhone, driverRating;
    Button btnSubmit, btncancel;
    CircleImageView imageAvatar;

    final boolean dialogstate = false;

    FirebaseDatabase database;
    DatabaseReference rateDetailRef;
    DatabaseReference driverInformationRef;

    DatabaseReference cancelOrderRef;

    IGoogleAPI mService;
    IFCMService mFCMService;

    static ViewDriverInfo pub_viewDriverInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_driver_info);

Toast.makeText(getApplicationContext(), "ewrwer", Toast.LENGTH_SHORT).show();
        ok_order = (Button)findViewById(R.id.btn_ok);
        cancel_order = (Button)findViewById(R.id.btn_cancel);

        driverName = (TextView) findViewById(R.id.txt_name);
        driverPhone = (TextView) findViewById(R.id.txt_phone);
        driverRating = (TextView) findViewById(R.id.txt_rate);

        imageAvatar = (CircleImageView) findViewById(R.id.avatar_image);

        mService = Common.getGoogleService();
        mFCMService = Common.getFCMService();

        pub_viewDriverInfo = this;

        if (getIntent() != null) {
            driverID = getIntent().getStringExtra("driverID");
            state = getIntent().getStringExtra("state");
        }

        loaddriverInfo(driverID);

        if(!state.equals("old_order"))
            registerCurrentOrder(driverID);

        ok_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        Toast.makeText(getApplicationContext(), "testt   " + driverID + ":::", Toast.LENGTH_LONG).show();
        cancel_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ViewDriverInfo.this);
                final View mView = getLayoutInflater().inflate(R.layout.layout_cancel_order, null);

                mBuilder.setView(mView);
                final AlertDialog cancel_dialog = mBuilder.create();
                cancel_dialog.show();

                Button ok_button = (Button)mView.findViewById(R.id.ok_button);
                Button cancel_button = (Button)mView.findViewById(R.id.cancel_button);
                RadioGroup radioGroup = (RadioGroup)mView.findViewById(R.id.radiogroup);

                radioTagValue = "";


                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton radioButton = (RadioButton)mView.findViewById(checkedId);
                        radioTagValue = radioButton.getTag().toString();
                    }
                });
                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancel_dialog.dismiss();
                        ViewDriverInfo.this.finish();
                    }
                });

                ok_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if(radioTagValue.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Please select one of the reasons.", Toast.LENGTH_LONG).show();
                        } else {

                            if(state.equals("old_order"))
                                Common.old_order = false;

                            final ProgressDialog progressDialog=new ProgressDialog(ViewDriverInfo.this);
                            progressDialog.setMessage("Loading...");
                            progressDialog.show();
                            progressDialog.setCancelable(false);

                            Common.isDriverFound = false;

                            Rate rate=new Rate();
                            if(radioTagValue.equals("radio1")) {
                                rate.setRates(String.valueOf(Common.cancel1_rate));
                                rate.setComment("I found another car. So I have canceled my order.");
                            } else if (radioTagValue.equals("radio2")) {
                                rate.setRates(String.valueOf(Common.cancel2_rate));
                                rate.setComment("Long time delay the car. So I have canceled my order.");
                            } else if (radioTagValue.equals("radio3")) {
                                rate.setRates(String.valueOf(Common.cancel3_rate));
                                rate.setComment("The order was canceled due to my circumstances.");
                            }

//                            if(!state.equals("old_order"))
//                                Common.sendRequestToDriver(Common.driverId, mFCMService, getBaseContext(), Common.mLastLocation, rate.getComment());

                            Common.sendRequestToDriver(driverID, mFCMService, getBaseContext(), Common.mLastLocation, rate.getComment());

//                            Toast.makeText(getApplicationContext(), ":::" + Common.driverId, Toast.LENGTH_LONG).show();
                            Common.driverId = "";

                            FirebaseDatabase db = FirebaseDatabase.getInstance();
                            final DatabaseReference current_orderRef = db.getReference(Common.register_current_order);
                            current_orderRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();


                            database=FirebaseDatabase.getInstance();
                            rateDetailRef =database.getReference(Common.rate_detail_tbl);
                            driverInformationRef=database.getReference(Common.user_driver_tbl);

                            rateDetailRef.child(driverID)
                                    //no va esta linea teng que borrarla  .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .push()//get Unique Key
                                    .setValue(rate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //if Upload succedd on firebase  calculate average and update infomation driver
                                            rateDetailRef.child(driverID)
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            double averageStars=0.0;
                                                            int count=0;
                                                            for (DataSnapshot postSnapshot:dataSnapshot.getChildren()) {
                                                                Rate rate=postSnapshot.getValue(Rate.class);
                                                                averageStars+=Double.parseDouble(rate.getRates());
                                                                count++;
                                                            }
                                                            double finalAverage=averageStars/count;
                                                            //  DecimalFormat df=new DecimalFormat("#.#");
                                                            String valueUpdate=String.valueOf(finalAverage);
                                                            //Create Objet update;
                                                            Map<String,Object> driverUpdateRate=new HashMap<>();
                                                            driverUpdateRate.put("rates",valueUpdate);
                                                            driverInformationRef.child(Common.driverId)
                                                                    .updateChildren(driverUpdateRate)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {




                                                                            progressDialog.dismiss();
                                                                            Toast.makeText(getApplicationContext(), "Gracias..", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            progressDialog.dismiss();
                                                                            Toast.makeText(getApplicationContext(), "Rate actualizado pero no se puedo escribir en información de Chofer", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                            cancel_dialog.dismiss();
                                                            ViewDriverInfo.this.finish();
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
                                    });
                        }

                    }
                });

            }
        });
    }

    public static ViewDriverInfo getInstance() {
        return pub_viewDriverInfo;
    }

    private void loaddriverInfo(String driverId) {
        FirebaseDatabase.getInstance()
                .getReference(Common.user_driver_tbl)
                .child(driverId)//x0OzvFjpLpOTxbS9QbFGSkSyGzp1
                .addListenerForSingleValueEvent(new ValueEventListener() {
//                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User driverUser = dataSnapshot.getValue(User.class);
                        if (driverUser.getAvatarUrl() != null && !TextUtils.isEmpty(driverUser.getAvatarUrl())) {
                            Picasso.with(getBaseContext())
                                    .load(driverUser.getAvatarUrl())
                                    .into(imageAvatar);
                        } else {
                            Picasso.with(getBaseContext())
                                    .load(R.drawable.default_avatar)
                                    .into(imageAvatar);
                        }


                        driverName.setText(driverUser.getName());
                        driverPhone.setText(driverUser.getPhone());
                        Float ratnum = Float.parseFloat(driverUser.getRates());
                        String tmp = String.format("%.2f", ratnum);
                        driverRating.setText(tmp);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    public void registerCurrentOrder(final String driverID) {

        DateFormat df = new SimpleDateFormat("EEE, MMM d yyyy, HH:mm a");
        String current_date_time = df.format(Calendar.getInstance().getTime());

        String fullAddress = "";
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses;
        try {

            addresses = geocoder.getFromLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);
            String area = addresses.get(0).getLocality();
            String city = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

//            fullAddress = address + ", " + area + ", " + city + ", " + country;
            fullAddress = address;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Google Service Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference current_order = db.getReference(Common.register_current_order);
        Map<String, String> orderData = new HashMap<String, String>();
        orderData.put("driverID", driverID);
        orderData.put("start_position", fullAddress);
        orderData.put("order_time", current_date_time);
        orderData.put("lat", String.valueOf(Common.mLastLocation.getLatitude()));
        orderData.put("lng", String.valueOf(Common.mLastLocation.getLongitude()));
        current_order.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(orderData);


    }


}
