package org.itson.tripsplit.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import org.itson.tripsplit.R
import org.itson.tripsplit.data.model.Usuario
import org.json.JSONObject
import org.w3c.dom.Text

class MiembrosDialogFragment : DialogFragment() {

    private var miembros: MutableList<Usuario> = mutableListOf()
    private val selectedMiembros: MutableList<Usuario> = mutableListOf()

    companion object {
        private const val ARG_MIEMBROS = "miembros"
        private const val ARG_MODE = "mode"

        fun newInstance(miembros: List<Usuario>, mode: String): MiembrosDialogFragment {
            val fragment = MiembrosDialogFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_MIEMBROS, ArrayList(miembros))
            args.putString(ARG_MODE, mode)
            fragment.arguments = args
            return fragment
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_miembros, container, false)

        miembros = arguments?.getParcelableArrayList(ARG_MIEMBROS) ?: mutableListOf()
        val mode = arguments?.getString(ARG_MODE)
        Log.d("NuenoGastoINFO", miembros.size.toString())
        val containerMiembros: LinearLayout = rootView.findViewById(R.id.containerMiembros)
        val btnGuardar: Button = rootView.findViewById(R.id.btnGuardar)
        val btnCerrar: Button = rootView.findViewById(R.id.btnCerrar)
        val txtTitulo: TextView = rootView.findViewById(R.id.txtTitulo)
        val txtMembers: TextView= rootView.findViewById(R.id.txtMembers)
        txtTitulo.text = "Miembros"
        txtTitulo.gravity = View.TEXT_ALIGNMENT_CENTER
        println(miembros)
        if (miembros.size <= 1){
            txtMembers.visibility=View.VISIBLE
            btnGuardar.visibility = View.GONE
        }else {
            if (mode == "single") {
                btnGuardar.visibility = View.GONE

                miembros.forEach { miembro ->
                    val button = Button(requireContext())
                    button.text = miembro.nombre
                    button.setBackgroundColor(resources.getColor(android.R.color.white, null))
                    button.setOnClickListener {
                        val result = Bundle().apply {
                            putParcelable("usuarioSeleccionado", miembro)
                        }
                        parentFragmentManager.setFragmentResult("pagadoPor", result)
                        dismiss()
                    }
                    containerMiembros.addView(button)
                }


            } else {
                btnGuardar.visibility = View.VISIBLE

                miembros.forEach { usuario ->
                    val checkBox = CheckBox(requireContext())
                    checkBox.text = usuario.nombre
                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedMiembros.add(usuario)
                        } else {
                            selectedMiembros.remove(usuario)
                        }
                    }
                    containerMiembros.addView(checkBox)
                }
            }
        }

        btnGuardar.setOnClickListener {
            if (selectedMiembros.isNotEmpty()) {
                val idsSeleccionados = selectedMiembros.map { it.id }
                val result = Bundle()
                result.putStringArrayList("resultado", ArrayList(idsSeleccionados))
                parentFragmentManager.setFragmentResult("divididoEntre", result)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "No seleccionaste ningún miembro", Toast.LENGTH_SHORT).show()
            }
        }

        btnCerrar.setOnClickListener { dismiss() }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
