package org.itson.tripsplit.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Usuario(
    var id: String = "",
    var nombre: String = "",
    var email: String = "",
): Parcelable