package com.miscell.lucky;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import com.flurry.android.FlurryAgent;

/**
 * Created by chenjishi on 15/2/12.
 */
public class LuckyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sInstance = this;
    }

    private static LuckyApplication sInstance;

    public static LuckyApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FlurryAgent.init(this, "37T6NSWDQZ28FNJSWYBB");
    }

    public boolean isEnable() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("goal_switch", true);
    }

    public void setEnable(boolean enable) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                .putBoolean("goal_switch", enable)
                .apply();
    }
}
