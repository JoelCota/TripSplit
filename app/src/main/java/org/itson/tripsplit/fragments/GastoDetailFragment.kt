package org.itson.tripsplit.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import org.itson.tripsplit.R

class GastoDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gasto_detail, container, false)

        // Encontrar el botón
        val btnBack: ImageButton = view.findViewById(R.id.btnBack)


        // Hacer que btnBack regrese al fragmento anterior
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack() // Esto regresa al fragmento anterior
        }



        return view
    }
}
