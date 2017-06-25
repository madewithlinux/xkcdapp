package com.josh.wright.xkcdapp.Service;

import android.support.annotation.NonNull;
import android.util.SparseArray;

public final class ComicService {
    private static final String TAG = "ComicService";
    private static ComicService instance = null;

    private final SparseArray<ComicBean> comics;

    private ComicService() {
        comics = new SparseArray<>();
    }

    public static ComicService getInstance() {
        if (instance == null) {
            instance = new ComicService();
        }
        return instance;
    }

    @NonNull
    public ComicBean getComic(int position) {return getComic(position, ComicUpdateCallback.noopCallback);}

    @NonNull
    public ComicBean getComic(int position, ComicUpdateCallback callback) {
        ComicBean comicBean = comics.get(position);
        if (comicBean == null) {
            comicBean = new ComicBean();
            comicBean.setNumber(position);
            comics.put(position, comicBean);
        }
        ComicDownloadHandler.downloadComic(comicBean, callback);
        return comicBean;
    }

    public void getMostRecentComic(final MostRecentComicCallback callback) {
        ComicDownloadHandler.findMostRecentComic(callback);
    }
}
