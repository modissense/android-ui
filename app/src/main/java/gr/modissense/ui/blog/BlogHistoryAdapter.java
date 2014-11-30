package gr.modissense.ui.blog;


import android.view.LayoutInflater;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import gr.modissense.R;
import gr.modissense.core.Blogs;
import gr.modissense.ui.view.AlternatingColorListAdapter;

public class BlogHistoryAdapter extends AlternatingColorListAdapter<Blogs> {
    private SimpleDateFormat df = new SimpleDateFormat("MMMMM dd, yyyy");

    /**
     * @param inflater
     * @param items
     * @param selectable
     */
    public BlogHistoryAdapter(LayoutInflater inflater, List<Blogs> items,
                              boolean selectable) {
        super(R.layout.blog_history_item, inflater, items, selectable);
    }

    /**
     * @param inflater
     * @param items
     */
    public BlogHistoryAdapter(LayoutInflater inflater, List<Blogs> items) {
        super(R.layout.blog_history_item, inflater, items);

    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.history_title};
    }

    @Override
    protected void update(int position, Blogs item) {
        super.update(position, item);
        //System.out.println(item.getDate());
        //setText(0, df.format(item.getDate()));
        setText(0, item.getDate());
//        setText(0, item.startToHours());
//        setText(1, item.endToHours());
//        setText(2, item.getName());
//        setText(3, item.getComment());
        //setNumber(R.id.tv_date, item.getCreatedAt());
    }

    public static void main(String[] args){
        SimpleDateFormat df = new SimpleDateFormat("MMMMM dd, yyyy");
        System.out.println(df.format(new Date()));

    }

}
