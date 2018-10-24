package pe.com.asur.asurapppasajero.Common;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import pe.com.asur.asurapppasajero.Home;
import pe.com.asur.asurapppasajero.Model.DataMessage;
import pe.com.asur.asurapppasajero.Model.FCMResponse;
import pe.com.asur.asurapppasajero.Model.User;
import pe.com.asur.asurapppasajero.Model.Token;
import pe.com.asur.asurapppasajero.Remote.FCMClient;
import pe.com.asur.asurapppasajero.Remote.GoogleMapAPI;
import pe.com.asur.asurapppasajero.Remote.IFCMService;
import pe.com.asur.asurapppasajero.Remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by agus on 14/03/2018.
 */

public class Common {

    public  static final int PICK_IMAGE_REQUEST=9999;

    public static  boolean isDriverFound = false;
    public static String driverId = "";
    public static Location mLastLocation;
    public static User currentUser=new User();

    public static final String driver_tbl = "Drivers";
    public static final String user_driver_tbl = "DriversInformation";
    public static final String user_rider_tbl = "RidersInformation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";
    public static final String rate_detail_tbl = "RateDetails";//if
    public static final String register_current_order = "CurrentOrder";//if

    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static final String googleAPIUrl = "https://maps.googleapis.com/";
    public static final String user_field="rider_user";
    public static final String pwd_field="rider_pwd";


    public static double base_fare = 3.5;
    private static double time_rate = 0.15;
    private static double distance_rate = 0.73;

    public static int radius = 5;

    public static int cancel1_rate = 3; // I found another car
    public static int cancel2_rate = 1; // Long time delay the car
    public static int cancel3_rate = 4; // others

    public static boolean old_order = false;
    public static boolean order_canceled = false;

    public static String old_order_driverID = "";

    public static boolean appoint = false;

    public static int send_request_count = 0;


    public static double getPrice(double km,int min)
    {
        return (base_fare+(time_rate*min)+(distance_rate*km));
    }



    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
    public static IGoogleAPI getGoogleService()
    {
        return GoogleMapAPI.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }


    public static void sendRequestToDriver(final String driverId, final IFCMService mService, final Context context, final Location currentLocation, final String sendstr) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        tokens.orderByKey().equalTo(driverId)//x0OzvFjpLpOTxbS9QbFGSkSyGzp1
      //  tokens.orderByValue()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            Token token = postSnapShot.getValue(Token.class); //get token object from database whit key
                            final String riderToken = FirebaseInstanceId.getInstance().getToken();
                            Map<String,String> content=new HashMap<String, String>();

                            Log.d("MainActivity", "RIDERTOKEN::" + riderToken);

                            content.put("customer",riderToken);
                            content.put("lat",String.valueOf(currentLocation.getLatitude()));
                            content.put("lng",String.valueOf(currentLocation.getLongitude()));
                            content.put("str", sendstr);
                            DataMessage dataMessage=new DataMessage(token.getToken(), content);
                            mService.sendMessage(dataMessage)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.body().success == 1) {
                                                Toast.makeText(context, "Request sent:  " + driverId, Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                                Toast.makeText(context, "Failed !", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                            Toast.makeText(context, "Network Failure !", Toast.LENGTH_SHORT).show();
                                            Home.progressDialog.dismiss();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(context, "Entro al onCancel", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
