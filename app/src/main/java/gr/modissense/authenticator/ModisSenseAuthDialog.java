package gr.modissense.authenticator;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import gr.modissense.core.Constants;
import gr.modissense.util.DialogListener;
import gr.modissense.util.Ln;
import gr.modissense.util.ModiAuthError;
import gr.modissense.util.Util;

import java.util.Map;

public class ModisSenseAuthDialog extends Dialog {

    // Variables
    public static final int BLUE = 0xFF6D84B4;
    public static final int MARGIN = 4;
    public static final int PADDING = 2;
    public static final float width = 40;
    public static final float height = 60;
    public static final float[] DIMENSIONS_DIFF_LANDSCAPE = {width, height};
    public static final float[] DIMENSIONS_DIFF_PORTRAIT = {width, height};
    public static final String DISPLAY_STRING = "touch";
    static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
            ViewGroup.LayoutParams.FILL_PARENT);
    public static boolean titleStatus = false;
    private final String mUrl;
    private final DialogListener mListener;
    // SocialAuth Components
    private final Provider mProviderName;
    private String newUrl;
    private int count;
    // Android Components
    private TextView mTitle;
    private ProgressDialog mSpinner;
    private CustomWebView mWebView;
    private LinearLayout mContent;
    private Drawable icon;
    private Handler handler;


    /**
     * Constructor for the dialog
     *
     * @param context      Parent component that opened this dialog
     * @param url          URL that will be used for authenticating
     * @param providerName Name of provider that is being authenticated
     * @param listener     Listener object to handle events
     */
    public ModisSenseAuthDialog(Context context, String url, Provider providerName,
                                DialogListener listener) {
        super(context);
        mProviderName = providerName;
        mUrl = url;
        mListener = listener;
    }

    public static void startDialogAuth(final Context context, final Provider provider,
                                       final Handler handler,
                                       final DialogListener listener, final String token) {
        CookieSyncManager.createInstance(context);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // Get Callback url
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            new ModisSenseAuthDialog(context, provider.getModissenseURL(token), provider, listener).show();
                        }
                    });
                } catch (Exception e) {

                    listener.onError(new ModiAuthError("URL Authentication error", e));
                }
            }
        };

        new Thread(runnable).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        Util.getDisplayDpi(getContext());

        mSpinner = new ProgressDialog(getContext());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");
        mSpinner.setCancelable(true);

        mContent = new LinearLayout(getContext());
        mContent.setOrientation(LinearLayout.VERTICAL);
        setUpTitle();
        setUpWebView();

        Display display = getWindow().getWindowManager().getDefaultDisplay();
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int orientation = getContext().getResources().getConfiguration().orientation;
        float[] dimensions = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? DIMENSIONS_DIFF_LANDSCAPE
                : DIMENSIONS_DIFF_PORTRAIT;

        addContentView(mContent, new LinearLayout.LayoutParams(display.getWidth()
                - ((int) (dimensions[0] * scale + 0.5f)), display.getHeight() - ((int) (dimensions[1] * scale + 0.5f)
        )));

        mSpinner.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                mWebView.stopLoading();
                mListener.onBack();
                ModisSenseAuthDialog.this.dismiss();
            }
        });

        this.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    mWebView.stopLoading();
                    dismiss();
                    mListener.onBack();
                    return true;
                }
                return false;
            }
        });

    }

    /**
     * Sets title and icon of provider
     */

    private void setUpTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mTitle = new TextView(getContext());
        int res = getContext().getResources().getIdentifier(mProviderName.toString(), "drawable",
                getContext().getPackageName());
        icon = getContext().getResources().getDrawable(res);
        StringBuilder sb = new StringBuilder();
        sb.append(mProviderName.toString().substring(0, 1).toUpperCase());
        sb.append(mProviderName.toString().substring(1, mProviderName.toString().length()));
        mTitle.setText(sb.toString());
        mTitle.setGravity(Gravity.CENTER_VERTICAL);
        mTitle.setTextColor(Color.WHITE);
        mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mTitle.setBackgroundColor(BLUE);
        mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
        mTitle.setCompoundDrawablePadding(MARGIN + PADDING);
        mTitle.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);

        if (!titleStatus)
            mContent.addView(mTitle);
    }

    /**
     * Set up WebView to load the provider URL
     */
    private void setUpWebView() {
        mWebView = new CustomWebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new SocialAuthWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(FILL);
        mContent.addView(mWebView);
    }


    public enum Provider {
        FACEBOOK("facebook", "fbconnect://success", "fbconnect://success?error_reason"), TWITTER(
                "twitter", "http://socialauth.in/socialauthdemo/socialAuthSuccessAction.do",
                "http://socialauth.in/socialauthdemo/socialAuthSuccessAction.do?denied"), FOURSQUARE(
                "foursquare", "http://socialauth.in/socialauthdemo/socialAuthSuccessAction.do",
                "http://socialauth.in/socialauthdemo/socialAuthSuccessAction.do/?oauth_problem");
        private String name;
        private String cancelUri;
        private String callbackUri;


        /**
         * Constructor with unique string representing the provider
         *
         * @param name
         */
        Provider(String name, String callbackUri, String cancelUri) {
            this.name = name;
            this.cancelUri = cancelUri;
            this.callbackUri = callbackUri;
        }

        /**
         * returns cancel URI
         */
        String getCancelUri() {
            return this.cancelUri;
        }

        /**
         * returns Callback URI
         */
        String getCallBackUri() {
            return this.callbackUri;
        }

        /**
         * Set callback URI
         */
        public void setCallBackUri(String callbackUri) {
            this.callbackUri = callbackUri;
        }

        /**
         * Returns the unique string representing the provider
         */
        @Override
        public String toString() {
            return name;
        }

        public String getModissenseURL(String tokenToUse) {
            if(tokenToUse==null||"".equals(tokenToUse)){
                tokenToUse = "null";
            }
            return Constants.Http.URL_BASE + "/user/register?network=" + name + "&token=" +tokenToUse+"&callback=modi://finalized?";
        }
    }

    /**
     * WebView Client
     */

    private class SocialAuthWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Log.d("SocialAuth-WebView", "Override url: " + url);
            Ln.d("MODI1:"+url);
            if (url.startsWith("modi://finalized")
                    ) {

                final Map<String, String> params = Util.parseUrl(url);
                Ln.d("MODI1:"+params);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {


                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mSpinner != null && mSpinner.isShowing())
                                        mSpinner.dismiss();

                                    Bundle bundle = new Bundle();
                                    bundle.putString("uid", params.get("uid"));
                                    mListener.onComplete(bundle);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            mListener.onError(new ModiAuthError("Unknown Error", e));
                        }
                    }
                };
                new Thread(runnable).start();

                ModisSenseAuthDialog.this.dismiss();
                return true;
            }

            return false;

        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.d("SocialAuth-WebView", "Inside OnReceived Error");
            Log.d("SocialAuth-WebView", String.valueOf(errorCode));
            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(new ModiAuthError(description, new Exception(failingUrl)));
            ModisSenseAuthDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("SocialAuth-WebView", "onPageStart:" + url);
            mSpinner.show();
            Ln.d("MODI:"+url);
            // For Linkedin, MySpace, Runkeeper - Calls onPageStart to
            // authorize.
            if (url.startsWith("modi://finalized")) {

                final Map<String, String> params = Util.parseUrl(url);
                Ln.d("MODI:"+params);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {


                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    if (mSpinner != null && mSpinner.isShowing())
                                        mSpinner.dismiss();

                                    Bundle bundle = new Bundle();
                                    bundle.putString("uid", params.get("uid"));
                                    mListener.onComplete(bundle);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            mListener.onError(new ModiAuthError("Could not connect using SocialAuth", e));
                        }
                    }
                };
                new Thread(runnable).start();

                ModisSenseAuthDialog.this.dismiss();
            }
        }

        @Override
        public void onPageFinished(WebView view, final String url) {

            super.onPageFinished(view, url);
            String title = mWebView.getTitle();
            if (title != null && title.length() > 0) {
                mTitle.setText(title);
            }
            mSpinner.dismiss();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // Ignore SSL certificate errors
        }


    }

    /**
     * Workaround for Null pointer exception in WebView.onWindowFocusChanged in
     * droid phones and emulator with android 2.2 os. It prevents first time
     * WebView crash.
     */

    public class CustomWebView extends WebView {

        public CustomWebView(Context context) {
            super(context);
        }

        public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public CustomWebView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            try {
                super.onWindowFocusChanged(hasWindowFocus);
            } catch (NullPointerException e) {
                // Catch null pointer exception
            }
        }
    }

}
