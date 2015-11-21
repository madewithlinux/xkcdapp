package example.j0sh.xkcdapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetCurrentComic extends AsyncTask<Integer, Integer, Integer> {

    public static final String TAG = "GetCurrentComic";
    MainActivity context;

    public GetCurrentComic(MainActivity _context) {
        context = _context;
    }

    @Override
    protected Integer doInBackground(Integer... params) {

        String url_string = "http://xkcd.com/info.0.json";
        URL first_comic_url = null;
        try {
            first_comic_url = new URL(url_string);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            connection = (HttpURLConnection) first_comic_url.openConnection();
            inputStream = new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonobj = null;
        try {
            jsonobj = new JSONObject(total.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            assert jsonobj != null;
            Log.i(TAG, "Downloaded current comic number");
            return jsonobj.getInt("num");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer comic) {
        super.onPostExecute(comic);
        context.setCurrent_comic(comic);
        context.setMax_comic(comic);
        new DownloadAndSetImage(context).execute(comic);
    }
}
