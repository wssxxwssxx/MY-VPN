package com.example.chikuvpn.view

import android.app.Activity
import android.content.*
import android.net.VpnService
import android.os.Bundle
import android.os.RemoteException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.chikuvpn.CheckInternetConnection
import com.example.chikuvpn.R
import com.example.chikuvpn.SharedPreference
import com.example.chikuvpn.databinding.FragmentMainBinding
import com.example.chikuvpn.interfaces.ChangeServer
import com.example.chikuvpn.model.Server
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNThread
import de.blinkt.openvpn.core.VpnStatus
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainFragment : Fragment(), View.OnClickListener,
    ChangeServer {
    private var server: Server? = null
    private var connection: CheckInternetConnection? = null
    private val vpnThread = OpenVPNThread()
    private val vpnService = OpenVPNService()
    var vpnStart = false
    private var preference: SharedPreference? = null
    private var binding: FragmentMainBinding? = null
    var powerButton: ImageView? = null
    var logTxt: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        val view = binding!!.getRoot()
        initializeAll()
        powerButton = view.findViewById(R.id.power_button)
        logTxt = view.findViewById(R.id.logTv)
        powerButton!!.setOnClickListener(View.OnClickListener {
            if (vpnStart) {
                confirmDisconnect()
            } else {
                prepareVpn()
            }
        })

        return view
    }

    /**
     * Initialize all variable and object
     */
    private fun initializeAll() {
        preference = SharedPreference(context!!)
        server = preference!!.getServer()

        // Update current selected server icon
        updateCurrentServerIcon(server!!.flagUrl)
        connection = CheckInternetConnection()
        LocalBroadcastManager.getInstance(activity!!)
            .registerReceiver(broadcastReceiver, IntentFilter("connectionState"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.vpnBtn.setOnClickListener(this)

        // Checking is vpn already running or not
        isServiceRunning
        VpnStatus.initLogCache(activity!!.cacheDir)
    }

    /**
     * @param v: click listener view
     */
    override fun onClick(v: View) {
        when (v.id) {
            R.id.vpnBtn -> if (vpnStart) {
                confirmDisconnect()
            } else {
                prepareVpn()
            }
        }
    }

    /**
     * Show show disconnect confirm dialog
     */
    fun confirmDisconnect() {
        val builder = AlertDialog.Builder(
            activity!!
        )
        builder.setMessage(activity!!.getString(R.string.connection_close_confirm))
        builder.setPositiveButton(
            activity!!.getString(R.string.yes)
        ) { dialog, id -> stopVpn() }
        builder.setNegativeButton(
            activity!!.getString(R.string.no)
        ) { dialog, id ->
            // User cancelled the dialog
        }

        // Create the AlertDialog
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Prepare for vpn connect with required permission
     */
    private fun prepareVpn() {
        if (!vpnStart) {
            if (internetStatus) {

                // Checking permission for network monitor
                val intent = VpnService.prepare(context)
                if (intent != null) {
                    startActivityForResult(intent, 1)
                } else startVpn() //have already permission

                // Update confection status
                status("connecting")
            } else {

                // No internet connection available
                showToast("you have no internet connection !!")
            }
        } else if (stopVpn()) {

            // VPN is stopped, show a Toast message.
            showToast("Disconnect Successfully")
        }
    }

    /**
     * Stop vpn
     *
     * @return boolean: VPN status
     */
    fun stopVpn(): Boolean {
        try {
            OpenVPNThread.stop()
            status("connect")
            vpnStart = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Taking permission for network access
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            //Permission granted, start the VPN
            startVpn()
        } else {
            showToast("Permission Deny !! ")
        }
    }

    /**
     * Internet connection status.
     */
    val internetStatus: Boolean
        get() = connection!!.netCheck(context!!)

    /**
     * Get service status
     */
    val isServiceRunning: Unit
        get() {
            setStatus(OpenVPNService.getStatus())
        }

    /**
     * Start the VPN
     */
    private fun startVpn() {
        try {
// .ovpn file
            val conf = activity?.assets?.open(server!!.ovpn)
            val isr = InputStreamReader(conf)
            val br = BufferedReader(isr)
            var config = ""
            var line: String?
            while (true) {
                line = br.readLine()
                if (line == null) break
                config += "$line\n"
            }

            br.readLine()
            OpenVpnApi.startVpn(context, config, server?.country, server?.ovpnUserName, server?.ovpnUserPassword)

            // Update log
            binding!!.logTv.text = "Connecting..."
            vpnStart = true

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    /**
     * Status change with corresponding vpn connection status
     *
     * @param connectionState
     */
    fun setStatus(connectionState: String?) {
        if (connectionState != null) when (connectionState) {
            "DISCONNECTED" -> {
                status("connect")
                vpnStart = false
                OpenVPNService.setDefaultStatus()
            }
            "CONNECTED" -> {
                vpnStart = true // it will use after restart this activity
                status("connected")
                binding!!.logTv.text = "Connected"
                logTxt!!.setTextColor(resources.getColor(R.color.colorPrimary))
            }
            "WAIT" -> binding!!.logTv.text = "waiting for server connection!!"
            "AUTH" -> binding!!.logTv.text = "server authenticating!!"
            "RECONNECTING" -> {
                status("connecting")
                binding!!.logTv.text = "Reconnecting..."
            }
            "NONETWORK" -> binding!!.logTv.text = "No network connection"
        }
    }

    /**
     * Change button background color and text
     *
     * @param status: VPN current status
     */
    //If You are Using button then you can use setText() method
    fun status(status: String) {
        if (status == "connect") {
            powerButton!!.setImageResource(R.drawable.power_off)
            logTxt!!.text = getString(R.string.not_connect)
            logTxt!!.setTextColor(resources.getColor(R.color.red))
        } else if (status == "connecting") {
            powerButton!!.setImageResource(R.drawable.power_connecting)
        } else if (status == "connected") {
            powerButton!!.setImageResource(R.drawable.power_on)
        } else if (status == "tryDifferentServer") {
            powerButton!!.setImageResource(R.drawable.power_on)

            // binding.vpnBtn.setText("Try Different\nServer");
        } else if (status == "loading") {
            powerButton!!.setImageResource(R.drawable.power_off)

            // binding.vpnBtn.setText("Loading Server..");
        } else if (status == "invalidDevice") {
            powerButton!!.setImageResource(R.drawable.power_on)

            // binding.vpnBtn.setText("Invalid Device");
        } else if (status == "authenticationCheck") {
            powerButton!!.setImageResource(R.drawable.power_connecting)


            // binding.vpnBtn.setText("Authentication \n Checking...");
        }
    }

    /**
     * Receive broadcast message
     */
    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                setStatus(intent.getStringExtra("state"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                var duration = intent.getStringExtra("duration")
                var lastPacketReceive = intent.getStringExtra("lastPacketReceive")
                var byteIn = intent.getStringExtra("byteIn")
                var byteOut = intent.getStringExtra("byteOut")
                if (duration == null) duration = "00:00:00"
                if (lastPacketReceive == null) lastPacketReceive = "0"
                if (byteIn == null) byteIn = " "
                if (byteOut == null) byteOut = " "
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Update status UI
     *
     * @param duration:          running time
     * @param lastPacketReceive: last packet receive time
     * @param byteIn:            incoming data
     * @param byteOut:           outgoing data
     */
    fun updateConnectionStatus(
        duration: String?,
        lastPacketReceive: String,
        byteIn: String?,
        byteOut: String?
    ) {
        binding!!.durationTv.text = duration
        binding!!.lastPacketReceiveTv.text = "$lastPacketReceive sec."

        //Speed Download
        binding!!.byteInTv.text = byteIn
        //Speed upload
        binding!!.byteOutTv.text = byteOut
    }

    /**
     * Show toast message
     *
     * @param message: toast message
     */
    fun showToast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * VPN server country icon change
     *
     * @param serverIcon: icon URL
     */
    fun updateCurrentServerIcon(serverIcon: String?) {
        Glide.with(context!!)
            .load(serverIcon)
            .into(binding!!.connectedCountry)
    }

    /**
     * Change server when user select new server
     *
     * @param server ovpn server details
     */
    override fun newServer(server: Server) {
        this.server = server
        updateCurrentServerIcon(server.flagUrl)

        // Stop previous connection
        if (vpnStart) {
            stopVpn()
        }
        prepareVpn()
    }

    override fun onResume() {
        if (server == null) {
            server = preference!!.getServer()
        }
        super.onResume()
    }

    /**
     * Save current selected server on local shared preference
     */
    override fun onStop() {
        if (server != null) {
            preference!!.saveServer(server!!)
        }
        super.onStop()
    }
}