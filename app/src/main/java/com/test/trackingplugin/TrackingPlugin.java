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
    private HashMap<Class, HashMap<Integer, View.OnClickListener>> onClickListenerHashMap;
    private List<TrackingListener> trackingOnclickListeners;
    private static TrackingPlugin instance;
    private TrackingListener activityistener;
    private View root;
    private View childView;
    private View.OnClickListener childViewOnclickListener;
    private Class viewClass;
    private Object mListenerInfo;
    private Field[] fields;
    private Class listenerInfoClass;

    public static TrackingPlugin getInstance() {
        if (instance == null) {
            instance = new TrackingPlugin();
        }
        return instance;
    }


    private TrackingPlugin() {
        trackingOnclickListeners = new ArrayList<TrackingListener>();
        onClickListenerHashMap = new HashMap<Class, HashMap<Integer, View.OnClickListener>>();
    }

    public void registerActivity(Activity activity) {
        activityistener = null;
        for (TrackingListener trackingOnclickListener : trackingOnclickListeners) {
            if (trackingOnclickListener.getActivityClass() == activity.getClass()) {
                activityistener = trackingOnclickListener;
            }
        }
        if (onClickListenerHashMap.get(activity.getClass()) == null) {
            onClickListenerHashMap.put(activity.getClass(), new HashMap<Integer, View.OnClickListener>());
            root = activity.getWindow().getDecorView();
            if (activityistener != null) {
                findHasOnClickListenerView((ViewGroup) root);
            }
        }
    }

    private void findHasOnClickListenerView(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            childView = viewGroup.getChildAt(i);
            childViewOnclickListener = getViewOnclickListener(childView);
            if (childViewOnclickListener != null) {
                if (!(childViewOnclickListener instanceof TrackingClickListener)) {
                    onClickListenerHashMap.get(activityistener.getActivityClass()).put(childView.getId(), childViewOnclickListener);
                    childView.setOnClickListener(new TrackingClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickListenerHashMap.get(activityistener.getActivityClass()).get(v.getId()).onClick(v);
                            if (activityistener != null) {
                                activityistener.onClick(v);
                            }
                        }
                    });
                }
            }
            if (childView instanceof ViewGroup) {
                findHasOnClickListenerView((ViewGroup) childView);
            }
        }

    }

    private View.OnClickListener getViewOnclickListener(View view) {
        viewClass = null;
        View.OnClickListener viewOnclickListener = null;
        try {
            viewClass = Class.forName("android.view.View");
            mListenerInfo = null;
            fields = viewClass.getDeclaredFields();
            try {
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.getName().equals("mListenerInfo")) {
                        mListenerInfo = field.get(view);
                    }
                }
                if (mListenerInfo != null) {
                    listenerInfoClass = Class.forName("android.view.View$ListenerInfo");
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

    public void unRegisterActivity(Activity activity) {
        onClickListenerHashMap.remove(activity.getClass());
    }

}
