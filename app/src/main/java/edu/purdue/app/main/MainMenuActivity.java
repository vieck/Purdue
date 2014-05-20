package edu.purdue.app.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.askerov.dynamicgid.BaseDynamicGridAdapter;
import org.askerov.dynamicgid.DynamicGridView;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import edu.purdue.app.PurdueApplication;
import edu.purdue.app.R;
import edu.purdue.app.WebViewActivity;

import static android.widget.AdapterView.OnItemClickListener;


public class MainMenuActivity extends Activity implements OnItemClickListener {

    private static final String PREF_PAGE_GRID = "grid_locations";
    private static final String PREF_GRID_ITEMS = "grid_items";
    List<MainMenuItem> menuItems;
    private DynamicGridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setIcon(R.drawable.ic_p);

        setContentView(R.layout.activity_main_menu);

        gridView = (DynamicGridView) findViewById(R.id.dynamic_grid);

        CustomGridAdapter adapter = prepareAdapter();
        gridView.setAdapter(adapter);

        assignGridListeners();

        if(!isOnline())
        {
            showNoInternetAlert();
        }



        // Get tracker.
        Tracker t = ((PurdueApplication) getApplication()).getTracker(
                PurdueApplication.TrackerName.APP_TRACKER);

        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName(this.getLocalClassName());

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    private void showNoInternetAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle("Sorry");
        alertDialog.setMessage("The Purdue App currently does not function offline. " +
                "Please connect to the internet and try again. " +
                "We hope to add offline browsing in the near future.");

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch(which)
                {
                    case AlertDialog.BUTTON_POSITIVE:
                        dialog.dismiss();
                        break;
                    case AlertDialog.BUTTON_NEUTRAL:
                        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        startActivity(intent);
                }
            }
        };

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", listener);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Open network settings", listener);

        alertDialog.show();
    }

    private CustomGridAdapter prepareAdapter() {

        menuItems = MainMenuItem.getDefaultMainMenuItems(this.getResources());
        CustomGridAdapter ada = new CustomGridAdapter<MainMenuItem>(this,
                new ArrayList<MainMenuItem>(menuItems), 4);

        SharedPreferences prefs = getSharedPreferences(PREF_PAGE_GRID, 0);
        if(prefs.contains(PREF_GRID_ITEMS))
        {
            String itemsJson = prefs.getString(PREF_GRID_ITEMS, null);
            JSONArray jsonArray = null;
            ArrayList<Integer> items = null;
            try {
                jsonArray = new JSONArray(itemsJson);
                items = new ArrayList<Integer>();
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    items.add(jsonArray.getInt(i));
                }

                while(menuItems.size() > items.size())
                    items.add(items.size());

            } catch (JSONException e) {
                Log.e("MainMenuActivity", "JSON from Shared Prefs was not valid", e);
            }

            if(items != null)
                ada.setLocations(items);
        }
        return ada;
    }

    private void assignGridListeners() {
        gridView.setOnDropListener(new DynamicGridView.OnDropListener() {
            @Override
            public void onActionDrop() {
            saveGridState();
            gridView.stopEditMode();
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            gridView.startEditMode();
            return false;
            }
        });

        gridView.setOnItemClickListener(this);
    }

    private void saveGridState() {
        BaseDynamicGridAdapter dga = (BaseDynamicGridAdapter) gridView.getAdapter();
        ArrayList<Integer> items = dga.getLocations();

        JSONArray jarr = new JSONArray(items);
        SharedPreferences prefs = getSharedPreferences(PREF_PAGE_GRID, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_GRID_ITEMS, jarr.toString()).apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (gridView.isEditMode()) {
            gridView.stopEditMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        if(isOnline()) {
            Object o = view.getTag();

            if (o instanceof MainMenuItem) {
                MainMenuItem item = (MainMenuItem) o;
                String url = item.getUrl();
                String name = item.getName();


                // Get tracker.
                Tracker t = ((PurdueApplication) getApplication()).getTracker(
                        PurdueApplication.TrackerName.APP_TRACKER);

                t.setScreenName(this.getLocalClassName());

                // Send an event.
                t.send(new HitBuilders.EventBuilder("ui_interaction", "grid_icon_click").setLabel(name).build());



                Log.d("GridItemClicked", "Opening url: " + url);
                Intent webViewIntent = new Intent(getBaseContext(), WebViewActivity.class);
                webViewIntent.putExtra(WebViewActivity.EXTRA_URL, url);
                webViewIntent.putExtra(WebViewActivity.EXTRA_NAME, name);
                startActivity(webViewIntent);
            }
        }
        else
        {
            Toast.makeText(this, "No internet connection. Connect and try again.", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}