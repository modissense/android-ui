package gr.modissense.ui.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SingleChoiceListAdapter<T> extends BaseAdapter {
    private Context ctx;

    private Collection<T> options;
    private Collection<T> selection;
    private List<T> filteredOptions;

    public SingleChoiceListAdapter(Context context, Collection<T> options,
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

    public class ChoiceView extends CheckBox implements CompoundButton.OnCheckedChangeListener {
        private T object;

        public ChoiceView(Context context, T object, Boolean selected) {
            super(context);
            this.object = object;
            setOnCheckedChangeListener(this);
            setItem(object, selected);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (selection != null) {
                if (isChecked && !selection.contains(object)){
                    selection.clear();
                    selection.add(object);
                }
                else if (!isChecked)
                    selection.remove(object);
            }
            notifyDataSetChanged();
        }

        public void setItem(T object, Boolean selected) {
            this.object = object;
            setChecked(selected);
            setText(object.toString());
        }
    }
}