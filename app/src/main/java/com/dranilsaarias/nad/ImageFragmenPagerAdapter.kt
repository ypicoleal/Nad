package com.dranilsaarias.nad

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter


class ImageFragmenPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return ImageFragment()
    }

    override fun getCount(): Int {
        return 3
    }
}
