package xyz.prathamgandhi.copx.APIInterface;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import xyz.prathamgandhi.copx.models.MarkAttendanceModel;
import xyz.prathamgandhi.copx.models.MarkAttendanceResponseModel;

public interface RetrofitAttendanceAPI {

    @GET("/attendance/get_attendance/")
    @Streaming
    Call<ResponseBody> fetchAttendance();

    @GET("/attendance/start_attendance/")
    Call<ResponseBody> startAttendance();

    @POST("/attendance/mark_attendance/")
    Call<MarkAttendanceResponseModel> markAttendance(@Body MarkAttendanceModel markAttendanceModel);

}
