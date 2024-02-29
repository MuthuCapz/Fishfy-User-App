package com.example.seafishfy.ui.activities.adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.seafishfy.R

class AddressAdapter(context: Context, private val addresses: List<String>) :
    ArrayAdapter<String>(context, R.layout.item_saved_address, addresses) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.item_saved_address, parent, false)
        }

        val addressTextView = view?.findViewById<TextView>(R.id.addresstextview)
        addressTextView?.text = addresses[position]

        return view!!
    }
}
