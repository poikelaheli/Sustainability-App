package com.example.sustainabilityapp

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager.PeerListListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment

class DevicesFragment : Fragment(R.layout.devices), PeerListListener {
    companion object {
        private const val TAG = "sustAppDeviceList"
    }

    private var peers = mutableListOf<WifiP2pDevice>()
    private var contentView: View? = null
    private var listAdapter: WiFiPeerListAdapter? = null
    var device: WifiP2pDevice? = null
        private set

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "OnViewCreate")
        contentView = inflater.inflate(R.layout.devices, null)
        return contentView
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listAdapter = WiFiPeerListAdapter(requireActivity(), R.layout.device_list_item, peers)
    }

    private fun getDeviceStatus(deviceStatus: Int): String {
        Log.d(TAG, "Peer status :$deviceStatus")
        return when (deviceStatus) {
            WifiP2pDevice.AVAILABLE -> "Available"
            WifiP2pDevice.INVITED -> "Invited"
            WifiP2pDevice.CONNECTED -> "Connected"
            WifiP2pDevice.FAILED -> "Failed"
            WifiP2pDevice.UNAVAILABLE -> "Unavailable"
            else -> "Unknown"
        }
    }

    override fun onPeersAvailable(peers: WifiP2pDeviceList?) {
        TODO("Not yet implemented")
        this.peers.clear()
        this.peers.addAll(peers?.deviceList?.toMutableList() ?: this.peers)
        for (s in peers?.deviceList!!) {
            Log.d(TAG, "onPeersAvailable, $s")
        }
        listAdapter?.notifyDataSetChanged()
        if (this.peers.isEmpty()) {
            Log.d(TAG, "No devices found")
            return
        }

    }

    private inner class WiFiPeerListAdapter
        (context: Context, textViewResourceId: Int,
        private val items: List<WifiP2pDevice>) : ArrayAdapter<WifiP2pDevice>(context, textViewResourceId, items) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var v = convertView
            if (v == null) {
                val vi = requireActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                v = vi.inflate(R.layout.device_list_item, null)
            }
            val device = items[position]
            if (device != null) {
                val top = v!!.findViewById<View>(R.id.deviceName) as TextView
                val bottom = v.findViewById<View>(R.id.deviceDetails) as TextView
                if (top != null) {
                    top.text = device.deviceName
                }
                if (bottom != null) {
                    bottom.text = getDeviceStatus(device.status)
                    Log.d(TAG, "WiFiPeerListAdapter getView")
                }
            }
            return v!!
        }
        }

    fun updateThisDevice(device: WifiP2pDevice) {
        Log.d(TAG, "DEVICE FRAGMENT")
        Log.d(TAG, device.toString())
        Log.d(TAG, device.status.toString())
        this.device = device

        val nameView = contentView!!.findViewById<View>(R.id.ownDeviceName) as TextView
        val detailView = contentView!!.findViewById<View>(R.id.ownDeviceDetails) as TextView
        nameView.text = device.deviceName
        detailView.text = getDeviceStatus(device.status)
        setUpDummyContent()
    }

    fun setUpDummyContent () {
        var device1 = WifiP2pDevice()
        device1.status = 2
        device1.deviceName = "Test Device 1"
        peers.add(device1)
        var device2 = WifiP2pDevice()
        device2.status = 4
        device2.deviceName = "Test Device 2"
        peers.add(device2)
        listAdapter?.notifyDataSetChanged()
    }
}

