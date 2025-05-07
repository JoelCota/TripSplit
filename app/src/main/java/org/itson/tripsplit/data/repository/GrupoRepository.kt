package org.itson.tripsplit.data.repository

import android.util.Log
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.itson.tripsplit.data.model.Grupo
import org.itson.tripsplit.data.model.Gasto
import org.itson.tripsplit.data.model.Usuario

import java.util.*

class GrupoRepository {

    private val database = FirebaseDatabase.getInstance().reference

    // Función para generar un ID aleatorio de 6 caracteres (letras y números)
    private fun generarIdAleatorio(): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        return (1..6)
            .map { caracteres[random.nextInt(caracteres.length)] }
            .joinToString("")
    }

    fun crearGrupo(nombreGrupo: String, userId: String, callback: (Boolean) -> Unit) {
        val idGrupo = generarIdAleatorio()
        val grupo = Grupo(id = idGrupo, nombre = nombreGrupo, idCreador = userId)

        // Guardar el grupo
        database.child("grupos").child(idGrupo).setValue(grupo)
            .addOnSuccessListener {
                // Agregar al usuario como miembro en usuariosPorGrupo
                database.child("usuariosPorGrupo").child(idGrupo).child(userId).setValue(true)
                    .addOnSuccessListener {
                        // Agregar el grupo en el perfil del usuario
                        database.child("usuarios").child(userId).child("grupos").child(idGrupo).setValue(true)
                            .addOnSuccessListener { callback(true) }
                            .addOnFailureListener { callback(false) }
                    }
                    .addOnFailureListener { callback(false) }
            }
            .addOnFailureListener { callback(false) }
    }

     fun obtenerUsuariosDeGrupo(grupoId: String, onResultado: (List<Usuario>) -> Unit) {
        val usuariosRef = database.child("usuariosPorGrupo").child(grupoId)
        usuariosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaUsuarios = mutableListOf<Usuario>()
                val usuariosKeys = snapshot.children.mapNotNull { it.key }
                var count = 0

                for (uid in usuariosKeys) {
                    database.child("usuarios").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val usuario = userSnapshot.getValue(Usuario::class.java)
                            usuario?.let { listaUsuarios.add(it) }
                            count++
                            if (count == usuariosKeys.size) {
                                onResultado(listaUsuarios)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onResultado(emptyList())
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onResultado(emptyList())
            }
        })
    }

    fun getNombreGrupo(grupoId: String, callback: (String?) -> Unit) {
        val grupoRef = FirebaseDatabase.getInstance().getReference("grupos").child(grupoId)

        grupoRef.child("nombre").get().addOnSuccessListener { task ->
            val nombre = task.value as? String
            callback(nombre)
        }.addOnFailureListener {
            callback(null)
        }
    }



    fun eliminarGrupo(grupoId: String, callback: (Boolean) -> Unit) {
        val updates = hashMapOf<String, Any?>(
            "/grupos/$grupoId" to null,
            "/usuariosPorGrupo/$grupoId" to null,
            "/gastosPorGrupo/$grupoId" to null
        )

        database.updateChildren(updates)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun unirseAGrupo(grupoId: String, usuarioId: String, callback: (Boolean, String) -> Unit) {
        val grupoRef = FirebaseDatabase.getInstance().getReference("grupos").child(grupoId)
        val usuarioRef = FirebaseDatabase.getInstance().getReference("usuarios").child(usuarioId)

        // Verificamos si el grupo existe
        grupoRef.get().addOnSuccessListener { grupoSnapshot ->
            if (grupoSnapshot.exists()) {
                // El grupo existe, ahora añadimos al usuario al grupo
                val usuarioEnGrupoRef =
                    grupoRef.child("usuarios").push() // Usamos push() para agregar un nuevo usuario

                usuarioEnGrupoRef.setValue(usuarioId).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Ahora actualizamos el grupo en el perfil del usuario
                        val usuarioGrupoRef =
                            usuarioRef.child("grupos").push() // Añadimos el ID del grupo al usuario
                        usuarioGrupoRef.setValue(grupoId).addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                callback(true, "Usuario $usuarioId se unió al grupo $grupoId")
                            } else {
                                callback(false, "Error al agregar grupo al perfil del usuario")
                            }
                        }
                    } else {
                        callback(false, "Error al agregar usuario al grupo")
                    }
                }
            } else {
                callback(false, "El grupo no existe")
            }
        }
    }
    fun esUsuarioCreador(grupoId: String, userId: String, callback: (Boolean) -> Unit) {
        val grupoRef = database.child("grupos").child(grupoId)
        grupoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val grupo = snapshot.getValue(Grupo::class.java)
                val esCreador = grupo?.idCreador == userId
                callback(esCreador)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    fun eliminarUsuarioDelGrupo(
        grupoId: String,
        usuarioId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val grupoRef = FirebaseDatabase.getInstance().getReference("grupos").child(grupoId)
        val usuarioRef = FirebaseDatabase.getInstance().getReference("usuarios").child(usuarioId)
        val usuariosPorGrupoRef =FirebaseDatabase.getInstance().getReference("usuariosPorGrupo").child(grupoId)

        grupoRef.get().addOnSuccessListener { grupoSnapshot ->
            if (grupoSnapshot.exists()) {
                val usuariosRef = grupoRef.child("usuarios")
                usuariosRef.orderByValue().equalTo(usuarioId).get()
                    .addOnSuccessListener { userSnapshot ->
                        if (userSnapshot.exists()) {
                            userSnapshot.children.firstOrNull()?.ref?.removeValue()
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val gruposRef = usuarioRef.child("grupos")
                                        gruposRef.orderByValue().equalTo(grupoId).get()
                                            .addOnSuccessListener { userGroupsSnapshot ->
                                                if (userGroupsSnapshot.exists()) {
                                                    userGroupsSnapshot.children.firstOrNull()?.ref?.removeValue()
                                                        ?.addOnCompleteListener { task2 ->
                                                            if (task2.isSuccessful) {
                                                                usuariosPorGrupoRef.child(usuarioId)
                                                                    .removeValue()
                                                                    .addOnCompleteListener { task3 ->
                                                                        if (task3.isSuccessful) {
                                                                            callback(
                                                                                true,
                                                                                "Usuario $usuarioId eliminado completamente del grupo $grupoId"
                                                                            )
                                                                        } else {
                                                                            callback(
                                                                                false,
                                                                                "Error al eliminar usuario de usuariosPorGrupo"
                                                                            )
                                                                        }
                                                                    }
                                                            } else {
                                                                callback(
                                                                    false,
                                                                    "Error al eliminar el grupo del perfil del usuario"
                                                                )
                                                            }
                                                        }
                                                } else {
                                                    callback(
                                                        false,
                                                        "El usuario no está en el grupo"
                                                    )
                                                }
                                            }
                                    } else {
                                        callback(false, "Error al eliminar usuario del grupo")
                                    }
                                }
                        } else {
                            callback(false, "El usuario no está en el grupo")
                        }
                    }
            } else {
                callback(false, "El grupo no existe")
            }
        }
    }
}
