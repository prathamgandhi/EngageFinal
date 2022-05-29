package xyz.prathamgandhi.copx;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class IPActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_p);

        EditText ipText = findViewById(R.id.ip_field);
        Button button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringConstants.BASE_URL = ipText.getText().toString();
                Intent intent = new Intent(IPActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}