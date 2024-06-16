package com.r1cc4rdo.cancello

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.r1cc4rdo.cancello.databinding.ActivityConfigurationBinding

class ConfigurationActivity : AppCompatActivity()
{
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityConfigurationBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        binding.configSave.setOnClickListener { saveConfigurations() }
        loadConfigurations()
    }

    private fun saveConfigurations()
    {
        var ok = true
        val components = listOf(binding.credsPin, binding.credsSerial, binding.credsPassword, binding.credsUsername)
        for (component in components)
            if (component.text.toString().isEmpty())
            {
                component.setBackgroundColor(Color.argb(200, 255, 128, 128))
                component.requestFocus()
                ok = false
            }

        if (!ok) return
        with(sharedPreferences.edit())
        {
            putString("username", binding.credsUsername.text.toString())
            putString("password", binding.credsPassword.text.toString())
            putString("deviceSerial", binding.credsSerial.text.toString())
            putString("devicePin", binding.credsPin.text.toString())
            putBoolean("autoClose", binding.autoClose.isChecked)
            apply()
        }
        finish()
    }

    private fun loadConfigurations()
    {
        binding.credsUsername.setText(sharedPreferences.getString("username", ""))
        binding.credsPassword.setText(sharedPreferences.getString("password", ""))
        binding.credsSerial.setText(sharedPreferences.getString("deviceSerial", ""))
        binding.credsPin.setText(sharedPreferences.getString("devicePin", ""))
        binding.autoClose.isChecked = sharedPreferences.getBoolean("autoClose", false)
    }
}
