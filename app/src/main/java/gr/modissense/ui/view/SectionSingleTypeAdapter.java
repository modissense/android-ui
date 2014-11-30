package gr.modissense.ui.view;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.SectionIndexer;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import gr.modissense.core.BlogItem;

/**
 * @param <V>
 */
public abstract class SectionSingleTypeAdapter extends SingleTypeAdapter<BlogItem>
        implements SectionIndexer {

    private final SectionFinder sections = new SectionFinder();

    /**
     * @param activity
     * @param layoutResourceId
     */
    public SectionSingleTypeAdapter(Activity activity, int layoutResourceId) {
        super(activity, layoutResourceId);
    }

    /**
     * @param context
     * @param layoutResourceId
     */
    public SectionSingleTypeAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);
    }

    /**
     * @param inflater
     * @param layoutResourceId
     */
    public SectionSingleTypeAdapter(LayoutInflater inflater, int layoutResourceId) {
        super(inflater, layoutResourceId);
    }

    @Override
    public void setItems(Object[] items) {
        super.setItems(items);
        sections.clear().index(items);
    }

    public int getPositionForSection(int section) {
        return sections.getPositionForSection(section);
    }

    public int getSectionForPosition(int position) {
        return sections.getSectionForPosition(position);
    }

    public Object[] getSections() {
        return sections.getSections();
    }
}
