package org.itson.tripsplit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.itson.tripsplit.Group
import org.itson.tripsplit.R

class GruposFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        val view = inflater.inflate(R.layout.fragment_grupos, container, false)

        // Obtener el LinearLayout donde se agregarán los grupos
        val linearLayoutGroups: LinearLayout = view.findViewById(R.id.linearLayoutGroups)

        // Crear una lista manual de grupos (simulando datos de la base de datos)
        val groupList = listOf(
            Group("Grupo 1", "100€"),
            Group("Grupo 2", "200€"),
            Group("Grupo 3", "Sin gastos")
        )

        // Inflar los items en el ScrollView y agregar listeners
        for (group in groupList) {
            val groupView = LayoutInflater.from(context).inflate(R.layout.list_item_group, linearLayoutGroups, false)

            val groupName: TextView = groupView.findViewById(R.id.group_name)
            val gastos: TextView = groupView.findViewById(R.id.gastos)

            groupName.text = group.groupName
            gastos.text = group.gastos

            // Agregar el listener para navegar a ItemDetailFragment usando FragmentTransaction
            groupView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("groupName", group.groupName)
                bundle.putString("gastos", group.gastos)

                val gruposDetailFragment = GruposDetailFragment()
                gruposDetailFragment.arguments = bundle

                // Reemplazar el fragmento actual con ItemDetailFragment
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, gruposDetailFragment) // Asegúrate de tener el contenedor adecuado
                    .addToBackStack(null) // Agregar el fragmento al back stack si quieres que el usuario pueda volver atrás
                    .commit()
            }

            // Añadir el item al LinearLayout dentro del ScrollView
            linearLayoutGroups.addView(groupView)
        }

        return view
    }
}
