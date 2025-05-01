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
import org.itson.tripsplit.R

class EditarGrupoFragment : Fragment() {

    private lateinit var txtTripTitle: TextView
    private lateinit var btnEditTitle: ImageButton
    private lateinit var memberJuan: TextView
    private lateinit var btnDeleteJuan: ImageButton
    private lateinit var memberDiego: TextView
    private lateinit var btnDeleteDiego: ImageButton
    private lateinit var memberJoel: TextView
    private lateinit var btnDeleteJoel: ImageButton
    private lateinit var txtCopyLink: TextView
    private lateinit var btnBack: ImageButton
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editar_grupo, container, false)
        btnBack = view.findViewById(R.id.btnBack)
        val grupoId = arguments?.getString("grupoId") ?: ""
        // Inicializar vistas
        txtTripTitle = view.findViewById(R.id.txtTripTitle)
        btnEditTitle = view.findViewById(R.id.btnEditGroup)
        memberJuan = view.findViewById(R.id.memberJuan)
        btnDeleteJuan = view.findViewById(R.id.btnDeleteJuan)
        memberDiego = view.findViewById(R.id.memberDiego)
        btnDeleteDiego = view.findViewById(R.id.btnDeleteDiego)
        memberJoel = view.findViewById(R.id.memberJoel)
        btnDeleteJoel = view.findViewById(R.id.btnDeleteJoel)
        txtCopyLink = view.findViewById(R.id.txtCopyLink)

        // Lógica para editar el título
        txtTripTitle.setOnClickListener() {
            showEditTitleDialog()
        }
        btnEditTitle.setOnClickListener {
            // Cambiar visibilidad de los botones de eliminación
            btnDeleteJuan.visibility = View.VISIBLE
            btnDeleteDiego.visibility = View.VISIBLE
            btnDeleteJoel.visibility = View.VISIBLE
        }

        // Lógica para eliminar miembros
        btnDeleteJuan.setOnClickListener {
            showDeleteConfirmationDialog(memberJuan)
        }
        btnDeleteDiego.setOnClickListener {
            showDeleteConfirmationDialog(memberDiego)
        }
        btnDeleteJoel.setOnClickListener {
            showDeleteConfirmationDialog(memberJoel)
        }
        // Hacer que btnBack regrese al fragmento anterior
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // Lógica para copiar el enlace
        txtCopyLink.setOnClickListener {
            copyLinkToClipboard(grupoId)
        }

        return view
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
