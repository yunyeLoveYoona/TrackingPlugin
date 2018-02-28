package com.test.trackingplugin;

import android.app.Activity;
import android.view.View;

/**
 * Created by dell on 2018/2/28.
 */

public interface TrackingListener extends View.OnClickListener {
    /**
     * 获取所属Activity
     *
     * @return
     */
    public Class getActivityClass();

    public void onResume(Activity activity);

    public void onPause(Activity activity);
}
