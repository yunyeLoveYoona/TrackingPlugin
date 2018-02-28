package com.test.trackingplugin;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by dell on 2018/2/27.
 */

public class MyApplication extends Application {
    private TrackingPlugin trackingPlugin;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                TrackingPlugin.getInstance().addTrackingOnclickListener(new TrackingListener() {
                    @Override
                    public Class getActivityClass() {
                        return MainActivity.class;
                    }

                    @Override
                    public void onResume(Activity activity) {

                    }

                    @Override
                    public void onPause(Activity activity) {

                    }

                    @Override
                    public void onClick(View v) {
                        Log.v("点击事件", v.getId() + "");
                    }
                });
                TrackingPlugin.getInstance().registerActivity(activity);
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                TrackingPlugin.getInstance().unRegisterActivity();
            }
        });
    }
}
