package org.itson.tripsplit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import org.itson.tripsplit.R

class ListaSeleccionDialogFragment : DialogFragment() {

    interface OnItemSelectedListener {
        fun onItemSelected(item: String)
    }

    private var items: Array<String>? = null
    private var title: String? = null
    private var listener: OnItemSelectedListener? = null

    companion object {
        private const val ARG_ITEMS = "items"
        private const val ARG_TITLE = "title"

        fun newInstance(items: Array<String>, title: String, listener: OnItemSelectedListener): ListaSeleccionDialogFragment {
            val fragment = ListaSeleccionDialogFragment()
            val args = Bundle()
            args.putStringArray(ARG_ITEMS, items)
            args.putString(ARG_TITLE, title)
            fragment.arguments = args
            fragment.listener = listener
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_lista_seleccion, container, false)

        val containerItems: LinearLayout = rootView.findViewById(R.id.containerItems)
        val txtTitulo: TextView = rootView.findViewById(R.id.txtTitulo)

        items = arguments?.getStringArray(ARG_ITEMS)
        title = arguments?.getString(ARG_TITLE)

        txtTitulo.text = title

        items?.forEach { item ->
            val textView = TextView(requireContext())
            textView.text = item
            textView.textSize = 16f
            textView.setPadding(20, 10, 20, 10)
            textView.setOnClickListener {
                listener?.onItemSelected(item)
                dismiss()
            }
            containerItems.addView(textView)
        }

        return rootView
    }
}
