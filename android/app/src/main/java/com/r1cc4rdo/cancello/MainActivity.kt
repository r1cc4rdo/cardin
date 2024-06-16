package com.r1cc4rdo.cancello

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.util.Log
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.*
import java.io.IOException
import java.util.concurrent.TimeUnit

import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject

import com.r1cc4rdo.cancello.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var configurationLauncher: ActivityResultLauncher<Intent>
    private var bannerClicks = 0

    private fun log(message: String)
    {
        Log.d("MainActivity", message)
        runOnUiThread {
            binding.log.text = "${binding.log.text}\n$message"
            binding.logContainer.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun showLog()
    {
        binding.logContainer.visibility = View.VISIBLE
        binding.banner.visibility = View.GONE
        binding.result.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        try
        {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            configurationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {
                result -> run { sendCommand() }
            }
            val intent = Intent(this, ConfigurationActivity::class.java)

            val keys = listOf("username", "password", "deviceSerial", "devicePin")
            val hasConfig = keys.all { key -> !sharedPreferences.getString(key, null).isNullOrBlank()}
            if (!hasConfig) configurationLauncher.launch(intent)
            else sendCommand()

            binding.log.setOnClickListener {
                val shareIntent:Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, binding.log.text)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "@string/share_via"))
            }
            binding.config.setOnClickListener {
                configurationLauncher.launch(intent)
            }
            binding.send.setOnClickListener {
                sendCommand()
            }
            binding.banner.setOnClickListener {
                bannerClicks++
                if (bannerClicks == 5)
                {
                    Toast.makeText(this,"@string/log_mode", Toast.LENGTH_LONG).show()
                    showLog()
                }
            }
        }
        catch (e: Exception)
        {
            log("Error during onCreate: $e")
            showLog()
        }
    }

    private fun sendCommand()
    {
        log("Entering sendCommand")
        runOnUiThread {
            binding.config.isEnabled = false
            binding.send.isEnabled = false
            binding.spinner.visibility = View.VISIBLE
            binding.result.visibility = View.GONE
        }

        val user = sharedPreferences.getString("username", null)
        val pass = sharedPreferences.getString("password", null)
        val serial = sharedPreferences.getString("deviceSerial", null)
        val pin = sharedPreferences.getString("devicePin", null)

        val auth = Base64.encodeToString("$user:$pass".toByteArray(), Base64.NO_WRAP)
        val headers = Headers.Builder()
            .add("Authorization", "Basic $auth")
            .add("Content-type", "application/json; charset=UTF-8")
            .add("Accept", "application/json; charset=UTF-8")
            .add("Cache-Control", "no-cache")
            .build()

        val requestBody = JSONObject()
            .put("context", "cardin")
            .put("serialKey", serial)
            .put("pin", pin)
            .put("channel", "A")
            .put("deviceType", "R")
            .toString()

        //val client = OkHttpClient()
        val client = OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("https://gateway.cardin.it/publicapi/v3/receiver/activatechannel?_=${System.currentTimeMillis()}")
            .post(requestBody.toRequestBody("application/json; charset=UTF-8".toMediaTypeOrNull()))
            .headers(headers)
            .cacheControl(CacheControl.Builder().noCache().build())
            .build()

        log("Request: ${request.toString()}")
        log("Request body: $requestBody")

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            var success = false
            try {
//                val responseBody = """{"result":"OK","data":{"resultCode":"ACTIVATE_CHANNEL_OK"}}"""
//                run {

                val response = withTimeoutOrNull(120000L) {
                    client.newCall(request).execute()
                }
                response?.use {
                    if (!it.isSuccessful) throw IOException("Unexpected code $it")
                    val responseBody = it.body?.string()

//                    Thread.sleep(3000)

                    responseBody?.let { body ->
                        val jsonResponse = JSONObject(body)
                        if (jsonResponse.getString("result") == "OK" &&
                            jsonResponse.getJSONObject("data").getString("resultCode") == "ACTIVATE_CHANNEL_OK") {
                            log("Success: $jsonResponse")
                            success  = true
                        }
                        else throw IOException("Command failed")
                    }
                }
            } catch (e: Exception) {
                log("Error: ${e.message}")
            }
            finally {
                runOnUiThread {
                    binding.config.isEnabled = true
                    binding.send.isEnabled = true
                    binding.spinner.visibility = View.GONE
                    binding.result.visibility = if (binding.logContainer.visibility == View.GONE) View.VISIBLE else View.GONE
                    binding.result.setTextColor(if (success) Color.GREEN else Color.RED)
                    binding.result.text = if (success) "✔" else "✘"
                }
                if (success &&
                    binding.logContainer.visibility == View.GONE &&
                    sharedPreferences.getBoolean("autoClose", false))
                {
                    Thread.sleep(1000)
                    finish()
                }
            }
        }
    }
}
