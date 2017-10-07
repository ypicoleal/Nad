package com.dranilsaarias.nad

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import org.json.JSONArray


class ImageFragmenPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    var data: JSONArray? = null

    override fun getItem(position: Int): Fragment {
        val ad = data!!.getJSONObject(position)
        return ImageFragment.newInstance(ad.toString())
    }

    override fun getCount(): Int {
        if (data == null) {
            return 0
        }
        return data!!.length()
    }
}
