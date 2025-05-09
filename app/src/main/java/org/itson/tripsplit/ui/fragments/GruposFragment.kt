package org.itson.tripsplit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import org.itson.tripsplit.R
import org.itson.tripsplit.data.repository.GrupoRepository
import org.itson.tripsplit.databinding.FragmentGruposBinding
import org.itson.tripsplit.data.model.Grupo
import org.itson.tripsplit.data.repository.UserRepository
import org.itson.tripsplit.repository.GastoRepository

class GruposFragment : Fragment() {

    private lateinit var binding: FragmentGruposBinding
    private val userRepository = UserRepository()
    private val gastoRepository = GastoRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inicializar el binding
        binding = FragmentGruposBinding.inflate(inflater, container, false)

        // Configurar el FAB
        binding.fabAddGrupo.setOnClickListener { view ->
            mostrarPopupMenu(view)
        }

        cargarGrupos()

        return binding.root
    }

    private fun cargarGrupos() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userRepository.getUserGroups(userId) { grupos ->
            if (grupos.isEmpty()) {
                Toast.makeText(context, "No hay grupos disponibles", Toast.LENGTH_SHORT).show()
            } else {
                mostrarGrupos(grupos)
            }
        }
    }

    private fun mostrarGrupos(grupos: List<Grupo>) {
        val linearLayoutGroups = binding.linearLayoutGroups
        linearLayoutGroups.removeAllViews()

        for (grupo in grupos) {
            val groupView = LayoutInflater.from(context).inflate(R.layout.list_item_group, linearLayoutGroups, false)

            val groupNameTextView: TextView = groupView.findViewById(R.id.group_name)
            val groupDescriptionTextView: TextView = groupView.findViewById(R.id.group_description)
            val gastosTextView: TextView = groupView.findViewById(R.id.gastos)
            groupNameTextView.text = grupo.nombre
            gastosTextView.text = "Te deben $0.00"
            gastoRepository.obtenerGastosConTotal(grupo.id) { listaGastos, total ->
                groupDescriptionTextView.text = "Total: $${"%.2f".format(total)}"
            }
            groupView.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("grupoId", grupo.id)
                }



                val detalleFragment = GruposDetailFragment().apply {
                    arguments = bundle
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, detalleFragment)
                    .addToBackStack(null)
                    .commit()
            }

            linearLayoutGroups.addView(groupView)
        }
    }

    private fun mostrarPopupMenu(view: View) {
        val popupView = layoutInflater.inflate(R.layout.popup_menu_add_group, null)
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        val createGroup = popupView.findViewById<TextView>(R.id.menu_create_group)
        val joinGroup = popupView.findViewById<TextView>(R.id.menu_join_group)

        createGroup.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CrearGrupoFragment())
                .addToBackStack(null)
                .commit()
            popupWindow.dismiss()
        }

        joinGroup.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, UnirseGrupoFragment())
                .addToBackStack(null)
                .commit()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(view, -view.width - 250, -view.height - 90)
    }
}
