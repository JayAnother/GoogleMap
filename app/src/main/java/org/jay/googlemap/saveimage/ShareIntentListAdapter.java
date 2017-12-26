
package org.jay.googlemap.saveimage;

import android.app.Activity;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.jay.googlemap.R;


public class ShareIntentListAdapter extends ArrayAdapter {

    private final Activity context;
    Object[] items;
    OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    public ShareIntentListAdapter(Activity context, Object[] items) {

        super(context, R.layout.layout_shared, items);
        this.context = context;
        this.items = items;

    }// end HomeListViewPrototype

    class ShareHolder {
        TextView shareName;
        ImageView imageShare;


    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        ShareHolder hodler = null;
        if (view == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.layout_shared, null, true);
            hodler = new ShareHolder();
            hodler.imageShare = (ImageView) view.findViewById(R.id.shareImage);
            hodler.shareName = (TextView) view.findViewById(R.id.shareName);
            view.setTag(hodler);
        } else {
            hodler = (ShareHolder) view.getTag();
        }
        // set native name of App to share
        hodler.shareName.setText(((ResolveInfo) items[position]).activityInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());

        // share native image of the App to share
        hodler.imageShare.setImageDrawable(((ResolveInfo) items[position]).activityInfo.applicationInfo.loadIcon(context.getPackageManager()));
        hodler.imageShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onClick(v);
                }
            }
        });

        return view;
    }// end getView

    public interface OnItemClickListener {
        void onClick(View v);
    }

}// end main onCreate