package com.example.sustainabilityapp

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.fragment.app.commit
import com.example.sustainabilityapp.databinding.ActivityMainBinding
import com.example.sustainabilityapp.ui.theme.SustainabilityAppTheme

class MainActivity : AppCompatActivity() {
    companion object {
        private const val DEVICES = "devices"
        private const val LOGIN = "login"
        private const val NETWORKS = "networks"
    }

    /**
     * Private and public variables
     */
    private lateinit var listView : ListView
    private var loggedIn = false
    private var curretView = DEVICES
    private var networkFragmentAdded = false

    lateinit var homeFragment: HomeFragment
    lateinit var loginRegistrationFragment: LoginRegistrationFragment
    lateinit var deviceFragment: DevicesFragment
    lateinit var networksFragment: NetworksFragment

    lateinit var binding: ActivityMainBinding
    lateinit var dbService: DBService
    private val intentFilter = IntentFilter()
    lateinit var channel: WifiP2pManager.Channel
    lateinit var manager: WifiP2pManager
    lateinit var receiver: AppBroadcastReceiver
    lateinit var peerListListener: WifiP2pManager.PeerListListener
    val fragmentManager = supportFragmentManager
    var actionListener = object : WifiP2pManager.ActionListener {

        override fun onSuccess() {
            Log.d(TAG, "$channel")
            Log.d(TAG, "$peerListListener")
        }

        override fun onFailure(reasonCode: Int) {
            Log.d(TAG, "Discovery failure, code: $reasonCode")
        }
    }

    var TAG = "sustApp"
    var isWifiP2pEnabled = false

    private val peers = mutableListOf<WifiP2pDevice>()

    /**
     * Custom onCreate to initialize necessary variables and start processes
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getDeviceList(true, 1000)
        dbService = DBService(this, null)
        initializeFragments()
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.contentFragmentContainer, deviceFragment)
        fragmentTransaction.replace(R.id.networkFragmentContainer, networksFragment)
        fragmentTransaction.commit()
        initiatePeerDiscovery(manager)
    }

    /**
     * Initialize all needed fragments
     */
    fun initializeFragments () {
        homeFragment = HomeFragment()
        deviceFragment = DevicesFragment()
        loginRegistrationFragment = LoginRegistrationFragment()
        networksFragment = NetworksFragment()
        networksFragment.defineDBHelper(dbService)
    }

    /**
     * Custom onResume. Initialize and register BroadcastReceiver
     */
    public override fun onResume() {
        super.onResume()
        receiver = AppBroadcastReceiver()
        receiver.setVariables(this, manager, channel)
        Log.d(TAG, intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION).toString())
        registerReceiver(receiver, intentFilter)
    }

    /**
     * Custom onPause. Pauses custom receiver
     */
    public override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    /**
     * Custom onClick to handle majority of navigation button actions
     */
    @RequiresPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
                if (validateLoginUser()) {

                    this.findViewById<View>(R.id.loginButton).visibility = View.GONE
                    this.findViewById<View>(R.id.logoutButton).visibility = View.VISIBLE
                    this.findViewById<View>(R.id.profileButton).visibility = View.VISIBLE
                    this.findViewById<View>(R.id.networksButton).visibility = View.VISIBLE
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.contentFragmentContainer, deviceFragment)
                    fragmentTransaction.commit()
                    loggedIn = !loggedIn
                    handleDevicesView(v)
                }
            }
            R.id.regFormButton -> {
                if (createNewUser()) {
                    this.findViewById<View>(R.id.loginButton).visibility = View.GONE
                    this.findViewById<View>(R.id.logoutButton).visibility = View.VISIBLE
                    this.findViewById<View>(R.id.profileButton).visibility = View.VISIBLE
                    this.findViewById<View>(R.id.networksButton).visibility = View.VISIBLE
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.contentFragmentContainer, deviceFragment)
                    fragmentTransaction.commit()
                    loggedIn = !loggedIn
                    handleDevicesView(v)
                }

            }
            R.id.loginButton -> {
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.contentFragmentContainer, loginRegistrationFragment)
                fragmentTransaction.commit()
                curretView = LOGIN
            }
            R.id.logoutButton -> {
                loggedIn = !loggedIn
                v.visibility = View.GONE
                this.findViewById<View>(R.id.loginButton).visibility = View.VISIBLE
                this.findViewById<View>(R.id.profileButton).visibility = View.GONE
                this.findViewById<View>(R.id.networksButton).visibility = View.GONE
                if (curretView == NETWORKS) {
                    (this.findViewById<View>(R.id.networkFragmentContainer) as View).visibility = View.GONE
                    (this.findViewById<View>(R.id.contentFragmentContainer) as View).visibility = View.VISIBLE
                }
                handleDevicesView(v)
            }
        }
    }

    /**
     * Validate Login credentials
     * @return Boolean
     */
    fun validateLoginUser(): Boolean {
        val username: EditText = this.findViewById<EditText>(R.id.loginName)
        val password: EditText = this.findViewById<EditText>(R.id.loginPassword)
        try {
            val cursor = dbService.getUser(username.text.toString())
            cursor.use {
                if (cursor.moveToFirst()) {
                    do {
                        val dbUsername = cursor.getString(cursor.getColumnIndexOrThrow(DBService.USERNAME_COL))
                        val dbPassword = cursor.getString(cursor.getColumnIndexOrThrow(DBService.PASSWORD_COL))
                        Log.d(TAG, dbUsername)
                        Log.d(TAG, dbPassword)
                        if (username.text.toString() == dbUsername && password.text.toString() == dbPassword) {
                            username.text.clear()
                            password.text.clear()
                            return true
                        }
                    } while (cursor.moveToNext())
                }
                return false
            }
        }
        catch (e: Exception) {
            Log.d(TAG, "Logging in failed")
            return false
        }
    }

    /**
     * Validate user input and create new user to database
     * @return Boolean
     */
    fun createNewUser(): Boolean {
        val username: EditText = this.findViewById<EditText>(R.id.regName)
        val password1: EditText = this.findViewById<EditText>(R.id.regPassword1)
        val password2: EditText = this.findViewById<EditText>(R.id.regPassword2)
        Log.d(TAG, password1.text.toString())
        Log.d(TAG, password2.text.toString())
        if (password2.text.toString() != password1.text.toString()) {
            // TODO: Throw an error
            return false
        }
        Log.d(TAG, username.text.toString())
        Log.d(TAG, username.toString())
        dbService.addUser(username.text.toString(), password1.text.toString())
        return true
    }

    /**
     * Update layout visibility on loginRegistrationLayout based on pressed button
     * @param buttonName - selected button name
     */
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

    /**
     * Set up intentFilter, peer to peer manager and peerListListener
     */
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
        onResume()
        peerListListener = object : WifiP2pManager.PeerListListener {
            override fun onPeersAvailable(peerList: WifiP2pDeviceList?) {
                Log.d(TAG, "$peerList")
                val refreshedPeers = peerList?.deviceList
                Log.d(TAG, refreshedPeers.toString())
                if (refreshedPeers != peers) {
                    peers.clear()
                    peers.addAll(refreshedPeers?.toMutableList() ?: peers)

                    // If an AdapterView is backed by this data, notify it
                    // of the change. For instance, if you have a ListView of
                    // available peers, trigger an update.
                    //(listAdapter as WiFiPeerListAdapter).notifyDataSetChanged()

                    // Perform any other updates needed based on the new list of
                    // peers connected to the Wi-Fi P2P network.
                }
                if (peers.isEmpty()) {
                    Log.d(TAG, "No devices found")
                }
            }
        }
    }

    /**
     * Initialising WIFI peer device discovery
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
    fun initiatePeerDiscovery (manager: WifiP2pManager) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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
            // Bypassing permissions for prototype purposes
            //return
        }
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank. Code for peer discovery goes in the
                // onReceive method, detailed below.
                Log.d(TAG, "SUCCESS")
            }

            override fun onFailure(reasonCode: Int) {
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.
                Log.d(TAG, "FAILURE")
            }
        })
    }

    /**
     * OnClick event handler for refreshing device list on Device fragment
     */
    fun refreshDeviceList (view: View) {
        var fragment = this.fragmentManager.findFragmentById(R.id.contentFragmentContainer) as DevicesFragment
        fragment.refreshDeviceList()
    }

    /**
     * OnClick event handler for opening Networks view
     */
    fun handleNetworksView(view: View) {
        if (curretView == NETWORKS) {
            return
        }
        Log.d(TAG, networksFragment.toString())
        var networks = ArrayList<Map<String,String>>()
        var cursor = dbService.getAllNetworks()
        cursor.use {
            if (cursor.moveToFirst()) {
                do{
                    val networkName = cursor.getString(cursor.getColumnIndexOrThrow(DBService.NETWORK_NAME))
                    val networkId = cursor.getString(cursor.getColumnIndexOrThrow(DBService.NETWORK_ID))
                    var map = mapOf<String, String>("name" to networkName, "id" to networkId)
                    networks.add(map)
                } while (cursor.moveToNext())
            }
        }

        (this.findViewById<View>(R.id.networkFragmentContainer) as View).visibility = View.VISIBLE
        (this.findViewById<View>(R.id.contentFragmentContainer) as View).visibility = View.GONE
        if (networks.isNotEmpty()) {
            networksFragment.updateNetworksList(networks)
        }
        curretView = NETWORKS
    }

    /**
     * OnClick event handler for opening Device view
     */
    fun handleDevicesView(view: View) {
        if (curretView == DEVICES) {
            return
        }
        if (curretView == NETWORKS) {
            (this.findViewById<View>(R.id.networkFragmentContainer) as View).visibility = View.GONE
            (this.findViewById<View>(R.id.contentFragmentContainer) as View).visibility = View.VISIBLE
        }
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.contentFragmentContainer, deviceFragment)
        fragmentTransaction.commit()
        curretView = DEVICES

    }
}