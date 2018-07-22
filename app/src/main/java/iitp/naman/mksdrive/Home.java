package iitp.naman.mksdrive;

import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String username;
    private String name;
    private String phone;
    private String folderID;
    private String secureKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            name = extras.getString("name");
            phone = extras.getString("phone");
            folderID = extras.getString("folderID");
            secureKey = extras.getString("secureKey");
        }
        else{
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_error), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setContentView(R.layout.activity_home);
        DrawerLayout drawer;
        ActionBarDrawerToggle toggle;
        Toolbar toolbar;
        NavigationView navigationView;
        Button btnGotoDrive;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        btnGotoDrive = findViewById(R.id.gotoDrive);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        TextView intro;
        intro = findViewById(R.id.intro);
        String introText = "Hello " + name + "!\n" + "Welcome To MKS Drive\n"+"Phone: "+ phone + "\nEmail: "+ username;
        intro.setText(introText);

        btnGotoDrive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FolderView.class);
                intent.putExtra("username", username);
                intent.putExtra("name", "Files");
                intent.putExtra("mimeType", "application/vnd.google-apps.folder");
                intent.putExtra("folderID", folderID);
                intent.putExtra("secureKey", secureKey);
                intent.putExtra("downloadPath", Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.app_name));
                startActivity(intent);
            }
        });

        navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_home));
        }
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialog);
            builder.setMessage(getResources().getString(R.string.java_home_1))
                    .setCancelable(true)
                    .setPositiveButton(getResources().getString(R.string.java_home_2), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new Exit(Home.this);
                            finish();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.java_home_3), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuHelp:
                String url1 = getResources().getString(R.string.url_help);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url1));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {

        }
        else if (id == R.id.nav_myprofile) {
            Intent intent = new Intent(getApplicationContext(), MyProfile.class);
            intent.putExtra("username", username);
            intent.putExtra("secureKey", secureKey);
            startActivity(intent);
        }
        else if (id == R.id.nav_help) {
            Intent intent = new Intent(getApplicationContext(), Help.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_logout) {
            new ProcessRegister(Home.this).execute();
        }
        else if (id == R.id.nav_exit) {
            new Exit(Home.this);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static class ProcessRegister extends AsyncTask<String, Void, JSONObject> {

        private ProgressDialog pDialog;
        private WeakReference<Home> activityReference;

        // only retain a weak reference to the activity
        ProcessRegister(Home context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Home activity = activityReference.get();
            if(activity==null){
                return;
            }
            pDialog = new ProgressDialog(activity, R.style.MyProgressDialog);
            pDialog.setMessage(activity.getResources().getString(R.string.java_home_4));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            JSONObject jsonIn = new JSONObject();
            final Home activity = activityReference.get();
            if(activity==null){
                return null;
            }
            try {
                RequestQueue que = Volley.newRequestQueue(activity);
                String urlString = activity.getResources().getString(R.string.url_signout);
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, urlString, jsonIn,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String status = response.getString("status");
                                    if (status.compareTo("ok") == 0) {
                                        SharedPreferences.Editor e = activity.getSharedPreferences("cookie_data", MODE_PRIVATE).edit();
                                        e.putBoolean("rm", false);
                                        e.apply();
                                        e.commit();
                                        Intent intent = new Intent(activity, Login.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        if (pDialog.isShowing()) {
                                            pDialog.dismiss();
                                        }
                                        activity.startActivity(intent);
                                        activity.finish();
                                    } else {
                                        if (pDialog.isShowing()) {
                                            pDialog.dismiss();
                                        }
                                        Toast.makeText(activity, activity.getResources().getString(R.string.connection_fail), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    if (pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                    Toast.makeText(activity, activity.getResources().getString(R.string.connection_fail), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        Toast.makeText(activity, activity.getResources().getString(R.string.connection_fail), Toast.LENGTH_SHORT).show();
                    }
                });
                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(StartScreen.MAX_TIMEOUT,StartScreen.MAX_RETRY,StartScreen.BACKOFF_MULT));
                que.add(jsonObjReq);

            } catch (Exception e) {
                e.printStackTrace();
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                Toast.makeText(activity, activity.getResources().getString(R.string.connection_fail), Toast.LENGTH_SHORT).show();
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject response) {

        }
    }
}