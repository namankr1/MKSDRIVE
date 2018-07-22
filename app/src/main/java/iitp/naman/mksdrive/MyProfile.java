package iitp.naman.mksdrive;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
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

import java.lang.ref.WeakReference;

/**
 * Created by naman on 14-12-2017.
 * Retrieves profile
 */

public class MyProfile extends AppCompatActivity{
    private String username;
    private String secureKey;
    private String name;
    private String phone;
    private String email;
    private String pan;
    private String aadhar;
    private String gender;
    private String address;
    private String state;
    private String zipcode;

    private TextView name1;
    private TextView phone1;
    private TextView email1;
    private TextView pan1;
    private TextView aadhar1;
    private TextView gender1;
    private TextView address1;
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_myprofile);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username=extras.getString("username");
            secureKey=extras.getString("secureKey");
        }
        else{
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_error), Toast.LENGTH_SHORT).show();
            finish();
        }

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(" ");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        name1 = findViewById(R.id.name);
        email1 = findViewById(R.id.email);
        pan1 = findViewById(R.id.pan);
        aadhar1 = findViewById(R.id.aadhar);
        gender1 = findViewById(R.id.gender);
        address1 = findViewById(R.id.address);
        phone1 = findViewById(R.id.phone);
        new ProcessGetProfile(MyProfile.this).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_myprofile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                this.finish();
                return true;
            }
            case R.id.change_password: {
                Intent intent = new Intent(getApplicationContext(), ChangePassword.class);
                intent.putExtra("username", username);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class ProcessGetProfile extends AsyncTask<String,Void,JSONObject> {

        private WeakReference<MyProfile> activityReference;

        // only retain a weak reference to the activity
        ProcessGetProfile(MyProfile context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MyProfile activity = activityReference.get();
            if(activity==null){
                return;
            }
            activity.pDialog = new ProgressDialog(activity, R.style.MyProgressDialog);
            activity.pDialog.setMessage(activity.getResources().getString(R.string.java_please_wait));
            activity.pDialog.setCancelable(false);
            activity.pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            JSONObject jsonIn = new JSONObject();
            final MyProfile activity = activityReference.get();
            if(activity==null){
                return null;
            }
            try {
                jsonIn.put("email",activity.username);
                jsonIn.put("secureKey",activity.secureKey);
                RequestQueue que = Volley.newRequestQueue(activity);
                String urlString = activity.getResources().getString(R.string.url_getprofile);
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, urlString, jsonIn,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String status = response.getString("status");
                                    if (status.compareTo("ok") == 0) {
                                        JSONObject tempData =  response.getJSONObject("profile");
                                        activity.name = tempData.getString("name");
                                        activity.phone = tempData.getString("phone");
                                        activity.email = tempData.getString("email");
                                        activity.pan = tempData.getString("pan");
                                        activity.aadhar = tempData.getString("aadhar");
                                        activity.gender = tempData.getString("gender");
                                        activity.address = tempData.getString("address");
                                        activity.state = tempData.getString("state");
                                        activity.zipcode = tempData.getString("zipcode");

                                        switch (activity.gender){
                                            case "M" :
                                                activity.gender="Male";
                                                break;
                                            case "F" :
                                                activity.gender="Female";
                                                break;
                                            default:
                                                activity.gender="Other";
                                        }

                                        activity.name1.setText(activity.name);
                                        activity.phone1.setText(activity.phone);
                                        activity.email1.setText(activity.email);
                                        activity.pan1.setText(activity.pan);
                                        activity.aadhar1.setText(activity.aadhar);
                                        activity.gender1.setText(activity.gender);
                                        String addressText = activity.address + ", " + activity.state + ", " + activity.zipcode;
                                        activity.address1.setText(addressText);
                                        if (activity.pDialog.isShowing()) {
                                            activity.pDialog.dismiss();
                                        }
                                    }
                                    else if(status.compareTo("err") == 0){
                                        String resp = response.getString("message");
                                        if(resp.equals("Invalid session, please login again")){
                                            AlertDialogInvalidSession(activity,resp);
                                        }
                                        else {
                                            AlertDialogError(activity);
                                        }
                                    }
                                    else{
                                        AlertDialogError(activity);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    AlertDialogError(activity);
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        AlertDialogError(activity);
                    }
                });
                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(StartScreen.MAX_TIMEOUT,StartScreen.MAX_RETRY,StartScreen.BACKOFF_MULT));
                que.add(jsonObjReq);

            } catch (JSONException e) {
                e.printStackTrace();
                AlertDialogError(activity);
                return null;
            }
            return null;

        }
        @Override
        protected void onPostExecute(JSONObject response) {

        }
    }

    private static void AlertDialogError(final MyProfile activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialog);
        builder.setMessage(activity.getResources().getString(R.string.connection_fail))
                .setCancelable(false)
                .setPositiveButton(activity.getResources().getString(R.string.java_folderview_2), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        activity.finish();
                    }
                });
        AlertDialog alert = builder.create();
        if (activity.pDialog.isShowing()) {
            activity.pDialog.dismiss();
        }
        alert.show();
    }

    private static void AlertDialogInvalidSession(final MyProfile activity, String resp){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialog);
        builder.setMessage(resp)
                .setCancelable(false)
                .setPositiveButton(activity.getResources().getString(R.string.java_folderview_2), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences.Editor e = activity.getSharedPreferences("cookie_data", MODE_PRIVATE).edit();
                        e.putBoolean("rm", false);
                        e.apply();
                        e.commit();
                        dialog.cancel();
                        Intent intent = new Intent(activity, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                });
        AlertDialog alert = builder.create();
        if (activity.pDialog.isShowing()) {
            activity.pDialog.dismiss();
        }
        alert.show();
    }
}
