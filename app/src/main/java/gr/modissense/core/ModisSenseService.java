package gr.modissense.core;

import android.os.Build;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import gr.modissense.authenticator.ModisSenseAuthDialog;
import gr.modissense.core.gps.GPSLogItem;
import gr.modissense.util.Ln;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.*;

import static gr.modissense.core.Constants.Http.*;

/**
 * Bootstrap API service
 */
public class ModisSenseService {

    /**
     * GSON instance to use for all request  with date format set up for proper parsing.
     */
    public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * Read and connect timeout in milliseconds
     */
    private static final int TIMEOUT = 30 * 1000;
    /**
     * You can also configure GSON with different naming policies for your API. Maybe your api is Rails
     * api and all json values are lower case with an underscore, like this "first_name" instead of "firstName".
     * You can configure GSON as such below.
     * <p/>
     * public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").setFieldNamingPolicy
     * (LOWER_CASE_WITH_UNDERSCORES).create();
     */
    private final String apiKey;
    private final String username;
    private final String password;
    private UserAgentProvider userAgentProvider;

    /**
     * Create bootstrap service
     *
     * @param username
     * @param password
     */
    public ModisSenseService(final String username, final String password) {
        this.username = username;
        this.password = password;
        this.apiKey = null;
    }

    /**
     * Create bootstrap service
     *
     * @param userAgentProvider
     * @param apiKey
     */
    public ModisSenseService(final String apiKey, final UserAgentProvider userAgentProvider) {
        this.userAgentProvider = userAgentProvider;
        this.username = null;
        this.password = null;
        this.apiKey = apiKey;
    }

    /**
     * Execute request
     *
     * @param request
     * @return request
     * @throws java.io.IOException
     */
    protected HttpRequest execute(HttpRequest request) throws IOException {
        if (!configure(request).ok())
            throw new IOException("Unexpected response code: " + request.code());
        return request;
    }

    protected HttpRequest execute(HttpRequest request, String data) throws IOException {
        if (!configure(request).send(data).ok())
            throw new IOException("Unexpected response code: " + request.code());
        return request;
    }

    private HttpRequest configure(final HttpRequest request) {
        request.connectTimeout(TIMEOUT).readTimeout(TIMEOUT);
        if (userAgentProvider != null && userAgentProvider.get() != null) {
            request.userAgent(userAgentProvider.get());
        }

        //Accept all certificates
        request.trustAllCerts();
        //Accept all hostnames
        request.trustAllHosts();
        if (isPostOrPut(request))
            request.contentType(Constants.Http.CONTENT_TYPE_JSON); // All PUT & POST requests to Parse.com api must
        // be in JSON - https://www.parse.com/docs/rest#general-requests

        return addCredentialsTo(request);
    }

    private boolean isPostOrPut(HttpRequest request) {
        return request.getConnection().getRequestMethod().equals(HttpRequest.METHOD_POST)
                || request.getConnection().getRequestMethod().equals(HttpRequest.METHOD_PUT);

    }

    private HttpRequest addCredentialsTo(HttpRequest request) {

        // Required params for
        //request.header(HEADER_PARSE_REST_API_KEY, PARSE_REST_API_KEY);
        //request.header(HEADER_PARSE_APP_ID, PARSE_APP_ID);

        /**
         * NOTE: This may be where you want to add a header for the api token that was saved when you
         * logged in. In the bootstrap sample this is where we are saving the session id as the token.
         * If you actually had received a token you'd take the "apiKey" (aka: token) and add it to the
         * header or form values before you make your requests.
         */

        /**
         * Add the user name and password to the request here if your service needs username or password for each
         * request. You can do this like this:
         * request.basic("myusername", "mypassword");
         */

        return request;
    }

    private <V> V fromJson(HttpRequest request, Class<V> target) throws IOException {
        Reader reader = request.bufferedReader();
        try {
            return GSON.fromJson(reader, target);
        } catch (JsonParseException e) {
            throw new JsonException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {
                // Ignored
            }
        }
    }

    public List<Poi> getPois(PoiSearchParams params) throws IOException {
        String ds = "";
        String de = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        if(params==null){
            return Collections.emptyList();
        }
        if (params.getDateStart() != null) {
            ds = dateFormat.format(params.getDateStart());
        }
        if (params.getDateEnd() != null) {
            de = dateFormat.format(params.getDateEnd());
        }
        return getPois(ds, de, params.getLocation1(), params.getLocation2(), params.friendsList(), params.getSort(), params.getNumberOfResults(), params.keywordsList());
    }

    ///poi/getpois?&stime=&etime=&x1=38.32570395063274&y1=23.98850285546871&x2=37.89348668792841&y2=23
    // .02719914453121&friends=&keywords=&orderby=&nresults=10&format=json&_=1379334520085
    public List<Poi> getPois(String stime, String etime, Location location1, Location location2,
                             List<String> friendsList, String orderBy, int numOfResults,
                             List<String> keywords) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            addParam("token", apiKey, params);
            addParam("stime", stime, params);
            addParam("etime", etime, params);
            addParam(location1, 1, params);
            addParam(location2, 2, params);
            addListParam("friends", friendsList, params);
            addListParam("keywords", keywords, params);
            addParam("orderby", orderBy, params);
            addParam("nresults", String.valueOf(numOfResults), params);
            addParam("format", "json", params);
            HttpRequest request = HttpRequest.get(URL_POI, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            PoiWrapper response = fromJson(request, PoiWrapper.class);
            if (response != null && response.poiList != null)
                return response.poiList;
            return Collections.emptyList();
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    //https://snf-97398.vm.okeanos.grnet.gr/poi/getnn?&lat=38.132602902266726&lon=23.352669115234324&k=20&format=jsonp&callback=callback&_=1379509400904
    public List<Poi> getPois(double latitude, double longtitude) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            addParam("token", apiKey, params);
            addParam("lat", String.valueOf(latitude), params);
            addParam("lon", String.valueOf(longtitude), params);
            addParam("k", String.valueOf(10), params);
            addParam("format", "json", params);
            HttpRequest request = HttpRequest.get(URL_NEIGHBORS, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            PoiWrapper response = fromJson(request, PoiWrapper.class);
            if (response != null && response.poiList != null)
                return response.poiList;
            return Collections.emptyList();
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }


    public List<Poi> getTrending(double latitude, double longtitude, Location location1, Location location2) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            addParam("token", apiKey, params);
            addParam("ypos", String.valueOf(latitude), params);
            addParam("xpos", String.valueOf(longtitude), params);
            addParam("k", String.valueOf(10), params);
            addParam(location1, 1, params);
            addParam(location2, 2, params);
            addParam("format", "json", params);
            HttpRequest request = HttpRequest.get(URL_TRENDING, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            PoiWrapper response = fromJson(request, PoiWrapper.class);
            if (response != null && response.poiList != null)
                return response.poiList;
            return Collections.emptyList();
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public Poi getPois(String poiId) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            addParam("token", apiKey, params);
            addParam("poi_id", poiId, params);
            addParam("format", "json", params);
            HttpRequest request = HttpRequest.get(URL_POI_GET, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            Poi response = fromJson(request, Poi.class);
            Ln.d("Response is "+response);
            if (response != null) {
                Ln.d(response.getPersonalized());
                return response;

            }
            return null;
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    //findduplicates
    public List<Poi> getPoisDuplicates(double latitude, double longtitude) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            addParam("token", apiKey, params);
            addParam("lat", String.valueOf(latitude), params);
            addParam("lon", String.valueOf(longtitude), params);
            addParam("format", "json", params);
            HttpRequest request = HttpRequest.get(URL_DUPLICATES, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            PoiWrapper response = fromJson(request, PoiWrapper.class);
            if (response != null && response.poiList != null)
                return response.poiList;
            return Collections.emptyList();
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public List<ModiAccount> getConnectedSocialAccounts() throws IOException {
        List<ModiAccount> result = Arrays.asList(new ModiAccount(ModisSenseAuthDialog.Provider.FACEBOOK), new ModiAccount(ModisSenseAuthDialog.Provider.TWITTER),
                new ModiAccount(ModisSenseAuthDialog.Provider.FOURSQUARE));
        List<UserNetwork> userNetworks = getUserNetworks(apiKey);
        for (UserNetwork network : userNetworks) {
            for (ModiAccount account : result) {
                if (account.getProvider().toString().equals(network.name)) {
                    account.setConnected(true);
                }
            }
        }
        return result;

    }

    public List<UserNetwork> getUserNetworks(String userId) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("token", userId);
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_USER_NETWORKS, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            UserNetworkWrapper response = fromJson(request, UserNetworkWrapper.class);
            if (response != null && response.networks != null)
                return response.networks;
            return Collections.emptyList();
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public List<Blogs> getUserBlogsExpanded() throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("token", apiKey);
            //params.put("token", "3n09jk1r2Nov5fbKJoav7qeay9Uf6uEd");
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_USER_BLOGS, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            System.out.println(request);
            BlogsWrapper response = fromJson(request, BlogsWrapper.class);
            if (response != null && response.blogs != null) {
                List<Blogs> blogs = response.blogs;
                for (Blogs blogs1 : blogs) {
                    blogs1.setBlog(getUserBlog(blogs1.getDate()));
                }
                return blogs;
            }
            return Collections.emptyList();
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public List<Blogs> getUserBlogs() throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("token", apiKey);
            //params.put("token", "3n09jk1r2Nov5fbKJoav7qeay9Uf6uEd");
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_USER_BLOGS, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            System.out.println(request);
            BlogsWrapper response = fromJson(request, BlogsWrapper.class);
            if (response != null && response.blogs != null) {
                return response.blogs;
            }
            return Collections.emptyList();
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public Blog getUserBlog(String date) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("token", apiKey);
            //https://snf-97398.vm.okeanos.grnet.gr/user/blog/getmicroblog/?token=TxT7og9YtY48pjfu1G14hSMmon8zm9W7&date=2013-09-24&format=jsonp&jsonpcallback=callback&_=1380612794869
            //params.put("token", "3n09jk1r2Nov5fbKJoav7qeay9Uf6uEd");
            params.put("date", date);
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_USER_BLOG, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            Blog response = fromJson(request, Blog.class);
            System.out.println(response);
            if (response != null)
                return response;
            return null;
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    //userinfo?token=ypv05E0mUo5rnWj9RuAz9bDfKzZyH7w8&format=json
    public ModiUserInfo getUserInfo() throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("token", apiKey);
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_USER_INFO, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            ModiUserInfo.ModiUserWrapper response = fromJson(request, ModiUserInfo.ModiUserWrapper.class);
            if (response != null && response.user != null)
                return response.user;
            return null;
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public ModiResult deleteUser(String network) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("token", apiKey);
            if(network != null){
                params.put("network", network);
            }
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_USER_DELETE, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            ModiResult response = fromJson(request, ModiResult.class);
            return response;

        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public Poi addPoi(Poi poi) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            Ln.d("-------------------__>APIKEY IS:" + apiKey);
            addParam("token", apiKey, params);
            params.put("name", poi.getName());
            params.put("publicity", String.valueOf(poi.isPublicity()));
            params.put("description", poi.getDescription());
            params.put("x", String.valueOf(poi.getX()));
            params.put("y", String.valueOf(poi.getY()));
            params.put("keywords", poi.keywordsToString());
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_POI_ADD, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            return fromJson(request, Poi.class);
        } catch (HttpRequest.HttpRequestException e) {
            Ln.e(e);
            throw e.getCause();
        }

    }

    public ModiResult addVisit(VisitItem item) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            Ln.d("-------------------__>APIKEY IS:" + apiKey);
            addParam("token", apiKey, params);
            params.put("poi_id", item.getPoiId());
            params.put("date", item.getDate());
            if(item.isPublicity()){
                params.put("public", "");
            }
            else{
                params.put("public", String.valueOf(item.isPublicity()));
            }
            addParam("comments", item.getComments(), params);
            addParam("arrived", item.getArrived(), params);
            addParam("off", item.getOff(), params);
            addParam("seq_num", String.valueOf(item.getSeqNum()), params);
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_POI_ADD_VISIT, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            return fromJson(request, ModiResult.class);
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public ModiResult reorderVisitItem(String date, int seqOriginal, int seqReorder, String comment) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            Ln.d("-------------------__>APIKEY IS:" + apiKey);
            addParam("token", apiKey, params);
            params.put("date", date);
            params.put("seqid", String.valueOf(seqOriginal));
            params.put("newseq", String.valueOf(seqReorder));
            params.put("comment", comment);
            params.put("delete", "false");
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_USER_BLOG_UPDATE, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            return fromJson(request, ModiResult.class);
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public ModiResult updateVisitItem(VisitItem item) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            Ln.d("-------------------__>APIKEY IS:" + apiKey);
            addParam("token", apiKey, params);
            params.put("date", item.getDate());
            params.put("arrived", item.getArrived());
            params.put("off", item.getOff());
            params.put("seqid", String.valueOf(item.getSeqNum()));
            //params.put("newseq", String.valueOf(seqReorder));
            params.put("comment", item.getComments());
            params.put("delete", "false");
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_USER_BLOG_UPDATE, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            return fromJson(request, ModiResult.class);
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public ModiResult deletePoi(Poi poi) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            addParam("token", apiKey, params);
            params.put("poi_id", poi.getId());
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_POI_REMOVE, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            return fromJson(request, ModiResult.class);

        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }

    }

    public ModiResult logGPSTrace(GPSLogItem poi) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            addParam("token", apiKey, params);
            params.put("lat", String.valueOf(poi.getLat()));
            params.put("lon", String.valueOf(poi.getLng()));
            params.put("timestamp", String.valueOf(poi.getTimestamp()));
            params.put("format", "json");
            JSONObject obj = new JSONObject();
            JSONArray array = new JSONArray();
            JSONObject trace = new JSONObject();
            trace.put("lat", poi.getLat());
            trace.put("lon", poi.getLng());
            trace.put("timestamp", df.format(new Date(poi.getTimestamp())));
            trace.put("token", apiKey);
            array.put(trace);
            obj.put("traces", array);

            HttpRequest request = HttpRequest.post(URL_POI_LOGGPS);
            Ln.d(request);
            Ln.d("GPS TRACES: "+obj.toString());
            request = execute(request.acceptCharset("UTF-8"), obj.toString());
            return fromJson(request, ModiResult.class);
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        } catch (JSONException e) {
            if (Build.VERSION.SDK_INT >= 9) {
                throw new IOException(e);
            } else {
                throw new IOException(e.getMessage());
            }
        }

    }

    private void addParam(String name, String value, Map<String, String> params) {
        params.put(name, value != null ? value : "");
    }

    private void addParam(Location location, int index, Map<String, String> params) {
        params.put("x" + index, location != null ? String.valueOf(location.getLat()) : "");
        params.put("y" + index, location != null ? String.valueOf(location.getLon()) : "");

    }

    private void addParam(Location location, Map<String, String> params) {
        params.put("x", location != null ? String.valueOf(location.getLat()) : "");
        params.put("y", location != null ? String.valueOf(location.getLon()) : "");

    }

    private void addListParam(String name, List<String> value, Map<String, String> params) {
        if (value == null || value.size() == 0) {
            params.put(name, "");
            return;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : value) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(s);
        }
        params.put(name, sb.toString());

    }

    public ModiResult updatePoi(Poi poi) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            Ln.d("-------------------__>APIKEY IS:" + apiKey);
            addParam("token", apiKey, params);
            params.put("name", poi.getName());
            params.put("publicity", String.valueOf(poi.isPublicity()));
            params.put("description", poi.getDescription());
            params.put("keywords", poi.keywordsToString());
            params.put("poi_id", poi.getId());
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_POI_UPDATE, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            return fromJson(request, ModiResult.class);
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public ModiResult postOnFacebook(String date) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            Ln.d("-------------------__>APIKEY IS:" + apiKey);
            addParam("token", apiKey, params);
            params.put("date", date);
            params.put("network", "facebook");
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_USER_BLOG_SHARE, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            return fromJson(request, ModiResult.class);
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public ModiResult postOnTwitter(String date) throws IOException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            Ln.d("-------------------__>APIKEY IS:" + apiKey);
            addParam("token", apiKey, params);
            params.put("date", date);
            params.put("network", "twitter");
            params.put("format", "json");
            HttpRequest request = HttpRequest.get(URL_USER_BLOG_SHARE, params, true);
            Ln.d(request);
            request = execute(request.acceptCharset("UTF-8"));
            return fromJson(request, ModiResult.class);
        } catch (HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }

    public static class PoiWrapper {
        @Expose
        private List<Poi> poiList;
    }

    public static class SinglePoiWrapper {
        @Expose
        private Poi poi;

        @Override
        public String toString() {
            return "SinglePoiWrapper{" +
                    "poi=" + poi +
                    '}';
        }
    }

    public static class UserNetworkWrapper {
        @Expose
        private List<UserNetwork> networks;
    }

    public static class UserNetwork {
        @Expose
        @SerializedName("Name")
        String name;
    }

    public static class BlogsWrapper {
        @Expose
        private List<Blogs> blogs;
    }


    private static class JsonException extends IOException {

        private static final long serialVersionUID = 3774706606129390273L;

        /**
         * Create exception from {@link com.google.gson.JsonParseException}
         *
         * @param cause
         */
        public JsonException(JsonParseException cause) {
            super(cause.getMessage());
            initCause(cause);
        }
    }


}
