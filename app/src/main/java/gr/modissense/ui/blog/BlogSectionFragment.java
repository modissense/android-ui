package gr.modissense.ui.blog;


import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


import com.squareup.otto.Bus;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.authenticator.LogoutService;
import gr.modissense.core.BlogItem;
import gr.modissense.core.Blogs;
import gr.modissense.ui.view.ItemListSectionFragment;
import gr.modissense.ui.view.ThrowableLoader;
import gr.modissense.ui.view.SectionSingleTypeAdapter;

public class BlogSectionFragment extends ItemListSectionFragment<BlogItem> {

    @Inject
    protected ModisSenseServiceProvider serviceProvider;
    @Inject
    protected LogoutService logoutService;
    @Inject
    protected Bus BUS;
    private String date = "2013-10-02";//new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    private Blogs blogSet = null;
    private List<Blogs> blogsList = null;
    private View blogDescriptionView;
    private View pagerView;
    private TextView titleTextView;
    private ImageButton nextBlogButton;
    private ImageButton prevBlogButton;
    private int blogIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModisSenseApplication.getInstance().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        BUS.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BUS.register(this);
//        if (ModisSenseApplication.getInstance().getBlogItemSearchParams() != null) {
//            if (!ModisSenseApplication.getInstance().getBlogItemSearchParams().equals(searchParams)) {
//                forceRefresh();
//            }
//        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_blogs);


    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);
        pagerView = activity.getLayoutInflater()
                .inflate(R.layout.blog_pager, null);
        getListAdapter().addHeader(pagerView);
        titleTextView = (TextView) pagerView.findViewById(R.id.blogDateTitle);
        prevBlogButton = (ImageButton) pagerView.findViewById(R.id.prevBlogDate);
        nextBlogButton = (ImageButton) pagerView.findViewById(R.id.netxBlogDate);
        prevBlogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prev();
            }
        });
        nextBlogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });
        blogDescriptionView = BlogSectionFragment.this.getActivity().getLayoutInflater()
                .inflate(R.layout.blog_list_item_labels, null);
        getListAdapter()
                .addHeader(blogDescriptionView);

    }

    private void prev() {
        if (blogIndex > 0 && blogIndex < blogsList.size() - 1) {
            blogIndex++;
            forceRefresh();
        }
    }

    private void next() {
        if (blogIndex > 0 && blogIndex < blogsList.size() - 1) {
            blogIndex--;
            forceRefresh();
        }
    }

    public void updateButtons() {

    }

    public void updateList(Blogs blog) {
        if (blog != null) {
            titleTextView.setText(blog.getDate());
            ((TextView) blogDescriptionView.findViewById(R.id.blog_header_date)).setText(blog.getDate());
            ((TextView) blogDescriptionView.findViewById(R.id.blog_header_desc)).setText(blog.getBlog().getDescription());
        } else {
            titleTextView.setText("");
            ((TextView) blogDescriptionView.findViewById(R.id.blog_header_date)).setText("");
            ((TextView) blogDescriptionView.findViewById(R.id.blog_header_desc)).setText("");
        }


    }

    @Override
    protected LogoutService getLogoutService() {
        return logoutService;
    }

    @Override
    public void onDestroyView() {
        setListAdapter(null);

        super.onDestroyView();
    }

    @Override
    protected void showList() {
        super.showList();
        updateList(blogSet);
    }

    @Override
    public Loader<List<BlogItem>> onCreateLoader(int id, Bundle args) {
        final List<BlogItem> initialItems = items;
        return new ThrowableLoader<List<BlogItem>>(getActivity(), items) {

            @Override
            public List<BlogItem> loadData() throws Exception {
                try {
                    if (getActivity() != null) {
                        if (blogsList == null)
                            blogsList = serviceProvider.getService(getActivity()).getUserBlogs();
                        if (blogsList != null && blogsList.size() > 0) {
                            Blogs blog = blogsList.get(blogIndex);
                            blog.setBlog(serviceProvider.getService(getActivity()).getUserBlog(blog.getDate()));
                            blogSet = blog;
                            return blog.getBlog().toItems(blog.getDate());
                        }
                    }
                    return Collections.emptyList();

                } catch (OperationCanceledException e) {
                    Activity activity = getActivity();
                    if (activity != null)
                        activity.finish();
                    return initialItems;
                }
            }
        };
    }

    @Override
    protected SectionSingleTypeAdapter createAdapter(List<BlogItem> items) {
        return new BlogSectionAdapter(getActivity().getLayoutInflater(), items);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        //startActivityForResult(new Intent(getActivity(), ModiBlogItemViewActivity.class).putExtra(ModiBlogItemViewActivity.VIEW_PARAMS, (BlogItem) l.getItemAtPosition(position)), 1001);
    }

    @Override
    protected boolean onListItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        /*BlogItem poi = (BlogItem) parent.getItemAtPosition(position);
        ModiBlogItemEditDialog addDialog = new ModiBlogItemEditDialog(poi);
        ModisSenseApplication.getInstance().inject(addDialog);
        addDialog.show(getSherlockActivity().getSupportFragmentManager(), "dlg_edit_poi");*/
        return true;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_loading_pois;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED)
            return;
        forceRefresh();
    }
}
