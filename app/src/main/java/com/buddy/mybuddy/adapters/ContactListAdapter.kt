package com.buddy.mybuddy.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.buddy.mybuddy.R
import com.buddy.mybuddy.models.DtContact
import com.buddy.mybuddy.ui.AddContactActivity

import kotlin.collections.ArrayList


class ContactListAdapter(private val context: Context, private val contactList: List<DtContact?>) : BaseAdapter() {


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
                holder.chkcontact=convertView!!.findViewById(R.id.chkBuddy) as CheckBox
                convertView.tag = holder

                if ((context as AddContactActivity).CONTACT_LIST!!.contains(
                        contactList?.get(
                            position
                        )!!.contactID
                    )
                )
                    holder.chkcontact!!.setChecked(true);
                else
                    holder.chkcontact!!.setChecked(false);


                holder.chkcontact!!.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        if (context is AddContactActivity) {
                            if (!(context as AddContactActivity).CONTACT_LIST!!.contains(
                                    contactList?.get(
                                        position
                                    )!!.contactID
                                )
                            )
                                (context as AddContactActivity).CONTACT_LIST!!.add(contactList?.get(position)!!.contactID)
                            /* println("chk changed add id=>" +recList?.get(
                                 position
                             ).id + "now list->"+(context as AddContactActivity).vm.delList)*/
                        }
                    } else {

                        if ((context as AddContactActivity).CONTACT_LIST!!.contains(contactList?.get(position)!!.contactID)) {
                            (context as AddContactActivity).CONTACT_LIST!!.remove(contactList?.get(position)!!.contactID)
                        }
                        /* println("chk changed remove id=>" +recList?.get(
                             position
                         ).id + "now list->"+(context as AddContactActivity).vm.delList)*/
                    }
                }



            } else {
                // the getTag returns the viewHolder object set as a tag to the view
                holder = convertView.tag as ViewHolder
            }

            holder.cname!!.text= contactList!![position]!!.contactName+": " + contactList!![position]!!.contactID

            return convertView
        }

        private inner class ViewHolder {
            var cname: TextView? = null
            var chkcontact: CheckBox? = null
        }
    }
