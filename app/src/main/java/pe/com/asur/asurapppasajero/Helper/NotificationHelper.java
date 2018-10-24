package pe.com.asur.asurapppasajero.Helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import pe.com.asur.asurapppasajero.R;

/**
 * Created by agus on 20/03/2018.
 */

public class NotificationHelper extends ContextWrapper {

    private static final String EDMT_CHANNEL_ID="pe.com.asur.asurapppasajero.EDMTDEV";
    private static final String EDMT_CHANNEL_NAME="EDMTDEV uber";
    private NotificationManager manager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        createChannels();
        
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel edmtChannels=new NotificationChannel(EDMT_CHANNEL_ID,EDMT_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        edmtChannels.enableLights(true);
        edmtChannels.enableVibration(true);
        edmtChannels.setLightColor(Color.GRAY);
        edmtChannels.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(edmtChannels);

    }

    public NotificationManager getManager() {
        if(manager==null)
            manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            return manager;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public  Notification.Builder getUberNotification(String title, String content, PendingIntent contentIntent, Uri defaultSound){
        return new Notification.Builder(getApplicationContext(),EDMT_CHANNEL_ID)
                .setContentText(content)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_car);
    }

}
