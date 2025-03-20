package org.itson.tripsplit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import org.itson.tripsplit.R

class GruposDetailFragment : Fragment() {

    private lateinit var btnEditTitle: ImageButton
    private lateinit var btnBack: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grupos_detail, container, false)

        btnEditTitle = view.findViewById(R.id.btnEditTitle)
        btnBack = view.findViewById(R.id.btnBack)
        val gastoNieve: View = view.findViewById(R.id.gastoNieve) // Referencia al LinearLayout

        // Navegar a org.itson.tripsplit.fragments.EditarGrupoFragment
        btnEditTitle.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, EditarGrupoFragment())
                .addToBackStack(null)
                .commit()
        }

        // Hacer que btnBack regrese al fragmento anterior
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Hacer que gastoNieve navegue a GastoDetailFragment
        gastoNieve.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, GastoDetailFragment()) // Instancia de GastoDetailFragment
                .addToBackStack(null) // Agrega a la pila de retroceso
                .commit()
        }

        return view
    }
}
