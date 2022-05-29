/*
Fragment used for capturing the attendance of the registered users
 */

package xyz.prathamgandhi.copx;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.prathamgandhi.copx.APIInterface.RetrofitAttendanceAPI;
import xyz.prathamgandhi.copx.models.MarkAttendanceModel;
import xyz.prathamgandhi.copx.models.MarkAttendanceResponseModel;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class AttendanceFragment extends Fragment {


    RetrofitAttendanceAPI retrofitAttendanceAPI;

    FrameLayout cameraFrameLayout;

    PreviewView previewView;
    View view;
    PopupWindow popupWindow;
    Button capturePhoto;
    ExecutorService cameraExecutor;
    ImageCapture imageCapture;
    ProcessCameraProvider cameraProvider;

    FaceDetector faceDetector;

    DrawBoundingBox drawBoundingBox;


    public AttendanceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        // This call simply informs the server that attendance requests are coming and to load the data in memory
        Call<ResponseBody> call = retrofitAttendanceAPI.startAttendance();


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_attendance, container, false);

        Button captureAttendance = view.findViewById(R.id.mark_attendance_button), fetchAttendance = view.findViewById(R.id.get_attendance_button);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(StringConstants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        retrofitAttendanceAPI = retrofit.create(RetrofitAttendanceAPI.class);
        cameraFrameLayout = view.findViewById(R.id.camera_frame_layout);
        cameraFrameLayout.setVisibility(View.GONE);
        previewView = view.findViewById(R.id.viewFinder);
        capturePhoto = view.findViewById(R.id.capturePhoto);

        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();

        faceDetector = FaceDetection.getClient(options);

        drawBoundingBox = view.findViewById(R.id.attendance_bounding_box);

        fetchAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Call<ResponseBody> fetchCall = retrofitAttendanceAPI.fetchAttendance();

                fetchCall.enqueue(new Callback<ResponseBody>() {
                    @RequiresApi(api = Build.VERSION_CODES.R)
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            File path = Environment.getExternalStorageDirectory();
                            File file = new File(path, "Attendance.xlsx");
                            if (!Environment.isExternalStorageManager()) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                startActivity(intent);
                                return;
                            }
                            file.createNewFile();
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        fileOutputStream.write(response.body().bytes());
                                        fileOutputStream.close();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            Toast.makeText(getActivity(), "Attendance sheet downloaded", Toast.LENGTH_SHORT).show();
                            t.start();
                            t.join();
                            response.body().close();
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        System.out.println(t.getMessage());
                    }
                });

            }
        });

        captureAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera();
                cameraFrameLayout.setVisibility(View.VISIBLE);
                captureAttendance.setVisibility(View.GONE);
                fetchAttendance.setVisibility(View.GONE);
            }
        });

        capturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageCapture == null){
                    Toast.makeText(getActivity(), "failed...", Toast.LENGTH_SHORT).show();
                }
                imageCapture.takePicture(ContextCompat.getMainExecutor(getActivity()), new ImageCapture.OnImageCapturedCallback() {
                    @SuppressLint("UnsafeOptInUsageError")
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
                        Bitmap bmp = Utility.toBitmap(image.getImage());
                        showPopup(bmp);
                        image.close();
                        capturePhoto.setVisibility(View.GONE);
                    }
                });
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();



        return view;
    }

    private void showPopup(Bitmap bmp) {
        LayoutInflater inflater = (LayoutInflater)
                getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.photo_check_popup, null);

        // create the popup window
        FrameLayout frameLayout = view.findViewById(R.id.camera_frame_layout);

        int width = (int) (frameLayout.getWidth() * 0.8);
        int height = (int) (frameLayout.getHeight());
        boolean focusable = false; // avoids dismissal of popup by clicking outside

        popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window at the frameLayout view
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(frameLayout, Gravity.CENTER, 0, 0);


        ImageView imageView = popupView.findViewById(R.id.checkImage);
        imageView.setImageBitmap(bmp);

        AppCompatButton rejectButton = popupView.findViewById(R.id.reject_button);
        AppCompatButton acceptButton = popupView.findViewById(R.id.accept_button);
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                capturePhoto.setVisibility(View.VISIBLE);

            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ByteArrayOutputStream Baos = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 5, Baos);
                        byte[] byteArray = Baos.toByteArray();
                        String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        markAttendance(base64Image);
                    }
                });
                thread.start();
                try {
                    thread.join();
                    capturePhoto.setVisibility(View.VISIBLE);
                    popupWindow.dismiss();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    // An API request to mark the attendance of the user whose image is captured
    private void markAttendance(String base64Image){
        MarkAttendanceModel markAttendanceModel = new MarkAttendanceModel(base64Image);
        Call<MarkAttendanceResponseModel> call = retrofitAttendanceAPI.markAttendance(markAttendanceModel);

        call.enqueue(new Callback<MarkAttendanceResponseModel>() {
            @Override
            public void onResponse(Call<MarkAttendanceResponseModel> call, Response<MarkAttendanceResponseModel> response) {
                if(response.code() == 201) {
                    System.out.println(response.body());
                    String firstName = response.body().getFirstName();
                    String lastName = response.body().getLastName();
                    Toast.makeText(getActivity(), "Attendance marked for " + firstName + " " + lastName, Toast.LENGTH_SHORT).show();
                }
                else if(response.code() == 404){
                    Toast.makeText(getActivity(), "User not found", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getActivity(), "Attendance could not be marked", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MarkAttendanceResponseModel> call, Throwable t) {
                Toast.makeText(getActivity(), "Some error occurred \n" + t.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println(t.getMessage() + " " + t.getStackTrace());
            }
        });
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getActivity());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().setTargetRotation(getActivity().getWindowManager().getDefaultDisplay().getRotation()).build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                imageAnalysis.setAnalyzer(cameraExecutor, new BoundingBoxFrameAnalyzer(faceDetector, drawBoundingBox, BoundingBoxFrameAnalyzer.BACK));

                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);
                } catch (Exception e) {
                    Log.e("Attendance Fragment", "Use case binding failed", e);
                }


            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getActivity()));
    }
}