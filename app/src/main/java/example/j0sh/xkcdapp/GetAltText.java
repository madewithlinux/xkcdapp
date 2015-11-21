package example.j0sh.xkcdapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.DialerFilter;

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

public class GetAltText extends AsyncTask<Integer, Integer, String> {

    public static final String TAG = "GetAltText";

    MainActivity main;

    public GetAltText(MainActivity _main) {
        main = _main;
    }

    @Override
    protected String doInBackground(Integer... params) {
        int comic = params[0];


        String url_string;
        Log.i(TAG, String.valueOf(comic));
        if (comic > 0) {
            url_string = String.format("http://xkcd.com/%d/info.0.json", comic);
        } else {
            url_string = "http://xkcd.com/info.0.json";
        }

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
            Log.i(TAG, "Downloaded alt text: " + String.valueOf(comic));
            return jsonobj.getString("alt");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        builder.setMessage(s);
        builder.setTitle("alt-text");
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
