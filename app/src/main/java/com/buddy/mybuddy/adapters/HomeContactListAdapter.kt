package com.buddy.mybuddy.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.buddy.mybuddy.R
import com.buddy.mybuddy.models.DtContact
import com.buddy.mybuddy.ui.AddContactActivity

import kotlin.collections.ArrayList


class HomeContactListAdapter(private val context: Context, private val contactList: List<String?>) : BaseAdapter() {


        override fun getViewTypeCount(): Int {
            return count
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun getCount(): Int {
            return contactList!!.size
        }

        override fun getItem(position: Int): Any {
            return contactList!!.get(position)!!
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            Log.i("chktot:",contactList!!.size.toString())
            var convertView = convertView
            val holder: ViewHolder

            if (convertView == null) {
                holder = ViewHolder()
                val inflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertView = inflater.inflate(R.layout.add_buddy_item, null, true)

                holder.cname = convertView!!.findViewById(R.id.txtBuddy) as TextView
                holder.btnGo=convertView!!.findViewById(R.id.chkBuddy) as CheckBox
                convertView.tag = holder




                holder.btnGo!!.setOnClickListener {
                    Log.i("chkpress","i press")
                }



            } else {
                // the getTag returns the viewHolder object set as a tag to the view
                holder = convertView.tag as ViewHolder
            }

            holder.cname!!.text= contactList!![position]!!

            return convertView
        }

        private inner class ViewHolder {
            var cname: TextView? = null
            var btnGo: Button? = null
        }
    }
