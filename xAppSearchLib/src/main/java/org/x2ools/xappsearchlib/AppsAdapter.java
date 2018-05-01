package org.x2ools.xappsearchlib;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.x2ools.xappsearchlib.model.AppItem;
import org.x2ools.xappsearchlib.model.SearchItem;

import java.util.List;

/**
 * @author zhoubinjia
 * @date 2017/8/21
 */
public class AppsAdapter extends BaseAdapter {

    private List<SearchItem> mItems;
    private Context mContext;

    public AppsAdapter(Context context, List<SearchItem> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        final SearchItem item = (SearchItem) getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.package_item, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.textTitle = convertView.findViewById(R.id.textTitle);
            viewHolder.icon = convertView.findViewById(R.id.icon);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (item.getIcon() == null) {
            if (item instanceof AppItem) {
                ComponentName componentName = ComponentName.unflattenFromString(((AppItem) item).getComponentName());
                Drawable icon = null;
                try {
                    icon = mContext.getPackageManager().getActivityIcon(componentName);
                } catch (PackageManager.NameNotFoundException e) {
                    icon = mContext.getPackageManager().getDefaultActivityIcon();
                }
                item.setIcon(icon);
            }
        }
        if (item.getIcon() == null) {
            viewHolder.icon.setVisibility(View.GONE);
        } else {
            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.icon.setImageDrawable(item.getIcon());
        }
        viewHolder.textTitle.setText(item.getName());
        return convertView;
    }
}
