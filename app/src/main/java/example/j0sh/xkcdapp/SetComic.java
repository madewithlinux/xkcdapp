package example.j0sh.xkcdapp;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SetComic extends AsyncTask<Integer, Integer, Integer> {

    public static final String TAG = "SetComic";

    int comic;
    MainActivity_old main;
    Boolean downloading;
    String comic_url_str;
    String comic_title;

    public SetComic(MainActivity_old _context) {
        comic = -1;
        main = _context;
        downloading = false;
    }

    @Override
    protected Integer doInBackground(Integer... _comic) {
        comic = _comic[0];
        try {
            main.setLoading(true);
            JSONObject jobj = GetComicJSON.byNumber(comic);
            Log.i(TAG,"Image URL: " + jobj.getString("img"));
            comic = jobj.getInt("num");
            comic_url_str = jobj.getString("img");
            comic_title = jobj.getString("title");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Integer result) {
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
