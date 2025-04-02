package org.itson.tripsplit.ui.activities

import android.app.Application
import com.google.firebase.FirebaseApp

class TripSplitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}