package pe.com.asur.asurapppasajero;

import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import pe.com.asur.asurapppasajero.Common.Common;
import pe.com.asur.asurapppasajero.Model.Rate;

public class RateActivity extends AppCompatActivity {

    Button btnSubmit;
    MaterialRatingBar ratingBar;
    MaterialEditText edtComment;

    FirebaseDatabase database;
    DatabaseReference rateDetailRef;
    DatabaseReference driverInformationRef;

    String driverId;
    String state;


    double ratingStars=0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        ViewDriverInfo.getInstance().finish();

//        driverId = Common.driverId;
        driverId = getIntent().getStringExtra("driverID");
        state = getIntent().getStringExtra("state");
        Common.driverId = "";

        database=FirebaseDatabase.getInstance();
        rateDetailRef =database.getReference(Common.rate_detail_tbl);
        driverInformationRef=database.getReference(Common.user_driver_tbl);

        btnSubmit=(Button)findViewById(R.id.btnSubmit);
        ratingBar=(MaterialRatingBar)findViewById(R.id.ratingBar);
        edtComment=(MaterialEditText)findViewById(R.id.edtComment);

        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                ratingStars=rating;
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubmitRateDetails(driverId);
//                Intent intent = new Intent(RateActivity.this, Home.class);
//                startActivity(intent);
            }
        });

    }

    private void SubmitRateDetails(final String driverId) {
        final android.app.AlertDialog alertDialog=new SpotsDialog(this);
        alertDialog.show();

        Common.isDriverFound = false;

//        if(state.equals("old_order"))
            Common.old_order = false;

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference current_order = db.getReference(Common.register_current_order);
        current_order.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();


        Rate rate=new Rate();
        rate.setRates(String.valueOf(ratingStars));
        rate.setComment(edtComment.getText().toString());

        //Update to firebase
        rateDetailRef.child(driverId)
          //no va esta linea teng que borrarla  .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .push()//get Unique Key
            .setValue(rate)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //if Upload succedd on firebase  calculate average and update infomation driver
                    rateDetailRef.child(driverId)
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
                                double finalAverage = averageStars / count;
                              //  DecimalFormat df=new DecimalFormat("#.#");
                                String valueUpdate=String.valueOf(finalAverage);
                                //Create Objet update;
                                Map<String,Object>driverUpdateRate=new HashMap<>();
                                driverUpdateRate.put("rates",valueUpdate);
                                driverInformationRef.child(driverId)
                                        .updateChildren(driverUpdateRate)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                alertDialog.dismiss();
                                                Toast.makeText(RateActivity.this, "Gracias..", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RateActivity.this, Home.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                alertDialog.dismiss();
                                                Toast.makeText(RateActivity.this, "Rate actualizado pero no se puedo escribir en información de Chofer", Toast.LENGTH_SHORT).show();
                                            }
                                        });




                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                }
            });

    }
}
