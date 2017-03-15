package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.dm.material.dashboard.candybar.utils.LogUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
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
    public static final String UIL_CACHE_DIR = "uil-images";
    public static final int MB = (1024 * 1014);
    public static final int KB = 1024;

    private static final int BUFFER = 2048;

    public static long getCacheSize(@NonNull File dir) {
        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    result += getCacheSize(aFileList);
                } else {
                    result += aFileList.length();
                }
            }
            return result;
        }
        return 0;
    }

    static boolean copyFile(@NonNull File file, @NonNull File target) {
        try {
            if (!target.getParentFile().exists()) {
                if (!target.getParentFile().mkdirs()) return false;
            }

            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = new FileOutputStream(target);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return false;
    }

    public static void createZip (@NonNull List<String> files, String directory) {
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
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    @Nullable
    public static String saveIcon (File directory, Bitmap bitmap, String name) {
        String fileName = name.toLowerCase().replace(" ", "_") + ".png";
        File file = new File(directory, fileName);
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, outStream);
            outStream.flush();
            outStream.close();
            return directory.toString() + "/" + fileName;
        } catch (Exception | OutOfMemoryError e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }

    @Nullable
    public static Uri getUriFromFile(Context context, String applicationId, File file) {
        try {
            return FileProvider.getUriForFile(context, applicationId + ".fileProvider", file);
        } catch (IllegalArgumentException e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }
}
