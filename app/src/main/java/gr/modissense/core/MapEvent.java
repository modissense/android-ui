package gr.modissense.core;

import java.io.Serializable;

public class MapEvent implements Serializable {
    private PoiSearchParams searchParams;


    public PoiSearchParams getSearchParams() {
        return searchParams;
    }

    public void setSearchParams(PoiSearchParams searchParams) {
        this.searchParams = searchParams;
    }


}
