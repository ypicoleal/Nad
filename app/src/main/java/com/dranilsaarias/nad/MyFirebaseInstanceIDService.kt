package com.dranilsaarias.nad

import android.provider.Settings.Secure
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        super.onTokenRefresh()
        val refreshedToken = FirebaseInstanceId.getInstance().token
        val deviceId = Secure.getString(this.contentResolver,
                Secure.ANDROID_ID)

        Log.i("token", refreshedToken)
        Log.i("device_id", deviceId)

        sendRegistrationToServer(refreshedToken, deviceId)
    }

    private fun sendRegistrationToServer(refreshedToken: String?, deviceId: String) {
        val serviceUrl = getString(R.string.token_registration)
        val url = getString(R.string.host, serviceUrl)

        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    Log.e("tales", response)
                },
                Response.ErrorListener { error ->

                    if (error.networkResponse != null) {
                        Log.e("error", String(error.networkResponse.data))
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("registration_id", refreshedToken!!)
                params.put("device_id", deviceId)
                params.put("type", "android")
                return params
            }
        }
        request.setShouldCache(false)
        VolleySingleton.getInstance().addToRequestQueue(request, this)
    }


}
