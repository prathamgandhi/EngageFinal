package xyz.prathamgandhi.copx.APIInterface;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import xyz.prathamgandhi.copx.models.LostCheckModel;
import xyz.prathamgandhi.copx.models.LostCheckResponseModel;
import xyz.prathamgandhi.copx.models.LostRegisterModel;

public interface RetrofitLostAPI {
    @POST("/lost/register_lost")
    Call<LostRegisterModel> registerLost(@Body LostRegisterModel lostRegisterModel);

    @POST("/lost/check_lost")
    Call<LostCheckResponseModel> checkLost(@Body LostCheckModel lostCheckModel);
}
