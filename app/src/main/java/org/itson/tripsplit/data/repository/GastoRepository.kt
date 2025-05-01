package org.itson.tripsplit.repository

import com.google.firebase.database.*
import org.itson.tripsplit.data.model.Gasto
import java.util.UUID

class GastoRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun agregarGasto(grupoId: String, gasto: Gasto, onComplete: (Boolean) -> Unit) {
        val gastoId = UUID.randomUUID().toString()
        val gastoConId = gasto.copy(id = gastoId)
        val gastoRef = database.child("gastosPorGrupo").child(grupoId).child(gastoId)

        gastoRef.setValue(gastoConId)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun obtenerGastos(grupoId: String, callback: (List<Gasto>) -> Unit) {
        val gastosRef = database.child("gastosPorGrupo").child(grupoId)

        gastosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaGastos = mutableListOf<Gasto>()
                for (gastoSnap in snapshot.children) {
                    val gasto = gastoSnap.getValue(Gasto::class.java)
                    gasto?.let { listaGastos.add(it) }
                }
                callback(listaGastos)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    fun eliminarGasto(grupoId: String, gastoId: String, onComplete: (Boolean) -> Unit) {
        val gastoRef = database.child("gastosPorGrupo").child(grupoId).child(gastoId)
        gastoRef.removeValue()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
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
}
