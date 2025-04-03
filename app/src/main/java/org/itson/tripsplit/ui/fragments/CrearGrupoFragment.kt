package org.itson.tripsplit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import org.itson.tripsplit.R
import org.itson.tripsplit.data.model.GrupoRepository
import org.itson.tripsplit.databinding.FragmentCrearGrupoBinding

class CrearGrupoFragment : Fragment() {

    private lateinit var binding: FragmentCrearGrupoBinding
    private val grupoRepository = GrupoRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCrearGrupoBinding.inflate(inflater, container, false)

        binding.btnCrearGrupo.setOnClickListener {
            val nombreGrupo = binding.edtGroupName.text.toString()
            if (nombreGrupo.isNotEmpty()) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                grupoRepository.crearGrupo(nombreGrupo, userId) { success ->
                    if (success) {
                        Toast.makeText(context, "Grupo creado", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_crearGrupoFragment_to_gruposFragment)
                    } else {
                        Toast.makeText(context, "Error al crear grupo", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Ingrese un nombre", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }
}