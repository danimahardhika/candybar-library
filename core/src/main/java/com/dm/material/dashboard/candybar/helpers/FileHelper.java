package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;

import com.dm.material.dashboard.candybar.utils.Tag;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

public class FileHelper {

    public static final String IMAGE_EXTENSION = ".jpeg";

    private static final int BUFFER = 2048;

    public static File getCacheDirectory(@NonNull Context context) {
        if (PermissionHelper.isPermissionStorageGranted(context)) {
            File cache = context.getExternalCacheDir();
            if (cache != null) return new File(cache.toString());
        }
        return new File(context.getCacheDir().toString());
    }

    public static void createZip (SparseArrayCompat<String> files, String directory) {
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(directory);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];
            for (int i = 0; i < files.size(); i++) {
                FileInputStream fi = new FileInputStream(files.get(i));
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(files.get(i).substring(
                        files.get(i).lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
    }

    @Nullable
    public static String saveIcon (File directory, Bitmap bitmap, String name) {
        String fileName = name.toLowerCase().replace(" ", "_") + ".png";
        boolean createFolder = true;
        if (!directory.exists()) {
            createFolder = directory.mkdirs();
        }
        if (createFolder) {
            File file = new File(directory, fileName);
            try {
                FileOutputStream outStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, outStream);
                outStream.flush();
                outStream.close();
                return directory.toString() + "/" + fileName;
            } catch (Exception | OutOfMemoryError e) {
                Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
            }
        }
        return null;
    }

    @Nullable
    public static Uri getUriFromFile(Context context, String applicationId, File file) {
        try {
            return FileProvider.getUriForFile(context, applicationId + ".fileProvider", file);
        } catch (IllegalArgumentException ignored) {}
        return null;
    }

}
