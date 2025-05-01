package org.itson.tripsplit.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import org.itson.tripsplit.R
import org.itson.tripsplit.data.repository.GrupoRepository
import org.itson.tripsplit.databinding.FragmentGruposBinding

class GruposDetailFragment : Fragment() {

    private lateinit var btnEditGroup: ImageButton
    private lateinit var btnBack: ImageButton
    private val grupoRepository = GrupoRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grupos_detail, container, false)
        val grupoId = arguments?.getString("grupoId") ?: ""
        Log.d("ID GRUPO", grupoId)
        btnEditGroup = view.findViewById(R.id.btnEditGroup)
        btnBack = view.findViewById(R.id.btnBack)
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_gasto)
        fab.setOnClickListener {
            val nuevoGastoFragment = NuevoGastoFragment().apply {
                arguments = Bundle().apply {
                    putString("grupoId", grupoId)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, nuevoGastoFragment)
                .addToBackStack(null)
                .commit()
        }

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUid != null) {
            grupoRepository.esUsuarioCreador(grupoId, currentUid) { esCreador ->
                if (esCreador) {
                    btnEditGroup.visibility = View.VISIBLE
                } else {
                    btnEditGroup.visibility = View.GONE
                }
            }
        }


        // Navegar a org.itson.tripsplit.fragments.EditarGrupoFragment
        btnEditGroup.setOnClickListener {
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
