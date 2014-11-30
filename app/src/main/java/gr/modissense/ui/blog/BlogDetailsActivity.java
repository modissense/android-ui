package gr.modissense.ui.blog;


import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.Window;

import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.Views;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.core.Blog;
import gr.modissense.core.Blogs;
import gr.modissense.ui.CarouselActivity;
import gr.modissense.ui.ModisSenseFragmentActivity;
import gr.modissense.ui.view.NonSwipeableViewPager;
import gr.modissense.ui.view.ThrowableLoader;
import gr.modissense.util.Ln;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class BlogDetailsActivity extends ModisSenseFragmentActivity implements LoaderManager.LoaderCallbacks<Blogs> {
    protected static final String FORCE_REFRESH = "forceRefresh";
    @Inject
    protected ModisSenseServiceProvider serviceProvider;
    @Inject
    protected Bus BUS;
    @InjectView(R.id.vp_blog_pages)
    NonSwipeableViewPager pager;
    private Blogs theBlog;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog_view);
        // VIew injection with Butterknife
        Views.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        Bundle bundle = new Bundle();
        bundle.putString("date", getIntent().getExtras().getString("date"));
        getSupportLoaderManager().initLoader(0, bundle, this);
        BUS.register(this);
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int i) {
                Bundle bundle = new Bundle();
                switch (i) {
                    case 0:
                        BlogFragment blogFragment = new BlogFragment();
                        blogFragment.setArguments(bundle);
                        return blogFragment;
                    case 1:
                        BlogMapFragment blogMapFragment = new BlogMapFragment();
                        blogMapFragment.setArguments(bundle);
                        return blogMapFragment;
                    default:
                        throw new IllegalStateException("Invalid fragment index");

                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
        pager.setCurrentItem(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BUS.unregister(this);
    }

    @Override
    public Loader<Blogs> onCreateLoader(int i, final Bundle bundle) {
        return new ThrowableLoader<Blogs>(this, theBlog) {

            @Override
            public Blogs loadData() throws Exception {
                try {
                    System.out.println("----->" + bundle.getString("date"));
                    if (bundle.getString("date") != null) {
                        setDate(bundle.getString("date"));
                        Blog blog = serviceProvider.getService(BlogDetailsActivity.this).getUserBlog(bundle.getString("date"));
                        Blogs blogs = new Blogs();
                        blogs.setBlog(blog);
                        blogs.setDate(bundle.getString("date"));
                        Ln.d("MAPLINK "+blog.getMapLink());
                        return blogs;
                    }

                } catch (OperationCanceledException e) {
                    BlogDetailsActivity.this.finish();
                } finally {
                    //ModisSenseApplication.getInstance().setBlogItemSearchParams(searchParams);
                }
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Blogs> blogsLoader, Blogs blogs) {
        Exception exception = getException(blogsLoader);
        if (exception != null) {

            showError(getErrorMessage(exception));
            //showList();
            return;
        }
        theBlog = blogs;
        if (theBlog != null) {
            BUS.post(theBlog);
        }
    }

    @Override
    public void onLoaderReset(Loader<Blogs> blogsLoader) {

    }

    protected void showError(final int message) {
        Toaster.showLong(this, message);
    }

    /**
     * Get exception from loader if it provides one by being a
     * {@link ThrowableLoader}
     *
     * @param loader
     * @return exception or null if none provided
     */
    protected Exception getException(final Loader<Blogs> loader) {
        if (loader instanceof ThrowableLoader)
            return ((ThrowableLoader<Blogs>) loader).clearException();
        else
            return null;
    }

    int getErrorMessage(Exception e) {
        return R.string.error_loading_pois;
    }

    public Blogs getTheBlog() {
        return theBlog;
    }

    public void forceRefresh() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
        bundle.putString("date", date);
        //getActionBarActivity().setSupportProgressBarIndeterminateVisibility(true);
        getSupportLoaderManager().restartLoader(0, bundle, this);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void navigateToMap(){
        pager.setCurrentItem(1);
        forceRefresh();
    }

    public void navigateToList(){
        pager.setCurrentItem(0);
        forceRefresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:  // This is the home button in the top left corner of the screen.
                // Dont call finish! Because activity could have been started by an outside activity and the home button would not operated as expected!
                Intent homeIntent = new Intent(this, CarouselActivity.class);
                homeIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(homeIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
