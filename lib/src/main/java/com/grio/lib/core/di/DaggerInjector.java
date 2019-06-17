package com.grio.lib.core.di;

import android.content.Context;


public class DaggerInjector {

    private static ApplicationComponent component;

    private DaggerInjector() {
        super();
    }

    public static ApplicationComponent getComponent() {
        return component;
    }

    public static ApplicationComponent buildComponent(Context context) {
        component = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(context))
                .build();

        return component;
    }
}
