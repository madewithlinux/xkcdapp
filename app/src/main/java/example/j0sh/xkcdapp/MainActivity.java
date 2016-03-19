package example.j0sh.xkcdapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;
import java.util.UUID;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "xkcdMainActivity";
    private static final String explination_url = "http://www.explainxkcd.com/wiki/index.php/";
    private static final String xkcd_mobile_url = "http://m.xkcd.com/";
    private int current_comic;/*the current comic being displayed*/
    private int max_comic; /*the most recent comic, and therefore the maximum comic to serve*/
    private ContentResolver resolver;
    private ImageViewTouch imageViewTouch;
    private ProgressBar loader;

    public MainActivity() {
        current_comic = -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resolver = getContentResolver();
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
                final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(R.string.number_popup_title);
                final LayoutInflater inflater = getLayoutInflater();
                final View view = inflater.inflate(R.layout.layout_select_comic, null);
                dialog.setView(view);
                final EditText comic_field = (EditText) view.findViewById(R.id.comic_number_input);
                dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ((comic_field.getText() == null)
                                || comic_field.getText().toString().equals("")) {
                            return;
                        }
                        final int comic_num;
                        try {
                            comic_num = Integer.parseInt(comic_field.getText().toString());
                        } catch (NumberFormatException e) {
                            /*invalid number, do nothing*/
                            Log.i(TAG, "Invalid number: " + comic_field.getText());
                            return;
                        }
                        if (comic_num > max_comic) {
                            Log.i(TAG, "comic number past current comic: " + comic_field.getText());
                            return;
                        }
                        new SetComic(MainActivity.this).execute(comic_num);
                    }
                });
                dialog.show();
                return true;
            case R.id.action_random_comic:
                final Random r = new Random();
                final int next_comic = r.nextInt(max_comic);
                new SetComic(MainActivity.this).execute(next_comic);
                return true;
            case R.id.action_todays_comic:
                new GetCurrentComic(this).execute();
                return true;
            case R.id.action_explination:
                final Intent browserExplination = new Intent(Intent.ACTION_VIEW, Uri.parse(explination_url + current_comic));
                startActivity(browserExplination);
                return true;
            case R.id.action_view_in_browser:
                final Intent browserComic = new Intent(Intent.ACTION_VIEW, Uri.parse(xkcd_mobile_url + current_comic));
                startActivity(browserComic);
                return true;
            case R.id.action_share_image: {
                final GlideBitmapDrawable drawable = (GlideBitmapDrawable) imageViewTouch.getDrawable();
                this.shareBitmap(drawable.getBitmap());
                return true;
            }
            case R.id.action_save_image: {

                /*TODO: make this part work with android 6.0 new permissions system*/
//                int permission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                final Uri uri = Uri.parse("content://media/external/images/media");
                final String provider = "com.android.providers.media.MediaProvider";
                grantUriPermission(provider, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                grantUriPermission(provider, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                grantUriPermission(provider, uri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                final GlideBitmapDrawable drawable = (GlideBitmapDrawable) imageViewTouch.getDrawable();
                MediaStore.Images.Media.insertImage(resolver, drawable.getBitmap(), this.getTitle().toString(), null);
                return true;
            }
        }
        return false;
    }

    @SuppressLint("SetWorldReadable")
    private void shareBitmap(Bitmap bitmap) {
        /*puts the image in the cache so as not to litter the filesystem*/
        try {
            File file = new File(this.getCacheDir(), UUID.randomUUID().toString() + ".png");
            FileOutputStream fOut = new FileOutputStream(file);
            Log.i(TAG, "sharing image");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent shareImageIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareImageIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            shareImageIntent.setType("image/png");
            Intent intentChooser = Intent.createChooser(shareImageIntent, "Share Comic Image");
            startActivity(intentChooser);
        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
    }


    public void setCurrent_comic_num(int current_comic) {
        this.current_comic = current_comic;
        Log.i(TAG, String.format("Set comic: %d", current_comic));
    }

    public void setMax_comic(int max_comic) {
        this.max_comic = max_comic;
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
