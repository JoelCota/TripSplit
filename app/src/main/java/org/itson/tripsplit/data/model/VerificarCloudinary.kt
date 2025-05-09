package org.itson.tripsplit.data.model

import android.app.Application
import com.cloudinary.android.MediaManager

class VerificarCloudinary : Application() {
    companion object {
        var isCloudinaryInitialized = false
    }

    override fun onCreate() {
        super.onCreate()
        if (!isCloudinaryInitialized) {
            val config = HashMap<String, String>()
            config["cloud_name"] = "dw8yxze4m"
            MediaManager.init(this, config)
            isCloudinaryInitialized = true
        }
    }
}