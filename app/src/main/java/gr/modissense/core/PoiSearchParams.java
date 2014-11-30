package gr.modissense.core;


import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class PoiSearchParams implements Serializable {
    /*public static PoiSearchParams defaultSearchParams;
    public static PoiSearchParams initialSearchParams;

    static {
        defaultSearchParams = new PoiSearchParams();
        defaultSearchParams.location1 = new Location(new LatLng(38.32570395063274d, 23.98850285546871d));
        defaultSearchParams.location2 = new Location(new LatLng(37.89348668792841d, 23.02719914453121));

        initialSearchParams = new PoiSearchParams();
        initialSearchParams.location1 = new Location(new LatLng(23.32570395063274d, 24.98850285546871d));
        initialSearchParams.location2 = new Location(new LatLng(33.89348668792841d, 24.02719914453121));
    } */

    private Date dateStart;
    private Date dateEnd;
    private String searchString = "";
    private String keywords = "";
    private String friends = "";
    private Location location1;
    private Location location2;
    private String sort = "";
    private boolean nearest = false;
    private double nearestLat;
    private double nearestLon;
    private Set<ModiUserInfo.UserExpanded> friendsSet = new HashSet<ModiUserInfo.UserExpanded>();
    private int numberOfResults = 10;

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getFriends() {
        return friends;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }

    public Location getLocation1() {
        return location1;
    }

    public void setLocation1(Location location1) {
        this.location1 = location1;
    }

    public Location getLocation2() {
        return location2;
    }

    public void setLocation2(Location location2) {
        this.location2 = location2;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public List<String> friendsList() {
        List<String> res = new ArrayList<String>();
        for(ModiUserInfo.UserExpanded u : getFriendsSet()){
            res.add(u.getId());
        }
        return res;
    }

    public List<String> keywordsList() {
        List<String> res = new ArrayList<String>();
        if(keywords != null){
            StringTokenizer str = new StringTokenizer(keywords,",");
            while(str.hasMoreTokens()){
                res.add(str.nextToken());
            }
        }
        return res;
    }

    public boolean isNearest() {
        return nearest;
    }

    public void setNearest(boolean nearest) {
        this.nearest = nearest;
    }

    public double getNearestLat() {
        return nearestLat;
    }

    public void setNearestLat(double nearestLat) {
        this.nearestLat = nearestLat;
    }

    public double getNearestLon() {
        return nearestLon;
    }

    public void setNearestLon(double nearestLon) {
        this.nearestLon = nearestLon;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PoiSearchParams that = (PoiSearchParams) o;

        if (nearest != that.nearest) return false;
        if (Double.compare(that.nearestLat, nearestLat) != 0) return false;
        if (Double.compare(that.nearestLon, nearestLon) != 0) return false;
        if (dateEnd != null ? !dateEnd.equals(that.dateEnd) : that.dateEnd != null) return false;
        if (dateStart != null ? !dateStart.equals(that.dateStart) : that.dateStart != null) return false;
        if (friends != null ? !friends.equals(that.friends) : that.friends != null) return false;
        if (keywords != null ? !keywords.equals(that.keywords) : that.keywords != null) return false;
        if (location1 != null ? !location1.equals(that.location1) : that.location1 != null) return false;
        if (location2 != null ? !location2.equals(that.location2) : that.location2 != null) return false;
        if (searchString != null ? !searchString.equals(that.searchString) : that.searchString != null) return false;
        if (sort != null ? !sort.equals(that.sort) : that.sort != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = dateStart != null ? dateStart.hashCode() : 0;
        result = 31 * result + (dateEnd != null ? dateEnd.hashCode() : 0);
        result = 31 * result + (searchString != null ? searchString.hashCode() : 0);
        result = 31 * result + (keywords != null ? keywords.hashCode() : 0);
        result = 31 * result + (friends != null ? friends.hashCode() : 0);
        result = 31 * result + (location1 != null ? location1.hashCode() : 0);
        result = 31 * result + (location2 != null ? location2.hashCode() : 0);
        result = 31 * result + (sort != null ? sort.hashCode() : 0);
        result = 31 * result + (nearest ? 1 : 0);
        temp = Double.doubleToLongBits(nearestLat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(nearestLon);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "PoiSearchParams{" +
                "dateStart=" + dateStart +
                ", dateEnd=" + dateEnd +
                ", searchString='" + searchString + '\'' +
                ", keywords='" + keywords + '\'' +
                ", friends='" + friends + '\'' +
                ", location1=" + location1 +
                ", location2=" + location2 +
                ", sort='" + sort + '\'' +
                '}';
    }

    public void setFriendsSet(Set<ModiUserInfo.UserExpanded> friendsSet) {
        this.friendsSet = friendsSet;
    }

    public Set<ModiUserInfo.UserExpanded> getFriendsSet() {
        return friendsSet;
    }

    public int getNumberOfResults() {
        return numberOfResults;
    }

    public void setNumberOfResults(int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }
}

