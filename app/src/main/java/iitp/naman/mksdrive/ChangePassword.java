package iitp.naman.mksdrive;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by naman on 14-Dec-17.
 * Changes Password
 */

public class ChangePassword extends AppCompatActivity {
    private EditText newPass;
    private EditText oldPass;
    private EditText confirmNewPass;
    private String serverEmail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(" ");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        Button btnConfirm;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            serverEmail=extras.getString("username");
            newPass = findViewById(R.id.newPass);
            oldPass = findViewById(R.id.oldPass) ;
            confirmNewPass= findViewById(R.id.confirmNewPass) ;
            btnConfirm = findViewById(R.id.btnConfirm);

            btnConfirm.setOnClickListener(view -> {
                String inputNewPass=newPass.getText().toString();
                String inputConfirmNewPass=confirmNewPass.getText().toString();
                String inputOldPass = oldPass.getText().toString();
                if(inputConfirmNewPass.equals(inputNewPass)) {
                    if(inputNewPass.equals("")||inputOldPass.equals("")){
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_changepassword_2), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        new ProcessRegister(ChangePassword.this, inputNewPass, inputOldPass).execute();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_changepassword_1), Toast.LENGTH_SHORT).show();
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
        private final String newPass1;
        private final String oldPass1;
        private ProgressDialog pDialog;
        private final WeakReference<ChangePassword> activityReference;

        // only retain a weak reference to the activity
        ProcessRegister(ChangePassword context, String newPass1, String oldPass1) {
            activityReference = new WeakReference<>(context);
            this.newPass1 = newPass1;
            this.oldPass1 = oldPass1;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ChangePassword activity = activityReference.get();
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
            final ChangePassword activity = activityReference.get();
            if(activity==null){
                return null;
            }
            try {
                jsonIn.put("email", activity.serverEmail);
                jsonIn.put("oldpassword",oldPass1);
                jsonIn.put("newpassword", newPass1);
                RequestQueue que = Volley.newRequestQueue(activity);
                String urlString = activity.getResources().getString(R.string.url_changepassword);
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, urlString, jsonIn,
                        response -> {
                            try {
                                String status = response.getString("status");
                                if (status.compareTo("ok") == 0) {
                                    if (pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                    Toast.makeText(activity, response.getString("message"), Toast.LENGTH_SHORT).show();
                                    activity.finish();
                                }
                                else if (status.compareTo("err") == 0) {
                                    if (pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                    Toast.makeText(activity, response.getString("message"), Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    if (pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                    Toast.makeText(activity, activity.getResources().getString(R.string.connection_fail), Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                Toast.makeText(activity, activity.getResources().getString(R.string.connection_fail), Toast.LENGTH_SHORT).show();
                            }
                        }, error -> {
                            if (pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            Toast.makeText(activity, activity.getResources().getString(R.string.connection_fail), Toast.LENGTH_SHORT).show();
                        });
                que.add(jsonObjReq);
            }
            catch (JSONException e) {
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