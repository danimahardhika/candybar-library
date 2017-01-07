package com.dm.material.dashboard.candybar.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.fragments.dialog.IconPreviewFragment;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;

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

public class IconsHelper {

    public static String replaceIconName(@NonNull Context context, boolean iconReplacer, String name) {
        if (iconReplacer) {
            String[] replacer = context.getResources().getStringArray(R.array.icon_name_replacer);
            for (String replace : replacer) {
                String[] strings = replace.split(",");
                if (strings.length > 0)
                    name = name.replace(strings[0], strings.length > 1 ? strings[1] : "");
            }
        }
        char character = Character.toUpperCase(name.charAt(0));
        String finalString = character + name.substring(1);
        return finalString.replace("_", " ");
    }

    public static void selectIcon(@NonNull Context context, int action, Icon icon) {
        if (action == IntentHelper.ICON_PICKER) {
            Intent intent = new Intent();
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(
                    "drawable://" + icon.getRes());

            intent.putExtra("icon", bitmap);
            ((AppCompatActivity) context).setResult(bitmap != null ?
                    Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
            ((AppCompatActivity) context).finish();
        } else if (action == IntentHelper.IMAGE_PICKER) {
            Intent intent = new Intent();
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(
                    "drawable://" + icon.getRes());
            if (bitmap != null) {
                File file = new File(context.getCacheDir(), icon.getTitle() + ".png");
                FileOutputStream outStream;
                try {
                    outStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();

                    Uri uri = FileHelper.getUriFromFile(context, context.getPackageName(), file);
                    if (uri == null) uri = Uri.fromFile(file);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception | OutOfMemoryError e) {
                    Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                }
                intent.putExtra("return-data", false);
            }
            ((AppCompatActivity) context).setResult(bitmap != null ?
                    Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
            ((AppCompatActivity) context).finish();
        } else {
            IconPreviewFragment.showIconPreview(((AppCompatActivity) context)
                            .getSupportFragmentManager(),
                    icon.getTitle(), icon.getRes());
        }
    }

}
