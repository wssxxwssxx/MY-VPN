package com.example.chikuvpn.view

import android.os.Bundle
import android.view.Menu
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chikuvpn.R
import com.example.chikuvpn.Utils
import com.example.chikuvpn.adapter.ServerListRVAdapter
import com.example.chikuvpn.interfaces.ChangeServer
import com.example.chikuvpn.interfaces.NavItemClickListener
import com.example.chikuvpn.model.Server
import java.util.*

class MainActivity : AppCompatActivity(),
    NavItemClickListener {
    private val transaction = supportFragmentManager.beginTransaction()
    private var fragment: Fragment? = null
    private var serverListRv: RecyclerView? = null
    private var serverLists: ArrayList<Server>? = null
    private var serverListRVAdapter: ServerListRVAdapter? = null
    private var drawer: DrawerLayout? = null
    private var changeServer: ChangeServer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            // Initialize all variable
            initializeAll()
            val menuRight = findViewById<ImageButton>(R.id.navbar_right)
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            val toggle = ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            )
            drawer!!.addDrawerListener(toggle)
            menuRight.setOnClickListener { closeDrawer() }
            transaction.add(R.id.container, fragment!!)
            transaction.commit()

            // Server List recycler view initialize
            if (serverLists != null) {
                serverListRVAdapter = ServerListRVAdapter(serverLists!!, this)
                serverListRv!!.adapter = serverListRVAdapter
            }

        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Initialize all object, listener etc
     */
    private fun initializeAll() {
        drawer = findViewById(R.id.drawer_layout)
        fragment = MainFragment()
        serverListRv = findViewById(R.id.serverListRv)
        serverListRv!!.setHasFixedSize(true)
        serverListRv!!.setLayoutManager(LinearLayoutManager(this))
        serverLists = getServerList()
        changeServer = fragment as ChangeServer?
    }

    /**
     * Close navigation drawer
     */
    fun closeDrawer() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else {
            drawer!!.openDrawer(GravityCompat.START)
        }
    }

    /**
     * Generate server array list
     */
    private fun getServerList(): ArrayList<Server> {
        val servers: ArrayList<Server> = ArrayList()
        servers.add(
            Server(
                "United States",
                Utils.getImgURL(R.drawable.usa_flag),
                "us.ovpn"
            )
        )
        servers.add(
            Server(
                "Japan",
                Utils.getImgURL(R.drawable.japan),
                "japan.ovpn"
            )
        )
        servers.add(
            Server(
                "Sweden",
                Utils.getImgURL(R.drawable.sweden),
                "sweden.ovpn"
            )
        )

        servers.add(
            Server(
                "Korea",
                Utils.getImgURL(R.drawable.korea),
                "nk.ovpn"
            )
        )

        servers.add(
            Server(
                "USA1",
                Utils.getImgURL(R.drawable.usa_flag),
                "usa.ovpn"
            )
        )

        return servers
    }
    /**
     * On navigation item click, close drawer and change server
     *
     * @param index: server index
     */
    override fun clickedItem(index: Int) {
        closeDrawer()
        changeServer!!.newServer(serverLists!![index])
    }


    companion object {
        const val TAG = "ChikuVPN"
    }
}
