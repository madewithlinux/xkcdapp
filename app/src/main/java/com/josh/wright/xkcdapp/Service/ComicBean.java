package com.josh.wright.xkcdapp.Service;

import android.graphics.Bitmap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONObject;

import java.util.Date;
import java.util.GregorianCalendar;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ComicBean {
    @JsonIgnore
    public static final int MOST_RECENT_COMIC = -1;
    @JsonProperty("num")
    private int number;
    private String title;
    @JsonProperty("safeTitle")
    private String safeTitle;
    @JsonProperty("alt")
    private String altText;
    @JsonProperty("img")
    private String imageUrl;
    @JsonIgnore
    private Bitmap imageBitmap;
//    @JsonProperty("year")
    private String year;
//    @JsonProperty("month")
    private String month;
//    @JsonProperty("day")
    private String day;
    private String transcript;

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSafeTitle() {
        return safeTitle;
    }

    public void setSafeTitle(String safeTitle) {
        this.safeTitle = safeTitle;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDateAsString() {
        return year + "/" + month + "/" + day;
    }

    public void setFrom(ComicBean other) {
        setNumber(other.getNumber());
        setTitle(other.getTitle());
        setSafeTitle(other.getSafeTitle());
        setAltText(other.getAltText());
        setImageUrl(other.getImageUrl());
        setImageBitmap(other.getImageBitmap());
        setYear(other.getYear());
        setMonth(other.getMonth());
        setDay(other.getDay());
    }

}
