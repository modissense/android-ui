package gr.modissense.ui.view;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.Toaster;
import com.github.kevinsawicki.wishlist.ViewUtils;

import java.util.Collections;
import java.util.List;

import gr.modissense.R;
import gr.modissense.authenticator.LogoutService;

public abstract class ItemListSectionFragment<E> extends Fragment
        implements LoaderManager.LoaderCallbacks<List<E>> {

    private static final String FORCE_REFRESH = "forceRefresh";

    /**
     * @param args bundle passed to the loader by the LoaderManager
     * @return true if the bundle indicates a requested forced refresh of the
     * items
     */
    protected static boolean isForceRefresh(Bundle args) {
        return args != null && args.getBoolean(FORCE_REFRESH, false);
    }

    /**
     * List items provided to {@link #onLoadFinished(android.support.v4.content.Loader, java.util.List)}
     */
    protected List<E> items = Collections.emptyList();

    /**
     * List view
     */
    protected ListView listView;

    /**
     * Empty view
     */
    protected TextView emptyView;

    /**
     * Progress bar
     */
    protected ProgressBar progressBar;

    /**
     * Is the list currently shown?
     */
    protected boolean listShown;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!items.isEmpty())
            setListShown(true, false);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.item_list, null);
    }

    /**
     * Detach from list view.
     */
    @Override
    public void onDestroyView() {
        listShown = false;
        emptyView = null;
        progressBar = null;
        listView = null;

        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = (ListView) view.findViewById(android.R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onListItemClick((ListView) parent, view, position, id);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return onListItemLongClick(adapterView, view, i, l);
            }
        });
        progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);

        emptyView = (TextView) view.findViewById(android.R.id.empty);

        configureList(getActivity(), getListView());
    }

    /**
     * Configure list after view has been created
     *
     * @param activity
     * @param listView
     */
    protected void configureList(Activity activity, ListView listView) {
        listView.setAdapter(createAdapter());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(R.menu.bootstrap, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isUsable())
            return false;
        switch (item.getItemId()) {
            case R.id.refresh:
                forceRefresh();
                return true;
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected abstract LogoutService getLogoutService();

    private void logout() {
        getLogoutService().logout(new Runnable() {
            @Override
            public void run() {
                // Calling a refresh will force the service to look for a logged in user
                // and when it finds none the user will be requested to log in again.
                forceRefresh();
            }
        });
    }

    /**
     * Force a refresh of the items displayed ignoring any cached items
     */
    protected void forceRefresh() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
        refresh(bundle);
    }

    /**
     * Refresh the fragment's list
     */
    public void refresh() {
        refresh(null);
    }

    private void refresh(final Bundle args) {
        if (!isUsable())
            return;

        getActionBarActivity().setSupportProgressBarIndeterminateVisibility(true);

        getLoaderManager().restartLoader(0, args, this);
    }

    private ActionBarActivity getActionBarActivity() {
        return ((ActionBarActivity) getActivity());
    }

    /**
     * Get error message to display for exception
     *
     * @param exception
     * @return string resource id
     */
    protected abstract int getErrorMessage(Exception exception);

    protected boolean onListItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    public void onLoadFinished(Loader<List<E>> loader, List<E> items) {

        getActionBarActivity().setSupportProgressBarIndeterminateVisibility(false);

        Exception exception = getException(loader);
        if (exception != null) {
            showError(getErrorMessage(exception));
            showList();
            return;
        }

        this.items = items;
        getListAdapter().getWrappedAdapter().setItems(items.toArray());
        showList();
    }

    /**
     * Create adapter to display items
     *
     * @return adapter
     */
    protected HeaderFooterListAdapter<SingleTypeAdapter<E>> createAdapter() {
        SingleTypeAdapter<E> wrapped = createAdapter(items);
        return new HeaderFooterListAdapter<SingleTypeAdapter<E>>(getListView(),
                wrapped);
    }

    /**
     * Create adapter to display items
     *
     * @param items
     * @return adapter
     */
    protected abstract SingleTypeAdapter<E> createAdapter(final List<E> items);

    /**
     * Set the list to be shown
     */
    protected void showList() {
        setListShown(true, isResumed());
    }

    @Override
    public void onLoaderReset(Loader<List<E>> loader) {
        // Intentionally left blank
    }

    /**
     * Show exception in a Toast
     *
     * @param message
     */
    protected void showError(final int message) {
        Toaster.showLong(getActivity(), message);
    }

    /**
     * Get exception from loader if it provides one by being a
     * {@link gr.modissense.ui.view.ThrowableLoader}
     *
     * @param loader
     * @return exception or null if none provided
     */
    protected Exception getException(final Loader<List<E>> loader) {
        if (loader instanceof ThrowableLoader)
            return ((ThrowableLoader<List<E>>) loader).clearException();
        else
            return null;
    }

    /**
     * Refresh the list with the progress bar showing
     */
    protected void refreshWithProgress() {
        items.clear();
        setListShown(false);
        refresh();
    }

    /**
     * Get {@link android.widget.ListView}
     *
     * @return listView
     */
    public ListView getListView() {
        return listView;
    }

    /**
     * Get list adapter
     *
     * @return list adapter
     */
    @SuppressWarnings("unchecked")
    protected HeaderFooterListAdapter<SingleTypeAdapter<E>> getListAdapter() {
        if (listView != null)
            return (HeaderFooterListAdapter<SingleTypeAdapter<E>>) listView
                    .getAdapter();
        else
            return null;
    }

    /**
     * Set list adapter to use on list view
     *
     * @param adapter
     * @return this fragment
     */
    protected ItemListSectionFragment<E> setListAdapter(final ListAdapter adapter) {
        if (listView != null)
            listView.setAdapter(adapter);
        return this;
    }

    private ItemListSectionFragment<E> fadeIn(final View view, final boolean animate) {
        if (view != null)
            if (animate)
                view.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                        android.R.anim.fade_in));
            else
                view.clearAnimation();
        return this;
    }

    private ItemListSectionFragment<E> show(final View view) {
        ViewUtils.setGone(view, false);
        return this;
    }

    private ItemListSectionFragment<E> hide(final View view) {
        ViewUtils.setGone(view, true);
        return this;
    }

    /**
     * Set list shown or progress bar show
     *
     * @param shown
     * @return this fragment
     */
    public ItemListSectionFragment<E> setListShown(final boolean shown) {
        return setListShown(shown, true);
    }

    /**
     * Set list shown or progress bar show
     *
     * @param shown
     * @param animate
     * @return this fragment
     */
    public ItemListSectionFragment<E> setListShown(final boolean shown,
                                            final boolean animate) {
        if (!isUsable())
            return this;

        if (shown == listShown) {
            if (shown)
                // List has already been shown so hide/show the empty view with
                // no fade effect
                if (items.isEmpty())
                    hide(listView).show(emptyView);
                else
                    hide(emptyView).show(listView);
            return this;
        }

        listShown = shown;

        if (shown)
            if (!items.isEmpty())
                hide(progressBar).hide(emptyView).fadeIn(listView, animate)
                        .show(listView);
            else
                hide(progressBar).hide(listView).fadeIn(emptyView, animate)
                        .show(emptyView);
        else
            hide(listView).hide(emptyView).fadeIn(progressBar, animate)
                    .show(progressBar);

        return this;
    }

    /**
     * Set empty text on list fragment
     *
     * @param message
     * @return this fragment
     */
    protected ItemListSectionFragment<E> setEmptyText(final String message) {
        if (emptyView != null)
            emptyView.setText(message);
        return this;
    }

    /**
     * Set empty text on list fragment
     *
     * @param resId
     * @return this fragment
     */
    protected ItemListSectionFragment<E> setEmptyText(final int resId) {
        if (emptyView != null)
            emptyView.setText(resId);
        return this;
    }

    /**
     * Callback when a list view item is clicked
     *
     * @param l
     * @param v
     * @param position
     * @param id
     */
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    /**
     * Is this fragment still part of an activity and usable from the UI-thread?
     *
     * @return true if usable on the UI-thread, false otherwise
     */
    protected boolean isUsable() {
        return getActivity() != null;
    }
}
