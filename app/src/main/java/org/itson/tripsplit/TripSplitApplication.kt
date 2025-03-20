package org.itson.tripsplit

import android.app.Application
import com.google.firebase.FirebaseApp

class TripSplitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}