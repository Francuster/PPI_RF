package com.example.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfiguracionRRHHActivity extends AppCompatActivity {

    private SeekBar seekBar;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuracion_rrhh);

        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.certeza_configuracion);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://tu-api-base-url.com") // Reemplaza con la URL base de tu API
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(" " +progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No necesitas hacer nada aquí
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No necesitas hacer nada aquí
            }
        });
    }

    public void goBackInicioRRHH(View view) {
        Intent intent = new Intent(getApplicationContext(), InicioRrHhActivity.class);
        startActivity(intent);
    }
}