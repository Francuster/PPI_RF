package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.activity.CameraxActivity
import com.example.myapplication.utils.NetworkChangeService


class MainActivity: AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if(!isServiceRunning(applicationContext, NetworkChangeService ::class.java)){
                val intent = Intent(this, NetworkChangeService::class.java)
                startService(intent)
            }
            setContentView(R.layout.activity_main)


        }

    fun Siguiente(view: View) {

        if(deviceIsConnected(applicationContext)){
            val intent = Intent(applicationContext, CameraLoginActivity::class.java)
            startActivity(intent)
        }else{
            val intent = Intent(applicationContext, CameraxActivity::class.java)
            startActivity(intent)
        }



    }



}




