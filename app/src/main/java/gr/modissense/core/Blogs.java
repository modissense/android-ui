package gr.modissense.core;


import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class Blogs implements Serializable, Comparable<Blogs> {
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    @Expose
    private String date;
    private Blog blog;


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public Blog getBlog() {
        return blog;
    }

    @Override
    public String toString() {
        return "Blogs{" +
                "date='" + date + '\'' +
                ", blog=" + blog +
                '}';
    }

    @Override
    public int compareTo(Blogs blogs) {
        try{
            return df.parse(blogs.date).compareTo(df.parse(date));
        }
        catch(Exception e){
            return 0;
        }
    }
}
