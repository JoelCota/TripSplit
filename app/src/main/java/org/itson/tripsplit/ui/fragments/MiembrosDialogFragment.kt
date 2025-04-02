package org.itson.tripsplit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import org.itson.tripsplit.R

class MiembrosDialogFragment : DialogFragment() {

    private var miembros: Array<String>? = null
    private var selectedMiembros: MutableList<String> = mutableListOf()

    companion object {
        private const val ARG_MIEMBROS = "miembros"
        private const val ARG_MODE = "mode"

        fun newInstance(miembros: Array<String>, mode: String): MiembrosDialogFragment {
            val fragment = MiembrosDialogFragment()
            val args = Bundle()
            args.putStringArray(ARG_MIEMBROS, miembros)
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

        miembros = arguments?.getStringArray(ARG_MIEMBROS)
        val mode = arguments?.getString(ARG_MODE)

        val containerMiembros: LinearLayout = rootView.findViewById(R.id.containerMiembros)
        val btnGuardar: Button = rootView.findViewById(R.id.btnGuardar)
        val btnCerrar: Button = rootView.findViewById(R.id.btnCerrar)
        val txtTitulo: TextView = rootView.findViewById(R.id.txtTitulo)

        // Configurar el título del diálogo
        txtTitulo.text = "Miembros"
        txtTitulo.gravity = View.TEXT_ALIGNMENT_CENTER

        if (mode == "single") {
            btnGuardar.visibility = View.GONE // Ocultar botón "Guardar" en modo único

            miembros?.forEach { miembro ->
                val button = Button(requireContext())
                button.text = miembro
                button.setBackgroundColor(resources.getColor(android.R.color.white))
                button.setOnClickListener {
                    (targetFragment as? NuevoGastoFragment)?.actualizarPagadoPor(miembro)
                    // Enviar resultado al fragmento objetivo
                    val result = Bundle()
                    result.putString("resultado", miembro)
                    parentFragmentManager.setFragmentResult("pagadoPor", result)
                    dismiss()
                }
                containerMiembros.addView(button)
            }
        } else {
            btnGuardar.visibility = View.VISIBLE // Mostrar botón "Guardar" en selección múltiple

            miembros?.forEach { miembro ->
                val checkBox = CheckBox(requireContext())
                checkBox.text = miembro
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedMiembros.add(miembro)
                    } else {
                        selectedMiembros.remove(miembro)
                    }
                }
                containerMiembros.addView(checkBox)
            }
        }

        btnGuardar.setOnClickListener {
            if (selectedMiembros.isNotEmpty()) {
                // Enviar los miembros seleccionados al fragmento objetivo
                val result = Bundle()
                result.putStringArrayList("resultado", ArrayList(selectedMiembros))
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
