package com.josh.wright.xkcdapp.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.josh.wright.xkcdapp.Constants;
import com.josh.wright.xkcdapp.R;
import com.josh.wright.xkcdapp.Service.ComicBean;
import com.josh.wright.xkcdapp.Service.ComicService;
import com.josh.wright.xkcdapp.Service.MostRecentComicCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String KEY_LAST_COMIC = "lastComic";
    private static final String KEY_COUNT = "comicCount";

    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Loading...");
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.container);
        assert viewPager != null;
        viewPager.setAdapter(sectionsPagerAdapter);
        /*this updated the title for after the active comic is changed*/
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setTitle(getCurrentComic().getTitle());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (savedInstanceState == null) {
            goToMostRecent();
        } else {
            viewPager.setCurrentItem(savedInstanceState.getInt(KEY_LAST_COMIC));
            sectionsPagerAdapter.setCount(savedInstanceState.getInt(KEY_COUNT));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_LAST_COMIC, getComicNumber());
        outState.putInt(KEY_COUNT, sectionsPagerAdapter.getCount());
    }

    private void goToMostRecent() {
        ComicService.getInstance().getMostRecentComic(new MostRecentComicCallback() {
            @Override
            public void onDone(int mostRecent) {
                Log.d(TAG, "setting current comic to " + mostRecent);
                sectionsPagerAdapter.setCount(mostRecent + 1);
                viewPager.setCurrentItem(mostRecent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            /*todo: these first two are a little messy*/
            case R.id.action_show_alt_text:
                showAltText();
                return true;
            case R.id.action_goto_comic: {
                final EditText editText = new EditText(this);
                editText.setTextSize((float) 22);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(this)
                        .setView(editText)
                        .setTitle(getString(R.string.comic_number))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    int comicNumber = Integer.valueOf(editText.getText().toString());
                                    viewPager.setCurrentItem(comicNumber);
                                } catch (NumberFormatException e) { }
                            }
                        })
                        .show();
                return true;
            }
            case R.id.action_random_comic: {
                final Random r = new Random();
                final int next_comic = r.nextInt(sectionsPagerAdapter.getCount());
                viewPager.setCurrentItem(next_comic);
                return true;
            }
            case R.id.action_todays_comic:
                goToMostRecent();
                return true;
            case R.id.action_explination: {
                final Uri uri = Uri.parse(Constants.EXPLINATION_URL + viewPager.getCurrentItem());
                final Intent browserExplination = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserExplination);
                return true;
            }
            case R.id.action_view_in_browser: {
                final Uri uri = Uri.parse(Constants.XKCD_MOBILE_URL + viewPager.getCurrentItem());
                final Intent browserComic = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserComic);
                return true;
            }
            case R.id.action_share_image: {
                try {
                    File file = new File(this.getCacheDir(), getCurrentComic().getSafeTitle() + ".png");
                    FileOutputStream fOut = new FileOutputStream(file);
                    Log.i(TAG, "sharing image");
                    getCurrentComic().getImageBitmap().compress(Bitmap.CompressFormat.PNG, 100, fOut);
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
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAltText() {
        final TextView textView = new TextView(this);
        textView.setTextSize(22);
        textView.setPadding(50, 0, 50, 0);
        textView.setText(getCurrentComic().getAltText());
        new AlertDialog.Builder(this)
                .setView(textView)
                .setTitle(getString(R.string.alt_text_title))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private final class SectionsPagerAdapter extends FragmentPagerAdapter {
        private int count;

        public void setCount(int count) {
            this.count = count;
            notifyDataSetChanged();
        }

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ComicFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return count;
        }
    }

    public int getComicNumber() {
        return viewPager.getCurrentItem();
    }

    private ComicBean getCurrentComic() {
        return ComicService.getInstance().getComic(viewPager.getCurrentItem());
    }
}
