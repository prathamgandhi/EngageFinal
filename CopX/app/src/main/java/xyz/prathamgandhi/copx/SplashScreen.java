/*

----------------------------------------------------------------------------------------------------

This code is written by Pratham Gandhi for the Microsoft Engage internship program under the mentor
Sapna Maid.

This project is a face_recognition project using java for the android project and python along wit
the Django framework for the backend.

The app has been designed with the law enforcement in mind. It demonstrates the application of face
recognition in the following three areas :
1. Device safety with face unlock as the second factor of authentication
2. Attendance marking system (for the various under officers present in a police station)
3. Searching a criminal database for a criminal by face

This file is the entry point of the program
----------------------------------------------------------------------------------------------------

 */

package xyz.prathamgandhi.copx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;


public class SplashScreen extends AppCompatActivity {
    // Display the splash screen for 500 ms
    private final int SPLASH_DISPLAY_LENGTH = 500;

    private final String[] REQUIRED_PERMISSIONS = new String[]{
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    private final int REQUEST_CODE_PERMISSIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);


        // Grant permissions if not granted, else open the activity for entering the IP Address
        if(allPermissionsGranted()) {
            Handler h = new Handler();
            h.postDelayed(() -> {
                Intent intent = new Intent(SplashScreen.this, IPActivity.class);
                SplashScreen.this.startActivity(intent);
                SplashScreen.this.finish();
            }, SPLASH_DISPLAY_LENGTH);
        }
        else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }


    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_PERMISSIONS) {
            if(allPermissionsGranted()) {
                Handler h = new Handler();
                h.postDelayed(() -> {
                    Intent intent = new Intent(SplashScreen.this, HomePage.class);
                    SplashScreen.this.startActivity(intent);
                    SplashScreen.this.finish();
                }, SPLASH_DISPLAY_LENGTH);

            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, permission, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;

    }
}