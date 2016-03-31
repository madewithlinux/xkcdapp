package example.j0sh.xkcdapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ComicModel {

    private static String TAG = "ComicModel";

    public Boolean ready; /*have we downloaded this yet?*/
    public Integer comic_number;
    public String title;
    public String alttext;
    public Bitmap image;

    private URL jsonUrl;
    private JSONObject comicJson;

    public ComicModel(int comic_number) {
        /*todo: fragment context view parameter?*/
        this.ready = false;
        this.comic_number = comic_number;
        this.title = "";
        this.alttext = "";
        this.image = null;
        try {
            this.jsonUrl = new URL(String.format("http://xkcd.com/%d/info.0.json", comic_number));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void download() {
        /*TODO: the downloading*/
        /*TODO: callback*/
        this.ready = true;
    }


    private static class Downloader extends AsyncTask<ComicModel, Integer, ComicModel> {
        private static String TAG = ComicModel.TAG + ".Downloader";

        @Override
        protected ComicModel doInBackground(ComicModel... params) {
            ComicModel thisComic = params[0];
            URL to_download = thisComic.jsonUrl;
            Log.i(TAG, String.valueOf("Downloading " + thisComic.comic_number));


            /*download the json*/
            OkHttpClient client = new OkHttpClient();
            Request json_request = new Request.Builder()
                    .url(thisComic.jsonUrl)
                    .build();
            Response json_request_response = null;
            try {
                json_request_response = client.newCall(json_request).execute();
            } catch (IOException e) {
                Log.e(TAG, "Failed to download metadata for comic: " + String.valueOf(thisComic.comic_number), e);
            }
            assert json_request_response != null;

            /*decode the json*/
            JSONObject jobj = null;
            try {
                jobj = new JSONObject(json_request_response.body().string());
            } catch (JSONException e) {
                Log.e(TAG, "Comic " + thisComic.comic_number + " had bad JSON", e);
            } catch (IOException e) {
                Log.e(TAG, "Failed to download comic " + thisComic.comic_number, e);
            }
            assert jobj != null;
            URL image_url;
            try {
                thisComic.title = jobj.getString("title");
                thisComic.alttext = jobj.getString("alt");
                image_url = new URL(jobj.getString("img"));
            } catch (JSONException | MalformedURLException e) {
                Log.e(TAG, "Comic " + thisComic.comic_number + " had bad JSON", e);
            }

            /*download the image*/
            Request image_request = new Request.Builder()
                    .url(thisComic.jsonUrl)
                    .build();
            Response image_response;
            Bitmap comic_bitmap = null;
            try {
                image_response = client.newCall(image_request).execute();
                comic_bitmap = BitmapFactory.decodeByteArray(
                        image_response.body().bytes(),
                        0,
                        image_response.body().bytes().length
                );
            } catch (IOException e) {
                Log.e(TAG, "Failed to download comic image " + thisComic.comic_number, e);
            }
            if (comic_bitmap == null) {
                Log.i(TAG, "failed to decode bitmap");
            }
            thisComic.image = comic_bitmap;

            return thisComic;
        }

        @Override
        protected void onPostExecute(ComicModel comicModel) {
            super.onPostExecute(comicModel);
            /*TODO: set image bitmap in the fragment?*/
        }

    }
}