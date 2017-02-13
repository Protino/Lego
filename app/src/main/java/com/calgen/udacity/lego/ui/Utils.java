/*
 * Copyright 2016 Gurupad Mamadapur
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.calgen.udacity.lego.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;

import com.calgen.udacity.lego.R;
import com.calgen.udacity.lego.data.ArticleLoader;

/**
 * Created by Gurupad Mamadapur on 10/30/2016.
 */

public class Utils {
    /**
     * @param dp dp value
     * @return converted px value
     */
    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * @param px dp value
     * @return converted dp value
     */

    public static int pxToDp(int px) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * @param context {@link Context} needed to fetch resources
     * @return height of status bar of current device
     */
    @android.support.annotation.Px
    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * @param mCursor needed to fetch data
     * @param context needed to fetch string resources
     * @return modified string as [date] by [author]
     */
    public static String getModifiedByline(Cursor mCursor, Context context) {
        return String.format(context.getString(R.string.by_line), DateUtils.getRelativeTimeSpanString(
                mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL).toString(), mCursor.getString(ArticleLoader.Query.AUTHOR));
    }

    /**
     * @param context needed to fetch display metrics
     * @return <code>{@link Pair} width, height</code> height and width of device screen in px
     */
    public static Pair<Integer, Integer> getScreenWidthAndHeight(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new Pair<>(metrics.widthPixels, metrics.heightPixels);
    }

    /**
     * @param direction {@link android.view.Gravity#START} or {@link android.view.Gravity#END}
     * @param context {@link Context} to fetch current layoutDirection
     * @return appropriate {@link android.view.Gravity#LEFT} or {@link android.view.Gravity#RIGHT}
     */
    public static int resolveDirection(int direction, Context context) {
        return GravityCompat.getAbsoluteGravity(direction, context.getResources().getConfiguration().getLayoutDirection());
    }
}
