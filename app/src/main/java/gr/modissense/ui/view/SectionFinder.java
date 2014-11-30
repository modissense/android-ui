package gr.modissense.ui.view;

import android.text.TextUtils;
import android.util.SparseIntArray;
import android.widget.SectionIndexer;

import java.util.LinkedHashSet;
import java.util.Set;

import gr.modissense.core.BlogItem;

/**
 * Section finder helper
 */
public class SectionFinder implements SectionIndexer {

    private final Set<BlogItem> sections = new LinkedHashSet<BlogItem>();

    private final SparseIntArray sectionPositions = new SparseIntArray();

    private final SparseIntArray itemSections = new SparseIntArray();

    private int index = 0;

    /**
     * Clear all sections
     *
     * @return this finder
     */
    public SectionFinder clear() {
        sections.clear();
        sectionPositions.clear();
        itemSections.clear();
        index = 0;
        return this;
    }

    /**
     * Get section for item
     * <p>
     * The default behavior is to use the first character from the item's
     * {@link #toString()} method
     *
     * @param item
     * @return section
     */
    protected BlogItem getSection(final BlogItem item) {
        BlogItem section = new BlogItem();
        section.setSection(item.toString());
        return section;
    }

    private void addSection(BlogItem section) {
        int count = sections.size();
        if (sections.add(section))
            sectionPositions.put(count, index);
    }

    private void addItem(Object item) {
        itemSections.put(index, sections.size());
        index++;
    }

    /**
     * Index items by section returned from {@link #getSection(Object)}
     *
     * @param items
     * @return this finder
     */
    public SectionFinder index(BlogItem... items) {
        for (BlogItem item : items) {
            addSection(getSection(item));
            addItem(item);
        }
        return this;
    }


    public SectionFinder index(Object... items) {
        for (Object item : items) {
            addSection(getSection((BlogItem)item));
            addItem(item);
        }
        return this;
    }
    /**
     * Add items to given section
     *
     * @param section
     * @param items
     * @return this finder
     */
    public SectionFinder add(final BlogItem section, final Object... items) {
        addSection(section);
        for (Object item : items)
            addItem(item);
        return this;
    }

    public int getPositionForSection(final int section) {
        return sectionPositions.get(section);
    }

    public int getSectionForPosition(final int position) {
        return itemSections.get(position);
    }

    public Object[] getSections() {
        return sections.toArray();
    }
}