/*
The page used for registration purposes within the app, creates a new user on the server
 */

package xyz.prathamgandhi.copx;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        Button button = findViewById(R.id.submit);
        EditText first_name = findViewById(R.id.first_name),
                last_name = findViewById(R.id.last_name),
                password = findViewById(R.id.password),
                phone_num = findViewById(R.id.phone_num),
                station_id = findViewById(R.id.station_id);

        button.setOnClickListener(view -> {
            String firstName = first_name.getText().toString().trim(),
                    lastName = last_name.getText().toString().trim(),
                    passWord = password.getText().toString().trim(),
                    phoneNum = phone_num.getText().toString().trim(),
                    stationId = station_id.getText().toString().trim();

            // All the data is taken and passed to the next activity from where it is sent to the server along with the image
            if(firstName.length() > 0 && lastName.length() > 0 && passWord.length() > 0 && phoneNum.length() > 0 && stationId.length() > 0){
                Intent intent = new Intent(this, RegisterPhoto.class);
                intent.putExtra("firstName", firstName);
                intent.putExtra("lastName", lastName);
                intent.putExtra("passWord", passWord);
                intent.putExtra("phoneNum", phoneNum);
                intent.putExtra("stationId", stationId);
                startActivity(intent);
            }
            else {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });


    }
}