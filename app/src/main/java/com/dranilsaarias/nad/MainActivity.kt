package com.dranilsaarias.nad

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
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
import kotlinx.android.synthetic.main.nav_header_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var direccion: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        replaceFragment(AgendarFragment(), getString(R.string.agendar_cita))
        nav_view.setCheckedItem(R.id.nav_agendar)

        setHeader()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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
                replaceFragment(AgendarFragment(), getString(R.string.agendar_cita))
            }

            R.id.nav_mis_citas -> {
                replaceFragment(CitasFragment(), getString(R.string.mis_citas))
            }

            R.id.nav_tos -> {
                val fragment = TosFragment.newInstance(getString(R.string.tos_content))
                replaceFragment(fragment, getString(R.string.tos_title))
            }

            R.id.nav_privacidad -> {
                val fragment = TosFragment.newInstance(getString(R.string.privacy))
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
                    val sAux = "Descarga la App NAD DoctorOnline en tu smartphone. Descargalo hoy desde http://dranilsaarias.com/"
                    i.putExtra(Intent.EXTRA_TEXT, sAux)
                    startActivity(Intent.createChooser(i, "Escoja"))
                } catch (e: Exception) {

                }
            }

            R.id.nav_comentarios -> {
                val fragment = CommentsFragment.newInstance(user_email.text.toString())
                replaceFragment(fragment, getString(R.string.comentarios))
            }

            R.id.nav_conexion -> {
                val fragment = TosFragment.newInstance(getString(R.string.connections))
                replaceFragment(fragment, getString(R.string.condiciones_de_conexion))
            }

            R.id.nav_map -> {
                val fragment = MapsFragment.newInstance(direccion)
                replaceFragment(fragment, getString(R.string.ubicacion_en_mapa))
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
                    findViewById<TextView>(R.id.user_full_name).setText(response.getString("nombre") + " " + response.getString("apellidos"))
                    findViewById<TextView>(R.id.user_email).setText(response.getString("email"))
                    direccion = response.getString("direccion")
                },
                Response.ErrorListener { error ->
                    Log.e("error", error.message)
                })
        VolleySingleton.getInstance().addToRequestQueue(request, this)
    }
}