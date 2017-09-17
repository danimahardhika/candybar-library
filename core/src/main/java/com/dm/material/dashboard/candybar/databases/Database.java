package com.dm.material.dashboard.candybar.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danimahardhika.android.helpers.core.TimeHelper;
import com.dm.material.dashboard.candybar.helpers.JsonHelper;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.ArrayList;
import java.util.List;

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

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "candybar_database";
    private static final int DATABASE_VERSION = 9;

    private static final String TABLE_REQUEST = "icon_request";
    private static final String TABLE_PREMIUM_REQUEST = "premium_request";
    private static final String TABLE_WALLPAPERS = "wallpapers";

    private static final String KEY_ID = "id";

    private static final String KEY_ORDER_ID = "order_id";
    private static final String KEY_PRODUCT_ID = "product_id";

    private static final String KEY_NAME = "name";
    private static final String KEY_ACTIVITY = "activity";
    private static final String KEY_REQUESTED_ON = "requested_on";

    private static final String KEY_AUTHOR = "author";
    private static final String KEY_THUMB_URL = "thumbUrl";
    private static final String KEY_URL = "url";
    private static final String KEY_ADDED_ON = "added_on";
    private static final String KEY_MIME_TYPE = "mimeType";
    private static final String KEY_COLOR = "color";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_SIZE = "size";

    private final Context mContext;

    private static Database mDatabase;
    private SQLiteDatabase mSQLiteDatabase;

    public static Database get(@NonNull Context context) {
        if (mDatabase == null) {
            mDatabase = new Database(context);
        }
        return mDatabase;
    }

    private Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_REQUEST = "CREATE TABLE IF NOT EXISTS " +TABLE_REQUEST+ "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_ACTIVITY + " TEXT NOT NULL, " +
                KEY_REQUESTED_ON + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE (" +KEY_ACTIVITY+ ") ON CONFLICT REPLACE)";
        String CREATE_TABLE_PREMIUM_REQUEST = "CREATE TABLE IF NOT EXISTS " +TABLE_PREMIUM_REQUEST+ "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_ORDER_ID + " TEXT NOT NULL, " +
                KEY_PRODUCT_ID + " TEXT NOT NULL, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_ACTIVITY + " TEXT NOT NULL, " +
                KEY_REQUESTED_ON + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE (" +KEY_ACTIVITY+ ") ON CONFLICT REPLACE)";
        String CREATE_TABLE_WALLPAPER = "CREATE TABLE IF NOT EXISTS " +TABLE_WALLPAPERS+ "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_NAME+ " TEXT NOT NULL, " +
                KEY_AUTHOR + " TEXT NOT NULL, " +
                KEY_URL + " TEXT NOT NULL, " +
                KEY_THUMB_URL + " TEXT NOT NULL, " +
                KEY_MIME_TYPE + " TEXT, " +
                KEY_SIZE + " INTEGER DEFAULT 0, " +
                KEY_COLOR + " INTEGER DEFAULT 0, " +
                KEY_WIDTH + " INTEGER DEFAULT 0, " +
                KEY_HEIGHT + " INTEGER DEFAULT 0, " +
                KEY_ADDED_ON + " TEXT NOT NULL, " +
                "UNIQUE (" +KEY_URL+ "))";
        db.execSQL(CREATE_TABLE_REQUEST);
        db.execSQL(CREATE_TABLE_PREMIUM_REQUEST);
        db.execSQL(CREATE_TABLE_WALLPAPER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
         * Need to clear shared preferences with version 3.4.0
         */
        if (newVersion == 9) {
            Preferences.get(mContext).clearPreferences();
        }
        resetDatabase(db, oldVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db, oldVersion);
    }

    private void resetDatabase(SQLiteDatabase db, int oldVersion) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type=\'table\'", null);
        List<String> tables = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                tables.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        List<Request> requests = getRequestedApps(db);
        List<Request> premiumRequest = getPremiumRequest(db);

        for (int i = 0; i < tables.size(); i++) {
            try {
                String dropQuery = "DROP TABLE IF EXISTS " + tables.get(i);
                if (!tables.get(i).equalsIgnoreCase("SQLITE_SEQUENCE"))
                    db.execSQL(dropQuery);
            } catch (Exception ignored) {}
        }
        onCreate(db);

        for (Request request : requests) {
            addRequest(db, request);
        }

        if (oldVersion <= 3) {
            return;
        }

        for (Request premium : premiumRequest) {
            Request r = Request.Builder()
                    .name(premium.getName())
                    .activity(premium.getActivity())
                    .orderId(premium.getOrderId())
                    .productId(premium.getProductId())
                    .requestedOn(premium.getRequestedOn())
                    .build();
            addPremiumRequest(db, r);
        }
    }

    public boolean openDatabase() {
        try {
            if (mDatabase == null) {
                LogUtil.e("Database error: openDatabase() database instance is null");
                return false;
            }

            if (mDatabase.mSQLiteDatabase == null) {
                mDatabase.mSQLiteDatabase = mDatabase.getWritableDatabase();
            }

            if (!mDatabase.mSQLiteDatabase.isOpen()) {
                LogUtil.e("Database error: database openable false, trying to open the database again");
                mDatabase.mSQLiteDatabase = mDatabase.getWritableDatabase();
            }
            return mDatabase.mSQLiteDatabase.isOpen();
        } catch (SQLiteException | NullPointerException e) {
            LogUtil.e(Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean closeDatabase() {
        try {
            if (mDatabase == null) {
                LogUtil.e("Database error: closeDatabase() database instance is null");
                return false;
            }

            if (mDatabase.mSQLiteDatabase == null) {
                LogUtil.e("Database error: trying to close database which is not opened");
                return false;
            }
            mDatabase.mSQLiteDatabase.close();
            return true;
        } catch (SQLiteException | NullPointerException e) {
            LogUtil.e(Log.getStackTraceString(e));
            return false;
        }
    }

    public void addRequest(@Nullable SQLiteDatabase db, Request request) {
        SQLiteDatabase database = db;
        if (database == null) {
            if (!openDatabase()) {
                LogUtil.e("Database error: addRequest() failed to open database");
                return;
            }

            database = mDatabase.mSQLiteDatabase;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, request.getName());
        values.put(KEY_ACTIVITY, request.getActivity());

        String requestedOn = request.getRequestedOn();
        if (requestedOn == null) requestedOn = TimeHelper.getLongDateTime();
        values.put(KEY_REQUESTED_ON, requestedOn);

        database.insert(TABLE_REQUEST, null, values);
    }

    public boolean isRequested(String activity) {
        if (!openDatabase()) {
            LogUtil.e("Database error: isRequested() failed to open database");
            return false;
        }

        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_REQUEST, null, KEY_ACTIVITY + " = ?",
                new String[]{activity}, null, null, null, null);
        int rowCount = cursor.getCount();
        cursor.close();
        return rowCount > 0;
    }

    private List<Request> getRequestedApps(@Nullable SQLiteDatabase db) {
        SQLiteDatabase database = db;
        if (database == null) {
            if (!openDatabase()) {
                LogUtil.e("Database error: getRequestedApps() failed to open database");
                return new ArrayList<>();
            }

            database = mDatabase.mSQLiteDatabase;
        }

        List<Request> requests = new ArrayList<>();
        Cursor cursor = database.query(TABLE_REQUEST, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Request request = Request.Builder()
                        .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                        .activity(cursor.getString(cursor.getColumnIndex(KEY_ACTIVITY)))
                        .requestedOn(cursor.getString(cursor.getColumnIndex(KEY_REQUESTED_ON)))
                        .requested(true)
                        .build();

                requests.add(request);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return requests;
    }

    public void addPremiumRequest(@Nullable SQLiteDatabase db, Request request) {
        SQLiteDatabase database = db;
        if (database == null) {
            if (!openDatabase()) {
                LogUtil.e("Database error: addPremiumRequest() failed to open database");
                return;
            }

            database = mDatabase.mSQLiteDatabase;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_ORDER_ID, request.getOrderId());
        values.put(KEY_PRODUCT_ID, request.getProductId());
        values.put(KEY_NAME, request.getName());
        values.put(KEY_ACTIVITY, request.getActivity());

        String requestedOn = request.getRequestedOn();
        if (requestedOn == null) requestedOn = TimeHelper.getLongDateTime();
        values.put(KEY_REQUESTED_ON, requestedOn);

        database.insert(TABLE_PREMIUM_REQUEST, null, values);
    }

    public List<Request> getPremiumRequest(@Nullable SQLiteDatabase db) {
        SQLiteDatabase database = db;
        if (database == null) {
            if (!openDatabase()) {
                LogUtil.e("Database error: getPremiumRequest() failed to open database");
                return new ArrayList<>();
            }

            database = mDatabase.mSQLiteDatabase;
        }

        List<Request> requests = new ArrayList<>();

        Cursor cursor = database.query(TABLE_PREMIUM_REQUEST,
                null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Request request = Request.Builder()
                        .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                        .activity(cursor.getString(cursor.getColumnIndex(KEY_ACTIVITY)))
                        .orderId(cursor.getString(cursor.getColumnIndex(KEY_ORDER_ID)))
                        .productId(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_ID)))
                        .build();
                requests.add(request);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return requests;
    }

    public void addWallpapers(List<?> list) {
        if (!openDatabase()) {
            LogUtil.e("Database error: addWallpapers() failed to open database");
            return;
        }

        String query = "INSERT OR IGNORE INTO " +TABLE_WALLPAPERS+ " (" +KEY_NAME+ "," +KEY_AUTHOR+ "," +KEY_URL+ ","
                +KEY_THUMB_URL+ "," +KEY_ADDED_ON+ ") VALUES (?,?,?,?,?);";
        SQLiteStatement statement = mDatabase.mSQLiteDatabase.compileStatement(query);
        mDatabase.mSQLiteDatabase.beginTransaction();

        for (int i = 0; i < list.size(); i++) {
            statement.clearBindings();

            Wallpaper wallpaper;
            if (list.get(i) instanceof Wallpaper) {
                wallpaper = (Wallpaper) list.get(i);
            } else {
                wallpaper = JsonHelper.getWallpaper(list.get(i));
            }

            if (wallpaper != null) {
                if (wallpaper.getURL() != null) {
                    String name = wallpaper.getName();
                    if (name == null) name = "";

                    statement.bindString(1, name);

                    if (wallpaper.getAuthor() != null) {
                        statement.bindString(2, wallpaper.getAuthor());
                    } else {
                        statement.bindNull(2);
                    }

                    statement.bindString(3, wallpaper.getURL());
                    statement.bindString(4, wallpaper.getThumbUrl());
                    statement.bindString(5, TimeHelper.getLongDateTime());
                    statement.execute();
                }
            }
        }
        mDatabase.mSQLiteDatabase.setTransactionSuccessful();
        mDatabase.mSQLiteDatabase.endTransaction();
    }

    public void updateWallpaper(Wallpaper wallpaper) {
        if (!openDatabase()) {
            LogUtil.e("Database error: updateWallpaper() failed to open database");
            return;
        }

        if (wallpaper == null) return;

        ContentValues values = new ContentValues();
        if (wallpaper.getSize() > 0) {
            values.put(KEY_SIZE, wallpaper.getSize());
        }

        if (wallpaper.getMimeType() != null) {
            values.put(KEY_MIME_TYPE, wallpaper.getMimeType());
        }

        if (wallpaper.getDimensions() != null) {
            values.put(KEY_WIDTH, wallpaper.getDimensions().getWidth());
            values.put(KEY_HEIGHT, wallpaper.getDimensions().getHeight());
        }

        if (wallpaper.getColor() != 0) {
            values.put(KEY_COLOR, wallpaper.getColor());
        }

        if (values.size() > 0) {
            mDatabase.mSQLiteDatabase.update(TABLE_WALLPAPERS,
                    values, KEY_URL +" = ?", new String[]{wallpaper.getURL()});
        }
    }

    public int getWallpapersCount() {
        if (!openDatabase()) {
            LogUtil.e("Database error: getWallpapersCount() failed to open database");
            return 0;
        }

        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_WALLPAPERS,
                null, null, null, null, null, null, null);
        int rowCount = cursor.getCount();
        cursor.close();
        return rowCount;
    }

    @Nullable
    public Wallpaper getWallpaper(String url) {
        if (!openDatabase()) {
            LogUtil.e("Database error: getWallpaper() failed to open database");
            return null;
        }

        Wallpaper wallpaper = null;
        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_WALLPAPERS,
                null, KEY_URL +" = ?", new String[]{url}, null, null, null, "1");
        if (cursor.moveToFirst()) {
            do {
                int width = cursor.getInt(cursor.getColumnIndex(KEY_WIDTH));
                int height = cursor.getInt(cursor.getColumnIndex(KEY_HEIGHT));
                ImageSize dimensions = null;
                if (width  > 0 && height > 0) {
                    dimensions = new ImageSize(width, height);
                }

                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                if (name.length() == 0) {
                    name = "Wallpaper "+ id;
                }

                wallpaper = Wallpaper.Builder()
                        .name(name)
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .dimensions(dimensions)
                        .mimeType(cursor.getString(cursor.getColumnIndex(KEY_MIME_TYPE)))
                        .size(cursor.getInt(cursor.getColumnIndex(KEY_SIZE)))
                        .color(cursor.getInt(cursor.getColumnIndex(KEY_COLOR)))
                        .build();
            } while (cursor.moveToNext());
        }
        cursor.close();
        return wallpaper;
    }

    public List<Wallpaper> getWallpapers() {
        if (!openDatabase()) {
            LogUtil.e("Database error: getWallpapers() failed to open database");
            return new ArrayList<>();
        }

        List<Wallpaper> wallpapers = new ArrayList<>();
        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_WALLPAPERS,
                null, null, null, null, null, KEY_ADDED_ON + " DESC, " +KEY_ID);
        if (cursor.moveToFirst()) {
            do {
                int width = cursor.getInt(cursor.getColumnIndex(KEY_WIDTH));
                int height = cursor.getInt(cursor.getColumnIndex(KEY_HEIGHT));
                ImageSize dimensions = null;
                if (width  > 0 && height > 0) {
                    dimensions = new ImageSize(width, height);
                }

                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                if (name.length() == 0) {
                    name = "Wallpaper "+ id;
                }

                Wallpaper wallpaper = Wallpaper.Builder()
                        .name(name)
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .color(cursor.getInt(cursor.getColumnIndex(KEY_COLOR)))
                        .mimeType(cursor.getString(cursor.getColumnIndex(KEY_MIME_TYPE)))
                        .dimensions(dimensions)
                        .size(cursor.getInt(cursor.getColumnIndex(KEY_SIZE)))
                        .build();
                wallpapers.add(wallpaper);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return wallpapers;
    }

    @Nullable
    public Wallpaper getRandomWallpaper() {
        if (!openDatabase()) {
            LogUtil.e("Database error: getRandomWallpaper() failed to open database");
            return null;
        }

        Wallpaper wallpaper = null;
        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_WALLPAPERS,
                null, null, null, null, null, "RANDOM()", "1");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                if (name.length() == 0) {
                    name = "Wallpaper "+ id;
                }

                wallpaper = Wallpaper.Builder()
                        .name(name)
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .build();
            } while (cursor.moveToNext());
        }
        cursor.close();
        return wallpaper;
    }

    public void deleteIconRequestData() {
        if (!openDatabase()) {
            LogUtil.e("Database error: deleteIconRequestData() failed to open database");
            return;
        }

        mDatabase.mSQLiteDatabase.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{TABLE_REQUEST});
        mDatabase.mSQLiteDatabase.delete(TABLE_REQUEST, null, null);
    }

    public void deleteWallpapers(List<Wallpaper> wallpapers) {
        if (!openDatabase()) {
            LogUtil.e("Database error: deleteWallpapers() failed to open database");
            return;
        }

        for (Wallpaper wallpaper : wallpapers) {
            mDatabase.mSQLiteDatabase.delete(TABLE_WALLPAPERS, KEY_URL +" = ?",
                    new String[]{wallpaper.getURL()});
        }
    }

    public void deleteWallpapers() {
        if (!openDatabase()) {
            LogUtil.e("Database error: deleteWallpapers() failed to open database");
            return;
        }

        mDatabase.mSQLiteDatabase.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{TABLE_WALLPAPERS});
        mDatabase.mSQLiteDatabase.delete(TABLE_WALLPAPERS, null, null);
    }
}
