package com.example.sustainabilityapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment

class NetworksFragment : Fragment(R.layout.networks){
    companion object {
        private const val TAG = "sustAppNetworkList"
    }
    private var networks = ArrayList<Map<String, String>>()
    private var contentView: View? = null
    private var listAdapter: NetworksListAdapter? = null
    private var dbService: DBService? = null

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "OnActivatyCreate")
        listAdapter = NetworksListAdapter(getActivity() as MainActivity, R.layout.network_list_item, networks)
        var listview = contentView?.findViewById<ListView>(R.id.networksListView)
        listview?.adapter = listAdapter
        Log.d(TAG, listview.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, getActivity().toString())
        Log.d(TAG, contentView.toString())
        listAdapter = NetworksListAdapter(requireActivity() as MainActivity, R.layout.network_list_item, networks)
        var listview = contentView?.findViewById<ListView>(R.id.networksListView)
        listview?.adapter = listAdapter
        Log.d(TAG, listview.toString())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "OnViewCreate")
        contentView = inflater.inflate(R.layout.networks, null)
        Log.d(TAG, getActivity().toString())
        Log.d(TAG, contentView.toString())
        listAdapter = NetworksListAdapter(requireActivity() as MainActivity, R.layout.network_list_item, networks)
        var listview = contentView?.findViewById<ListView>(R.id.networksListView)
        listview?.adapter = listAdapter
        Log.d(TAG, listview.toString())
        return contentView
    }

    fun defineDBHelper (db: DBService) {
        dbService = db
    }

    fun updateNetworksList(newList: ArrayList<Map<String, String>>) {
        networks = newList
        if (listAdapter == null) {
            Log.d(TAG, getActivity().toString())
            Log.d(TAG, contentView.toString())
            listAdapter = NetworksListAdapter(requireActivity() as MainActivity, R.layout.network_list_item, networks)
            var listview = contentView?.findViewById<ListView>(R.id.networksListView)
            listview?.adapter = listAdapter
            Log.d(TAG, listview.toString())
        }
        (listAdapter as NetworksListAdapter).notifyDataSetChanged()
    }
    /**
     * Custom ArrayAdapter for list view
     */
    private inner class NetworksListAdapter
        (context: Context, textViewResourceId: Int,
         private val items: List<Map<String, String>>) : ArrayAdapter<Map<String, String>>(context, textViewResourceId, items) {
        /**
         * Overriding standard getView method to manage shown data and the format
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            Log.d(TAG, "GET VIEW")
            var v = convertView
            val vi = requireActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if (v == null) {
                v = vi.inflate(R.layout.network_list_item, null)
            }
            val network = items[position]
            if (network != null) {
                val name = v!!.findViewById<View>(R.id.networkName) as TextView
                name.text = network["name"]
                var list = v.findViewById<View>(R.id.devicesListLayout) as LinearLayout
                list.removeAllViews()
                try {
                    var cursor = dbService?.getDevicesByNetwork(network["id"]?.toInt() ?: -1 )
                    cursor.use {
                        if (cursor?.moveToFirst() == true) {
                            do {
                                val deviceName= cursor.getString(cursor.getColumnIndexOrThrow(DBService.DEVICE_NAME))
                                val deviceStatus = cursor.getString(cursor.getColumnIndexOrThrow(DBService.DEVICE_STATUS))
                                Log.d(TAG, deviceName)
                                Log.d(TAG, deviceStatus)
                                var line = vi.inflate (R.layout.device_list_item, null)
                                (line.findViewById<View>(R.id.deviceName) as TextView).text = deviceName
                                (line.findViewById<View>(R.id.deviceDetails) as TextView).text = deviceStatus
                                list.addView(line)
                            } while (cursor.moveToNext())
                        }
                    }
                    return v
                }
                catch (e: Exception) {
                    Log.d(TAG, "Logging in failed: ${e.message}")
                }
            }
            return v!!
        }
    }
}