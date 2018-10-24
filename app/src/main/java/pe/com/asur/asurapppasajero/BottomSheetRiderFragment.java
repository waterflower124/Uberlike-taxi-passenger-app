package pe.com.asur.asurapppasajero;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pe.com.asur.asurapppasajero.Common.Common;
import pe.com.asur.asurapppasajero.Remote.IGoogleAPI;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BottomSheetRiderFragment extends BottomSheetDialogFragment {
    String mLocation,mDestination;

    boolean isTapOnMap;

    IGoogleAPI mService;

    TextView txtCalculate,txtLocation,txtDestination;

    public static BottomSheetRiderFragment newInstance(String location,String destination,boolean isTapOnMap)
    {
        BottomSheetRiderFragment f = new BottomSheetRiderFragment();
        Bundle args = new Bundle();
        args.putString("location",location);
        args.putString("destination",destination);
        args.putBoolean("isTapOnMap",isTapOnMap);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = getArguments().getString("location");
        mDestination = getArguments().getString("destination");
        isTapOnMap = getArguments().getBoolean("isTapOnMap");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_rider,container,false);
        txtLocation = (TextView)view.findViewById(R.id.txtLocation);
        txtDestination = (TextView)view.findViewById(R.id.txtDestination);
        txtCalculate = (TextView)view.findViewById(R.id.txtCalculate);

        mService = Common.getGoogleService();
        getPrice(mLocation,mDestination);

        //set Data
        if (!isTapOnMap) {
            //Call this fragment from place autocomplete text view
            txtLocation.setText(mLocation);
            txtDestination.setText(mDestination);
        }
        return view;
    }

    private void getPrice(String mLocation, String mDestination) {
        String requestUrl=null;

        try {
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"
                    +"transit_routing_preference=less_driving&"
                    +"origin="+mLocation+"&"
                    +"destination="+mDestination+"&"        // abajo el sensor=true le puse para probar pero sin sensor = true tambien me trabaja
                    +"key=AIzaSyBQFCqY7afcjleEKi0YRlv1XHBKRxn8pxE&sensor=true";// el google place service usa la llave de servidor del listado de opciones https://stackoverflow.com/questions/21933247/this-ip-site-or-mobile-application-is-not-authorized-to-use-this-api-key/
                  //  +"key="+getResources().getString(R.string.google_browser_key);
            Log.e("LINK",requestUrl);  //print url for
            mService.getPath(requestUrl).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    //Get Object
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        //get distance
                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_text = distance.getString("text");
                        //Use regex to extract double from string
                        //This regex will remove all text not is digit
                        Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));

                        //Get Time
                        JSONObject time = legsObject.getJSONObject("duration");
                        String time_text = time.getString("text");
                        Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+",""));

                        String final_caculate = String.format("%s + %s = S/.%.2f",distance_text,time_text,
                                Common.getPrice(distance_value,time_value));

                        txtCalculate.setText(final_caculate);

                        if (isTapOnMap)
                        {
                            String start_address = legsObject.getString("start_address");
                            String end_address = legsObject.getString("end_address");

                            txtLocation.setText(start_address);
                            txtDestination.setText(end_address);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }



                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("ERROR",t.getMessage());
                }
            });
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
