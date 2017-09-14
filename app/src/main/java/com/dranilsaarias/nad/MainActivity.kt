package com.dranilsaarias.nad

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_agendar -> {
                replaceFragment(AgendarFragment(), getString(R.string.agendar_cita))
            }

            R.id.nav_tos -> {
                val fragment = TosFragment.newInstance(getString(R.string.tos_content))
                replaceFragment(fragment, getString(R.string.tos_title))
            }

            R.id.nav_privacidad -> {
                //TODO cambiar el texto por el texto de correspondiente
                val fragment = TosFragment.newInstance(getString(R.string.ipsum))
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
                    var sAux = "\nPrueba esta aplicación\n\n"
                    sAux += getString(R.string.playstore_url) + "\n\n"

                    sAux += getString(R.string.appstore_url) + "\n\n"
                    i.putExtra(Intent.EXTRA_TEXT, sAux)
                    startActivity(Intent.createChooser(i, "Escoja"))
                } catch (e: Exception) {

                }
            }

            R.id.nav_comentarios -> {
                replaceFragment(CommentsFragment(), getString(R.string.comentarios))
            }

            R.id.nav_conexion -> {
                //TODO cambiar el texto por el texto de correspondiente
                val fragment = TosFragment.newInstance(getString(R.string.ipsum))
                replaceFragment(fragment, getString(R.string.condiciones_de_conexion))
            }

            R.id.nav_map -> {
                replaceFragment(MapsFragment(), getString(R.string.ubicacion_en_mapa))
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
}
