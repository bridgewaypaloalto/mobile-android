package com.bcsv.mobile;

import java.util.ArrayList;
import java.util.List;

public class BibleReview {

    private String myDate;
    private String title;
    private String chapter;
    public List<String> review;
    public List<String> application;
    public List<String> in_depth;

    public BibleReview(){
        this.title = "";
        this.chapter = "";
        this.myDate = "";
        this.review = new ArrayList<>();
        this.application = new ArrayList<>();
        this.in_depth = new ArrayList<>();
    }

    public void clearContent(){
        title = "";
        chapter = "";
        myDate = "";
        review.clear();
        application.clear();
        in_depth.clear();
    }

    public void setDate(String date){
        this.myDate = date;
    }

    public void setMyTitle(String title){
        this.title = title;
    }

    public void setChapter(String chapter){
        this.chapter = chapter;
    }

    public void addReview(String review){
        this.review.add(review);
    }

    public void addApplication(String application){
        this.application.add(application);
    }

    public void addIndepth(String in_depth){
        this.in_depth.add(in_depth);
    }

    public String getMyDate(){
        return myDate;
    }

    public String getMyTitle(){
        return title;
    }

    public String getChapter(){
        return chapter;
    }
}
