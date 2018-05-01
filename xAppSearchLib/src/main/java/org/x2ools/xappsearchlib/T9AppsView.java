
package org.x2ools.xappsearchlib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;

import org.x2ools.xappsearchlib.model.AppItem;
import org.x2ools.xappsearchlib.model.ContactItem;
import org.x2ools.xappsearchlib.model.SearchItem;

import java.util.List;

public class T9AppsView extends FrameLayout implements T9ViewDelegate {

    public static final boolean DEBUG = true;
    private static final String TAG = "T9AppsView";

    private T9ViewModel mViewModel;

    private EditText mFilterView;
    private GridView mAppsGridView;

    private StringBuilder mFilterText = new StringBuilder();
    private AppsAdapter mAdapter;

    public T9AppsView(Context context) {
        this(context, null);
    }

    public T9AppsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public T9AppsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.t9_apps_content, this);

        if (!isInEditMode()) {
            mViewModel = new T9ViewModel(context);
            mViewModel.setDelegate(this);
            initSubViews();
            mViewModel.filter("");
        }
    }

    private void initSubViews() {
        mAppsGridView = findViewById(R.id.appsList);
        mFilterView = findViewById(R.id.numFilter);

        mAppsGridView.setOnItemClickListener((adapterView, view, i, l) -> {
            SearchItem item = (SearchItem) mAdapter.getItem(i);
            Intent intent = mViewModel.getLaunchIntent(item);
            if (intent != null) {
                try {
                    getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mViewModel.recordUsage(item);
            hideView();
        });

        mAppsGridView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            SearchItem item = (SearchItem) mAdapter.getItem(i);
            if (item instanceof ContactItem) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
                        item.getId() + "");
                intent.setData(uri);
                getContext().startActivity(intent);
            } else if (item instanceof AppItem) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.parse("package:" + ((AppItem)item).getComponentName()));
                getContext().startActivity(intent);
                hideView();
            }
            return true;
        });

        mFilterView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.filter(s.toString());
            }
        });
    }

    public void clearFilter() {
        mFilterText = new StringBuilder();
        onTextChanged();
    }

    private void onTextChanged() {
        mFilterView.setText(mFilterText);
    }

    public void hideView() {
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).finish();
        }
    }

    @Override
    public void showItems(List<SearchItem> searchItems) {
        mAdapter = new AppsAdapter(getContext(), searchItems);
        mAppsGridView.setAdapter(mAdapter);
    }
}
