package org.itson.tripsplit.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import org.itson.tripsplit.data.model.Grupo
import org.itson.tripsplit.data.model.Usuario

class UserRepository {

    private val database = FirebaseDatabase.getInstance().reference
    private val usersRef = database.child("usuarios")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun addUser(user: Usuario) {
        usersRef.child(user.id).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UserRepository", "Usuario agregado con éxito")
            } else {
                Log.e("UserRepository", "Error al agregar usuario", task.exception)
            }
        }
    }

    fun updateUser(uid: String, user: Usuario) {
        usersRef.child(uid).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UserRepository", "Usuario actualizado con éxito")
            } else {
                Log.e("UserRepository", "Error al actualizar usuario", task.exception)
            }
        }
    }

    fun deleteUser(uid: String) {
        usersRef.child(uid).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UserRepository", "Usuario eliminado con éxito")
            } else {
                Log.e("UserRepository", "Error al eliminar usuario", task.exception)
            }
        }
    }


    fun getUser(uid: String, callback: (Usuario?) -> Unit) {
        usersRef.child(uid).get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                val user = task.result.getValue(Usuario::class.java)
                callback(user)
            } else {
                callback(null)
            }
        }
    }

    fun getUserGroups(uid: String, callback: (List<Grupo>) -> Unit) {
        val userRef = database.child("usuarios").child(uid)
        Log.d("INFO USER",uid)
        userRef.child("grupos").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val groups = mutableListOf<Grupo>()
                val groupIds = task.result.value as? Map<String, Boolean> ?: emptyMap()
                if (groupIds.isEmpty()) {
                    callback(emptyList())
                    return@addOnCompleteListener
                }
                val groupsRef = FirebaseDatabase.getInstance().getReference("grupos")
                var loadedGroups = 0
                for (groupId in groupIds.keys) {
                    groupsRef.child(groupId).get().addOnCompleteListener { groupTask ->
                        loadedGroups++
                        if (groupTask.isSuccessful) {
                            val group = groupTask.result.getValue(Grupo::class.java)
                            group?.let { groups.add(it) }
                        }
                        if (loadedGroups == groupIds.size) {
                            callback(groups)
                        }
                    }
                }
            } else {
                callback(emptyList())
            }
        }
    }

    fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
    }

    fun login(email: String, password: String, onSuccess: (FirebaseUser) -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onError("Error obteniendo usuario actual.")
                    }
                } else {
                    onError("Usuario y/o contraseña incorrectos.")
                }
            }
    }

}
