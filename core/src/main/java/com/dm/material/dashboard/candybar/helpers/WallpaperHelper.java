package com.dm.material.dashboard.candybar.helpers;

import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.dm.material.dashboard.candybar.preferences.Preferences;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
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

public class WallpaperHelper {

    public static File getDefaultWallpapersDirectory(@NonNull Context context) {
        try {
            if (Preferences.getPreferences(context).getWallsDirectory().length() == 0) {
                return new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES) +"/"+
                        context.getResources().getString(R.string.app_name));
            }
            return new File(Preferences.getPreferences(context).getWallsDirectory());
        } catch (Exception e) {
            return new File(context.getFilesDir().toString() +"/Pictures/"+
                    context.getResources().getString(R.string.app_name));
        }
    }

    private static String getWallpaperUri(@NonNull Context context, String url, String filename) {
        File directory = getDefaultWallpapersDirectory(context);
        if (new File(directory + File.separator + filename).exists()) {
            return "file://" + directory + File.separator + filename;
        }
        return url;
    }

    public static void downloadWallpaper(@NonNull Context context, @ColorInt int color,
                                         String link, String name, int res) {
        new AsyncTask<Void, Integer, Boolean>() {

            MaterialDialog dialog;
            HttpURLConnection connection;
            File output;
            File file;
            int fileLength;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                output = WallpaperHelper.getDefaultWallpapersDirectory(context);
                file = new File(output.toString() + File.separator + name + FileHelper.IMAGE_EXTENSION);

                MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
                builder.content(R.string.wallpaper_downloading)
                        .widgetColor(color)
                        .progress(true, 0)
                        .progressIndeterminateStyle(true);
                dialog = builder.build();
                dialog.setOnDismissListener(dialog1 -> {
                    try {
                        if (connection != null) connection.disconnect();
                    } catch (Exception ignored) {}
                    cancel(true);
                });
                dialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        if (!output.exists())
                            if (!output.mkdirs())
                                return false;
                        if (file.exists()) return true;

                        URL url = new URL(link);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setConnectTimeout(15000);

                        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            fileLength = urlConnection.getContentLength();
                            InputStream stream = new BufferedInputStream(url.openStream());
                            OutputStream output = new FileOutputStream(file.toString());

                            byte data[] = new byte[1024];
                            long total = 0;
                            int count;
                            while ((count = stream.read(data)) != -1) {
                                total += count;
                                if (fileLength > 0)
                                    publishProgress((int) (total * 100 / fileLength));
                                output.write(data, 0, count);
                            }

                            output.flush();
                            output.close();
                            stream.close();

                            return true;
                        }
                    } catch (Exception e) {
                        Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                int downloaded = fileLength / 1014;
                String size = String.valueOf(values[0] * fileLength/1024/100) + " KB" +
                        String.valueOf(fileLength == 0 ? "" : "/" + downloaded + " KB");
                String downloading = context.getResources().getString(
                        R.string.wallpaper_downloading);
                String text = downloading +" ("+ size + ")";
                dialog.setContent(text);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                Toast.makeText(context,
                        context.getResources().getString(R.string.wallpaper_download_cancelled),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                dialog.dismiss();
                if (aBoolean) {
                    if (Preferences.getPreferences(context).getWallsDirectory().length() == 0)
                        Preferences.getPreferences(context).setWallsDirectory(output.toString());
                    context.sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(
                            new File(file.toString()))));
                    String downloaded = context.getResources().getString(
                            R.string.wallpaper_downloaded);
                    if (res > 0) {
                        View rootView = ((AppCompatActivity) context).findViewById(res);
                        if (rootView != null) {
                            Snackbar snackbar = Snackbar.make(rootView,
                                    downloaded +" "+ file.toString(), 6000)
                                    .setAction(context.getResources().getString(R.string.open), view -> {
                                        try {
                                            context.startActivity(new Intent()
                                                    .setAction(Intent.ACTION_VIEW)
                                                    .setDataAndType(Uri.fromFile(
                                                            new File(file.toString())),
                                                            "image/*"));
                                        } catch (ActivityNotFoundException e) {
                                            Toast.makeText(context,
                                                    context.getResources().getString(R.string.wallpaper_noapp),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .setActionTextColor(color);
                            View snackBarView = snackbar.getView();
                            TextView textView = (TextView) snackBarView.findViewById(
                                    android.support.design.R.id.snackbar_text);
                            if (textView != null) textView.setMaxLines(5);
                            snackbar.show();
                        }
                    } else {
                        Toast.makeText(context,
                                downloaded +" "+ file.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

        }.execute();
    }

    public static void applyWallpaper(@NonNull Context context, @ColorInt int color, String url, String name) {
        new AsyncTask<Void, Void, Boolean>() {

            MaterialDialog dialog;
            WallpaperManager manager;
            DisplayMetrics displaymetrics;
            int orientation;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                manager = WallpaperManager.getInstance(context);
                displaymetrics = context.getResources().getDisplayMetrics();
                orientation = context.getResources().getConfiguration().orientation;

                MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
                builder.widgetColor(color)
                        .progress(true, 0)
                        .progressIndeterminateStyle(true)
                        .content(R.string.wallpaper_applying);
                dialog = builder.build();
                dialog.setOnCancelListener(dialogInterface -> {
                    ImageLoader.getInstance().stop();
                    cancel(true);
                });
                dialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(
                                getWallpaperUri(context, url, name + FileHelper.IMAGE_EXTENSION),
                                ImageConfig.getImageOptions(ImageScaleType.NONE));
                        if (bitmap != null) {
                            final int height, width;
                            height = orientation == Configuration.ORIENTATION_PORTRAIT ?
                                    displaymetrics.heightPixels : displaymetrics.widthPixels;
                            width = orientation == Configuration.ORIENTATION_PORTRAIT ?
                                    displaymetrics.widthPixels : displaymetrics.heightPixels;

                            double scale = (double) height / (double) bitmap.getHeight();
                            int targetWidth = Double.valueOf(
                                    bitmap.getWidth() * scale).intValue();

                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                                    bitmap, targetWidth, height, false);

                            if (scaledBitmap != null) {
                                manager.setBitmap(scaledBitmap);
                                return true;
                            }

                            manager.setBitmap(bitmap);
                            manager.setWallpaperOffsetSteps(1, 1);
                            manager.suggestDesiredDimensions(width, height);
                            return true;
                        }
                        return false;
                    } catch (Exception e) {
                        Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                Toast.makeText(context, R.string.wallpaper_apply_failed,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                dialog.dismiss();
                if (aBoolean) {
                    Toast.makeText(context, R.string.wallpaper_applied,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, R.string.wallpaper_apply_failed,
                            Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

}
