package com.dranilsaarias.nad

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var direccion: String = ""
    private var tos: String = ""
    private var privacy: String = ""
    private var conection: String = ""
    private var isPacient: Boolean = true
    private var accountData: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        setTos()
        setHeader()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            AlertDialog
                    .Builder(this)
                    .setMessage("¿Desea Salir?")
                    .setPositiveButton("Si", { _, _ ->
                        finish()
                    })
                    .setNegativeButton("No", { _, _ ->

                    })
                    .create()
                    .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_exit -> {
                logout(true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_agendar -> {
                var title = getString(R.string.agendar_cita)
                if (!isPacient) {
                    title = "Calendario"
                }
                val fragment = AgendarFragment.newInstance(isPacient)
                replaceFragment(fragment, title)
            }

            R.id.nav_mis_citas -> {
                val fragment = CitasFragment()
                val args = Bundle()
                args.putBoolean(CitasFragment.ARG_PACIENT, isPacient)
                fragment.arguments = args
                replaceFragment(fragment, getString(R.string.mis_citas))
            }

            R.id.nav_tos -> {
                val fragment = TosFragment.newInstance(tos)
                replaceFragment(fragment, getString(R.string.tos_title))
            }

            R.id.nav_privacidad -> {
                val fragment = TosFragment.newInstance(privacy)
                replaceFragment(fragment, getString(R.string.politicas_de_privacidad))
            }

            R.id.nav_web -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.web_url)))
                startActivity(browserIntent)
            }

            R.id.nav_share -> {
                try {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "text/plain"
                    i.putExtra(Intent.EXTRA_SUBJECT, "Doctor Online")
                    val sAux = "Descarga la App de Agendamiento Médico NAD DoctorOnline desde tu dispositivo móvil. (AQUI VA LA URL QUE GENEREN LAS TIENDAS)"
                    i.putExtra(Intent.EXTRA_TEXT, sAux)
                    startActivity(Intent.createChooser(i, "Escoja"))
                } catch (e: Exception) {

                }
            }

            R.id.nav_comentarios -> {
                //todo realizar videollamada
                //todo agregar notificaciones
                //todo recibir llamada
                val fragment = CommentsFragment.newInstance(accountData!!.getString("email"))
                replaceFragment(fragment, getString(R.string.comentarios))
            }

            R.id.nav_conexion -> {
                val fragment = TosFragment.newInstance(conection)
                replaceFragment(fragment, getString(R.string.condiciones_de_conexion))
            }

            R.id.nav_map -> {
                val fragment = MapsFragment.newInstance(direccion)
                replaceFragment(fragment, getString(R.string.ubicacion_en_mapa))
            }

            R.id.nav_cuenta -> {
                if (isPacient) {
                    replaceFragment(CuentaFragment.newInstance(accountData.toString()), getString(R.string.mi_cuenta))
                } else {
                    AlertDialog
                            .Builder(this)
                            .setMessage("Para modificar sus datos por favor dirigirse al aplicativo web")
                            .setPositiveButton("Ok", { _, _ ->

                            })
                            .create()
                            .show()
                }
            }

            R.id.nav_salir -> {
                logout(true)
            }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment, fragment)
                .commit()
        toolbar.title = title
    }

    private fun logout(openLogin: Boolean) {
        val serviceUrl = getString(R.string.logout)
        val url = getString(R.string.host, serviceUrl)

        val request = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    Log.e("tales", response)
                    finish()
                    if (openLogin) {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.networkResponse != null && error.networkResponse.statusCode == 403) {
                        Log.e("error", String(error.networkResponse.data))
                        Snackbar.make(loading, "Usuario y/o contraseña incorrecta", Snackbar.LENGTH_LONG).show()
                    } else {
                        Snackbar.make(loading, "Al parecer hubo un error en la peticion intentelo nuevamente mas tarde", Snackbar.LENGTH_LONG).show()
                    }
                })
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        loading.visibility = View.VISIBLE
    }

    private fun setHeader() {
        val serviceUrl = getString(R.string.is_login)
        val url = getString(R.string.host, serviceUrl)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    Log.i("user", response.toString())
                    accountData = response
                    findViewById<TextView>(R.id.user_full_name).setText(response.getString("nombre") + " " + response.getString("apellidos"))
                    findViewById<TextView>(R.id.user_email).setText(response.getString("email"))
                    isPacient = response.getInt("tipo") == 1
                    setupMenu()
                    loading.visibility = View.GONE

                    val fragment = AgendarFragment.newInstance(isPacient)
                    var title = getString(R.string.agendar_cita)
                    if (!isPacient) {
                        title = "Calendario"
                    }
                    replaceFragment(fragment, title)
                    nav_view.setCheckedItem(R.id.nav_agendar)
                },
                Response.ErrorListener { error ->
                    if (error.message != null) {
                        Log.e("error", error.networkResponse.toString())
                    }
                    loading.visibility = View.GONE
                })
        request.setShouldCache(false)
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        loading.visibility = View.VISIBLE
    }

    private fun setTos() {
        val serviceUrl = getString(R.string.tos_url)
        val url = getString(R.string.host, serviceUrl)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    Log.i("user", response.toString())
                    tos = response.getString("terminos")
                    conection = response.getString("condiciones")
                    privacy = response.getString("politica")
                    direccion = response.getString("direccion")

                },
                Response.ErrorListener { error ->
                    if (error.networkResponse != null) {
                        Log.e("error", error.networkResponse.toString())
                    }
                })
        request.setShouldCache(false)
        VolleySingleton.getInstance().addToRequestQueue(request, this)
    }

    private fun setupMenu() {
        Log.i("paciente", isPacient.toString())
        if (!isPacient) {
            nav_view.menu.findItem(R.id.legal_group).isVisible = false
            nav_view.menu.findItem(R.id.nav_comentarios).isVisible = false
            nav_view.menu.findItem(R.id.nav_agendar).setTitle("Calendario")
        }
    }
}