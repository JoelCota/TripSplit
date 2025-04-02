package org.itson.tripsplit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.itson.tripsplit.R

class SinGruposFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_sin_grupos, container, false)

        val btnNuevoGrupo = rootView.findViewById<Button>(R.id.btnNuevoGrupo)
        val btnUnirseGrupo = rootView.findViewById<Button>(R.id.btnUnirseGrupo)

        btnNuevoGrupo.setOnClickListener {
            // Acción para iniciar un nuevo grupo
            replaceFragment(CrearGrupoFragment())
            Toast.makeText(context, "Comenzar un nuevo grupo", Toast.LENGTH_SHORT).show()
        }

        btnUnirseGrupo.setOnClickListener {
            // Acción para unirse a un grupo
            replaceFragment(UnirseGrupoFragment())
            Toast.makeText(context, "Únete a un grupo", Toast.LENGTH_SHORT).show()
        }

        return rootView
    }

    private fun replaceFragment(fragment: Fragment) {
        // Cargar el fragmento en el FrameLayout
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null) // Opcional, para permitir volver al fragmento anterior
        transaction.commit()
    }
}