package com.wh.uni_pusher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wh.uni_pusher_client_lib.UniPusherClient
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        thread {
            UniPusherClient.test()
        }.run()
    }
}