package org.itson.tripsplit.data.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import org.itson.tripsplit.data.model.User

class UserRepository {

    private val database = FirebaseDatabase.getInstance().reference
    private val usersRef = database.child("Users")

    // Agregar un nuevo usuario
    fun addUser(user: User) {
        usersRef.child(user.uid).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UserRepository", "Usuario agregado con éxito")
            } else {
                Log.e("UserRepository", "Error al agregar usuario", task.exception)
            }
        }
    }

    // Obtener los datos de un usuario por UID
    fun getUser(uid: String, callback: (User?) -> Unit) {
        usersRef.child(uid).get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                val user = task.result.getValue(User::class.java)
                callback(user)
            } else {
                callback(null)
            }
        }
    }

    // Actualizar los datos de un usuario
    fun updateUser(uid: String, user: User) {
        usersRef.child(uid).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UserRepository", "Usuario actualizado con éxito")
            } else {
                Log.e("UserRepository", "Error al actualizar usuario", task.exception)
            }
        }
    }

    // Eliminar un usuario
    fun deleteUser(uid: String) {
        usersRef.child(uid).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UserRepository", "Usuario eliminado con éxito")
            } else {
                Log.e("UserRepository", "Error al eliminar usuario", task.exception)
            }
        }
    }
}
