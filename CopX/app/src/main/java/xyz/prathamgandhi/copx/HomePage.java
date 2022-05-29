/*
The primary home activity where the user can interact with the various features of the app
 */


package xyz.prathamgandhi.copx;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
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
import xyz.prathamgandhi.copx.APIInterface.RetrofitRegisterAPI;
import xyz.prathamgandhi.copx.models.LoginModel;

public class HomePage extends AppCompatActivity {

    private static final String TAG = "HomePage";
    BottomNavigationView bottomNavigationView;
    PopupWindow popupWindow;

    // Initialize all the fragments that will be used in the app
    HomeFragment homeFragment = new HomeFragment();
    CriminalFragment criminalFragment = new CriminalFragment();
    AttendanceFragment attendanceFragment = new AttendanceFragment();


    // The various views and variables defined globally to be used across the home page
    PreviewView previewView;
    ExecutorService cameraExecutor;
    ImageCapture imageCapture;
    ProcessCameraProvider cameraProvider;

    FaceDetector faceDetector;

    DrawBoundingBox drawBoundingBox;

    Button signInCapureButton;
    String firstName, lastName, passWord, phone;
    Bitmap bmp;

    ProgressBar spinningProgressBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        cameraExecutor = Executors.newSingleThreadExecutor();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Show the popup for sign in, user can proceed ahead without signing in
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showSigninPopup();
            }
        }, 200); // The delay is used because otherwise it is possible that the camera might not have been initialized earlier

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, homeFragment).commit();
                        return true;
                    case R.id.attendance:
                        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, attendanceFragment).commit();
                        return true;
                    case R.id.criminals:
                        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, criminalFragment).commit();
                        return true;
                }
                return false;
            }
        });


        // Using the MLKit face detector to draw a bounding box around the user
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();

        faceDetector = FaceDetection.getClient(options);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
        }
        this.finish();
    }

    private void showSigninPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.signin_popup_window, null);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = (int) (displayMetrics.widthPixels * 0.65);
        int height = (int) (displayMetrics.heightPixels * 0.65);
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(findViewById(R.id.flFragment), Gravity.CENTER, 0, 0);

        // Used to implement the feature of not closing the popup window on tapping outside the window
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getX() < 0 || motionEvent.getX() > popupView.getWidth()) return true;
                if (motionEvent.getY() < 0 || motionEvent.getY() > popupView.getHeight()) return true;

                return false;
            }
        });
        EditText firstNameEditText = popupView.findViewById(R.id.first_name_signin),
        lastNameEditText = popupView.findViewById(R.id.last_name_signin),
        phoneNumEditText = popupView.findViewById(R.id.phone_signin),
        passWordEditText = popupView.findViewById(R.id.password_signin);

        Button signInButton = popupView.findViewById(R.id.submitforsignin), registerButton = popupView.findViewById(R.id.register);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstName = firstNameEditText.getText().toString().trim();
                lastName = lastNameEditText.getText().toString().trim();
                phone = phoneNumEditText.getText().toString().trim();
                passWord = passWordEditText.getText().toString().trim();
                if(firstName.length() > 0 && lastName.length() > 0 && phone.length() > 0 && passWord.length() > 0){
                    popupWindow.dismiss();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showCameraPopup();
                            startCamera();
                            signInCapureButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(imageCapture == null) {
                                        Toast.makeText(HomePage.this, "failed...", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    imageCapture.takePicture(ContextCompat.getMainExecutor(HomePage.this), new ImageCapture.OnImageCapturedCallback() {
                                        @SuppressLint("UnsafeOptInUsageError")
                                        @Override
                                        public void onCaptureSuccess(@NonNull ImageProxy image) {
                                            super.onCaptureSuccess(image);
                                            bmp = Utility.toBitmap(image.getImage());
                                            // flip the image because of use of front camera
                                            Matrix matrix = new Matrix();
                                            matrix.postScale(-1, 1, bmp.getWidth() / 2f, bmp.getHeight() / 2f);
                                            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                                            Thread thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ByteArrayOutputStream Baos = new ByteArrayOutputStream();
                                                    bmp.compress(Bitmap.CompressFormat.JPEG, 5, Baos);
                                                    byte[] byteArray = Baos.toByteArray();
                                                    String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                                    uploadLoginData(base64Image, firstName, lastName, passWord, phone);
                                                }
                                            });
                                            thread.start();
                                            image.close();
                                            signInCapureButton.setVisibility(View.GONE);
                                            spinningProgressBar.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            });

                        }
                    }, 200);
                }
                else{
                    Toast.makeText(HomePage.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private void showCameraPopup() {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = (int) (displayMetrics.widthPixels * 0.65);
        int height = (int) (displayMetrics.heightPixels * 0.65);
        boolean focusable = false; // avoids dismissal of popup by clicking outside
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window at the fragment frame view
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(findViewById(R.id.flFragment), Gravity.CENTER, 0, 0);
        previewView = popupView.findViewById(R.id.viewFinder);
        drawBoundingBox = popupView.findViewById(R.id.boundingbox);

        signInCapureButton = popupView.findViewById(R.id.capturePhotoSignIn);

        spinningProgressBar = popupView.findViewById(R.id.spinner);
        spinningProgressBar.setVisibility(View.GONE);

    }


    // Upload the login data with the help of retrofit library
    private void uploadLoginData(String imageBase64, String firstName, String lastName, String password, String phone){
        Retrofit retrofit = new Retrofit.Builder().baseUrl(StringConstants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();

        RetrofitRegisterAPI registerAPI = retrofit.create(RetrofitRegisterAPI.class);

        LoginModel loginModel = new LoginModel(imageBase64, firstName, lastName, password, phone);

        Call<LoginModel> call = registerAPI.loginPost(loginModel);

        call.enqueue(new Callback<LoginModel>() {
            @Override
            public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                if(response.code() == 202) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HomePage.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                            popupWindow.dismiss();
                        }
                    });
                }
                else if(response.code() == 404) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HomePage.this, "No such user found, please register", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HomePage.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                            signInCapureButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<LoginModel> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(HomePage.this, "Something failed", Toast.LENGTH_SHORT).show();
                        System.out.println(t.getMessage());
                    }
                });
            }
        });
    }

    // The use of cameraX library to operate the phone camera, different use cases are used for analysis, preview and capture
    private void startCamera() {
        Toast.makeText(this, "Camera started...", Toast.LENGTH_SHORT).show();
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation()).build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                imageAnalysis.setAnalyzer(cameraExecutor, new BoundingBoxFrameAnalyzer(faceDetector, drawBoundingBox, BoundingBoxFrameAnalyzer.FRONT));

                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);
                }
                catch (Exception e){
                    Log.e(TAG, "Use case binding failed", e);
                }


            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }


}
