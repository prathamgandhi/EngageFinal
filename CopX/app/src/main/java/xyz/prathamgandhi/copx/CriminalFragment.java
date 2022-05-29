/*
The fragment used for identification of criminals from the database
Contains both registration and check methods
 */

package xyz.prathamgandhi.copx;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.prathamgandhi.copx.APIInterface.RetrofitCriminalAPI;
import xyz.prathamgandhi.copx.models.CriminalCheckModel;
import xyz.prathamgandhi.copx.models.CriminalCheckResponseModel;
import xyz.prathamgandhi.copx.models.CriminalRegisterModel;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class CriminalFragment extends Fragment {

    final int REGISTER = -1, CHECK = 1, NONE = 0; // The states in which the camera capture button could be in
    int state;

    View view;

    RetrofitCriminalAPI retrofitCriminalAPI;
    FrameLayout cameraFrameLayout;

    PreviewView previewView;
    Button capturePhoto;
    ExecutorService cameraExecutor;
    ImageCapture imageCapture;
    ProcessCameraProvider cameraProvider;

    ProgressBar spinningProgressBar;

    FaceDetector faceDetector;
    String firstName, lastName, age, crime;
    DrawBoundingBox drawBoundingBox;
    Button registerCriminal, checkCriminal;
    public CriminalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_criminal, container, false);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(StringConstants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        retrofitCriminalAPI = retrofit.create(RetrofitCriminalAPI.class);


        cameraFrameLayout = view.findViewById(R.id.camera_frame_layout);
        cameraFrameLayout.setVisibility(View.GONE);
        previewView = view.findViewById(R.id.viewFinder);
        capturePhoto = view.findViewById(R.id.capturePhoto);
        spinningProgressBar = view.findViewById(R.id.spinner);

        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();

        faceDetector = FaceDetection.getClient(options);

        drawBoundingBox = view.findViewById(R.id.boundingbox);

        registerCriminal = view.findViewById(R.id.registerCriminal);
        checkCriminal = view.findViewById(R.id.checkCriminal);

        // Set a listener on the capture Photo, different actions are taken depending upon its state
        capturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageCapture == null) {
                    Toast.makeText(getActivity(), "failed...", Toast.LENGTH_SHORT).show();
                    return;
                }

                imageCapture.takePicture(ContextCompat.getMainExecutor(getActivity()), new ImageCapture.OnImageCapturedCallback() {
                    @SuppressLint("UnsafeOptInUsageError")
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
                        Bitmap bmp = Utility.toBitmap(image.getImage());
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ByteArrayOutputStream Baos = new ByteArrayOutputStream();
                                bmp.compress(Bitmap.CompressFormat.JPEG, 5, Baos);
                                byte[] byteArray = Baos.toByteArray();
                                String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                if(state == REGISTER){
                                    uploadRegistrationData(base64Image, firstName, lastName, age, crime);
                                }
                                else {
                                    criminalCheckUpload(base64Image, bmp);
                                }
                            }
                        });
                        thread.start();
                        image.close();
                        capturePhoto.setVisibility(View.GONE);
                        spinningProgressBar.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        registerCriminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                registerCriminal.setVisibility(View.GONE);
                checkCriminal.setVisibility(View.GONE);
                state = REGISTER;
                showRegistrationPopup();
            }
        });

        checkCriminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraFrameLayout.setVisibility(View.VISIBLE);
                registerCriminal.setVisibility(View.GONE);
                checkCriminal.setVisibility(View.GONE);

                state = CHECK;

                startCamera();
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();

        return view;
    }

    private void criminalCheckUpload(String base64Image, Bitmap bmp){
        CriminalCheckModel criminalCheckModel = new CriminalCheckModel(base64Image);
        Call<CriminalCheckResponseModel> call = retrofitCriminalAPI.checkCriminal(criminalCheckModel);
        call.enqueue(new Callback<CriminalCheckResponseModel>() {
            @Override
            public void onResponse(Call<CriminalCheckResponseModel> call, Response<CriminalCheckResponseModel> response) {
                spinningProgressBar.setVisibility(View.GONE);
                capturePhoto.setVisibility(View.VISIBLE);
                cameraFrameLayout.setVisibility(View.GONE);
                registerCriminal.setVisibility(View.VISIBLE);
                checkCriminal.setVisibility(View.VISIBLE);

                // show a popup with the details of the criminal found
                if(response.code() == 200){
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.criminal_result_popup, null);
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int width = (int) (displayMetrics.widthPixels * 0.65);
                    int height = (int) (displayMetrics.heightPixels * 0.65);
                    boolean focusable = true;
                    PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                    popupWindow.showAtLocation(view.findViewById(R.id.camera_frame_layout), Gravity.CENTER, 0, 0);
                    TextView firstName = popupView.findViewById(R.id.first_name);
                    TextView lastName = popupView.findViewById(R.id.last_name);
                    TextView age = popupView.findViewById(R.id.age);
                    TextView crime = popupView.findViewById(R.id.crime);
                    firstName.setText("First Name : " + response.body().getFirst_name());
                    lastName.setText("Last Name : " + response.body().getLast_name());
                    age.setText("Age : " + response.body().getAge());
                    crime.setText("Crimes committed : " + response.body().getCrime());
                }

            }

            @Override
            public void onFailure(Call<CriminalCheckResponseModel> call, Throwable t) {

            }
        });
    }
    // Show a popup window for registration action
    private void showRegistrationPopup(){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.register_crimlost_popup, null);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = (int) (displayMetrics.widthPixels * 0.65);
        int height = (int) (displayMetrics.heightPixels * 0.65);
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view.findViewById(R.id.camera_frame_layout), Gravity.CENTER, 0, 0);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getX() < 0 || motionEvent.getX() > popupView.getWidth()) return true;
                if (motionEvent.getY() < 0 || motionEvent.getY() > popupView.getHeight()) return true;

                return false;
            }
        });
        EditText firstNameEditText = popupView.findViewById(R.id.first_name),
                lastNameEditText = popupView.findViewById(R.id.last_name),
                ageEditText = popupView.findViewById(R.id.age),
                crimeEditText = popupView.findViewById(R.id.misc);

        Button registerSubmit = popupView.findViewById(R.id.submitforregister), cancelButton = popupView.findViewById(R.id.cancel);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                registerCriminal.setVisibility(View.VISIBLE);
                checkCriminal.setVisibility(View.VISIBLE);
                state = NONE;
            }
        });
        registerSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstName = firstNameEditText.getText().toString().trim();
                lastName = lastNameEditText.getText().toString().trim();
                age = ageEditText.getText().toString().trim();
                crime = crimeEditText.getText().toString().trim();
                if(firstName.length() > 0 && lastName.length() > 0 && age.length() > 0 && crime.length() > 0){
                    popupWindow.dismiss();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startCamera();
                            cameraFrameLayout.setVisibility(View.VISIBLE);
                            registerCriminal.setVisibility(View.GONE);
                            checkCriminal.setVisibility(View.GONE);

                        }
                    }, 200);
                }
                else{
                    Toast.makeText(getActivity(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void uploadRegistrationData(String base64Image, String firstName, String lastName, String age, String crime) {

        CriminalRegisterModel criminalRegisterModel = new CriminalRegisterModel(firstName, lastName, crime, age, base64Image);
        Call<CriminalRegisterModel> call = retrofitCriminalAPI.registerCriminal(criminalRegisterModel);
        call.enqueue(new Callback<CriminalRegisterModel>() {
            @Override
            public void onResponse(Call<CriminalRegisterModel> call, Response<CriminalRegisterModel> response) {
                if (response.code() == 201) {
                    Toast.makeText(getActivity(), "Registered Successfully", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getActivity(), "Some error occurred", Toast.LENGTH_SHORT).show();
                }
                spinningProgressBar.setVisibility(View.GONE);
                capturePhoto.setVisibility(View.VISIBLE);
                cameraFrameLayout.setVisibility(View.GONE);
                registerCriminal.setVisibility(View.VISIBLE);
                checkCriminal.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<CriminalRegisterModel> call, Throwable t) {
                Toast.makeText(getActivity(), "Upload failed", Toast.LENGTH_SHORT).show();
                spinningProgressBar.setVisibility(View.GONE);
                capturePhoto.setVisibility(View.VISIBLE);
                cameraFrameLayout.setVisibility(View.GONE);
                registerCriminal.setVisibility(View.VISIBLE);
                checkCriminal.setVisibility(View.VISIBLE);
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
                    Log.e("Criminal Fragment", "Use case binding failed", e);
                }


            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getActivity()));
    }
}