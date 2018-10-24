package pe.com.asur.asurapppasajero;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import pe.com.asur.asurapppasajero.Common.Common;
import pe.com.asur.asurapppasajero.Model.User;


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

public class NotifyArrive extends AppCompatActivity {

    TextView recv_message;
    String message, driverID;

    TextView driverName, driverPhone, driverRating;
    Button btnOk;
    CircleImageView imageAvatar;

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_arrive);

        driverName = (TextView) findViewById(R.id.txt_name);
        driverPhone = (TextView) findViewById(R.id.txt_phone);
        driverRating = (TextView) findViewById(R.id.txt_rate);
        imageAvatar = (CircleImageView) findViewById(R.id.avatar_image);
        btnOk = (Button) findViewById(R.id.btn_ok);

//        msg = (TextView)findViewById(R.id.message);

        recv_message = (TextView)findViewById(R.id.message);

        if(getIntent() != null) {
            message = getIntent().getStringExtra("msg");
            driverID = getIntent().getStringExtra("driverID");

//            mag.setText(message);
            loaddriverInfo(driverID);

            mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotifyArrive.this, Home.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loaddriverInfo(String driverId) {
        FirebaseDatabase.getInstance()
                .getReference(Common.user_driver_tbl)
                .child(driverId)//x0OzvFjpLpOTxbS9QbFGSkSyGzp1
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User driverUser = dataSnapshot.getValue(User.class);
//                        if (driverUser.getAvatarUrl() != null && !TextUtils.isEmpty(driverUser.getAvatarUrl())) {
//                            Picasso.with(getBaseContext())
//                                    .load(driverUser.getAvatarUrl())
//                                    .into(imageAvatar);
//                        } else {
//                            Picasso.with(getBaseContext())
//                                    .load(R.drawable.default_avatar)
//                                    .into(imageAvatar);
//                        }


                        recv_message.setText(message);
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

    @Override
    protected void onStop() {
        if(mediaPlayer.isPlaying())
            mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaPlayer!=null && !mediaPlayer.isPlaying())
            mediaPlayer.start();
    }

}
