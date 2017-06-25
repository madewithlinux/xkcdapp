package com.josh.wright.xkcdapp.Service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josh.wright.xkcdapp.Constants;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ComicDownloadHandler {
    private static final String TAG = "ComicDownloadHandler";
    private static final String KEY_ALT = "alt";
    private static final String KEY_IMG = "img";
    private static final String KEY_TITLE = "title";
    private static final String KEY_NUMBER = "num";
    private static final String KEY_SAFE_TITLE = "safe_title";

    private static final ObjectMapper mapper = new ObjectMapper();

    /* may run on UI thread
     * if the comic is already downloaded, does nothing
     */
    public static void downloadComic(ComicBean comicBean, ComicUpdateCallback callback) {
        if (comicBean.getNumber() == 0) {
            /*there is not zero comic*/
            return;
        }
        if (comicBean.getImageUrl() == null) {
            new DownloadComicJson(comicBean, callback).execute();
        } else if (comicBean.getImageBitmap() == null) {
            new DownloadComicImage(comicBean, callback).execute();
        }
    }

    public static void findMostRecentComic(MostRecentComicCallback callback) {
        new FindMostRecentComic(callback).execute();

    }

    private static final class FindMostRecentComic extends AsyncTask<Void, Void, Void> {
        private final MostRecentComicCallback callback;
        private int mostRecent;

        public FindMostRecentComic(MostRecentComicCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                /*donwload json*/
                Request jsonRequest = new Request.Builder()
                        .url(Constants.JSON_URL_MOST_RECENT)
                        .build();
                Response jsonResponse = client.newCall(jsonRequest).execute();
                JSONObject jsonObject = new JSONObject(jsonResponse.body().string());
                mostRecent = jsonObject.getInt(KEY_NUMBER);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            callback.onDone(mostRecent);
        }
    }

    private static final class DownloadComicJson extends AsyncTask<Void, Void, Void> {
        private final ComicBean comicBean;
        private final ComicUpdateCallback updateCallback;
        private ComicUpdateCallback.Result result;

        public DownloadComicJson(ComicBean comicBean, ComicUpdateCallback updateCallback) {
            this.comicBean = comicBean;
            this.updateCallback = updateCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (comicBean.getImageUrl() != null) {
                    result = ComicUpdateCallback.Result.SUCCESS;
                    return null;
                }
                /*assume failure until we succeed*/
                result = ComicUpdateCallback.Result.FAILURE;
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
                // must copy into the destination, because overwriting it will break the reference
                // held in the other thread
                comicBean.setFrom(mapper.readValue(jsonResponse.body().bytes(), ComicBean.class));
                Log.d(TAG, "downloaded json for comic " + comicBean.getNumber());
                Log.d(TAG, "comic " + comicBean.getNumber() + "url: " + comicBean.getImageUrl());
                result = ComicUpdateCallback.Result.SUCCESS;
            } catch (IOException e) {
                /*todo error handling*/
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateCallback.onUpdate(result);
            if (result == ComicUpdateCallback.Result.SUCCESS) {
                new DownloadComicImage(comicBean, updateCallback).execute();
            }
        }
    }

    private final static class DownloadComicImage extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "DownloadComicImage";
        private final ComicBean comicBean;
        private final ComicUpdateCallback callback;
        private ComicUpdateCallback.Result result;

        public DownloadComicImage(ComicBean comicBean, ComicUpdateCallback callback) {
            this.comicBean = comicBean;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                /*assume failure until success*/
                result = ComicUpdateCallback.Result.FAILURE;

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

            } catch (IOException e) {
                Log.e(TAG, "error downloading comic", e);
            }

            result = ComicUpdateCallback.Result.SUCCESS;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "running ComicUpdateCallback");
            callback.onUpdate(result);
        }
    }


}
