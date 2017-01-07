package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.Tag;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

public class ReportBugsHelper {

    public static void checkForBugs(@NonNull Context context) {
        new AsyncTask<Void, Void, Boolean>() {

            MaterialDialog dialog;
            StringBuilder sb;
            File folder;
            String file;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                sb = new StringBuilder();
                folder = context.getCacheDir();
                file = folder.toString() + "/" + "reportbugs.zip";

                MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
                builder.content(R.string.report_bugs_building)
                        .progress(true, 0)
                        .progressIndeterminateStyle(true);

                dialog = builder.build();
                dialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        SparseArrayCompat<String> files = new SparseArrayCompat<>();
                        sb.append(DeviceHelper.getDeviceInfo(context));

                        String brokenAppFilter = buildBrokenAppFilter(context, folder);
                        if (brokenAppFilter != null) files.append(files.size(), brokenAppFilter);

                        String activityList = buildActivityList(context, folder);
                        if (activityList != null) files.append(files.size(), activityList);

                        String stackTrace = Preferences.getPreferences(context).getLatestCrashLog();
                        String crashLog = buildCrashLog(context, folder, stackTrace);
                        if (crashLog != null) files.append(files.size(), crashLog);

                        FileHelper.createZip(files, file);
                        return true;
                    } catch (Exception e) {
                        Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                dialog.dismiss();
                if (aBoolean) {
                    File zip = new File(file);

                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_EMAIL,
                            new String[]{context.getResources().getString(R.string.dev_email)});
                    intent.putExtra(Intent.EXTRA_SUBJECT,
                            "Report Bugs " + (context.getString(
                                    R.string.app_name)));
                    intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                    Uri uri = FileHelper.getUriFromFile(context, context.getPackageName(), zip);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);

                    context.startActivity(Intent.createChooser(intent,
                            context.getResources().getString(R.string.email_client)));

                } else {
                    Toast.makeText(context, R.string.report_bugs_failed,
                            Toast.LENGTH_LONG).show();
                }

                dialog = null;
                sb.setLength(0);
            }
        }.execute();
    }

    @Nullable
    private static String buildBrokenAppFilter(Context context, File folder) {
        try {
            AssetManager asset = context.getAssets();
            InputStream stream = asset.open("appfilter.xml");
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(stream);
            NodeList list = doc.getElementsByTagName("item");

            File fileDir = new File(folder.toString() + "/" + "broken_appfilter.xml");
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileDir), "UTF8"));

            boolean first = true;
            for (int i = 0; i < list.getLength(); i++) {
                Node nNode = list.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    if (first) {
                        first = false;
                        out.append("<!-- BROKEN APPFILTER -->");
                        out.append("\n\n\n");
                    }

                    int drawable = context.getResources().getIdentifier(
                            eElement.getAttribute("drawable"), "drawable", context.getPackageName());
                    if (drawable==0) {
                        out.append("Activity : ")
                                .append(eElement.getAttribute("component").replace(
                                        "ComponentInfo{", "").replace("}", ""));
                        out.append("\n");
                        out.append("Drawable : ").append(eElement.getAttribute("drawable"));
                        out.append("\n");
                        out.append("Reason : Drawable Not Found!");
                        out.append("\n\n");
                    }
                }
            }
            out.flush();
            out.close();

            return fileDir.toString();
        } catch (Exception | OutOfMemoryError e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    @Nullable
    private static String buildActivityList(Context context, File folder) {
        try {
            File fileDir = new File(folder.toString() + "/" + "activity_list.xml");
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileDir), "UTF8"));

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> appList = context.getPackageManager().queryIntentActivities(
                    intent, PackageManager.GET_RESOLVED_FILTER);

            try {
                Collections.sort(appList, new ResolveInfo.DisplayNameComparator(context.getPackageManager()));
            } catch (Exception ignored) {}

            boolean first = true;
            for (ResolveInfo app : appList) {

                if (first) {
                    first = false;
                    out.append("<!-- ACTIVITY LIST -->");
                    out.append("\n\n\n");
                }

                String name = app.activityInfo.loadLabel(context.getPackageManager()).toString();
                String activity = app.activityInfo.packageName +"/"+ app.activityInfo.name;
                out.append("<!-- ").append(name).append(" -->");
                out.append("\n").append(activity);
                out.append("\n\n");
            }
            out.flush();
            out.close();

            return fileDir.toString();
        } catch (Exception | OutOfMemoryError e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    @Nullable
    public static String buildCrashLog(Context context, File folder, String stackTrace) {
        try {
            if (stackTrace.length() == 0) return null;

            File fileDir = new File(folder.toString() + "/crashlog.txt");
            String deviceInfo = DeviceHelper.getDeviceInfoForCrashReport(context);
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileDir), "UTF8"));
            out.append(deviceInfo).append(stackTrace);
            out.flush();
            out.close();

            return fileDir.toString();
        } catch (Exception | OutOfMemoryError e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
        return null;
    }

}
