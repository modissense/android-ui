package gr.modissense.core;


import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import com.google.maps.android.clustering.ClusterItem;

public class Poi implements Serializable, ClusterItem {
    @Expose
    @SerializedName("poi_id")
    private String id;
    @Expose
    private String name;
    @Expose
    private Double x;
    @Expose
    private Double y;
    @Expose
    private Double interest;
    @Expose
    private Integer hotness;
    @Expose
    private boolean publicity;
    @Expose
    private List<String> keywords;
    @Expose
    private String description;
    @Expose
    private String image;
    @Expose
    @SerializedName("number_of_comments")
    private int numberOfComments;
    @Expose
    private Personalized personalized;
    @Expose
    @SerializedName("ismine")
    private boolean mine;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getInterest() {
        return interest;
    }

    public void setInterest(Double interest) {
        this.interest = interest;
    }

    public Integer getHotness() {
        return hotness;
    }

    public void setHotness(Integer hotness) {
        this.hotness = hotness;
    }

    public boolean isPublicity() {
        return publicity;
    }

    public void setPublicity(boolean publicity) {
        this.publicity = publicity;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public Personalized getPersonalized() {
        return personalized;
    }

    public void setPersonalized(Personalized personalized) {
        this.personalized = personalized;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    @Override
    public String toString() {
        String res = name;
        if (description != null && !"null".equals(description)) {
            if (description.length() > 20) {
                res = res + " " + description.substring(0, 19);
            } else {
                res = name + " " + description;
            }
        }
        return res;
    }
////        return "Poi{" +
////                "id='" + id + '\'' +
////                ", name='" + name + '\'' +
////                ", x=" + x +
////                ", y=" + y +
////                ", interest=" + interest +
////                ", hotness=" + hotness +
////                ", publicity=" + publicity +
////                ", keywords=" + keywords +
////                ", description='" + description + '\'' +
////                '}';
//    }


//    @Override
//    public String toString() {
//        return "Poi{" +
//                "id='" + id + '\'' +
//                ", name='" + name + '\'' +
//                ", x=" + x +
//                ", y=" + y +
//                ", interest=" + interest +
//                ", hotness=" + hotness +
//                ", publicity=" + publicity +
//                ", keywords=" + keywords +
//                ", description='" + description + '\'' +
//                ", image='" + image + '\'' +
//                ", numberOfComments=" + numberOfComments +
//                ", personalized=" + personalized +
//                '}';
//    }

    public String keywordsToString() {
        StringBuffer sb = new StringBuffer();
        if(keywords !=null){
            for (int i = 0; i < keywords.size(); i++) {
                sb.append(keywords.get(i)).append(",");
            }
        }
        return sb.toString();
    }

    public void keywordsFromString(String s) {
        keywords = new ArrayList<String>();
        if (s == null || "".equals(s.trim())) {

        } else {
            StringTokenizer str = new StringTokenizer(s, ",");
            while (str.hasMoreTokens()) {
                keywords.add(str.nextToken());
            }
        }
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(getX(),getY());
    }

    public static class Personalized implements Serializable{
        @Expose
        private Double interest;
        @Expose
        private Integer hotness;
        @Expose
        private Comment comment;

        public Double getInterest() {
            return interest;
        }

        public void setInterest(Double interest) {
            this.interest = interest;
        }

        public Integer getHotness() {
            return hotness;
        }

        public void setHotness(Integer hotness) {
            this.hotness = hotness;
        }

        public Comment getComment() {
            return comment;
        }

        public void setComment(Comment comment) {
            this.comment = comment;
        }

        @Override
        public String toString() {
            return "Personalized{" +
                    "interest=" + interest +
                    ", hotness=" + hotness +
                    ", comment=" + comment +
                    '}';
        }
    }

    public static class Comment implements Serializable{
        @Expose
        String text;
        @Expose
        String user;
        @Expose
        @SerializedName("user_picture")
        String userPicture;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getUserPicture() {
            return userPicture;
        }

        public void setUserPicture(String userPicture) {
            this.userPicture = userPicture;
        }

        @Override
        public String toString() {
            return "Comment{" +
                    "text='" + text + '\'' +
                    ", user='" + user + '\'' +
                    ", userPicture='" + userPicture + '\'' +
                    '}';
        }
    }
}
