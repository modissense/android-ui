package gr.modissense.ui.blog;


import android.view.LayoutInflater;

import java.util.List;

import gr.modissense.R;
import gr.modissense.core.BlogItem;
import gr.modissense.ui.view.AlternatingColorListAdapter;

public class BlogAdapter extends AlternatingColorListAdapter<BlogItem> {


    /**
     * @param inflater
     * @param items
     * @param selectable
     */
    public BlogAdapter(LayoutInflater inflater, List<BlogItem> items,
                       boolean selectable) {
        super(R.layout.blog_list_item, inflater, items, selectable);
    }

    /**
     * @param inflater
     * @param items
     */
    public BlogAdapter(LayoutInflater inflater, List<BlogItem> items) {
        super(R.layout.blog_list_item, inflater, items);

    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.blog_start, R.id.blog_end,
                R.id.blog_title, R.id.blog_comment};
    }

    @Override
    protected void update(int position, BlogItem item) {
        super.update(position, item);
        setText(0, item.startToHours());
        setText(1, item.endToHours());
        setText(2, item.getName());
        setText(3, item.getComment() + (item.toDuration().equals("") ? " " : " ("+item.toDuration()+")"));
        //setNumber(R.id.tv_date, item.getCreatedAt());
    }
}
