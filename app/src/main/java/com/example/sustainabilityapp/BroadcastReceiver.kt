package com.example.sustainabilityapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.IntentCompat

class BroadcastReceiver {
    val activity =  MainActivity()
    val manager = activity.manager
    val channel = activity.channel

    fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> ({
                // Determine if Wi-Fi Direct mode is enabled or not, alert
                // the Activity.
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                activity.isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
            })
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> (@androidx.annotation.RequiresPermission(
                allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.NEARBY_WIFI_DEVICES]
            ) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.NEARBY_WIFI_DEVICES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Log.d(activity.TAG, "Insufficient permissions")
                }
                else {
                    // The peer list has changed! We should probably do something about
                    // that.
                    // Request available peers from the wifi p2p manager. This is an
                    // asynchronous call and the calling activity is notified with a
                    // callback on PeerListListener.onPeersAvailable()
                    manager.requestPeers(channel, activity.peerListListener)
                    Log.d(activity.TAG, "P2P peers changed")
                }

            })
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> ({

                // Connection state changed! We should probably do something about
                // that.

            })
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                (activity.supportFragmentManager.findFragmentById(R.id.contentFragmentContainer) as DevicesFragment)
                    .apply {
                        updateThisDevice(
                            IntentCompat.getParcelableExtra(intent,
                                WifiP2pManager.EXTRA_WIFI_P2P_DEVICE, WifiP2pDevice::class.java) as WifiP2pDevice
                        )
                    }
            }
        }
    }
}