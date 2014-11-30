package gr.modissense.ui.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import gr.modissense.ModisSenseApplication;
import gr.modissense.R;

/**
 * {@link android.widget.BaseAdapter}-Implementation for the {@link gr.modissense.ui.view.MultiChoice} Dialog.
 *
 * @param <T>
 *            Type this Adapter contains. Should have some useful toString()
 *            implementation.
 */
public class MultiChoiceListAdapter<T extends MultiChoiceItem> extends BaseAdapter {
    private Context ctx;

    private Collection<T> options;
    private Collection<T> selection;
    private List<T> filteredOptions;

    public MultiChoiceListAdapter(Context context, Collection<T> options,
                                  Collection<T> selection) {
        this.ctx = context;

        this.options = options;
        this.selection = selection;

        this.filteredOptions = new ArrayList<T>(options.size());
        setFilter(null);
    }

    @Override
    public int getCount() {
        return filteredOptions.size();
    }

    @Override
    public T getItem(int position) {
        return filteredOptions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChoiceView view;
        T item = getItem(position);
        boolean selected = selection.contains(item);
        if (convertView == null) {
            view = new ChoiceView(ctx, item, selected);
        } else {
            view = (ChoiceView) convertView;
            view.setItem(item, selected);
        }
        return view;
    }

    public void setFilter(String filter) {
        if (filter != null)
            filter = filter.toLowerCase();

        filteredOptions.clear();
        for (T item : selection)
            filteredOptions.add(item);
        for (T item : options)
            if (!selection.contains(item)
                    && (filter == null || item.toString().toLowerCase()
                    .contains(filter)))
                filteredOptions.add(item);
    }

    public class ChoiceView extends CheckBox implements OnCheckedChangeListener {
        private T object;

        public ChoiceView(Context context, T object, Boolean selected) {
            super(context);
            this.object = object;

            setOnCheckedChangeListener(this);
            setPadding(10,10,10,10);
            setItem(object, selected);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (selection != null) {
                if (isChecked && !selection.contains(object))
                    selection.add(object);
                else if (!isChecked)
                    selection.remove(object);
            }
            notifyDataSetChanged();
        }

        public void setItem(T object, Boolean selected) {
            this.object = object;
            setChecked(selected);
            setText(object.toString());
            Target target = new Target() {
//            @Override
//            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//            }
//
//            @Override
//            public void onBitmapFailed() {
//            }

                @Override
                public void onSuccess(Bitmap bitmap) {
                    BitmapDrawable d = new BitmapDrawable(getResources(), bitmap);
                    //d.setBounds(10,10,10,10);
                    setCompoundDrawablesWithIntrinsicBounds(null, null, d ,null);
                }

                @Override
                public void onError() {

                }
            };
            Picasso.with(ModisSenseApplication.getInstance())
                    .load(object.getUrl())
                    .placeholder(R.drawable.gravatar_icon)
                    .into(target);

        }
    }
}