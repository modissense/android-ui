package gr.modissense.core;


import com.google.android.gms.internal.en;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BlogItem implements Serializable {
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int no;
    private String dateString;

    private Date start;

    private Date end;

    private Long poiId;

    private String name;

    private Double lat;

    private Double lon;

    private String comment;

    private boolean publicity;

    private String interest;

    private String hotness;

    private List<String> keywords;

    private String section;
    private Integer index;

    public BlogItem() {
    }

    public BlogItem(BlogSeq blog, int index) {
        this.index = index;
        this.no = Integer.parseInt(blog.getSeqid());
        try {
            this.start = df.parse(blog.getStart());
            this.end = df.parse(blog.getEnd());
        } catch (ParseException e) {
            //throw new IllegalStateException(e);
        }
        this.poiId = Long.parseLong(blog.getPoi_id());
        this.name = blog.getName();
        this.lat = Double.valueOf(blog.getLat());
        System.out.println("lon:" + blog.getLon());
        this.lon = Double.valueOf(blog.getLon());
        this.comment = blog.getComment();
        this.publicity = "t".equals(blog.getPublicity()) ? true : false;
        this.interest = blog.getInterest();
        this.hotness = blog.getHotness();
        this.keywords = blog.getKeywords();
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Long getPoiId() {
        return poiId;
    }

    public void setPoiId(Long poiId) {
        this.poiId = poiId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isPublicity() {
        return publicity;
    }

    public void setPublicity(boolean publicity) {
        this.publicity = publicity;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getHotness() {
        return hotness;
    }

    public void setHotness(String hotness) {
        this.hotness = hotness;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String toDuration(){
        if (start == null || "null".equals(start)||end == null || "null".equals(end) || start.equals(end)){
            return "";
        }
        long timeDiff = (end.getTime()/60000) - (start.getTime()/60000);
        return timeConvert((int)timeDiff);
    }

    public String startToHours() {
        if (start == null || "null".equals(start))
            return "";
        return df.format(start).split("\\s")[1];
    }

    public String endToHours() {
        if (end == null || "null".equals(end))
            return "";
        return df.format(end).split("\\s")[1];
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    @Override
    public String toString() {
        if (section != null) {
            return section;
        }
        if (start == null || "null".equals(start)) {
            return dateString;
        }
        return new SimpleDateFormat("yyyy/MM/dd").format(start);
    }

    public String toDate(String dateString) {
        this.dateString = dateString;
        if (start == null || "null".equals(start)) {
            return dateString;
        }
        return new SimpleDateFormat("yyyy/MM/dd").format(start);
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public VisitItem toVisitItem(){
        VisitItem v = new VisitItem();
        v.setArrived(startToHours());
        v.setOff(endToHours());
        v.setComments(comment);
        v.setPoiId(String.valueOf(poiId));
        v.setDate(dateString);
        v.setPublicity(publicity);
        v.setSeqNum(no);
        return v;
    }

    public static String timeConvert(int time){
        StringBuilder b = new StringBuilder();
        int j = time/(24*60);
        int h= (time%(24*60)) / 60;
        int m = (time%(24*60)) % 60;
        if(j>0){
            b.append(j).append(" days ");
        }
        if(h>0){
            b.append(h).append(" hours ");
        }
        if(m>0){
            b.append(m).append(" minutes ");
        }

        return b.toString();
    }
}
