package iitp.naman.mksdrive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;

/**
 * Created by naman on 21-12-2017.
 * requests file at entered email
 */

public class FileRequestByEmail extends AppCompatActivity {
    private EditText inputEmail;
    private String inputEmail1;
    private String username;
    private String secureKey;
    private String folderID;
    private String mimeType;
    private String name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filerequestbyemail);

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(" ");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        Button btnRequest;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            secureKey = extras.getString("secureKey");
            folderID = extras.getString("folderID");
            mimeType = extras.getString("mimeType");
            name = extras.getString("name");
            inputEmail = findViewById(R.id.email);
            inputEmail.setText(username);
            btnRequest = findViewById(R.id.btnRequest);

            btnRequest.setOnClickListener(view -> {
                inputEmail1=inputEmail.getText().toString();
                if(inputEmail1.equals("")){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_filerequestbyemail_1), Toast.LENGTH_SHORT).show();
                }
                else {
                    new ProcessRegister(FileRequestByEmail.this).execute();
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_error), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_blank, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class ProcessRegister extends AsyncTask<String,Void,JSONObject> {
        private ProgressDialog pDialog;
        private final WeakReference<FileRequestByEmail> activityReference;

        // only retain a weak reference to the activity
        ProcessRegister(FileRequestByEmail context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            FileRequestByEmail activity = activityReference.get();
            if(activity==null){
                return;
            }
            pDialog = new ProgressDialog(activity, R.style.MyProgressDialog);
            pDialog.setMessage(activity.getResources().getString(R.string.java_please_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            JSONObject jsonIn = new JSONObject();
            final FileRequestByEmail activity = activityReference.get();
            if(activity==null){
                return null;
            }
            try {
                jsonIn.put("email",activity.username);
                jsonIn.put("secureKey",activity.secureKey);
                jsonIn.put("folderID",activity.folderID);
                jsonIn.put("mimeType",activity.mimeType);
                jsonIn.put("name",activity.name);
                jsonIn.put("emailReceiver",activity.inputEmail1);
                RequestQueue que = Volley.newRequestQueue(activity);
                String urlString = activity.getResources().getString(R.string.url_senddata);
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, urlString, jsonIn,
                        response -> {
                            try {
                                String status = response.getString("status");
                                if (status.compareTo("ok") == 0) {
                                    AlertDialogError(response.getString("message"));
                                }
                                else if (status.compareTo("err") == 0) {
                                    String resp = response.getString("message");
                                    if(resp.equals("Invalid session, please login again")){
                                        AlertDialogInvalidSession(resp);
                                    }
                                    else {
                                        AlertDialogError(resp);
                                    }
                                }
                                else{
                                    AlertDialogError(activity.getResources().getString(R.string.connection_fail));
                                }
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                                AlertDialogError(activity.getResources().getString(R.string.connection_fail));
                            }
                        }, error -> AlertDialogError(activity.getResources().getString(R.string.connection_fail)));
                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(StartScreen.MAX_TIMEOUT,StartScreen.MAX_RETRY,StartScreen.BACKOFF_MULT));
                que.add(jsonObjReq);
            }
            catch (JSONException e) {
                e.printStackTrace();
                AlertDialogError(activity.getResources().getString(R.string.connection_fail));
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject response) {

        }

        private void AlertDialogError(String resp){
            final FileRequestByEmail activity = activityReference.get();
            if(activity==null){
                return ;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialog);
            builder.setMessage(resp)
                    .setCancelable(false)
                    .setPositiveButton(activity.getResources().getString(R.string.java_folderview_2), (dialog, id) -> {
                        dialog.cancel();
                        activity.finish();
                    });
            AlertDialog alert = builder.create();
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            alert.show();
        }

        private void AlertDialogInvalidSession(String resp){
            final FileRequestByEmail activity = activityReference.get();
            if(activity==null){
                return ;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialog);
            builder.setMessage(resp)
                    .setCancelable(false)
                    .setPositiveButton(activity.getResources().getString(R.string.java_folderview_2), (dialog, id) -> {
                        SharedPreferences.Editor e = activity.getSharedPreferences("cookie_data", MODE_PRIVATE).edit();
                        e.putBoolean("rm", false);
                        e.apply();
                        e.commit();
                        dialog.cancel();
                        Intent intent = new Intent(activity, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(intent);
                        activity.finish();
                    });
            AlertDialog alert = builder.create();
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            alert.show();
        }
    }
}
