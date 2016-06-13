package com.josh.wright.xkcdapp.Service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.josh.wright.xkcdapp.Constants;
import com.josh.wright.xkcdapp.UI.MainActivity;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public final class ComicService {
    private static final String TAG = "ComicService";
    private static ComicService instance = null;

    private final SparseArray<ComicBean> comics;

    private ComicService() {
        comics = new SparseArray<>();
    }

    public static ComicService getInstance() {
        if (instance == null) {
            instance = new ComicService();
        }
        return instance;
    }

    @NonNull
    public ComicBean getComic(int position) {return getComic(position, ComicUpdateCallback.noopCallback);}

    @NonNull
    public ComicBean getComic(int position, ComicUpdateCallback callback) {
        ComicBean comicBean = comics.get(position);
        if (comicBean == null) {
            comicBean = new ComicBean();
            comicBean.setNumber(position);
            comics.put(position, comicBean);
        }
        ComicDownloadHandler.downloadComic(comicBean, callback);
        return comicBean;
    }

    public void getMostRecentComic(final MostRecentComicCallback callback) {
        ComicDownloadHandler.findMostRecentComic(callback);
    }
}
