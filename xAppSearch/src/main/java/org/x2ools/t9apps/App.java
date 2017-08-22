package org.x2ools.t9apps;

import android.app.Application;
import android.util.Log;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * @author zhoubinjia
 * @date 2017/8/22
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                Log.e("App", "RxJavaError", throwable);
            }
        });
    }
}
