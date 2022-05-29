package xyz.prathamgandhi.copx.APIInterface;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import xyz.prathamgandhi.copx.models.CriminalCheckModel;
import xyz.prathamgandhi.copx.models.CriminalCheckResponseModel;
import xyz.prathamgandhi.copx.models.CriminalRegisterModel;

public interface RetrofitCriminalAPI {
    @POST("/criminal/register_criminal/")
    Call<CriminalRegisterModel> registerCriminal(@Body CriminalRegisterModel criminalRegisterModel);

    @POST("/criminal/check_criminal/")
    Call<CriminalCheckResponseModel> checkCriminal(@Body CriminalCheckModel criminalCheckModel);
}
