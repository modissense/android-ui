package gr.modissense.core;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kman on 10/9/13.
 */
public class Blog {
    @Expose
    private List<BlogSeq> blog;
    @Expose
    private String description;
    private String date;

    public List<BlogItem> toItems(String date) {
        if (blog != null) {
            List<BlogItem> items = new ArrayList<BlogItem>();
            int indx=1;
            for (BlogSeq seq : blog) {
                BlogItem item = new BlogItem(seq, indx);
                this.date = item.toDate(date);
                items.add(item);
                indx++;
            }
            return items;
        }
        return null;
    }

    public String getMapLink(){
        if(blog != null){
            StringBuilder baseUrl = new StringBuilder("http://maps.googleapis.com/maps/api/staticmap?size=400x400&sensor=false");
            for (BlogSeq seq : blog) {
                baseUrl.append("&markers=").append("color:blue|label:").append(seq.getName().substring(0,1).toUpperCase()).append("|").append(seq.getLat()).append(",").append(seq.getLon());
            }
            return baseUrl.toString();

        }
        return null;

    }



    public List<BlogSeq> getBlog() {
        return blog;
    }

    public void setBlog(List<BlogSeq> blog) {
        this.blog = blog;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Blog{" +
                "blog=" + blog +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
