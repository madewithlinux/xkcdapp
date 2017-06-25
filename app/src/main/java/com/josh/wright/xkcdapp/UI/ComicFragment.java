package com.josh.wright.xkcdapp.UI;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.josh.wright.xkcdapp.R;
import com.josh.wright.xkcdapp.Service.ComicBean;
import com.josh.wright.xkcdapp.Service.ComicService;
import com.josh.wright.xkcdapp.Service.ComicUpdateCallback;


public class ComicFragment extends Fragment implements ComicUpdateCallback, View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "ComicFragment";
    private static final String ARG_POSITION = "position";
    private int position;
    private ComicBean comicBean;
    private SubsamplingScaleImageView imageView;
    private ProgressBar progressBar;
    private MainActivity mainActivity;
    private Button retryButton;
    private RelativeLayout errorMessage;


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
        Log.d(TAG, "fragment " + String.valueOf(position));
        View view = inflater.inflate(R.layout.fragment_comic, container, false);

        this.imageView = (SubsamplingScaleImageView) view.findViewById(R.id.imageViewComic);
        this.progressBar = (ProgressBar) view.findViewById(R.id.progressBarComic);
        this.retryButton = (Button) view.findViewById(R.id.buttonRetry);
        this.errorMessage = (RelativeLayout) view.findViewById(R.id.relativeLayoutErrorMessage);
        this.position = getArguments().getInt(ARG_POSITION);
        this.mainActivity = (MainActivity) getActivity();

        if (position == 404) {
            // comic 404 does not exist
            errorMessage.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            TextView textViewErrorMessage = (TextView) errorMessage.findViewById(R.id.textViewErrorMessage);
            textViewErrorMessage.setText(R.string.comic_404_text);
            return view;
        }
        this.comicBean = ComicService.getInstance().getComic(position, this);

        errorMessage.setVisibility(View.INVISIBLE);
        retryButton.setOnClickListener(this);
        imageView.setOnClickListener(this);
        imageView.setOnLongClickListener(this);

        progressBar.setVisibility(View.VISIBLE);
        if (comicBean.getImageBitmap() != null) {
            imageView.setImage(ImageSource.bitmap(comicBean.getImageBitmap()));
            progressBar.setVisibility(View.INVISIBLE);
        }
        imageView.setMinimumDpi(40);
        updateTitle();
        return view;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "comic " + comicBean.getNumber());
        switch (v.getId()) {
            case R.id.buttonRetry:
                ComicService.getInstance().getComic(position, this);
                errorMessage.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                break;
            case R.id.imageViewComic:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        mainActivity.showAltText();
        return true;
    }

    private void updateTitle() {
        /*this updates the title for after the metadata is downloaded*/
        if (comicBean != null && mainActivity.getComicNumber() == comicBean.getNumber()) {
            Log.d(TAG, "setting title to " + comicBean.getTitle());
            mainActivity.setTitle(comicBean.getTitle());
        }
    }

    @Override
    public void onUpdate(ComicUpdateCallback.Result result) {
        if (result == Result.SUCCESS) {
            updateTitle();
            Log.d(TAG, "updating comicBean " + String.valueOf(comicBean.getNumber()));
            if (comicBean.getImageBitmap() != null) {
                imageView.setImage(ImageSource.bitmap(comicBean.getImageBitmap()));
                Log.d(TAG, comicBean.getImageBitmap().toString());
                progressBar.setVisibility(View.INVISIBLE);
            } else {
                Log.d(TAG, "bitmap is null");
                progressBar.setVisibility(View.VISIBLE);
            }
        } else {
            errorMessage.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            Log.e(TAG, "error updating comicBean " + comicBean.getNumber());
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
