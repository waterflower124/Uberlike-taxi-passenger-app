package pe.com.asur.asurapppasajero.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import pe.com.asur.asurapppasajero.Common.Common;
import pe.com.asur.asurapppasajero.Helper.NotificationHelper;

import pe.com.asur.asurapppasajero.Home;
import pe.com.asur.asurapppasajero.NotifyArrive;
import pe.com.asur.asurapppasajero.R;
import pe.com.asur.asurapppasajero.RateActivity;
import pe.com.asur.asurapppasajero.ViewDriverInfo;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by fumon_000 on 02/03/2018.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if(remoteMessage.getData()!=null){
            Map<String,String>data=remoteMessage.getData();
            String title=data.get("title");
            final String message=data.get("message");

            if(title.equals("Accept")) {
                Common.appoint = false;
                Common.send_request_count = 0;
                final String driverID = data.get("driverID");
                Common.driverId = driverID;
                Home.progressDialog.dismiss();
                Intent intent = new Intent(getBaseContext(), ViewDriverInfo.class);
                intent.putExtra("driverID", driverID);
                intent.putExtra("state", "");
                startActivity(intent);
            }
            else if (title.equals("Cancelado"))
            {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyFirebaseMessaging.this, ""+message, Toast.LENGTH_SHORT).show();
                    }
                });

            }
            else if (title.equals("Llego"))
            {
                ViewDriverInfo.getInstance().finish();
                final String driverID = data.get("driverID");
//                Toast.makeText(MyFirebaseMessaging.this, "arrival: " + message, Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getBaseContext(), NotifyArrive.class);
//                intent.putExtra("msg", message);
//                intent.putExtra("driverID", driverID);
//                startActivity(intent);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                    showArrivedNotificationApi26(message);
                else
                    showArrivedNotification(message);
            }
            else if (title.equals("Bajar"))
            {
                openRateActivity(message);
            }
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationApi26(String body) {
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper=new NotificationHelper(getBaseContext());


        Notification.Builder builder=notificationHelper.getUberNotification("Llego",body,contentIntent,defaultSound);
        notificationHelper.getManager().notify(1,builder.build());


    }

    private void openRateActivity(String body) {

        Intent intent=new Intent(this, RateActivity.class);
        intent.putExtra("driverID", Common.driverId);
        intent.putExtra("state", "");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void showArrivedNotification(String body) {
        //this code only work Android API 25 and below
        //From Android API 26 or higher you need create Notification Channel
        //I have publish tutorial about this content you can watch on my channel
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(android.app.Notification.DEFAULT_LIGHTS| android.app.Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_car)
                .setContentTitle("LLEGO")
                .setContentText(body)
                .setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());
    }

}
