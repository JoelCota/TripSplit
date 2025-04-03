package org.itson.tripsplit.data.model

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GrupoRepository {

    private val database = FirebaseDatabase.getInstance().reference

    fun crearGrupo(nombreGrupo: String, userId: String, callback: (Boolean) -> Unit) {
        val grupoId = database.child("grupos").push().key ?: return

        val nuevoGrupo = Group(
            id = grupoId,
            nombre = nombreGrupo,
            gastos = "",
            miembros = mapOf(userId to true)
        )

        database.child("grupos").child(grupoId).setValue(nuevoGrupo)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun obtenerGrupos(callback: (List<Group>) -> Unit) {
        database.child("grupos").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val grupos = mutableListOf<Group>()
                for (grupoSnapshot in snapshot.children) {
                    val grupo = grupoSnapshot.getValue(Group::class.java)
                    if (grupo != null) {
                        grupos.add(grupo)
                    }
                }
                callback(grupos)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
}