package org.itson.tripsplit.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Gasto(
    var id: String = "",
    var nombre: String = "",
    var cantidad: Double = 0.0,
    var categoria: String = "",
    var moneda: String = "USD",
    var pagadoPor: Usuario? = null,
    var divididoEntre: List<Usuario> = emptyList(),
    var fecha: String=""
): Parcelable
