package org.itson.tripsplit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.itson.tripsplit.R
import org.itson.tripsplit.data.model.Grupo
import org.itson.tripsplit.data.model.Usuario
import org.itson.tripsplit.data.repository.*

class EditarGrupoFragment : Fragment() {

    private lateinit var txtTripTitle: TextView
    private lateinit var btnEditTitle: ImageButton
    private lateinit var txtCopyLink: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnEliminarGrupo: ImageButton
    private val listaUsers: MutableList<Usuario> = mutableListOf()
    private lateinit var contenedorUsuarios: LinearLayout
    private lateinit var idCreadorDelGrupo: String

    private lateinit var groupId: String
    private val grupoRepository = GrupoRepository()
    private val userRepository = UserRepository()
    private val databaseRef = FirebaseDatabase.getInstance().getReference("grupos")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editar_grupo, container, false)

        btnBack = view.findViewById(R.id.btnBack)
        // Inicializar vistas
        txtTripTitle = view.findViewById(R.id.txtTripTitle)
        btnEditTitle = view.findViewById(R.id.btnEditGroup)
        txtCopyLink = view.findViewById(R.id.txtCopyLink)
        btnEliminarGrupo = view.findViewById(R.id.btnDeleteGroup)


        // Lógica para editar el título
        txtTripTitle.setOnClickListener() {
            showEditTitleDialog()
        }
        // Hacer que btnBack regrese al fragmento anterior
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // Lógica para copiar el enlace
        txtCopyLink.setOnClickListener {
            copyLinkToClipboard("http://example.com") // Cambia con el enlace real
        }
        btnEditTitle.setOnClickListener {
            val grupoId = arguments?.getString("grupoId")
            if (grupoId != null) {
                mostrarDialogoEditarNombre(grupoId)
            }
        }
        btnEliminarGrupo.setOnClickListener {
            val grupoId = arguments?.getString("grupoId")
            if (grupoId != null) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar grupo")
                    .setMessage("¿Estás seguro? Esta acción eliminará todos los gastos del grupo.")
                    .setPositiveButton("Eliminar") { _, _ ->
                        val repo = GrupoRepository()

                        repo.eliminarGrupo(grupoId) { success ->
                            if (success) {
                                Toast.makeText(requireContext(), "Grupo eliminado", Toast.LENGTH_SHORT).show()
                                irAGruposFragment()
                            } else {
                                Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .create()
                    .show()
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el ID del grupo desde los argumentos
        groupId = arguments?.getString("grupoId") ?: ""

        if (groupId.isEmpty()) {
            Toast.makeText(requireContext(), "ID del grupo inválido", Toast.LENGTH_SHORT).show()
            return
        }

        contenedorUsuarios = view.findViewById(R.id.contenedorUsuarios)

        // Cargar los miembros del grupo
        cargarMiembrosGrupo()

        // Cargar el título del grupo
        val grupoRepository = GrupoRepository()
        grupoRepository.getNombreGrupo(groupId) { nombreGrupo ->
            if (nombreGrupo != null) {
                txtTripTitle.text = nombreGrupo
            }
        }
    }
    private fun irAGruposFragment() {
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        // Reemplaza el contenido del contenedor por GruposFragment
        transaction.replace(R.id.fragmentContainer, GruposFragment())
        transaction.addToBackStack(null) // Opcional: permite volver atrás
        transaction.commit()
    }

    private fun cargarAvatarUsuario(userId: String, imageView: ImageView) {
        val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId)

        userRef.child("imagenURL").get().addOnSuccessListener { dataSnapshot ->
            val imageUrl = dataSnapshot.value as? String

            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(imageView)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_account)
                    .error(R.drawable.ic_account)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_account)
            }
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Error al obtener imagenURL de $userId: $error")
            imageView.setImageResource(R.drawable.ic_account)
        }
    }

    private fun cargarMiembrosGrupo() {
        val grupoId = arguments?.getString("grupoId") ?: return
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val grupoRepository = GrupoRepository()
        grupoRepository.esUsuarioCreador(grupoId, currentUserId) { esCreador ->
            
            grupoRepository.obtenerUsuariosDeGrupo(grupoId) { listaUsuarios ->

                val layoutContenedor = view?.findViewById<LinearLayout>(R.id.contenedorUsuarios)
                layoutContenedor?.removeAllViews()

                for (usuario in listaUsuarios) {
                    val vistaMiembro = layoutInflater.inflate(R.layout.user, layoutContenedor, false)

                    val txtNombre = vistaMiembro.findViewById<TextView>(R.id.member)
                    val btnEliminar = vistaMiembro.findViewById<ImageButton>(R.id.btnDeleteUser)
                    val imgAvatarUser = vistaMiembro.findViewById<ImageView>(R.id.imgAvatarUser)

                    txtNombre.text = usuario.nombre

                    // Mostrar el botón solo si es el creador
                    btnEliminar.visibility = if (esCreador) View.VISIBLE else View.GONE

                    btnEliminar.setOnClickListener {
                        mostrarDialogoConfirmacion(usuario.id, grupoId, vistaMiembro)
                    }

                    // Cargar la imagen del avatar
                    cargarAvatarUsuario(usuario.id, imgAvatarUser)

                    layoutContenedor?.addView(vistaMiembro)
                }
            }
        }
    }

    private fun mostrarDialogoConfirmacion(userId: String, grupoId: String, vista: View) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar miembro")
            .setMessage("¿Estás seguro de eliminar a este miembro del grupo?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarMiembroDeGrupo(grupoId, userId)
                val contenedor = view?.findViewById<LinearLayout>(R.id.contenedorUsuarios)
                contenedor?.removeView(vista)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarMiembroDeGrupo(grupoId: String, userId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("usuariosPorGrupo").child(grupoId).child(userId)
        ref.removeValue().addOnSuccessListener {
            Toast.makeText(requireContext(), "Miembro eliminado", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
        }
    }
    private fun mostrarDialogoEditarNombre(grupoId: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_editar_nombre_grupo, null)

        val inputNombre = view.findViewById<EditText>(R.id.editTextNuevoNombre)

        // Cargar nombre actual
        val repo = GrupoRepository()
        repo.getNombreGrupo(grupoId) { nombre ->
            if (nombre != null) {
                inputNombre.setText(nombre)
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar nombre")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = inputNombre.text.toString()
                if (nuevoNombre.isNotEmpty()) {
                    repo.actualizarNombreGrupo(grupoId, nuevoNombre) { success ->
                        if (success) {
                            Toast.makeText(requireContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show()
                            requireActivity().onBackPressed()
                        } else {
                            Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }


    private fun showEditTitleDialog() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Editar título")

        val input = EditText(requireContext())
        input.setText(txtTripTitle.text)
        builder.setView(input)

        builder.setPositiveButton("Guardar") { _, _ ->
            txtTripTitle.text = input.text.toString()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
    // Mostrar un cuadro de diálogo de confirmación antes de eliminar un miembro
    private fun showDeleteConfirmationDialog(memberTextView: TextView) {
        val dialog = AlertDialog.Builder(requireContext())
            .setMessage("¿Estás seguro de que quieres eliminar a este miembro?")
            .setPositiveButton("Eliminar") { _, _ ->
                memberTextView.visibility = View.GONE
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    // Copiar enlace al portapapeles
    private fun copyLinkToClipboard(link: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText("Copied Link", link)
        clipboard.setPrimaryClip(clip)
        showToast("Enlace copiado")
    }

    // Mostrar un Toast (mensaje)
    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
