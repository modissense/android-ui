package gr.modissense.ui.blog;


import android.view.LayoutInflater;


import java.util.Arrays;
import java.util.List;

import gr.modissense.R;
import gr.modissense.core.BlogItem;
import gr.modissense.ui.view.SectionSingleTypeAdapter;
import gr.modissense.util.Ln;

public class BlogSectionAdapter extends SectionSingleTypeAdapter {
    /**
     * @param inflater
     * @param items
     */
    public BlogSectionAdapter(LayoutInflater inflater, List<BlogItem> items) {
        super(inflater, R.layout.blog_list_item);
        setItems(items.toArray(new BlogItem[items.size()]));
    }


    public void setItems(List<BlogItem> items){
        setItems(items.toArray(new BlogItem[items.size()]));
        Ln.d("*******SECTIONS:" + Arrays.toString(getSections()));
    }

    @Override
    public void setItems(Object[] items) {
        super.setItems(items);
        Ln.d(getCount()+" *******SECTIONS:" + Arrays.toString(getSections()));
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.blog_start, R.id.blog_end,
                R.id.blog_title, R.id.blog_comment, R.id.blog_keywords};
    }


    @Override
    protected void update(int position, BlogItem item) {
        System.out.println(position+" "+item);
        //super.update(position, item);
        if(item.getSection()!=null){
            setText(0, String.valueOf(item.getSection()));
        }
        else{
            setText(0, String.valueOf(item.startToHours()));
            setText(1, String.valueOf(item.endToHours()));
            setText(2, item.getName());
            setText(3, item.getComment());
            setText(4,item.getKeywords().toString());
        }
    }
}
