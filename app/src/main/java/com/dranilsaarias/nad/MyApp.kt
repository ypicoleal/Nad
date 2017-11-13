package com.dranilsaarias.nad

import android.app.Application


class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        TypefaceUtil.overrideFont(applicationContext, "SERIF", "fonts/futurabt_book.otf") // font from assets: "assets/fonts/Roboto-Regular.ttf
    }
}
