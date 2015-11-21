package example.j0sh.xkcdapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadAndSetImage extends AsyncTask<Integer, Integer, Bitmap> {

    public static final String TAG = "DownloadAndSetImage";
    private static final int IO_BUFFER_SIZE = 4*1024;

    //    ImageView imageView;
    int comic;
    MainActivity main;
    Boolean downloading;

    public DownloadAndSetImage(MainActivity _context) {
//        imageView = target;
        comic = -1;
        main = _context;
        downloading = false;
    }

    @Override
    protected Bitmap doInBackground(Integer... _comic) {
        comic = _comic[0];
        try {
            String url_string;
            Log.i(TAG, String.valueOf(comic));
            if (comic > 0) {
                url_string = String.format("http://xkcd.com/%d/info.0.json", comic);
            } else {
                url_string = "http://xkcd.com/info.0.json";
            }
            URL json_url = new URL(url_string);

            HttpURLConnection jsonConnection = (HttpURLConnection) json_url.openConnection();
            InputStream jsonInputStream = new BufferedInputStream(jsonConnection.getInputStream());

            BufferedReader r = new BufferedReader(new InputStreamReader(jsonInputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }

            JSONObject jobj = new JSONObject(total.toString());
            Log.i(TAG,"Image URL: " + jobj.getString("img"));
            comic = jobj.getInt("num");
            URL comic_url = new URL(jobj.getString("img"));

//            HttpURLConnection imageConnection = (HttpURLConnection) comic_url.openConnection();
//            InputStream imageInputStream = new BufferedInputStream(imageConnection.getInputStream());
            InputStream imageInputStream = new BufferedInputStream(comic_url.openStream());

            BufferedInputStream bis = new BufferedInputStream(imageInputStream);
            Bitmap bmap = BitmapFactory.decodeStream(bis);
            Log.i(TAG, String.format("Downloaded %d by %d bitmap", bmap.getHeight(), bmap.getWidth()));

            return bmap;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Bitmap bitmap) {
//        super.onPostExecute(bitmap);
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setCurrent_comic(comic);
                main.setImageViewBitmap(bitmap);
            }
        });
    }
}
