package com.promiseek.cloudo.RegistationAndLogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.promiseek.cloudo.MainActivity;
import com.promiseek.cloudo.R;
import com.promiseek.cloudo.RegistationAndLogin.CountryList.Countries;

import org.drinkless.td.libcore.telegram.TdApi;

import static com.promiseek.cloudo.MainActivity.client;

public class Login extends AppCompatActivity {
    TextView country_Code;
    LinearLayout country_Code_LinearLayout;
    EditText mobile_number;
    Button generate_otp;
    public static String phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        getSupportActionBar().hide();
        country_Code = findViewById(R.id.country_Code);
        country_Code_LinearLayout = findViewById(R.id.country_Code_LinearLayout);
        mobile_number = findViewById(R.id.mobile_number);
        generate_otp = findViewById(R.id.generate_otp);



        country_Code_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Countries.class);
                startActivityForResult(intent, RESULT_FIRST_USER);
            }
        });

        generate_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doRegister();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            country_Code.setText(data.getStringExtra("code"));
        }
    }

    private void doRegister() {
        phone = mobile_number.getText().toString();
        if (!phone.equals("")) {
            //add a progress bar
            client.send(new TdApi.SetAuthenticationPhoneNumber(country_Code.getText().toString()+phone, null),
                    new MainActivity.AuthorizationRequestHandler(Login.this));
        } else {
            reportWrongNumberError();
        }
    }

    private void reportWrongNumberError() {
        Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
    }
}