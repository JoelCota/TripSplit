package org.itson.tripsplit.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.itson.tripsplit.data.model.Group
import org.itson.tripsplit.data.model.Expense

import java.util.*

class GrupoRepository {

    private val database = FirebaseDatabase.getInstance().reference
    private val groupsRef = database.child("Groups")

    // Función para generar un ID aleatorio de 6 caracteres (letras y números)
    private fun generarIdAleatorio(): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        return (1..6)
            .map { caracteres[random.nextInt(caracteres.length)] }
            .joinToString("")
    }

    // Crear un nuevo grupo con un ID aleatorio
    fun crearGrupo(nombreGrupo: String, userId: String, callback: (Boolean) -> Unit) {
        val grupoId = generarIdAleatorio() // Usamos el ID aleatorio generado

        val nuevoGrupo = Group(
            id = grupoId,
            name = nombreGrupo,
            expenses = emptyMap(),
            members = mapOf(userId to true)
        )

        // Primero, crear el grupo en la base de datos
        database.child("Groups").child(grupoId).setValue(nuevoGrupo)
            .addOnSuccessListener {
                // Una vez que el grupo se crea, agregamos el grupo al usuario
                val userRef = database.child("Users").child(userId)
                userRef.child("Groups").child(grupoId).setValue(true)
                    .addOnSuccessListener {
                        callback(true)
                    }
                    .addOnFailureListener {
                        // Fallo al agregar el grupo al usuario
                        callback(false)
                    }
            }
            .addOnFailureListener {
                // Fallo al crear el grupo
                callback(false)
            }
    }
    // Función para unirse a un grupo
    fun unirseAGrupo(codigoGrupo: String, userId: String, callback: (Boolean, String?) -> Unit) {
        val grupoRef = groupsRef.child(codigoGrupo)

        grupoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // El grupo existe, agregar al usuario
                    grupoRef.child("members").child(userId).setValue(true)
                        .addOnSuccessListener {
                            // También agregamos el grupo al usuario
                            val userRef = database.child("Users").child(userId)
                            userRef.child("Groups").child(codigoGrupo).setValue(true)
                                .addOnSuccessListener {
                                    callback(true, "Te has unido al grupo")
                                }
                                .addOnFailureListener {
                                    callback(false, "Error al agregar al usuario al grupo")
                                }
                        }
                        .addOnFailureListener {
                            callback(false, "Error al agregar al grupo")
                        }
                } else {
                    // El grupo no existe
                    callback(false, "El código de grupo no es válido")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Error al verificar el código del grupo")
            }
        })
    }

    fun getUserGroups(uid: String, callback: (List<Group>) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)

        userRef.child("Groups").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val groups = mutableListOf<Group>()
                val groupIds = task.result.value as? Map<String, Boolean> ?: emptyMap()

                if (groupIds.isEmpty()) {
                    callback(emptyList()) // Si no hay grupos, llamamos al callback con lista vacía
                    return@addOnCompleteListener
                }

                val groupsRef = FirebaseDatabase.getInstance().getReference("Groups")
                var loadedGroups = 0  // Contador de grupos cargados

                for (groupId in groupIds.keys) {
                    groupsRef.child(groupId).get().addOnCompleteListener { groupTask ->
                        loadedGroups++ // Contamos cada intento de carga

                        if (groupTask.isSuccessful) {
                            val group = groupTask.result.getValue(Group::class.java)
                            group?.let { groups.add(it) }
                        } else {
                            Log.e("GroupRepository", "Error al obtener grupo $groupId", groupTask.exception)
                        }

                        // Llamar al callback cuando todas las solicitudes se completen
                        if (loadedGroups == groupIds.size) {
                            callback(groups)
                        }
                    }
                }
            } else {
                Log.e("UserRepository", "Error al obtener grupos del usuario", task.exception)
                callback(emptyList()) // Llamamos al callback con lista vacía en caso de error
            }
        }
    }

    // Agregar un gasto a un grupo
    fun agregarGasto(grupoId: String, gasto: Expense, callback: (Boolean) -> Unit) {
        val gastoId = groupsRef.child(grupoId).child("expenses").push().key ?: return

        groupsRef.child(grupoId).child("expenses").child(gastoId).setValue(gasto)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }


    // Obtener los gastos de un grupo
    fun obtenerGastos(grupoId: String, callback: (Map<String, Expense>) -> Unit) {
        database.child("Groups").child(grupoId).child("expenses").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gastos = mutableMapOf<String, Expense>()
                for (gastoSnapshot in snapshot.children) {
                    val gasto = gastoSnapshot.getValue(Expense::class.java)
                    if (gasto != null) {
                        gastos[gastoSnapshot.key ?: ""] = gasto
                    }
                }
                callback(gastos)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyMap())
            }
        })
    }
}
