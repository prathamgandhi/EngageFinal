package xyz.prathamgandhi.copx.APIInterface;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import xyz.prathamgandhi.copx.models.LoginModel;
import xyz.prathamgandhi.copx.models.RegisterModel;

public interface RetrofitRegisterAPI {

    @POST("attendance/register/")
    Call<RegisterModel> registerPost(@Body RegisterModel registerModel);

    @POST("attendance/login/")
    Call<LoginModel> loginPost(@Body LoginModel loginModel);
}
