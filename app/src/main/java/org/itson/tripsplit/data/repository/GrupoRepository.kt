package org.itson.tripsplit.data.repository

import android.util.Log
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

    fun obtenerGastosPorGrupo(grupoId: String, onResultado: (List<Gasto>) -> Unit) {
        val grupoRef = database.child("gastosPorGrupo").child(grupoId)

        grupoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaGastos = mutableListOf<Gasto>()

                for (gastoSnapshot in snapshot.children) {
                    val gasto = gastoSnapshot.getValue(Gasto::class.java)
                    if (gasto != null) {
                        listaGastos.add(gasto)
                    }
                }

                onResultado(listaGastos)
            }

            override fun onCancelled(error: DatabaseError) {
                onResultado(emptyList()) // o manejar error como prefieras
            }
        })
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
        val database = FirebaseDatabase.getInstance().reference
        val grupoRef = database.child("grupos").child(grupoId)
        val usuarioRef = database.child("usuarios").child(usuarioId)

        // Verificar si el grupo existe
        grupoRef.get().addOnSuccessListener { grupoSnapshot ->
            if (grupoSnapshot.exists()) {
                // Verificar si el usuario ya es parte del grupo
                database.child("usuariosPorGrupo").child(grupoId).child(usuarioId).get()
                    .addOnSuccessListener { miembroSnapshot ->
                        if (miembroSnapshot.exists()) {
                            callback(false, "El usuario ya es miembro del grupo")
                        } else {
                            // Agregar usuario al grupo
                            database.child("usuariosPorGrupo").child(grupoId).child(usuarioId).setValue(true)
                                .addOnSuccessListener {
                                    // Agregar grupo al perfil del usuario
                                    database.child("usuarios").child(usuarioId).child("grupos").child(grupoId).setValue(true)
                                        .addOnSuccessListener {
                                            callback(true, "Usuario $usuarioId se unió al grupo $grupoId")
                                        }
                                        .addOnFailureListener {
                                            callback(false, "Error al agregar grupo al perfil del usuario")
                                        }
                                }
                                .addOnFailureListener {
                                    callback(false, "Error al agregar usuario al grupo")
                                }
                        }
                    }
            } else {
                callback(false, "El grupo no existe")
            }
        }.addOnFailureListener {
            callback(false, "Error al verificar existencia del grupo")
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

    fun actualizarGasto(grupoId: String, gasto: Gasto, onComplete: (Boolean) -> Unit) {
        val gastoRef = FirebaseDatabase.getInstance().getReference("gastosPorGrupo").child(grupoId).child(gasto.id)

        gastoRef.setValue(gasto)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun eliminarUsuarioDelGrupo(
        grupoId: String,
        usuarioId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val db = FirebaseDatabase.getInstance()
        val grupoRef = db.getReference("grupos").child(grupoId).child("usuarios")
        val usuarioRef = db.getReference("usuarios").child(usuarioId).child("grupos")
        val usuariosPorGrupoRef = db.getReference("usuariosPorGrupo").child(grupoId).child(usuarioId)

        // Eliminar usuario de la lista de usuarios del grupo
        grupoRef.orderByValue().equalTo(usuarioId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val userNode = snapshot.children.firstOrNull()
                userNode?.ref?.removeValue()?.addOnCompleteListener { task1 ->
                    if (task1.isSuccessful) {
                        // Eliminar grupo de la lista de grupos del usuario
                        usuarioRef.orderByValue().equalTo(grupoId).get().addOnSuccessListener { userGroupSnapshot ->
                            val groupNode = userGroupSnapshot.children.firstOrNull()
                            groupNode?.ref?.removeValue()?.addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    // Eliminar el nodo en usuariosPorGrupo
                                    usuariosPorGrupoRef.removeValue().addOnCompleteListener { task3 ->
                                        if (task3.isSuccessful) {
                                            callback(true, "Usuario $usuarioId eliminado correctamente del grupo $grupoId.")
                                        } else {
                                            callback(false, "Error al eliminar de usuariosPorGrupo: ${task3.exception?.message}")
                                        }
                                    }
                                } else {
                                    callback(false, "Error al eliminar el grupo del perfil del usuario: ${task2.exception?.message}")
                                }
                            }
                        }.addOnFailureListener {
                            callback(false, "Error al leer los grupos del usuario: ${it.message}")
                        }
                    } else {
                        callback(false, "Error al eliminar usuario del grupo: ${task1.exception?.message}")
                    }
                }
            } else {
                callback(false, "El usuario no está registrado en este grupo.")
            }
        }.addOnFailureListener {
            callback(false, "Error al leer los usuarios del grupo: ${it.message}")
        }
    }
}
