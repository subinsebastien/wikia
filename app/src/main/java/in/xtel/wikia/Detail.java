package in.xtel.wikia;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.google.gson.Gson;

import in.xtel.wikia.model.Wikia;

/**
 * A bonus class, which also displays another image retured by the
 * API with key "image". It also provides full view of the Wikia
 * description.
 */
public class Detail extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Create data object from JSON string attached from Home.
        String json = getIntent().getStringExtra("wikia");
        Wikia wikia = new Gson().fromJson(json, Wikia.class);

        getSupportActionBar().setTitle(wikia.title);

        AQuery aQuery = new AQuery(this);
        aQuery.id(R.id.tv_wikia_title).text(wikia.title.toUpperCase());
        aQuery.id(R.id.tv_wikia_desc).text(wikia.desc);
        aQuery.id(R.id.iv_cover).image(wikia.wordmark);
        aQuery.id(R.id.iv_thumb_image).image(wikia.image);
    }
}
