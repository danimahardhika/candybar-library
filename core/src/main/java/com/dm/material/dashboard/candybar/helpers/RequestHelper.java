package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.TimeHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.dm.material.dashboard.candybar.utils.listeners.RequestListener;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    public static final String APPFILTER = "appfilter.xml";
    public static final String APPMAP = "appmap.xml";
    public static final String THEME_RESOURCES = "theme_resources.xml";
    public static final String ZIP = "icon_request.zip";
    public static final String REBUILD_ZIP = "rebuild_icon_request.zip";

    public static String getGeneratedZipName(@NonNull String baseName) {
        return baseName.substring(0, baseName.lastIndexOf(".")) +"_"+ TimeHelper.getDateTime(
                new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault())) +".zip";
    }

    @Nullable
    public static File buildXml(@NonNull Context context, @NonNull List<Request> requests, @NonNull XmlType xmlType) {
        try {
            if (xmlType == XmlType.APPFILTER && !CandyBarApplication.getConfiguration().isGenerateAppFilter()) {
                return null;
            } else if (xmlType == XmlType.APPMAP && !CandyBarApplication.getConfiguration().isGenerateAppMap()) {
                return null;
            } else if (xmlType == XmlType.THEME_RESOURCES & !CandyBarApplication.getConfiguration().isGenerateThemeResources()) {
                return null;
            }

            File file = new File(context.getCacheDir().toString(), xmlType.getFileName());
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), "UTF8"));
            writer.append(xmlType.getHeader()).append("\n\n");

            for (Request request : requests) {
                writer.append(xmlType.getContent(request));
            }
            writer.append(xmlType.getFooter());
            writer.flush();
            writer.close();
            return file;
        } catch (IOException e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }

    @NonNull
    public static HashMap<String, String> getAppFilter(@NonNull Context context, @NonNull Key key) {
        try {
            HashMap<String, String> activities = new HashMap<>();
            XmlPullParser xpp = context.getResources().getXml(R.xml.appfilter);

            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("item")) {
                        String sKey = xpp.getAttributeValue(null, key.getKey());
                        String sValue = xpp.getAttributeValue(null, key.getValue());

                        if (sKey != null && sValue != null) {
                            activities.put(
                                    sKey.replace("ComponentInfo{", "").replace("}", ""),
                                    sValue.replace("ComponentInfo{", "").replace("}", ""));
                        } else {
                            LogUtil.e("Appfilter Error\nKey: " +sKey+ "\nValue: " +sValue);
                        }
                    }
                }
                xpp.next();
            }
            return activities;
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return new HashMap<>();
    }

    @NonNull
    public static List<Request> getMissingApps(@NonNull Context context) {
        List<Request> requests = new ArrayList<>();
        HashMap<String, String> appFilter = getAppFilter(context, Key.ACTIVITY);
        PackageManager packageManager = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installedApps = packageManager.queryIntentActivities(
                intent, PackageManager.GET_RESOLVED_FILTER);
        CandyBarMainActivity.sInstalledAppsCount = installedApps.size();

        try {
            Collections.sort(installedApps,
                    new ResolveInfo.DisplayNameComparator(packageManager));
        } catch (Exception ignored) {}

        for (ResolveInfo app : installedApps) {
            String packageName = app.activityInfo.packageName;
            String activity = packageName +"/"+ app.activityInfo.name;

            String value = appFilter.get(activity);

            if (value == null) {
                String name = LocaleHelper.getOtherAppLocaleName(context, new Locale("en"), packageName);
                if (name == null) {
                    name = app.activityInfo.loadLabel(packageManager).toString();
                }

                boolean requested = Database.get(context).isRequested(activity);
                Request request = Request.Builder()
                        .name(name)
                        .packageName(app.activityInfo.packageName)
                        .activity(activity)
                        .requested(requested)
                        .build();

                requests.add(request);
            }
        }
        return requests;
    }

    public static void showAlreadyRequestedDialog(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
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
                Preferences.get(context).getRegularRequestUsed());

        if (Preferences.get(context).isPremiumRequestEnabled())
            message += " "+ context.getResources().getString(R.string.request_limit_buy);

        if (reset) message += "\n\n"+ context.getResources().getString(R.string.request_limit_reset);
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.request_title)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestRequired(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.request_title)
                .content(R.string.premium_request_required)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestLimitDialog(@NonNull Context context, int selected) {
        String message = String.format(context.getResources().getString(R.string.premium_request_limit),
                Preferences.get(context).getPremiumRequestCount());
        message += " "+ String.format(context.getResources().getString(R.string.premium_request_limit1),
                selected);
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.premium_request)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestStillAvailable(@NonNull Context context) {
        String message = String.format(context.getResources().getString(
                R.string.premium_request_already_purchased),
                Preferences.get(context).getPremiumRequestCount());
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.premium_request)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static boolean isReadyToSendPremiumRequest(@NonNull Context context) {
        boolean isReady = Preferences.get(context).isConnectedToNetwork();
        if (!isReady) {
            new MaterialDialog.Builder(context)
                    .typeface(
                            TypefaceHelper.getMedium(context),
                            TypefaceHelper.getRegular(context))
                    .title(R.string.premium_request)
                    .content(R.string.premium_request_no_internet)
                    .positiveText(R.string.close)
                    .show();
        }
        return isReady;
    }

    public static void showPremiumRequestConsumeFailed(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.premium_request)
                .content(R.string.premium_request_consume_failed)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestExist(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .typeface(
                        TypefaceHelper.getMedium(context),
                        TypefaceHelper.getRegular(context))
                .title(R.string.premium_request)
                .content(R.string.premium_request_exist)
                .positiveText(R.string.close)
                .show();
    }

    public static void checkPiracyApp(@NonNull Context context) {
        boolean premiumRequest = context.getResources().getBoolean(R.bool.enable_premium_request);
        //Dashboard don't need to check piracy app if premium request is disabled
        if (!premiumRequest) {
            Preferences.get(context).setPremiumRequestEnabled(false);
            RequestListener listener = (RequestListener) context;
            listener.onPiracyAppChecked(true);
            return;
        }

        //Lucky Patcher and Freedom package name
        String[] strings = new String[] {
                "com.chelpus.lackypatch",
                "com.dimonvideo.luckypatcher",
                "com.forpda.lp",
                //"com.android.protips", This is not lucky patcher or freedom
                "com.android.vending.billing.InAppBillingService.LUCK",
                "com.android.vending.billing.InAppBillingService.LOCK",
                "cc.madkite.freedom",
                "com.android.vending.billing.InAppBillingService.LACK",
                "com.android.vending.billing.InAppBillingService.CLON",
                "com.android.vending.billing.InAppBillingService.CRAC"
        };

        boolean isPiracyAppInstalled = false;
        for (String string : strings) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                        string, PackageManager.GET_ACTIVITIES);
                if (packageInfo != null) {
                    isPiracyAppInstalled = true;
                    break;
                }
            } catch (Exception ignored) {}
        }

        Preferences.get(context).setPremiumRequestEnabled(!isPiracyAppInstalled);

        RequestListener listener = (RequestListener) context;
        listener.onPiracyAppChecked(isPiracyAppInstalled);
    }

    public enum XmlType {
        APPFILTER(RequestHelper.APPFILTER, "<resources>", "</resources>"),
        APPMAP(RequestHelper.APPMAP, "<appmap>", "</appmap>"),
        THEME_RESOURCES(RequestHelper.THEME_RESOURCES, "<Theme version=\"1\">", "</Theme>");

        private String fileName;
        private String header;
        private String footer;

        XmlType(String fileName, String header, String footer) {
            this.fileName = fileName;
            this.header = header;
            this.footer = footer;
        }

        private String getFileName() {
            return fileName;
        }

        private String getHeader() {
            return header;
        }

        private String getFooter() {
            return footer;
        }

        private String getContent(@NonNull Request request) {
            switch (this) {
                case APPFILTER:
                    return  "\t<!-- " + request.getName() + " -->" +
                            "\n" +
                            "\t<item component=\"ComponentInfo{" +request.getActivity()+
                            "}\" drawable=\"" +
                            request.getName().toLowerCase().replace(" ", "_") +
                            "\" />" +
                            "\n\n";
                case APPMAP:
                    String packageName = ""+ request.getPackageName()+ "/";
                    String className = request.getActivity().replaceFirst(packageName, "");
                    return  "\t<!-- " + request.getName() + " -->" +
                            "\n" +
                            "\t<item class=\"" +className+ "\" name=\"" +
                            request.getName().toLowerCase().replace(" ", "_") +
                            "\" />" +
                            "\n\n";
                case THEME_RESOURCES:
                    return  "\t<!-- " + request.getName() + " -->" +
                            "\n" +
                            "\t<AppIcon name=\"" +request.getActivity()+ "\" image=\"" +
                            request.getName().toLowerCase().replace(" ", "_") +
                            "\" />" +
                            "\n\n";
                default:
                    return "";
            }
        }
    }

    public enum Key {
        ACTIVITY("component", "drawable"),
        DRAWABLE("drawable", "component");

        private String key;
        private String value;

        Key(String key, String value) {
            this.key = key;
            this.value = value;
        }

        private String getKey() {
            return key;
        }

        private String getValue() {
            return value;
        }
    }
}
