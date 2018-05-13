package org.x2ools.xappsearchlib.tools;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

public class AppContext extends ContextWrapper {
    public AppContext(Context base) {
        super(base);
    }

    private static AppContext sInstance;

    public static void attachApp(Application application) {
        sInstance = new AppContext(application);
    }

    public static AppContext get() {
        return sInstance;
    }
}
