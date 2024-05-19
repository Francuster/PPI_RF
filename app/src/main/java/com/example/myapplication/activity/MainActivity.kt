package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.deviceIsConnected
import com.example.myapplication.utils.isServiceRunning


class MainActivity: AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if(!isServiceRunning(applicationContext, NetworkChangeService ::class.java)){
                val intent = Intent(this, NetworkChangeService::class.java)
                startService(intent)
            }
            setContentView(R.layout.inico_rrhh)


        }

    fun Siguiente(view: View) {

        if (deviceIsConnected(applicationContext)) {
            val intent = Intent(applicationContext, CameraLoginActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(applicationContext, CameraxActivity::class.java)
            startActivity(intent)
        }

    }



}




