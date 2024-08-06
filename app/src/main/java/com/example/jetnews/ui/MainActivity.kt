/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jetnews.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.example.jetnews.JetnewsApplication
import com.openreplay.tracker.OpenReplay
import com.openreplay.tracker.listeners.NetworkListener
import com.openreplay.tracker.models.OROptions
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appContainer = (application as JetnewsApplication).container
        setContent {
            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            JetnewsApp(appContainer, widthSizeClass)
        }

        OpenReplay.start(this, "PROJECT_KEY", OROptions(screen = true), onStarted = {
            println("OpenReplay Started")
            OpenReplay.setUserID("Jetpack Example")
            makeSampleRequest()

            data class User(val name: String, val age: Int)
            OpenReplay.event("Test Event", User("John", 25))

            // send random every 5 seconds
            val timer = java.util.Timer()
            timer.schedule(
                object : java.util.TimerTask() {
                    override fun run() {
                        OpenReplay.event("Random Event", User("John", (0..100).random()))
                    }
                }, 0, 5000
            )
        })
    }
}

fun makeSampleRequest() {
    Thread {
        try {
            val url = URL("https://jsonplaceholder.typicode.com/posts/1")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            // Optionally set request headers
            connection.setRequestProperty("Content-Type", "application/json")

            // Initialize the network listener for this connection
            val networkListener = NetworkListener(connection)

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            // Using the network listener to log the finish event
            networkListener.finish(connection, response.toString().toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}
