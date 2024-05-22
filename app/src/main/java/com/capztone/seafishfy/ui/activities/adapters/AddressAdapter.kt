package com.capztone.seafishfy.ui.activities.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.capztone.seafishfy.R

class AddressAdapter(context: Context, private var addresses: List<String>) :
    ArrayAdapter<String>(context, R.layout.item_saved_address, addresses) {

    fun updateAddresses(newAddresses: List<String>) {
        addresses = newAddresses
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_saved_address, parent, false)
            holder = ViewHolder()
            holder.addressTextView = view.findViewById(R.id.addresstextview)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val address = getItem(position)
        holder.addressTextView?.text = address

        return view!!
    }

    private class ViewHolder {
        var addressTextView: TextView? = null
    }
}
