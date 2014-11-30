

package gr.modissense.core;

/**
 * Bootstrap constants
 */
public class Constants {

    public static class Auth {
        /**
         * Account type id
         */
        public static final String BOOTSTRAP_ACCOUNT_TYPE = "gr.modissense";
        /**
         * Account name
         */
        public static final String BOOTSTRAP_ACCOUNT_NAME = "modissense";
        /**
         * Provider id
         */
        public static final String BOOTSTRAP_PROVIDER_AUTHORITY = "gr.modissense.sync";
        /**
         * Auth token type
         */
        public static final String AUTHTOKEN_TYPE = BOOTSTRAP_ACCOUNT_TYPE;

        private Auth() {
        }
    }

    /**
     * All HTTP is done through a REST style API built for demonstration purposes on Parse.com
     * Thanks to the nice people at Parse for creating such a nice system for us to use for bootstrap!
     */
    public static class Http {
        /**
         * Base URL for all requests
         */
        public static final String URL_BASE = "https://83.212.104.253";//"https://83.212.123.55";//////"https://83.212.123.55";


        //https://snf-97398.vm.okeanos.grnet.gr/poi/getpois?&stime=&etime=&x1=38.32570395063274&y1=23.98850285546871&x2=37.89348668792841&y2=23.02719914453121&friends=&keywords=&orderby=&nresults=10&format=json&_=1379334520085
        /**
         * Authentication URL
         */
        public static final String URL_POI = URL_BASE + "/poi/getpois";
        public static final String URL_POI_GET = URL_BASE + "/poi/getpoi";
        public static final String URL_POI_ADD = URL_BASE + "/poi/addnewpois";
        public static final String URL_POI_ADD_VISIT = URL_BASE + "/poi/addnewvisit";
        public static final String URL_POI_UPDATE = URL_BASE + "/poi/updatepoi";
        public static final String URL_POI_REMOVE = URL_BASE + "/poi/deletepoi";
        public static final String URL_NEIGHBORS = URL_BASE + "/poi/getnn";
        public static final String URL_TRENDING = URL_BASE + "/poi/showtrendingevents";
        public static final String URL_DUPLICATES = URL_BASE + "/poi/findduplicates";
        public static final String URL_POI_LOGGPS = URL_BASE + "/poi/loggpstraces";
        public static final String URL_USER_NETWORKS = URL_BASE + "/user/getnetworks";
        public static final String URL_USER_INFO = URL_BASE + "/user/userinfo";
        public static final String URL_USER_LOGGEDIN = URL_BASE + "/user/loggedin";
        public static final String URL_USER_CHECKNAME = URL_BASE + "/user/checkname";
        public static final String URL_USER_LOGOUT = URL_BASE + "/user/logout";
        public static final String URL_USER_DELETE = URL_BASE + "/user/delete";
        //https://snf-97398.vm.okeanos.grnet.gr/user/blog/getblogs/?token=TxT7og9YtY48pjfu1G14hSMmon8zm9W7&format=jsonp&jsonpcallback=callback&_=1380612617337
        public static final String URL_USER_BLOGS = URL_BASE + "/user/blog/getblogs";
        public static final String URL_USER_BLOG_SHARE = URL_BASE + "/user/blog/shareblog";
        public static final String URL_USER_BLOG = URL_BASE + "/user/blog/getmicroblog";
        public static final String URL_USER_BLOG_UPDATE = URL_BASE + "/user/blog/updateblog";
        public static final String CONTENT_TYPE_JSON = "application/json";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String SESSION_TOKEN = "sessionToken";

        private Http() {
        }


    }

    public static class Intent {
        /**
         * Action prefix for all intents created
         */
        public static final String INTENT_PREFIX = "gr.modissense.";

        private Intent() {
        }

    }

    public static class Notification {
        public static final int TIMER_NOTIFICATION_ID = 1000; // Why 1000? Why not? :)

        private Notification() {
        }
    }

}


