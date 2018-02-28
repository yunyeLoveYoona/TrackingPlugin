package com.test.trackingplugin;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 埋点插件
 * Created by dell on 2018/2/27.
 */

public class TrackingPlugin {
    private HashMap<Integer, View.OnClickListener> onClickListenerHashMap;
    private List<TrackingListener> trackingOnclickListeners;
    private static TrackingPlugin instance;
    private TrackingListener activityistener;

    public static TrackingPlugin getInstance() {
        if (instance == null) {
            instance = new TrackingPlugin();
        }
        return instance;
    }


    private TrackingPlugin() {
        trackingOnclickListeners = new ArrayList<TrackingListener>();
    }

    public void registerActivity(Activity activity) {
        activityistener = null;
        onClickListenerHashMap = new HashMap<Integer, View.OnClickListener>();
        View root = activity.getWindow().getDecorView();
        for (TrackingListener trackingOnclickListener : trackingOnclickListeners) {
            if (trackingOnclickListener.getActivityClass() == activity.getClass()) {
                activityistener = trackingOnclickListener;
            }
        }
        if (activityistener != null) {
            findHasOnClickListenerView((ViewGroup) root);
        }
    }

    private void findHasOnClickListenerView(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childView = viewGroup.getChildAt(i);
            View.OnClickListener childViewOnclickListener = getViewOnclickListener(childView);
            if (childViewOnclickListener != null) {
                onClickListenerHashMap.put(childView.getId(), childViewOnclickListener);
                childView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickListenerHashMap.get(v.getId()).onClick(v);
                        if (activityistener != null) {
                            activityistener.onClick(v);
                        }
                    }
                });
            }
            if (childView instanceof ViewGroup) {
                findHasOnClickListenerView((ViewGroup) childView);
            }
        }

    }

    private View.OnClickListener getViewOnclickListener(View view) {
        Class viewClass = null;
        View.OnClickListener viewOnclickListener = null;
        try {
            viewClass = Class.forName("android.view.View");
            Object mListenerInfo = null;
            Field[] fields = viewClass.getDeclaredFields();
            try {
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.getName().equals("mListenerInfo")) {
                        mListenerInfo = field.get(view);
                    }
                }
                if (mListenerInfo != null) {
                    Class listenerInfoClass = Class.forName("android.view.View$ListenerInfo");
                    fields = listenerInfoClass.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        if (field.getName().equals("mOnClickListener")) {
                            viewOnclickListener = (View.OnClickListener) field.get(mListenerInfo);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return viewOnclickListener;
    }



    public void addTrackingOnclickListener(TrackingListener trackingOnclickListener) {
        trackingOnclickListeners.add(trackingOnclickListener);
    }

    public void onResume(Activity activity) {
        if (activityistener != null) {
            activityistener.onResume(activity);
        }
    }

    public void onPause(Activity activity) {
        if (activityistener != null) {
            activityistener.onPause(activity);
        }
    }
}
