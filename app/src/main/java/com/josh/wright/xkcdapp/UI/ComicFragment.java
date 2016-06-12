package com.josh.wright.xkcdapp.UI;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.josh.wright.xkcdapp.R;
import com.josh.wright.xkcdapp.Service.ComicBean;
import com.josh.wright.xkcdapp.Service.ComicService;
import com.josh.wright.xkcdapp.Service.ComicUpdateCallback;


public class ComicFragment extends Fragment implements ComicUpdateCallback {
    private static final String TAG = "ComicFragment";
    private static final String ARG_POSITION = "position";
    private int position;
    private ComicBean comic;
    private SubsamplingScaleImageView imageView;
    private ProgressBar progressBar;
    private MainActivity mainActivity;

    public static ComicFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        ComicFragment fragment = new ComicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.position = getArguments().getInt(ARG_POSITION);
        this.comic = ComicService.getInstance().getComic(position, this);

        Log.d(TAG, "fragment " + String.valueOf(position));
        View view = inflater.inflate(R.layout.fragment_comic, container, false);
        mainActivity = (MainActivity) getActivity();
        imageView = (SubsamplingScaleImageView) view.findViewById(R.id.imageViewComic);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBarComic);
        progressBar.setVisibility(View.VISIBLE);
        if (comic.getImageBitmap() != null) {
            imageView.setImage(ImageSource.bitmap(comic.getImageBitmap()));
            progressBar.setVisibility(View.INVISIBLE);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "this comic is " + comic.getNumber());
            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mainActivity.showAltText();
                return true;
            }
        });
        imageView.setMinimumDpi(40);
        updateTitle();
        return view;
    }

    private void updateTitle() {
        /*this updates the title for after the metadata is downloaded*/
        if (comic != null && mainActivity.getComicNumber() == comic.getNumber()) {
            Log.d(TAG, "setting title to " + comic.getTitle());
            mainActivity.setTitle(comic.getTitle());
        }
    }

    @Override
    public void onUpdate() {
        updateTitle();
        Log.d(TAG, "updating comic " + String.valueOf(comic.getNumber()));
        if (comic.getImageBitmap() != null) {
            imageView.setImage(ImageSource.bitmap(comic.getImageBitmap()));
            Log.d(TAG, comic.getImageBitmap().toString());
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            Log.d(TAG, "bitmap is null");
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        updateTitle();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        updateTitle();
    }
}
