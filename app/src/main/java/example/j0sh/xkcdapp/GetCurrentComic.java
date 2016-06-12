package example.j0sh.xkcdapp;

import android.os.AsyncTask;

import org.json.JSONException;

import java.io.IOException;

public class GetCurrentComic extends AsyncTask<Integer, Integer, Integer> {

    public static final String TAG = "GetCurrentComic";
    MainActivity context;

    public GetCurrentComic(MainActivity _context) {
        context = _context;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        try {
            return GetComicJSON.byNumber(-1).getInt("num");
        } catch (IOException | JSONException e) {
            return 0;
        }
    }

    @Override
    protected void onPostExecute(Integer comic) {
        super.onPostExecute(comic);
        context.setCurrent_comic_num(comic);
        context.setMax_comic(comic);
        new SetComic(context).execute(comic);
    }
}
