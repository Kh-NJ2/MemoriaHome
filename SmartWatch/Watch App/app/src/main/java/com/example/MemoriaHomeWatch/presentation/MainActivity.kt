package com.example.MemoriaHomeWatch.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.MemoriaHomeWatch.BuildConfig
import com.example.MemoriaHomeWatch.R
import com.example.MemoriaHomeWatch.presentation.theme.ConnectToHubTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
// samsung Health service sdk


class MainActivity : ComponentActivity() {

    private lateinit var mqttClient: MqttClient
    private var receivedMessage by mutableStateOf("No message received")


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        mqttConnect(BuildConfig.MQTT_BROKER)

        setContent {
            ConnectToHubTheme {
                WearApp(
                    message = receivedMessage,
                    onButtonClick1 = { mqttSubscribe("to-watch", 1) },
                    onButtonClick2 = { mqttPublish("watch-data", "this is a test", 1) },
                    onButtonClick3 = { startActivity(Intent(this, PermissionActivity::class.java)) }
                )
            }
        }
    }

    fun mqttConnect(brokeraddr: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val clientId = MqttClient.generateClientId()
                mqttClient = MqttClient("tcp://$brokeraddr:1883", clientId, MemoryPersistence())

                val connOptions = MqttConnectOptions()
                connOptions.userName = BuildConfig.MQTT_USERNAME
                connOptions.password = BuildConfig.MQTT_PASSWORD.toCharArray()
                connOptions.isCleanSession = true

                mqttClient.connect(connOptions)
                Log.d("MQTT", "Connected!")

                mqttSetReceiveListener()

            } catch (e: MqttException) {
                Log.e("MQTT", "Connection failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun mqttPublish(topic: String, msg: String, qos: Int) {
        try {
            val mqttMessage = MqttMessage(msg.toByteArray(charset("UTF-8")))
            mqttMessage.qos = qos
            mqttMessage.isRetained = false
            // Publish the message
            mqttClient.publish(topic, mqttMessage)
        } catch (e: Exception) {
            Log.e("MQTT", "Publish failed: ${e.message}")
        }
    }

    fun mqttSubscribe(topic: String, qos: Int) {
        try {
            mqttClient.subscribe(topic, qos)
        } catch (e: Exception) {
            Log.e("MQTT", "Subscribe failed: ${e.message}")
        }

    }
    fun mqttSetReceiveListener() {
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
            // Connection Lost
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                // A message has been received
                val data = String(message.payload, charset("UTF-8"))
                Log.d("MQTT", "Message Arrived : $data")
                // Place the message into a specific TextBox object
                CoroutineScope(Dispatchers.Main).launch {
                    receivedMessage = data
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
            }
        })
    }
}



@Composable
fun WearApp(onButtonClick1: () -> Unit, onButtonClick2: () -> Unit, onButtonClick3: () -> Unit, message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message)
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier.size(ButtonDefaults.DefaultButtonSize),
                onClick = onButtonClick1,
                enabled = true
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_subscribe),
                    contentDescription = "subscribe",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(
                modifier = Modifier.size(ButtonDefaults.DefaultButtonSize),
                onClick = onButtonClick2,
                enabled = true
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = "send",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(
                modifier = Modifier.size(ButtonDefaults.DefaultButtonSize),
                onClick = onButtonClick3,
                enabled = true
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_connect),
                    contentDescription = "health", modifier = Modifier.size(24.dp))
            }

        }

    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp (message = "No message received", onButtonClick1 = {}, onButtonClick2 = {}, onButtonClick3 = {})
}
