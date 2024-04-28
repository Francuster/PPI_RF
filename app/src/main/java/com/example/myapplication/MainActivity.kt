package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCamara = findViewById<ImageButton>(R.id.botonIngresar)

        //Evento al presionar el botón
        btnCamara.setOnClickListener {
            startForResult.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE))
        }
    }

    //Evento que procesa el resultado de la cámara y reemplaza el icono por la foto (solo como prueba)

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val imageBitmap = intent?.extras?.get("data") as Bitmap

                //imageBitMap es la imagen obtenida, luego de esto abajo se reemplaza la imagen del icono por la foto...

                val imageView = findViewById<ImageView>(R.id.imageView)
                imageView.setImageBitmap(imageBitmap)
            }
        }
}