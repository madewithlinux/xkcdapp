package example.j0sh.xkcdapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "xkcdMainActivity";
    private int current_comic;
    private int max_comic;
    private ImageView imageView;

    public MainActivity() {
        current_comic = -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        Log.i(TAG, String.valueOf(current_comic));

        new GetCurrentComic(this).execute();

        Button prev_button = (Button) findViewById(R.id.btn_prev);
        prev_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_comic > 0) {
                    current_comic--;
                    new DownloadAndSetImage(MainActivity.this).execute(current_comic);
                }
            }
        });

        Button next_button = (Button) findViewById(R.id.btn_next);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_comic < max_comic) {
                    current_comic++;
                    new DownloadAndSetImage(MainActivity.this).execute(current_comic);
                }
            }
        });

        Button alt_text_button = (Button) findViewById(R.id.btn_alt_text);
        alt_text_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetAltText(MainActivity.this).execute(current_comic);
            }
        });


    }

    public void setCurrent_comic(int current_comic) {
        this.current_comic = current_comic;
        Log.i(TAG, String.format("Set comic: %d", current_comic));
    }

    public void setMax_comic(int max_comic) {
        this.max_comic = max_comic;
    }

    public void setImageViewBitmap(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

}
