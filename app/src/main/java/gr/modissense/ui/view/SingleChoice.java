package gr.modissense.ui.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * A dialog that allows the user to select multiple entries.
 *
 * @param <T>
 *            Type for this dialog. Should have some useful toString()
 *            implementation.
 */
public abstract class SingleChoice<T>  {
    private ListView listView;

    private Map<T, Boolean> optionsWithSelection;
    private Collection<T> options;
    private Collection<T> selection;
    private Context context;
    public SingleChoice(Context context, Collection<T> options,
                       Collection<T> selection) {
        this.options = options;
        this.selection = selection;
        this.context =context;

    }

    public void showDialog(String title){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        listView = new ListView(context);
        final SingleChoiceListAdapter<T> adapter;
        adapter = new SingleChoiceListAdapter<T>(context, options, selection);
        listView.setAdapter(adapter);

        if (options.size() > 10) {
            EditText search = new EditText(context);
            search.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER);
            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    adapter.setFilter(s.toString());
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            layout.addView(search);
        }

        layout.addView(listView);
        builder.setView(layout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onOk();
            }
        });
        builder.show();
    }

    protected abstract void onOk();


    public Map<T, Boolean> getOptionsMap() {
        return optionsWithSelection;
    }

    public Set<T> getSelection() {
        return new HashSet<T>(selection);
//        Set<T> result = new LinkedHashSet<T>();
//        for (Entry<T, Boolean> e : optionsWithSelection.entrySet())
//            if (Boolean.TRUE.equals(e.getValue()))
//                result.add(e.getKey());
//        return result;
    }
}
