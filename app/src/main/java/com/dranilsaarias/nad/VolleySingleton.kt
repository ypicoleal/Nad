package com.dranilsaarias.nad

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import java.net.*


class VolleySingleton {
    private var mRequestQueue: RequestQueue? = null
    private var cookieManager: CookieManager? = null


    private fun getRequestQueue(context: Context): RequestQueue? {
        if (mRequestQueue == null) {
            val siCookieStore = SiCookieStore2(context)
            val cookieManager = CookieManager(siCookieStore as CookieStore, CookiePolicy.ACCEPT_ALL)
            CookieHandler.setDefault(cookieManager)

            mRequestQueue = Volley.newRequestQueue(context.applicationContext)
        }
        return mRequestQueue
    }

    fun <T> addToRequestQueue(req: Request<T>, context: Context) {
        getRequestQueue(context)!!.add(req)
    }

    internal fun cancelAll() {
        mRequestQueue!!.cancelAll { request ->
            Log.d("DEBUG", "request running: " + request.url)
            true
        }
    }

    internal fun getCookie(name: String, url: URI): String? {
        val cookies = cookieManager!!.cookieStore.get(url)
        for (cookie in cookies) {
            if (cookie.name == name) {
                return cookie.value
            }
        }
        return null
    }

    companion object {
        private var mInstance: VolleySingleton? = null

        @Synchronized
        fun getInstance(): VolleySingleton {
            if (mInstance == null) {
                mInstance = VolleySingleton()
            }
            return mInstance as VolleySingleton
        }
    }
}
