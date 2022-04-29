package com.promiseek.cloudo.RegistationAndLogin.CountryList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.promiseek.cloudo.R;

public class Countries extends AppCompatActivity {
    Toolbar toolbar;
    ListView countriesList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countries);
        toolbar = findViewById(R.id.toolbar);
        countriesList = findViewById(R.id.countries_list);

        toolbar.setTitle("Country");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));

        CountryAdapter countryAdapter = new CountryAdapter(this);
        countriesList.setAdapter(countryAdapter);

        countriesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView name = (TextView) ((RelativeLayout) view).getChildAt(1);
                TextView code = (TextView) ((RelativeLayout) view).getChildAt(2);
                Intent intent = new Intent();
                intent.putExtra("name", name.getText().toString());
                intent.putExtra("code", code.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

    }
}