package com.dm.material.dashboard.candybar.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.items.WallpaperJSON;

import java.util.ArrayList;
import java.util.List;

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

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "candybar_database";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_REQUEST = "icon_request";
    private static final String TABLE_PREMIUM_REQUEST = "premium_request";
    private static final String TABLE_WALLPAPERS = "wallpapers";

    private static final String KEY_ID = "id";

    private static final String KEY_ORDER_ID = "order_id";
    private static final String KEY_PRODUCT_ID = "product_id";
    private static final String KEY_REQUEST = "request";

    private static final String KEY_NAME = "name";
    private static final String KEY_ACTIVITY = "activity";
    private static final String KEY_REQUESTED_ON = "requested_on";

    private static final String KEY_AUTHOR = "author";
    private static final String KEY_THUMB_URL = "thumbUrl";
    private static final String KEY_URL = "url";
    private static final String KEY_ADDED_ON = "added_on";

    public Database(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_REQUEST = "CREATE TABLE IF NOT EXISTS " +TABLE_REQUEST+ "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_ACTIVITY + " TEXT NOT NULL, " +
                KEY_REQUESTED_ON + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
        String CREATE_TABLE_PREMIUM_REQUEST = "CREATE TABLE IF NOT EXISTS " +TABLE_PREMIUM_REQUEST+ "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_ORDER_ID + " TEXT NOT NULL, " +
                KEY_PRODUCT_ID + " TEXT NOT NULL, " +
                KEY_REQUEST + " TEXT NOT NULL, " +
                KEY_REQUESTED_ON + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
        String CREATE_TABLE_WALLPAPER = "CREATE TABLE IF NOT EXISTS " +TABLE_WALLPAPERS+ "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_NAME+ " TEXT NOT NULL, " +
                KEY_AUTHOR + " TEXT NOT NULL, " +
                KEY_URL + " TEXT NOT NULL, " +
                KEY_THUMB_URL + " TEXT NOT NULL, " +
                KEY_ADDED_ON + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
        db.execSQL(CREATE_TABLE_REQUEST);
        db.execSQL(CREATE_TABLE_PREMIUM_REQUEST);
        db.execSQL(CREATE_TABLE_WALLPAPER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db);
    }

    private void resetDatabase(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type=\'table\'", null);
        List<String> tables = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                tables.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        for (String table : tables) {
            try {
                String dropQuery = "DROP TABLE IF EXISTS " + table;
                if (!table.equalsIgnoreCase("SQLITE_SEQUENCE"))
                    db.execSQL(dropQuery);
            } catch (Exception ignored) {}
        }
        onCreate(db);
    }

    public void addRequest (Request request) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, request.getName());
        values.put(KEY_ACTIVITY, request.getActivity());

        db.insert(TABLE_REQUEST, null, values);
        db.close();
    }

    public boolean isRequested (String activity) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REQUEST, null, KEY_ACTIVITY + " = ?",
                new String[]{activity}, null, null, null, null);
        int rowCount = cursor.getCount();
        cursor.close();
        db.close();
        return rowCount > 0;
    }

    public void addPremiumRequest(String orderId, String productId, String request) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ORDER_ID, orderId);
        values.put(KEY_PRODUCT_ID, productId);
        values.put(KEY_REQUEST, request);

        db.insert(TABLE_PREMIUM_REQUEST, null, values);
        db.close();
    }

    public List<Request> getPremiumRequest() {
        List<Request> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PREMIUM_REQUEST,
                null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Request request = new Request(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4));
                requests.add(request);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return requests;
    }

    public boolean isWallpapersEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WALLPAPERS, null, null, null, null, null, null, null);
        int rowCount = cursor.getCount();
        cursor.close();
        db.close();
        return rowCount == 0;
    }

    public void addAllWallpapers(WallpaperJSON wallpaper) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < wallpaper.getWalls.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, wallpaper.getWalls.get(i).name);
            values.put(KEY_AUTHOR, wallpaper.getWalls.get(i).author);
            values.put(KEY_URL, wallpaper.getWalls.get(i).url);
            values.put(KEY_THUMB_URL, wallpaper.getWalls.get(i).thumbUrl);

            db.insert(TABLE_WALLPAPERS, null, values);
        }
        db.close();
    }

    public List<Wallpaper> getWallpapers() {
        List<Wallpaper> wallpapers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WALLPAPERS,
                null, null, null, null, null, KEY_ADDED_ON + " DESC, " +KEY_ID);
        if (cursor.moveToFirst()) {
            do {
                Wallpaper wallpaper = new Wallpaper(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4));
                wallpapers.add(wallpaper);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return wallpapers;
    }

    public Wallpaper getRandomWallpaper() {
        Wallpaper wallpaper = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " +TABLE_WALLPAPERS+ " ORDER BY RANDOM() LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                wallpaper = new Wallpaper(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return wallpaper;
    }

    public List<Wallpaper> getWallpaperAddedOn() {
        List<Wallpaper> date = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WALLPAPERS, new String[]{
                KEY_URL, KEY_ADDED_ON}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Wallpaper item = new Wallpaper(
                        cursor.getString(0),
                        cursor.getString(1));
                date.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return date;
    }

    public void setWallpaperAddedOn (String url, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ADDED_ON, date);
        db.update(TABLE_WALLPAPERS, values, KEY_URL + " = ?", new String[]{url});
        db.close();
    }

    public void deleteAllWalls() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{TABLE_WALLPAPERS});
        db.delete(TABLE_WALLPAPERS, null, null);
        db.close();
    }

}
