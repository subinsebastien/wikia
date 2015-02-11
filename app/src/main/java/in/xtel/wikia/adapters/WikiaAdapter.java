package in.xtel.wikia.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.androidquery.AQuery;

import in.xtel.wikia.R;
import in.xtel.wikia.listeners.LoaderListener;
import in.xtel.wikia.model.Wikia;

/**
 * Created by napster on 05/02/15.
 */
public class WikiaAdapter extends ArrayAdapter<Wikia> {

    /**
     * loaderListener will be the hosting activity class itself.
     * A class cast exception means, the "Home" implementation of
     * this listener is incorrect.
     */
    LoaderListener loaderListener;
    boolean isLoading = false;

    public WikiaAdapter(Context context) {
        super(context, 0);
        loaderListener = (LoaderListener) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_cell_wikia, parent, false);
        }

        final Wikia item = getItem(position);

        // Prepare view for this row
        AQuery aQuery = new AQuery(convertView);
        aQuery.id(R.id.iv_wikia_thumb).image(item.wordmark);
        aQuery.id(R.id.tv_blog_name).text(item.title.toUpperCase());
        aQuery.id(R.id.tv_wikia_desc).text(item.desc);

        aQuery.id(R.id.tv_wikia_url).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(item.url));
                    getContext().startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Unable to open Browser", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         * User scrolled to the bottom & there is no actively running
         * download task on this device. The flag is unset from the
         * Home class. A bit tricky here, but saves lines and time.
         */
        if (position == (getCount() - 1) && !isLoading) {
            loaderListener.onLoadMoreRequested(getCount());
            isLoading = true;
            aQuery.id(R.id.pb_loading_more).visible();
        }   else {
            aQuery.id(R.id.pb_loading_more).gone();
        }

        return convertView;
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }
}
