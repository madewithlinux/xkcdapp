package com.josh.wright.xkcdapp.UI;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.josh.wright.xkcdapp.Service.ComicService;

public class ComicPagerAdapter extends FragmentPagerAdapter {
    private int count;

    public ComicPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return ComicFragment.newInstance(position);
    }

    public void setCount(int count) {
        this.count = count;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return count;
    }
}
