package org.x2ools.t9apps;

import android.app.Application;
import android.util.Log;

import io.reactivex.plugins.RxJavaPlugins;

/**
 * @author zhoubinjia
 * @date 2017/8/22
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        RxJavaPlugins.setErrorHandler(throwable -> Log.e("App", "RxJavaError", throwable));
    }
}
