package com.example.sustainabilityapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.sustainabilityapp.databinding.ActivityMainBinding
import com.example.sustainabilityapp.ui.theme.SustainabilityAppTheme
import java.net.InetAddress

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val intentFilter = IntentFilter()
    lateinit var channel: WifiP2pManager.Channel
    lateinit var manager: WifiP2pManager

    private var port = 2020
    private var mServiceName = ""
    private var mServiceType = "_nsdchat._tcp."
    private lateinit var nsdManager: NsdManager
    private lateinit var mService: NsdServiceInfo

    var TAG = "sustApp"
    var isWifiP2pEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getDeviceList(true, 1000)
        //registerService(port)
        //nsdManager.discoverServices(mServiceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    @RequiresPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
    fun onClick(v: View?) {
        when (v?.id) {
            R.id.openLogin -> {
                updateLayoutVisibility("openLogin")
            }
            R.id.openRegistration -> {
                updateLayoutVisibility("openRegistration")
            }
            R.id.returnButton -> {
                updateLayoutVisibility("returnButton")
            }
            R.id.loginFormButton -> {
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainer, HomeFragment())
                fragmentTransaction.commit()
                initiatePeerDiscovery(manager)
            }
            R.id.regFormButton -> {
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainer, HomeFragment())
                fragmentTransaction.commit()
                initiatePeerDiscovery(manager)
            }
            R.id.logoutButton -> {
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainer, LoginRegistrationFragment())
                fragmentTransaction.commit()
            }
        }
    }

    fun updateLayoutVisibility (buttonName: String) {
        val loginButtons: LinearLayout = this.findViewById<LinearLayout>(R.id.loginButtons)
        val loginForm: LinearLayout = this.findViewById<LinearLayout>(R.id.loginForm)
        val registrationForm: LinearLayout = this.findViewById<LinearLayout>(R.id.registrationForm)
        val navigationButtons: LinearLayout = this.findViewById<LinearLayout>(R.id.navigationButtons)
        when (buttonName) {
            "openLogin" -> {
                loginButtons.visibility = View.GONE
                loginForm.visibility = View.VISIBLE
                registrationForm.visibility = View.GONE
                navigationButtons.visibility = View.VISIBLE
            }
            "openRegistration" -> {
                loginButtons.visibility = View.GONE
                loginForm.visibility = View.GONE
                registrationForm.visibility = View.VISIBLE
                navigationButtons.visibility = View.VISIBLE
            }
            "returnButton" -> {
                loginButtons.visibility = View.VISIBLE
                loginForm.visibility = View.GONE
                registrationForm.visibility = View.GONE
                navigationButtons.visibility = View.INVISIBLE
            }
        }
    }
    fun getDeviceList(onlyReachable: Boolean, reachableTimeout: Int) {
        // Indicates a change in the Wi-Fi Direct status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicates the state of Wi-Fi Direct connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
    }

    @RequiresPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
    fun initiatePeerDiscovery (manager: WifiP2pManager) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
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
            Log.d(TAG, "Insufficient permissions")
            Log.d(TAG, ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).toString()
            )
            Log.d(TAG, ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ).toString()
            )
            //return
        }
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank. Code for peer discovery goes in the
                // onReceive method, detailed below.
            }

            override fun onFailure(reasonCode: Int) {
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.
                Log.d(TAG, "Discovery failure, code: $reasonCode")
            }
        })
    }

    private val peers = mutableListOf<WifiP2pDevice>()

    val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        Log.d(TAG, refreshedPeers.toString())
        if (refreshedPeers != peers) {
            peers.clear()
            peers.addAll(refreshedPeers)

            // If an AdapterView is backed by this data, notify it
            // of the change. For instance, if you have a ListView of
            // available peers, trigger an update.
            //(listAdapter as WiFiPeerListAdapter).notifyDataSetChanged()

            // Perform any other updates needed based on the new list of
            // peers connected to the Wi-Fi P2P network.
        }

        if (peers.isEmpty()) {
            Log.d(TAG, "No devices found")
            return@PeerListListener
        }
    }

    /*fun registerService(port: Int) {
        // Create the NsdServiceInfo object, and populate it.
        val serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceName = "NsdChat"
            serviceType = mServiceType
            setPort(port)
        }
        nsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
            registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        }
    }


    private val registrationListener = object : NsdManager.RegistrationListener {

        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            mServiceName = NsdServiceInfo.serviceName
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Registration failed! Put debugging code here to determine why.
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            // Service has been unregistered. This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Unregistration failed. Put debugging code here to determine why.
        }
    }
    // Instantiate a new DiscoveryListener
    private val discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d(TAG, "Service discovery success$service")
            when {
                service.serviceType != mServiceType -> // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
                service.serviceName == mServiceName -> // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: $mServiceName")
                service.serviceName.contains("NsdChat") -> nsdManager.resolveService(service, resolveListener)
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost: $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }
    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "Resolve Succeeded. $serviceInfo")

            if (serviceInfo.serviceName == mServiceName) {
                Log.d(TAG, "Same IP.")
                return
            }
            mService = serviceInfo
            val port: Int = serviceInfo.port
            val host: InetAddress = serviceInfo.host
        }
    }*/
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SustainabilityAppTheme {
        Greeting("Android")
    }
}