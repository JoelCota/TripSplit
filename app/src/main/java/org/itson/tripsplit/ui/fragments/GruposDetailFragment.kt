package org.itson.tripsplit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_gasto)
        fab.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, NuevoGastoFragment())
                .addToBackStack(null)
                .commit()
        }

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


        return view
    }
}
