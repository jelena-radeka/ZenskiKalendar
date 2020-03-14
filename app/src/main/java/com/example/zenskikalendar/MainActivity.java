package com.example.zenskikalendar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.zenskikalendar.model.DatabaseHelper;
import com.example.zenskikalendar.model.Grupa;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MyAdapter.OnItemClickListener {

    List<String> drawerItems;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    ListView drawerList;
    private AlertDialog dijalog;
    private RelativeLayout drawerPane;
    private ActionBarDrawerToggle drawerToggle;
    DatabaseHelper databaseHelper;
    private SharedPreferences prefs;
    public static final String DATE_FORMAT_1 = "dd.MM.yyyy.";
    public static final String DATE_FORMAT_2 = "HH:mm";
    MyAdapter adapter;
    RecyclerView rvGrupe;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Grupa> grupe=new ArrayList<>();
    public static final int NOTIF_ID = 101;
    public static final String NOTIF_CHANNEL_ID = "notif_channel_007";
    public static String GRUPA_KEY = "GRUPA_KEY";
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fillData();
        setupToolbar();
        setupDrawer();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        showGrupa();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.addGrupa:
                addGrupa();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addGrupa() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_grupa_layout);
        dialog.setTitle("Unesite naziv");
        dialog.setCanceledOnTouchOutside(false);

        final EditText etGrupaNaziv = dialog.findViewById(R.id.etGrupaNaziv);



        Button add = dialog.findViewById(R.id.bAddGrupa);
        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Grupa grupa = new Grupa();
                grupa.setDbNaziv(etGrupaNaziv.getText().toString());
                grupa.setDbDatum(getCurrentDate());
                grupa.setDbVreme(getCurrentTime());


                try {
                    getDatabaseHelper().getGrupaDao().create(grupa);


                    boolean toast = prefs.getBoolean(getString(R.string.toast_key), false);
                    boolean notif = prefs.getBoolean(getString(R.string.notif_key), false);

                    if (toast) {
                        Toast.makeText(MainActivity.this, "Uneta nova grupa", Toast.LENGTH_LONG).show();

                    }
                    if(notif){
                        showNotification("Unet nov glumac");
                    }

                    refresh();

                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Rating mora biti broj", Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();


            }

        });

        Button cancel = dialog.findViewById(R.id.cancel_grupa);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showNotification(String poruka) {

        NotificationManager notificationManager;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this,NOTIF_CHANNEL_ID);
        builder.setSmallIcon(android.R.drawable.ic_input_add);
        builder.setContentTitle("Kalendar");
        builder.setContentText(poruka);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.dialog_frame);


        builder.setLargeIcon(bitmap);
        notificationManager.notify(1, builder.build());
    }

    private void refresh() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvGrupe);

        if (recyclerView != null) {
            MyAdapter adapter = (MyAdapter) recyclerView.getAdapter();

            if (adapter != null) {

                try {
                    adapter.clear();
                    List<Grupa> list = getDatabaseHelper().getGrupaDao().queryForAll();

                    adapter.addAll(list);

                    adapter.notifyDataSetChanged();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public static String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_1);
        // get current date time with Date()
        Date date = new Date();
        // System.out.println(dateFormat.format(date));
        // don't print it, but save it!
        return dateFormat.format(date);
    }

    public static String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_2);
        // get current date time with Date()
        Date date = new Date();
        // System.out.println(dateFormat.format(date));
        // don't print it, but save it!
        return dateFormat.format(date);
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_all_black_24dp);
            actionBar.setHomeButtonEnabled(true);
            actionBar.show();
        }
    }

    private void fillData() {
        drawerItems = new ArrayList<>();
        drawerItems.add("Grupe");
        drawerItems.add("Settings");
        drawerItems.add("About");

    }

    private void setupDrawer() {
        drawerList = findViewById(R.id.left_drawer);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerPane = findViewById(R.id.drawerPane);

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String title = "Unknown";
                switch (i) {
                    case 0:
                        title = "Grupe";
                        showGrupa();
                        break;
                    case 1:
                        title = "Settings";
                        Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settings);
                        break;
                    case 2:
                        title = "About";
                        showDialog();
                        break;

                }
                drawerList.setItemChecked(i, true);
                setTitle(title);
                drawerLayout.closeDrawer(drawerPane);
            }
        });
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerItems));


        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.app_name,
                R.string.app_name
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
    }

    private void showDialog() {
        if (dijalog == null) {
            dijalog = new AboutDialog(MainActivity.this).prepareDialog();
        } else {
            if (dijalog.isShowing()) {
                dijalog.dismiss();
            }
        }
        dijalog.show();
    }



    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    protected void onResume() {
        super.onResume();

        refresh();

    }

    private void showGrupa() {

        try {
            grupe= (ArrayList<Grupa>) getDatabaseHelper().getGrupaDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rvGrupe = findViewById(R.id.rvGrupe);
        rvGrupe.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rvGrupe.setLayoutManager(layoutManager);

        adapter = new MyAdapter(grupe, this);
        rvGrupe.setAdapter(adapter);

    }

    private void showGrupaPrefs(){
        boolean filter   = prefs.getBoolean("filter_grupe", false);
        boolean period   = prefs.getBoolean("filter_vreme", false);

        try {
            grupe= (ArrayList<Grupa>) getDatabaseHelper().getGrupaDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ArrayList<Grupa> grupeFilter=new ArrayList<>();
        if(filter){
            for(int i=0;i<grupe.size();i++){
                try {
                    Time time= (Time) dateFormat.parse(grupe.get(i).getDbVreme());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(0==0){

                }
            }
        }

    }


    @Override
    public void OnItemClick(int position) {
        Grupa g = (Grupa) adapter.get(position);
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(GRUPA_KEY, g.getDbId());
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
    }

}
