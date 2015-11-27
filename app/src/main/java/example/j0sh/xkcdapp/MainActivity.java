package example.j0sh.xkcdapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.Random;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "xkcdMainActivity";
    private int current_comic;/*the current comic being displayed*/
    private int max_comic; /*the most recent comic, and therefore the maximum comic to serve*/
    private ImageViewTouch imageViewTouch;
    private ProgressBar loader;

    public MainActivity() {
        current_comic = -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewTouch = (ImageViewTouch) findViewById(R.id.imageView);
        imageViewTouch.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

        loader = (ProgressBar) findViewById(R.id.LoadingSpinner);
        loader.setIndeterminate(true);
        loader.setVisibility(View.INVISIBLE);


        Log.i(TAG, String.valueOf(current_comic));

        new GetCurrentComic(this).execute();

        Button prev_button = (Button) findViewById(R.id.btn_prev);
        prev_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_comic > 0) {
                    current_comic--;
                    new SetComic(MainActivity.this).execute(current_comic);
                }
            }
        });

        Button next_button = (Button) findViewById(R.id.btn_next);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_comic < max_comic) {
                    current_comic++;
                    new SetComic(MainActivity.this).execute(current_comic);
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

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
////        getSupportActionBar().setIcon(android.R.drawable.ic_menu_more);
//        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_more);
//        getSupportActionBar().setDisplayShowCustomEnabled(true);
//        getSupportActionBar().setCustomView(android.R.drawable.ic_menu_more);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_comic_label:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("xkcd Number:");
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.layout_select_comic, null);
                dialog.setView(view);
                final EditText comic_field = (EditText) view.findViewById(R.id.comic_number_input);
                dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ((comic_field.getText() == null) || comic_field.getText().toString().equals("")) {
                            return;
                        }
                        int comic_num = Integer.parseInt(comic_field.getText().toString());
                        new SetComic(MainActivity.this).execute(comic_num);
                    }
                });
                dialog.show();
                return true;
            case R.id.action_random_comic:
                Random r = new Random();
                int next_comic = r.nextInt(max_comic);
                new SetComic(MainActivity.this).execute(next_comic);
                return true;
            case R.id.action_todays_comic:
//                new SetComic(MainActivity.this).execute(max_comic);
                new GetCurrentComic(this).execute();
                return true;
        }
        return false;
    }


    public void setCurrent_comic_num(int current_comic) {
        this.current_comic = current_comic;
        Log.i(TAG, String.format("Set comic: %d", current_comic));
    }

    public void setMax_comic(int max_comic) {
        this.max_comic = max_comic;
    }

    public void setImageViewBitmap(Bitmap bitmap) {
        imageViewTouch.setImageBitmap(bitmap);
    }

    public void setImageViewByURL(String url) {
        /*remove the old image*/
        imageViewTouch.setImageBitmap(null);
        /*start loading*/
        MainActivity.this.setLoading(true);
        Glide.with(this).load(url).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                /*reset the loader if we get an exception*/
                MainActivity.this.setLoading(false);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                /*reset the loader when we set the image*/
                MainActivity.this.setLoading(false);
                return false;
            }
        }).into(imageViewTouch);
    }

    public void setLoading(final Boolean loading) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loading) {
                    loader.setVisibility(View.VISIBLE);
                } else {
                    loader.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public int getCurrent_comic() {
        return current_comic;
    }
}
