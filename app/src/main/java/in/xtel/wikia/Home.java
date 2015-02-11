package in.xtel.wikia;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import in.xtel.wikia.adapters.WikiaAdapter;
import in.xtel.wikia.listeners.LoaderListener;
import in.xtel.wikia.model.Wikia;

/**
 * Core class. Includes major data loading logic and UI handling.
 */
public class Home extends ActionBarActivity implements LoaderListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {


    private SwipeRefreshLayout swipeRefreshLayout;
    private WikiaAdapter adapter;

    public static final int LIMIT = 25;
    public static final String BASE_URL = "http://www.wikia.com/wikia.php?controller=WikisApi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_wikia_logo);

        adapter = new WikiaAdapter(this);
        ListView wikias = (ListView) findViewById(R.id.wikia_list);
        wikias.setAdapter(adapter);
        wikias.setOnItemClickListener(this);


        // Swipe down to refresh facility.
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.srl_green, R.color.srl_yellow, R.color.srl_blue, R.color.srl_red);
        swipeRefreshLayout.setOnRefreshListener(this);

        // Load initial set of data (25 wikias)
        loadWikias(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_about) {
            Toast.makeText(Home.this, getString(R.string.developer_info), Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Loads initial set of @LIMIT wikias from the API. Receives only basic
     * info such as wikia:id. Collexts ids from the data for the detail call.
     * @param batch
     */
    private void loadWikias(int batch) {

        // For initial set of data show the refreshing drawable.
        if(batch == 1) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        final String smallQueryUrl = BASE_URL
                + "&method=getList"
                + "&lang=en"
                + "&limit=" + LIMIT
                + "&batch=" + batch;

        AQuery aQuery = new AQuery(this);
        aQuery.ajax(smallQueryUrl, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {
                super.callback(url, object, status);

                if(object != null) {
                    // Collect ids from result callback
                    JSONArray items = object.optJSONArray("items");
                    List<Integer> ids = new ArrayList<>();
                    for(int i = 0; i < items.length(); i++) {
                        ids.add(items.optJSONObject(i).optInt("id"));
                    }
                    loadDetailedWikias(ids);
                }   else {
                    Toast.makeText(Home.this, "Failed loading items", Toast.LENGTH_SHORT).show();
                    // Stop refreshing, we can't go any further
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    /**
     * Uses the ids from the shallow API call and makes a detail
     * call for wikia image and text description.
     * @param ids
     */
    private void loadDetailedWikias(final List<Integer> ids) {
        String bigQueryUrl = BASE_URL
                + "&method=getDetails"
                + "&ids=";

        for(Integer i: ids) {
            bigQueryUrl += i + ",";
        }
        bigQueryUrl = bigQueryUrl.substring(0, bigQueryUrl.length() - 1)
                + "&height=100"
                + "&width=100";


        AQuery aQuery = new AQuery(this);
        aQuery.ajax(bigQueryUrl, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {
                super.callback(url, object, status);

                // Stop refreshing, if already showing.
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

                if(object != null) {
                    // Iterate through the JSON result for popular Wikias
                    JSONObject core = object.optJSONObject("items");
                    Iterator key = core.keys();
                    Gson gS = new Gson();

                    while (key.hasNext()) {
                        JSONObject item = core.optJSONObject(key.next().toString());
                        Wikia wikia = gS.fromJson(item.toString(), Wikia.class);
                        // addAll() is API-11 Code compatibility for API-10
                        adapter.add(wikia);
                    }

                    // Inform the adapter about loading finish
                    adapter.setLoading(false);
                }   else {
                    Toast.makeText(Home.this, "Failed loading items", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * LoaderListerner main callback. Itemcount is used to calculate batch number
     * @param itemCount
     */
    @Override
    public void onLoadMoreRequested(int itemCount) {
        Toast.makeText(this, "Loading more items", Toast.LENGTH_SHORT).show();
        int nextBatch = itemCount / LIMIT + 1;
        loadWikias(nextBatch);
    }

    /**
     * Before navigating to the new screen pack the data with the intent.
     * Saves one additional network call.
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Wikia item = adapter.getItem(position);
        String json = new Gson().toJson(item);
        Intent i = new Intent(this, Detail.class);
        i.putExtra("wikia", json);
        startActivity(i);
    }

    /**
     * When user swipes down to refresh, this method is triggered.
     */
    @Override
    public void onRefresh() {
        adapter.clear();
        loadWikias(1);
    }
}
