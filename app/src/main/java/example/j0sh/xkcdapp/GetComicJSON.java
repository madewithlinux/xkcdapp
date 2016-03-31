package example.j0sh.xkcdapp;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class GetComicJSON {

    public static final String TAG = "GetComicJSON";

    public static JSONObject byNumber(int comic) throws IOException {
        String url_string;
        Log.i(TAG, String.valueOf(comic));
        if (comic > 0) {
            url_string = String.format("http://xkcd.com/%d/info.0.json", comic);
        } else {
            url_string = "http://xkcd.com/info.0.json";
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url_string)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            Log.e(TAG, "Failed to download metadata for comic: " + String.valueOf(comic), e);
        }

        JSONObject jobj = null;
        try {
            jobj = new JSONObject(response.body().string());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jobj;
    }
}
