package com.gramzin.lesson

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (actionBar != null){
            actionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
            }
        }
    }
}