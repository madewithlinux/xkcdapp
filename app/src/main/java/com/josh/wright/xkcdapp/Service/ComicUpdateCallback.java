package com.josh.wright.xkcdapp.Service;

public interface ComicUpdateCallback {
    enum Result {
        SUCCESS, FAILURE
    }

    ComicUpdateCallback noopCallback = new ComicUpdateCallback() {
        @Override
        public void onUpdate(Result r) {

        }
    };

    void onUpdate(Result r);
}
