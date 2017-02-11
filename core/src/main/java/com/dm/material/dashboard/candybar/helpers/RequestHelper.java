package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.listeners.RequestListener;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

public class RequestHelper {

    @NonNull
    private static String loadAppFilter(@NonNull Context context) {
        try {
            StringBuilder sb = new StringBuilder();
            XmlPullParser xpp = context.getResources().getXml(R.xml.appfilter);

            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("item")) {
                        sb.append(xpp.getAttributeValue(null, "component"));
                    }
                }
                xpp.next();
            }
            return sb.toString();
        } catch (Exception e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
        return "";
    }

    @NonNull
    public static List<Request> loadMissingApps(@NonNull Context context) {
        List<Request> requests = new ArrayList<>();
        Database database = new Database(context);
        String activities = RequestHelper.loadAppFilter(context);
        PackageManager packageManager = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installedApps = packageManager.queryIntentActivities(
                intent, PackageManager.GET_RESOLVED_FILTER);

        try {
            Collections.sort(installedApps,
                    new ResolveInfo.DisplayNameComparator(packageManager));
        } catch (Exception ignored) {}

        for (ResolveInfo app : installedApps) {
            String packageName = app.activityInfo.packageName;
            String activity = packageName +"/"+ app.activityInfo.name;

            if (!activities.contains(activity)) {
                String name = LocaleHelper.getOtherAppLocaleName(
                        context, new Locale("en-US"), packageName);
                if (name == null)
                    name = app.activityInfo.loadLabel(packageManager).toString();

                boolean requested = database.isRequested(activity);
                requests.add(new Request(
                        name,
                        app.activityInfo.packageName,
                        activity,
                        requested));
            }
        }
        return requests;
    }

    public static String writeRequest(@NonNull Request request) {
        String link = "https://play.google.com/store/apps/details?id=";
        return "\n\n" +
                request.getName() +
                "\n" +
                request.getActivity() +
                "\n" +
                link + request.getPackageName();
    }

    public static String writeAppFilter(@NonNull Request request) {
        return  "<!-- " + request.getName() + " -->" +
                "\n" +
                "<item component=\"ComponentInfo{" +
                request.getActivity() +
                "}\" drawable=\"" +
                request.getName().toLowerCase().replace(" ", "_") +
                "\" />" +
                "\n\n";
    }

    public static void showAlreadyRequestedDialog(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.request_title)
                .content(R.string.request_requested)
                .positiveText(R.string.close)
                .show();
    }

    public static void showIconRequestLimitDialog(@NonNull Context context) {
        boolean reset = context.getResources().getBoolean(R.bool.reset_icon_request_limit);
        int limit = context.getResources().getInteger(R.integer.icon_request_limit);
        String message = String.format(context.getResources().getString(R.string.request_limit), limit);
        message += " "+ String.format(context.getResources().getString(R.string.request_used),
                Preferences.getPreferences(context).getRegularRequestUsed());

        if (Preferences.getPreferences(context).isPremiumRequestEnabled())
            message += " "+ context.getResources().getString(R.string.request_limit_buy);

        if (reset) message += "\n\n"+ context.getResources().getString(R.string.request_limit_reset);
        new MaterialDialog.Builder(context)
                .title(R.string.request_title)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestRequired(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.request_title)
                .content(R.string.premium_request_required)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestLimitDialog(@NonNull Context context, int selected) {
        String message = String.format(context.getResources().getString(R.string.premium_request_limit),
                Preferences.getPreferences(context).getPremiumRequestCount());
        message += " "+ String.format(context.getResources().getString(R.string.premium_request_limit1),
                selected);
        new MaterialDialog.Builder(context)
                .title(R.string.premium_request)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestStillAvailable(@NonNull Context context) {
        String message = String.format(context.getResources().getString(
                R.string.premium_request_already_purchased),
                Preferences.getPreferences(context).getPremiumRequestCount());
        new MaterialDialog.Builder(context)
                .title(R.string.premium_request)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static boolean isReadyToSendPremiumRequest(@NonNull Context context) {
        boolean isReady = Preferences.getPreferences(context).isConnectedToNetwork();
        if (!isReady) {
            new MaterialDialog.Builder(context)
                    .title(R.string.premium_request)
                    .content(R.string.premium_request_no_internet)
                    .positiveText(R.string.close)
                    .show();
        }
        return isReady;
    }

    public static void showPremiumRequestConsumeFailed(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.premium_request)
                .content(R.string.premium_request_consume_failed)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestExist(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.premium_request)
                .content(R.string.premium_request_exist)
                .positiveText(R.string.close)
                .show();
    }

    public static void checkPiracyApp(@NonNull Context context) {
        //Lucky Patcher and Freedom package name
        String[] strings = new String[] {
                "com.chelpus.lackypatch",
                "com.dimonvideo.luckypatcher",
                "com.forpda.lp",
                //"com.android.protips", This is not lucky patcher or freedom
                "com.android.vending.billing.InAppBillingService.LUCK",
                "com.android.vending.billing.InAppBillingService.LOCK",
                "cc.madkite.freedom",
                "com.android.vending.billing.InAppBillingService.LACK"
        };

        boolean isPiracyAppInstalled = false;
        for (String string : strings) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                        string, PackageManager.GET_ACTIVITIES);
                if (packageInfo != null) {
                    isPiracyAppInstalled = true;
                    Preferences.getPreferences(context).setPremiumRequestEnabled(false);
                    return;
                }
            } catch (Exception ignored) {}
        }

        Preferences.getPreferences(context).setPremiumRequestEnabled(
                context.getResources().getBoolean(R.bool.enable_premium_request));

        try {
            RequestListener listener = (RequestListener) context;
            listener.OnPiracyAppChecked(isPiracyAppInstalled);
        } catch (Exception ignored) {}
    }
}
