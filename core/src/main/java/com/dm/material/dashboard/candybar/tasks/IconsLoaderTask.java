package com.dm.material.dashboard.candybar.tasks;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.items.Home;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.AlphanumComparator;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.dm.material.dashboard.candybar.utils.listeners.HomeListener;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class IconsLoaderTask extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;
    private Home mHome;

    private IconsLoaderTask(Context context) {
        mContext = context;
    }

    public static AsyncTask start(@NonNull Context context) {
        return start(context, SERIAL_EXECUTOR);
    }

    public static AsyncTask start(@NonNull Context context, @NonNull Executor executor) {
        return new IconsLoaderTask(context).executeOnExecutor(executor);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        while (!isCancelled()) {
            try {
                Thread.sleep(1);
                if (CandyBarMainActivity.sSections == null) {
                    CandyBarMainActivity.sSections = IconsHelper.getIconsList(mContext);

                    for (int i = 0; i < CandyBarMainActivity.sSections.size(); i++) {
                        List<Icon> icons = CandyBarMainActivity.sSections.get(i).getIcons();

                        if (mContext.getResources().getBoolean(R.bool.show_icon_name) ||
                                mContext.getResources().getBoolean(R.bool.enable_icon_name_replacer)) {
                            for (Icon icon : icons) {
                                boolean replacer = mContext.getResources().getBoolean(
                                        R.bool.enable_icon_name_replacer);
                                String name = IconsHelper.replaceName(mContext, replacer, icon.getTitle());
                                icon.setTitle(name);
                            }
                        }

                        if (mContext.getResources().getBoolean(R.bool.enable_icons_sort) ||
                                mContext.getResources().getBoolean(R.bool.enable_icon_name_replacer)) {
                            Collections.sort(icons, new AlphanumComparator() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    String s1 = ((Icon) o1).getTitle();
                                    String s2 = ((Icon) o2).getTitle();
                                    return super.compare(s1, s2);
                                }
                            });

                            CandyBarMainActivity.sSections.get(i).setIcons(icons);
                        }
                    }

                    if (CandyBarApplication.getConfiguration().isShowTabAllIcons()) {
                        List<Icon> icons = IconsHelper.getTabAllIcons();
                        CandyBarMainActivity.sSections.add(new Icon(
                                CandyBarApplication.getConfiguration().getTabAllIconsTitle(), icons));
                    }
                }

                if (CandyBarMainActivity.sHomeIcon != null) return true;

                Random random = new Random();
                int index = random.nextInt(CandyBarMainActivity.sSections.size());
                List<Icon> icons = CandyBarMainActivity.sSections.get(index).getIcons();
                index = random.nextInt(icons.size());
                Icon icon = icons.get(index);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(mContext.getResources(),
                        icon.getRes(), options);

                if (!mContext.getResources().getBoolean(R.bool.show_icon_name)) {
                    String name = IconsHelper.replaceName(mContext, true, icon.getTitle());
                    icon.setTitle(name);
                }

                mHome = new Home(
                        icon.getRes(),
                        icon.getTitle(),
                        String.format(mContext.getResources().getString(R.string.home_icon_dimension),
                                options.outWidth +" x "+ options.outHeight),
                        Home.Type.DIMENSION);
                CandyBarMainActivity.sHomeIcon = mHome;
                return true;
            } catch (Exception e) {
                LogUtil.e(Log.getStackTraceString(e));
                return false;
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            if (mHome == null) return;
            if (mContext == null) return;

            FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
            if (fm == null) return;

            Fragment fragment = fm.findFragmentByTag("home");
            if (fragment == null) return;

            HomeListener listener = (HomeListener) fragment;
            listener.onHomeDataUpdated(mHome);
        }
    }
}
