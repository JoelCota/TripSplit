package org.itson.tripsplit.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.database.*
import org.itson.tripsplit.data.model.Deuda
import org.itson.tripsplit.data.model.Gasto
import org.itson.tripsplit.data.repository.UserRepository
import org.itson.tripsplit.utils.CurrencyUtils
import java.util.UUID

class GastoRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val userRepository= UserRepository()
    fun agregarGasto(grupoId: String, gasto: Gasto, onComplete: (Boolean) -> Unit) {
        val gastoId = UUID.randomUUID().toString()
        val gastoConId = gasto.copy(id = gastoId)
        val gastoRef = database.child("gastosPorGrupo").child(grupoId).child(gastoId)

        gastoRef.setValue(gastoConId)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun obtenerTotalesPorCategoria(grupoId: String, callback: (Map<String, Float>) -> Unit) {
        val gastosRef = database.child("gastosPorGrupo").child(grupoId)
        gastosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onDataChange(snapshot: DataSnapshot) {
                val totales = mutableMapOf<String, Float>()
                for (gastoSnap in snapshot.children) {
                    val gasto = gastoSnap.getValue(Gasto::class.java)
                    if (gasto != null) {
                        val categoria = gasto.categoria ?: "Sin categoría"
                        val cantidad = gasto.cantidad?.toFloat() ?: 0f
                        totales[categoria] = totales.getOrDefault(categoria, 0f) + cantidad
                    }
                }
                callback(totales)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyMap())
            }
        })
    }


    fun obtenerGastos(grupoId: String, callback: (List<Gasto>) -> Unit) {
        val gastosRef = database.child("gastosPorGrupo").child(grupoId)
        gastosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaGastos = mutableListOf<Gasto>()
                for (gastoSnap in snapshot.children) {
                    val gasto = gastoSnap.getValue(Gasto::class.java)
                    gasto?.let {
                        listaGastos.add(it)
                    }
                }
                callback(listaGastos)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error al obtener gastos: ${error.message}")
                callback(emptyList())
            }
        })
    }


    fun eliminarGasto(grupoId: String, gastoId: String, onComplete: (Boolean) -> Unit) {
        val gastoRef = FirebaseDatabase.getInstance().getReference("gastosPorGrupo").child(grupoId).child(gastoId)

        gastoRef.removeValue()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun actualizarGasto(grupoId: String, gasto: Gasto, onComplete: (Boolean) -> Unit) {
        if (gasto.id.isBlank()) {
            onComplete(false)
            return
        }
        val gastoRef = database.child("gastosPorGrupo").child(grupoId).child(gasto.id)
        gastoRef.setValue(gasto)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun obtenerTotalGastado(grupoId: String, callback: (Double) -> Unit) {
        val gastosRef = database.child("gastosPorGrupo").child(grupoId)
        gastosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0.0
                for (gastoSnap in snapshot.children) {
                    val gasto = gastoSnap.getValue(Gasto::class.java)
                    if (gasto != null) {
                        val moneda=gasto.moneda
                        val cantidadTotal = CurrencyUtils().toUsd(gasto.cantidad,moneda)
                        total += cantidadTotal
                    }
                }
                callback(total)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(0.0)
            }
        })
    }
    fun obtenerBalanceUsuario(grupoId: String, userId: String, callback: (Double) -> Unit) {
        val gastosRef = database.child("gastosPorGrupo").child(grupoId)
        gastosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalDeuda = 0.0
                var totalCredito = 0.0

                for (gastoSnap in snapshot.children) {
                    val gasto = gastoSnap.getValue(Gasto::class.java)
                    if (gasto != null) {
                        val participantes = gasto.divididoEntre.orEmpty()
                        val moneda=gasto.moneda
                        val cantidadTotal = CurrencyUtils().toUsd(gasto.cantidad,moneda)

                        if (participantes.any { it.id == userId }) {
                            val cantidadPorPersona = cantidadTotal / participantes.size
                            totalDeuda += cantidadPorPersona
                        }

                        if (gasto.pagadoPor?.id == userId) {
                            totalCredito += cantidadTotal
                        }
                    }
                }

                val balance = totalCredito - totalDeuda
                callback(balance)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al calcular balance: ${error.message}")
                callback(0.0)
            }
        })
    }


    fun calcularDeudasPorIdGrupo(grupoId: String, callback: (List<Deuda>) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("gastosPorGrupo").child(grupoId)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deudasMap = mutableMapOf<Pair<String, String>, Double>()
                for (gastoSnap in snapshot.children) {
                    val gasto = gastoSnap.getValue(Gasto::class.java)

                    if (gasto != null) {
                        val moneda=gasto.moneda
                        val cantidadTotal = CurrencyUtils().toUsd(gasto.cantidad,moneda)
                        val pagador = gasto.pagadoPor
                        val participantes = gasto.divididoEntre.orEmpty()

                        if (pagador != null && participantes.isNotEmpty()) {
                            val cantidadPorPersona = cantidadTotal / participantes.size

                            for (usuario in participantes) {
                                if (usuario.id != pagador.id) {
                                    val clave = Pair(usuario.id, pagador.id)
                                    deudasMap[clave] = (deudasMap[clave] ?: 0.0) + cantidadPorPersona
                                }
                            }
                        }
                    }
                }

                // Simplificar deudas bidireccionales
                val deudasSimplificadas = mutableMapOf<Pair<String, String>, Double>()

                for ((clave, monto) in deudasMap) {
                    val inversa = Pair(clave.second, clave.first)
                    if (deudasSimplificadas.containsKey(inversa)) {
                        val montoInverso = deudasSimplificadas[inversa]!!
                        val diferencia = montoInverso - monto
                        if (diferencia > 0) {
                            deudasSimplificadas[inversa] = diferencia
                        } else if (diferencia < 0) {
                            deudasSimplificadas.remove(inversa)
                            deudasSimplificadas[clave] = -diferencia
                        } else {
                            deudasSimplificadas.remove(inversa)
                        }
                    } else {
                        deudasSimplificadas[clave] = monto
                    }
                }

                val deudas = deudasSimplificadas.map { (clave, monto) ->
                    Deuda(deudor = clave.first, acreedor = clave.second, monto = monto,moneda="USD")
                }

                callback(deudas)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer gastos: ${error.message}")
            }
        })
    }

    fun calcularDeudasPorIdGasto(grupoId: String, gastoId: String, callback: (List<Deuda>) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("gastosPorGrupo").child(grupoId).child(gastoId)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deudasMap = mutableMapOf<Pair<String, String>, Double>()
                val gasto = snapshot.getValue(Gasto::class.java) // Asumiendo que 'Gasto' es tu clase de datos

                if (gasto != null) {
                    Log.d("Deuda", "Gasto encontrado: $gasto")
                    val cantidadTotal =gasto.cantidad
                    val pagador = gasto.pagadoPor
                    val participantes = gasto.divididoEntre.orEmpty()

                    if (pagador != null && participantes.isNotEmpty()) {
                        val cantidadPorPersona = cantidadTotal / participantes.size
                        Log.d("Deuda", "Cantidad total: $cantidadTotal, Cantidad por persona: $cantidadPorPersona")

                        for (usuario in participantes) {
                            if (usuario.id != pagador.id) {
                                val clave = Pair(usuario.id, pagador.id)
                                deudasMap[clave] = (deudasMap[clave] ?: 0.0) + cantidadPorPersona
                                Log.d("Deuda", "Deuda para ${usuario.id}: ${deudasMap[clave]}")
                            }
                        }
                    }

                    // Convertir el mapa de deudas a una lista de objetos 'Deuda'
                    val deudasList = deudasMap.map { (key, value) ->
                        Deuda(deudor = key.first, acreedor = key.second, monto = value,moneda=gasto.moneda)
                    }

                    // Log de la lista de deudas
                    Log.d("Deuda", "Deudas calculadas: $deudasList")

                    // Calculamos el total de las deudas para pasar al callback
                    val totalDeuda = deudasList.sumByDouble { it.monto }
                    Log.d("Deuda", "Total deuda: $totalDeuda")

                    // Llamamos al callback con el total y la lista de deudas
                    callback(deudasList)
                } else {
                    Log.d("Deuda", "No se encontró el gasto")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer el gasto: ${error.message}")
            }
        })
    }



    fun calcularCreditoUsuario(grupoId: String, idUsuario: String, callback: (Double) -> Unit) {
        val gastosRef = database.child("gastosPorGrupo").child(grupoId)
        gastosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalACobrar = 0.0
                for (gastoSnap in snapshot.children) {
                    val gasto = gastoSnap.getValue(Gasto::class.java)
                    val participantes = gasto?.divididoEntre.orEmpty()
                    Log.d("GastoCredito Inicio", gastoSnap.toString())
                    if (gasto != null && participantes.size > 1) {
                        val cantidadPorPersona = gasto.cantidad / participantes.size
                        val cantidadACobrar = cantidadPorPersona * participantes.count { it.id != idUsuario }
                        totalACobrar += cantidadACobrar
                    }
                }
                callback(totalACobrar)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer gastos: ${error.message}")
                callback(0.0)
            }
        })
    }


}
