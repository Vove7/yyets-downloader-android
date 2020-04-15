package com.yyets.zimuzu.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.yyets.zimuzu.db.bean.FilmCacheBean;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cn.vove7.rr_lib.InitCp;

public enum DBCache {
    instance;
    
    private Set<String> filmCacheBeans = new HashSet<>();
    private DBHelper mDBHelper = new DBHelper(InitCp.androidContext);

    private SQLiteDatabase mDb = mDBHelper.getWritableDatabase();
    static {
        instance.getAllCacheItems();
    }

    public synchronized void deleteCache(String key) {
        long delete = (long) this.mDb.delete(DBHelper.TB_DB_CACHE, "key=?", new String[]{key});
    }

    public synchronized void putString(String key, String data) {
        ContentValues cv = new ContentValues();
        byte[] dataArr = data.getBytes();
        cv.put("key", key);
        cv.put("data", dataArr);
        cv.put(DBHelper.COL_UPDATE_TIME, Integer.valueOf((int) (System.currentTimeMillis() / 1000)));
        long insertWithOnConflict = this.mDb.insertWithOnConflict(DBHelper.TB_DB_CACHE, (String) null, cv, 5);
    }

    public synchronized void putInt(String key, int data) {
        putString(key, String.valueOf(data));
    }

    public synchronized void putBoolean(String key, boolean data) {
        putString(key, data ? "true" : "false");
    }

    public String getString(String key) {
        String data = null;
        Cursor cursor = this.mDb.query(false, DBHelper.TB_DB_CACHE, (String[]) null, "key=?", new String[]{key}, (String) null, (String) null, (String) null, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int validity = cursor.getInt(cursor.getColumnIndex(DBHelper.COL_VALIDITY));
                    int updateTime = cursor.getInt(cursor.getColumnIndex(DBHelper.COL_UPDATE_TIME));
                    if (validity == 0 || (System.currentTimeMillis() / 1000) - ((long) validity) <= ((long) updateTime)) {
                        data = new String(cursor.getBlob(cursor.getColumnIndex("data")));
                    } else {
                        cursor.close();
                        if (cursor == null) {
                            return null;
                        }
                        cursor.close();
                        return null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
        cursor.close();
        if (cursor != null) {
            cursor.close();
        }
        return data;
    }

    public int getInt(String key, int def) {
        String data = getString(key);
        return data == null ? def : Integer.parseInt(data, def);
    }

    public boolean getBoolean(String key, boolean def) {
        String data = getString(key);
        return TextUtils.isEmpty(data) ? def : "true".equals(data);
    }

    public synchronized long updateDownloadPosition(FilmCacheBean data) {
        long updateDownloadPosition;
        if (data == null) {
            updateDownloadPosition = -1;
        } else {
            updateDownloadPosition = updateDownloadPosition(data.mFilmId, data.mFileId, data.mDownloadUrl, data.mFilmName, data.mFileName, data.mSeason, data.mEpisode, data.mSize, data.mFormatted, data.mSubtitle, data.mLoadPosition, data.mLength, data.mDownloadTime, data.mP4PUrl, data.mFilmImg);
        }
        return updateDownloadPosition;
    }

    public synchronized long updateDownloadPosition(String filmId, String fileId, String url, String filmName, String fileName, String season, String episode, long size, String formatted, String subtitle, long loadPos, long length, long downloadTime, String p4pUrl, String filmImg) {
        long result;
        if (this.filmCacheBeans.contains(url)) {
            ContentValues cv = new ContentValues();
            cv.put("film_id", filmId);
            cv.put(DBHelper.COL_FILE_ID, fileId);
            cv.put("url", url);
            cv.put(DBHelper.COL_FILM_NAME, filmName);
            cv.put(DBHelper.COL_FILE_NAME, fileName);
            cv.put("season", season);
            cv.put("episode", episode);
            cv.put(DBHelper.COL_FILE_SIZE, String.valueOf(size));
            cv.put(DBHelper.COL_FILM_FORMATTED, formatted);
            cv.put(DBHelper.COL_FILE_LOAD_POS, String.valueOf(loadPos));
            cv.put(DBHelper.COL_FILE_LENGTH, String.valueOf(length));
            cv.put(DBHelper.COL_FILM_SUBTITLE, subtitle);
            cv.put(DBHelper.COL_DOWNLOAD_TIME, String.valueOf(downloadTime));
            cv.put(DBHelper.COL_P4P_URL, p4pUrl);
            cv.put(DBHelper.COL_IMG_URL, filmImg);
            result = (long) this.mDb.update(DBHelper.TB_FILE_DOWNLOAD, cv, "url=?", new String[]{url});
        } else {
            this.filmCacheBeans.add(url);
            ContentValues cv2 = new ContentValues();
            cv2.put("film_id", filmId);
            cv2.put(DBHelper.COL_FILE_ID, fileId);
            cv2.put("url", url);
            cv2.put(DBHelper.COL_FILM_NAME, filmName);
            cv2.put(DBHelper.COL_FILE_NAME, fileName);
            cv2.put("season", season);
            cv2.put("episode", episode);
            cv2.put(DBHelper.COL_FILE_SIZE, String.valueOf(size));
            cv2.put(DBHelper.COL_FILM_FORMATTED, formatted);
            cv2.put(DBHelper.COL_FILE_LOAD_POS, String.valueOf(loadPos));
            cv2.put(DBHelper.COL_FILE_LENGTH, String.valueOf(length));
            cv2.put(DBHelper.COL_FILM_SUBTITLE, subtitle);
            cv2.put(DBHelper.COL_DOWNLOAD_TIME, String.valueOf(downloadTime));
            cv2.put(DBHelper.COL_P4P_URL, p4pUrl);
            cv2.put(DBHelper.COL_IMG_URL, filmImg);
            result = this.mDb.insert(DBHelper.TB_FILE_DOWNLOAD, (String) null, cv2);
        }
        return result;
    }

    public FilmCacheBean getCacheById(String id) {
        FilmCacheBean data = null;
        if (!TextUtils.isEmpty(id)) {
            Cursor cursor = this.mDb.query(false, DBHelper.TB_FILE_DOWNLOAD, (String[]) null, "file_id=?", new String[]{id}, (String) null, (String) null, (String) null, (String) null);
            if (cursor != null && cursor.moveToFirst()) {
                data = getFilmCacheByCursor(cursor);
            }
            cursor.close();
        }
        return data;
    }

    public ArrayList<FilmCacheBean> getAllCacheItems() {
        ArrayList<FilmCacheBean> results = new ArrayList<>();
        Cursor cursor = this.mDb.rawQuery("SELECT * FROM file_download ORDER BY film_name, CAST(season as int), CAST(episode as int), download_time DESC", (String[]) null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FilmCacheBean bean = getFilmCacheByCursor(cursor);
                results.add(bean);
                this.filmCacheBeans.add(bean.mDownloadUrl);
            }
            cursor.close();
        }
        return results;
    }

    public ArrayList<FilmCacheBean> getAllCacheItemsByTime() {
        ArrayList<FilmCacheBean> results = new ArrayList<>();
        Cursor cursor = this.mDb.rawQuery("SELECT * FROM file_download ORDER BY download_time DESC, film_name, CAST(season as int), CAST(episode as int)", (String[]) null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FilmCacheBean bean = getFilmCacheByCursor(cursor);
                results.add(bean);
                this.filmCacheBeans.add(bean.mDownloadUrl);
            }
            cursor.close();
        }
        return results;
    }

    public ArrayList<FilmCacheBean> getAllCacheItemsByASCTime() {
        ArrayList<FilmCacheBean> results = new ArrayList<>();
        Cursor cursor = this.mDb.rawQuery("SELECT * FROM file_download ORDER BY download_time ASC, film_name, CAST(season as int), CAST(episode as int)", (String[]) null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FilmCacheBean bean = getFilmCacheByCursor(cursor);
                results.add(bean);
                this.filmCacheBeans.add(bean.mDownloadUrl);
            }
            cursor.close();
        }
        return results;
    }

    public FilmCacheBean getFilmDownloadBean(String url) {
        FilmCacheBean data = null;
        if (!TextUtils.isEmpty(url)) {
            Cursor cursor = this.mDb.query(false, DBHelper.TB_FILE_DOWNLOAD, (String[]) null, "url=?", new String[]{url}, (String) null, (String) null, (String) null, (String) null);
            if (cursor != null && cursor.moveToFirst()) {
                data = getFilmCacheByCursor(cursor);
            }
            cursor.close();
        }
        return data;
    }

    private FilmCacheBean getFilmCacheByCursor(Cursor cursor) {
        DecimalFormat df = new DecimalFormat("#.00");
        FilmCacheBean cache = new FilmCacheBean();
        cache.mFilmId = cursor.getString(cursor.getColumnIndex("film_id"));
        cache.mFileId = cursor.getString(cursor.getColumnIndex(DBHelper.COL_FILE_ID));
        cache.mDownloadUrl = cursor.getString(cursor.getColumnIndex("url"));
        cache.mFileName = cursor.getString(cursor.getColumnIndex(DBHelper.COL_FILE_NAME));
        cache.mLoadPosition = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBHelper.COL_FILE_LOAD_POS)));
        cache.mFilmName = cursor.getString(cursor.getColumnIndex(DBHelper.COL_FILM_NAME));
        cache.mSeason = cursor.getString(cursor.getColumnIndex("season"));
        cache.mEpisode = cursor.getString(cursor.getColumnIndex("episode"));
        cache.mSize = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBHelper.COL_FILE_SIZE)));
        cache.mLength = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBHelper.COL_FILE_LENGTH)));
        if (cache.mLength == 0) {
            cache.mLength = cache.mSize;
        }
        cache.mProgress = cache.mLength == 0 ? 0.0f : Float.parseFloat(df.format((((double) cache.mLoadPosition) * 100.0d) / ((double) cache.mLength)));
        if (cache.mProgress > 100.0f) {
            cache.mProgress = 100.0f;
        }
        cache.mFormatted = cursor.getString(cursor.getColumnIndex(DBHelper.COL_FILM_FORMATTED));
        cache.mSubtitle = cursor.getString(cursor.getColumnIndex(DBHelper.COL_FILM_SUBTITLE));
        cache.mDownloadTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBHelper.COL_DOWNLOAD_TIME)));
        cache.mP4PUrl = cursor.getString(cursor.getColumnIndex(DBHelper.COL_P4P_URL));
        cache.mFilmImg = cursor.getString(cursor.getColumnIndex(DBHelper.COL_IMG_URL));
        return cache;
    }

    public long removeFilmCache(String downloadUrl) {
        this.filmCacheBeans.remove(downloadUrl);
        return (long) this.mDb.delete(DBHelper.TB_FILE_DOWNLOAD, "url=?", new String[]{downloadUrl});
    }

    public synchronized boolean hasCollectionResource(String resourceId) {
        boolean status;
        Cursor c = this.mDb.rawQuery("SELECT * FROM collection_resource WHERE resource_id = '" + resourceId + "'", (String[]) null);
        status = c.getCount() != 0;
        c.close();
        return status;
    }

}