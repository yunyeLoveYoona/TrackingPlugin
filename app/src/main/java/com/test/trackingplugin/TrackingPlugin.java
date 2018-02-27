package com.test.trackingplugin;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * 埋点插件
 * Created by dell on 2018/2/27.
 */

public class TrackingPlugin {
    private HashMap<Integer, View.OnClickListener> onClickListenerHashMap;
    private View.OnClickListener trackingOnclickListener;


    public TrackingPlugin(Activity activity) {
        onClickListenerHashMap = new HashMap<Integer, View.OnClickListener>();
        View root = activity.getWindow().getDecorView();
        findHasOnClickListenerView((ViewGroup) root);
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
                        if (trackingOnclickListener != null) {
                            trackingOnclickListener.onClick(v);
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


    public void setTrackingOnclickListener(View.OnClickListener trackingOnclickListener) {
        this.trackingOnclickListener = trackingOnclickListener;
    }

    public void destroy() {
        trackingOnclickListener = null;
        onClickListenerHashMap = null;
    }
}
