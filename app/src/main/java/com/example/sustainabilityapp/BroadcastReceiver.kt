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

/**
 * Custom BroadcastReceiver
 */
class AppBroadcastReceiver : android.content.BroadcastReceiver() {
    /**
     * Global variables
     */
    lateinit var activity : MainActivity
    lateinit var manager: WifiP2pManager
    lateinit var channel: WifiP2pManager.Channel

    /**
     * Set global variables
     * @param newActivity - MainActivity instance
     * @param newManager - WifiP2pManager instance
     * @param newChannel - WifiP2pManager Channel instance
     */
    fun setVariables (newActivity: MainActivity, newManager: WifiP2pManager, newChannel: WifiP2pManager.Channel) {
        activity = newActivity
        manager = newManager
        channel = newChannel
    }

    /**
     * Override standard onReceive method to customise handing
     */
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> ({
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                activity.isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
            })
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> (@androidx.annotation.RequiresPermission(
                allOf = [android.Manifest.permission.NEARBY_WIFI_DEVICES]
            ) {
                if ( ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.NEARBY_WIFI_DEVICES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(activity.TAG, "PEER: Insufficient permissions")
                }
                else {
                    manager.requestPeers(channel, activity.peerListListener)
                    Log.d(activity.TAG, "P2P peers changed")
                }

            })
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> ({
                Log.d(activity.TAG, "CONNECTION")
            })
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                Log.d(activity.TAG, "DEVICE")
                Log.d(activity.TAG, activity.supportFragmentManager.fragments.toString())
                Log.d(activity.TAG, activity.supportFragmentManager.findFragmentById(R.id.contentFragmentContainer).toString())
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