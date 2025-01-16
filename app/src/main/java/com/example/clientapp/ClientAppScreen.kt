package com.example.clientapp

import android.annotation.SuppressLint
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClientAppScreen : ComponentActivity() {
    private lateinit var contentObserver: ContentObserver

    companion object {
        const val PROVIDER_URI = "content://com.example.senderapp.secureprovider/data"
        const val COLUMN_INDEX = "data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val receivedMessage = mutableStateOf("")
        setContent {
            ClientAppView(receivedMessage.value)
        }

        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            @SuppressLint("Range")
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                CoroutineScope(Dispatchers.IO).launch {
                    val cursor = contentResolver.query(
                        Uri.parse(PROVIDER_URI),
                        null, null, null, null
                    )
                    cursor?.use {
                        if (it.moveToFirst()) {
                            receivedMessage.value = it.getString(it.getColumnIndex(COLUMN_INDEX))
                        }
                    }
                }
            }
        }
        contentResolver.registerContentObserver(
            Uri.parse(PROVIDER_URI),
            true,
            contentObserver
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)
    }
}