package com.example.sustainabilityapp

import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import androidx.fragment.app.Fragment

class DevicesFragment : Fragment(R.layout.devices){
}

public fun DevicesFragment.updateThisDevice(device: WifiP2pDevice) {
    Log.e("sustApp", device.status.toString())
}
