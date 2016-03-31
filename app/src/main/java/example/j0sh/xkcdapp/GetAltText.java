package example.j0sh.xkcdapp;

import android.app.AlertDialog;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class GetAltText extends AsyncTask<Integer, Integer, String> {

    public static final String TAG = "GetAltText";

    MainActivity_old main;

    public GetAltText(MainActivity_old _main) {
        main = _main;
    }

    @Override
    protected String doInBackground(Integer... params) {
        int comic = params[0];

        JSONObject jsonObject = null;
        try {
            jsonObject = GetComicJSON.byNumber(comic);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String alt_text;
        try {
            assert jsonObject != null;
            alt_text = jsonObject.getString("alt");
            return alt_text;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
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
