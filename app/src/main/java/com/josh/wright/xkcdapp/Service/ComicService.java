package com.josh.wright.xkcdapp.Service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.josh.wright.xkcdapp.Constants;
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

    private static final ComicUpdateCallback noopCallback = new ComicUpdateCallback() {
        @Override
        public void onUpdate() {

        }
    };

    @NonNull
    public ComicBean getComic(int position) {return getComic(position, noopCallback);}

    @NonNull
    public ComicBean getComic(int position, ComicUpdateCallback callback) {
        ComicBean comicBean = comics.get(position);
        if (comicBean == null) {
            comicBean = new ComicBean();
//            comicBean.setNumber(ComicBean.MOST_RECENT_COMIC);
            comicBean.setNumber(position);
            comics.put(position, comicBean);
            new DownloadComic(comicBean, callback).execute();
        }
        return comicBean;
    }

    private static void downloadComicJson(ComicBean comicBean) throws IOException, JSONException {
        if (comicBean.getJsonObject() != null) {
            return;
        }
        final String KEY_ALT = "alt";
        final String KEY_IMG = "img";
        final String KEY_TITLE = "title";
        final String KEY_NUMBER = "num";
        final String KEY_SAFE_TITLE = "safe_title";
        String url;
        int comicNumber = comicBean.getNumber();
        if (comicNumber == ComicBean.MOST_RECENT_COMIC) {
            url = Constants.JSON_URL_MOST_RECENT;
        } else {
            url = Constants.JSON_URL_1 + String.valueOf(comicNumber) + Constants.JSON_URL_2;
        }
        Log.d(TAG, "downloading comic " + String.valueOf(comicNumber)
                + " from url " + url
        );

        OkHttpClient client = new OkHttpClient();
                /*donwload json*/
        Request jsonRequest = new Request.Builder()
                .url(url)
                .build();
        Response jsonResponse = client.newCall(jsonRequest).execute();
        JSONObject jsonObject = new JSONObject(jsonResponse.body().string());
        comicBean.setJsonObject(jsonObject);
        comicBean.setTitle(jsonObject.getString(KEY_TITLE));
        comicBean.setNumber(jsonObject.getInt(KEY_NUMBER));
        comicBean.setImageUrl(jsonObject.getString(KEY_IMG));
        comicBean.setAltText(jsonObject.getString(KEY_ALT));
        comicBean.setSafeTitle(jsonObject.getString(KEY_SAFE_TITLE));
        Log.d(TAG, "downloaded json");
    }

    private final class DownloadComic extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "DownloadComic";

        private final ComicBean comicBean;
        private final ComicUpdateCallback callback;

        public DownloadComic(ComicBean comicBean, ComicUpdateCallback callback) {
            this.comicBean = comicBean;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (comicBean.getNumber() == 0) {
                    /*there is not zero comic*/
                    return null;
                }
                downloadComicJson(comicBean);

                /*download image*/
                OkHttpClient client = new OkHttpClient();
                Request imageRequest = new Request.Builder()
                        .url(comicBean.getImageUrl())
                        .build();
                Response imageResponse = client.newCall(imageRequest).execute();
                byte[] bytes = imageResponse.body().bytes();
                Log.d(TAG, "bytes: " + bytes + " length: " + bytes.length);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    Log.d(TAG, "decoded image to bitmap " + bitmap.toString());
                } else {
                    Log.e(TAG, "decoded image, bitmap is null");
                }
                comicBean.setImageBitmap(bitmap);

            } catch (IOException | JSONException e) {
                Log.e(TAG, "error downloading comic", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "running ComicUpdateCallback");
            callback.onUpdate();
        }
    }

    public void getMostRecentComic(final MostRecentComicCallback callback) {
        new DownloadMostRecent(callback).execute();
    }

    private final class DownloadMostRecent extends AsyncTask<Void, Void, Integer> {
        private final MostRecentComicCallback callback;

        public DownloadMostRecent(MostRecentComicCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            ComicBean comicBean = new ComicBean();
            comicBean.setNumber(ComicBean.MOST_RECENT_COMIC);
            try {
                downloadComicJson(comicBean);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return comicBean.getNumber();
        }

        @Override
        protected void onPostExecute(Integer i) {
            callback.onDone(i);
        }
    }


}
