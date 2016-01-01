package example.j0sh.xkcdapp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class SetComic extends AsyncTask<Integer, Integer, Bitmap> {

    public static final String TAG = "SetComic";
    private static final int IO_BUFFER_SIZE = 4*1024;

    int comic;
    MainActivity main;
    Boolean downloading;
    String comic_url_str;
    String comic_title;

    public SetComic(MainActivity _context) {
        comic = -1;
        main = _context;
        downloading = false;
    }

    @Override
    protected Bitmap doInBackground(Integer... _comic) {
        comic = _comic[0];
        try {
            main.setLoading(true);
            JSONObject jobj = GetComicJSON.byNumber(comic);
            Log.i(TAG,"Image URL: " + jobj.getString("img"));
            comic = jobj.getInt("num");
            URL comic_url = new URL(jobj.getString("img"));
            comic_url_str = jobj.getString("img");
            comic_title = jobj.getString("title");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Bitmap bitmap) {
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setImageViewByURL(comic_url_str);
                main.setCurrent_comic_num(comic);
                main.setTitle(comic_title);
            }
        });
    }
}
