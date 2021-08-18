package iitp.naman.mksdrive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
 * Created by naman on 25-Nov-16.
 * Forgot password or set new password
 */
public class ForgotPassword extends AppCompatActivity {
    private EditText inputOtp;
    private EditText inputNewPassword;
    private String inputEmail1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(" ");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        Button btnVerify;
        Button btnResend;

        inputOtp = findViewById(R.id.otp);
        inputNewPassword = findViewById(R.id.newPassword);
        btnVerify = findViewById(R.id.verify);
        btnResend = findViewById(R.id.resend);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            inputEmail1 = extras.getString("username");
        }
        else{
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_error), Toast.LENGTH_SHORT).show();
            finish();
        }
        new ProcessRegisterSendOtp(ForgotPassword.this).execute();

        btnResend.setOnClickListener(view -> new ProcessRegisterSendOtp(ForgotPassword.this).execute());

        btnVerify.setOnClickListener(view -> {
            if (inputOtp.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_forgotpassword_1), Toast.LENGTH_SHORT).show();
            }
            else {
                new ProcessRegisterForgotPassword(ForgotPassword.this).execute();
            }
        });
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

    private static class ProcessRegisterForgotPassword extends AsyncTask<String,Void,Boolean> {

        private String inputOtp1;
        private String inputNewPassword1;
        private final WeakReference<ForgotPassword> activityReference;
        private ProgressDialog pDialog;

        // only retain a weak reference to the activity
        ProcessRegisterForgotPassword(ForgotPassword context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ForgotPassword activity = activityReference.get();
            if(activity==null){
                return;
            }
            pDialog = new ProgressDialog(activity, R.style.MyProgressDialog);
            pDialog.setMessage(activity.getResources().getString(R.string.java_forgotpassword_3));
            pDialog.setCancelable(false);
            pDialog.show();
            inputOtp1 = activity.inputOtp.getText().toString();
            inputNewPassword1 = activity.inputNewPassword.getText().toString();
        }

        @Override
        protected Boolean doInBackground(String... args) {

            JSONObject jsonIn = new JSONObject();
            final ForgotPassword activity = activityReference.get();
            if(activity==null){
                return null;
            }
            try {
                jsonIn.put("email",activity.inputEmail1);
                jsonIn.put("otp",inputOtp1);
                jsonIn.put("newpassword",inputNewPassword1);
                RequestQueue que = Volley.newRequestQueue(activity);
                String urlString = activity.getResources().getString(R.string.url_forgotpassword);
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, urlString, jsonIn,
                        response -> {
                            try {
                                String status = response.getString("status");
                                if (status.compareTo("ok") == 0) {
                                    Toast.makeText(activity, response.getString("message"), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(activity, Login.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    if (pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                    activity.startActivity(intent);
                                    activity.finish();
                                }
                                else if(status.compareTo("err") == 0){
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
                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(StartScreen.MAX_TIMEOUT,StartScreen.MAX_RETRY,StartScreen.BACKOFF_MULT));
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
        protected void onPostExecute(Boolean json) {

        }
    }

    private static class ProcessRegisterSendOtp extends AsyncTask<String,Void,JSONObject> {

        private final WeakReference<ForgotPassword> activityReference;
        private ProgressDialog pDialog;

        // only retain a weak reference to the activity
        ProcessRegisterSendOtp(ForgotPassword context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ForgotPassword activity = activityReference.get();
            if(activity==null){
                return;
            }
            pDialog = new ProgressDialog(activity, R.style.MyProgressDialog);
            pDialog.setMessage(activity.getResources().getString(R.string.java_forgotpassword_2));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            JSONObject jsonIn = new JSONObject();
            final ForgotPassword activity = activityReference.get();
            if(activity==null){
                return null;
            }
            try {
                jsonIn.put("email", activity.inputEmail1);
                RequestQueue que = Volley.newRequestQueue(activity);
                String urlString = activity.getResources().getString(R.string.url_sendotp);
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, urlString, jsonIn,
                        response -> {
                            try {
                                String status = response.getString("status");
                                if (status.compareTo("ok") == 0) {
                                    if (pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                    Toast.makeText(activity, response.getString("message"), Toast.LENGTH_LONG).show();
                                }
                                else if(status.compareTo("err") == 0){
                                    if (pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                    Toast.makeText(activity, response.getString("message"), Toast.LENGTH_LONG).show();
                                }
                                else{
                                    if (pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                    Toast.makeText(activity, activity.getResources().getString(R.string.connection_fail), Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch (JSONException e) {
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
                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(StartScreen.MAX_TIMEOUT,StartScreen.MAX_RETRY,StartScreen.BACKOFF_MULT));
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
        protected void onPostExecute(JSONObject json) {

        }
    }
}