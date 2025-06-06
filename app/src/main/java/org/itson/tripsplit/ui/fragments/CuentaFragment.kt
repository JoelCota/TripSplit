package org.itson.tripsplit.ui.fragments

import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.itson.tripsplit.R
import org.itson.tripsplit.data.repository.UserRepository
import org.itson.tripsplit.ui.activities.LoginActivity

class CuentaFragment : Fragment() {

    private lateinit var userRepository: UserRepository
    private val REQUEST_IMAGE_GET = 1
    val CLOUD_NAME = "dw8yxze4m"
    val UPLOAD_PRESET = "TripSplit"
    var imageUri: Uri? = null
    private lateinit var imgAvatar: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_cuenta, container, false)

        userRepository = UserRepository()

        val btnLogout: Button = rootView.findViewById(R.id.btnCerrarSesion)

        imgAvatar = rootView.findViewById(R.id.imgAvatar)
        btnLogout.setOnClickListener {
            userRepository.cerrarSesion()

            // Volver a LoginActivity y limpiar el back stack
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        val btnSubirImagen: Button = rootView.findViewById(R.id.btnCambiarAvatar)
        btnSubirImagen.setOnClickListener {
            // Lógica para subir imagen
            val intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }
        cargarAvatarUsuario()


        return rootView
    }

    private fun cargarAvatarUsuario() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        if (uid == null) {
            Log.e("FragmentCuenta", "Usuario no autenticado")
            return
        }

        val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)

        userRef.child("imagenURL").get().addOnSuccessListener { dataSnapshot ->
            val imageUrl = dataSnapshot.value as? String

            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_account)
                    .error(R.drawable.ic_account)
                    .into(imgAvatar)
            } else {
                imgAvatar.setImageResource(R.drawable.ic_account)
            }
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Error al obtener imagenURL: $error")
            imgAvatar.setImageResource(R.drawable.ic_account)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK){
            val imageUri = data?.data
            imageUri?.let {
                changeImage(it)

                uploadImage(it, object : UploadCallback {
                    override fun onUploadSuccess(url: String) {
                        // Guardar en Firebase
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        uid?.let {
                            updateProfileImageUrl(uid, url)
                        }
                    }

                    override fun onUploadError(error: String) {
                        Toast.makeText(requireContext(), "Error al subir imagen: $error", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtNombre: TextView = view.findViewById(R.id.txtNombreUsuario)
        val userId = FirebaseAuth.getInstance().currentUser?.uid


        if (userId != null) {
            userRepository.getUser(userId) { user ->
                if (user != null) {
                    txtNombre.text  = "Hola, ${user.nombre.split(" ")[0]}"
                } else {
                    txtNombre.text = "Usuario no encontrado"
                }
            }
        } else {
            txtNombre.text = "Usuario no autenticado"
        }
    }

    interface UploadCallback {
        fun onUploadSuccess(url: String)
        fun onUploadError(error: String)
    }

    fun uploadImage(uri: Uri, callback: UploadCallback) {
        if (uri != null) {
            MediaManager.get().upload(uri).unsigned(UPLOAD_PRESET).callback(object :
                com.cloudinary.android.callback.UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Upload start")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    Log.d("Cloudinary", "Upload progress")
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    Log.d("Cloudinary", "Upload success")
                    val url = resultData?.get("secure_url") as String? ?: ""
                    if (url.isNotEmpty()) {
                        callback.onUploadSuccess(url)
                    } else {
                        callback.onUploadError("Empty URL from Cloudinary")
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload error: ${error?.description}")
                    callback.onUploadError(error?.description ?: "Unknown error")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
        } else {
            callback.onUploadError("No image selected")
        }
    }

    fun updateProfileImageUrl(uid: String, imageUrl: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
        val updates = hashMapOf<String, Any>(
            "imagenURL" to imageUrl
        )
        userRef.updateChildren(updates)
            .addOnSuccessListener {
                Log.d("Firebase", "Imagen URL actualizada correctamente")
                Toast.makeText(requireContext(), "Imagen actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al actualizar imagen: $e")
                Toast.makeText(requireContext(), "Error al actualizar imagen", Toast.LENGTH_SHORT).show()
            }
    }

    fun changeImage(uri: Uri) {
        val thumbnail: ImageView = view?.findViewById(R.id.imgAvatar) as ImageView
        try {
            thumbnail.setImageURI(uri)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}
