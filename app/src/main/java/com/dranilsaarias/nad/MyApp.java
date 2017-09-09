package com.dranilsaarias.nad;

import android.app.Application;

/**
 * Created by pico on 9/9/17.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/futurabt_book.otf"); // font from assets: "assets/fonts/Roboto-Regular.ttf
    }
}
