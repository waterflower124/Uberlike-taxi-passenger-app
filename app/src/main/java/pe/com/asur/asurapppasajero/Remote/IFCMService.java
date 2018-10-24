package pe.com.asur.asurapppasajero.Remote;

import pe.com.asur.asurapppasajero.Model.DataMessage;
import pe.com.asur.asurapppasajero.Model.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by fumon_000 on 02/03/2018.
 */

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAFJXfFds:APA91bERNVmgnlnJdmKIYihMUbnMV47zYyxK9WVZ8dHObtfPq41yZaO4w5Xy6joyLMVkrCOPHLgw1-HPP9_t8-CRA3B1v0EHJeJPIvidTZ3zzLoRfUGoCuQVwTJE27gn_vBjghv-D6Gc"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body DataMessage body);
}