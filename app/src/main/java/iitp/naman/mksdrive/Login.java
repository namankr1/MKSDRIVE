package iitp.naman.mksdrive;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.ToggleButton;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import android.os.Bundle;
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

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by naman on 19-12-2017.
 * Login Screen
 */

public class Login extends AppCompatActivity {
    private EditText inputEmail;
    private EditText inputPassword;
    private CheckBox ch2;
    SharedPreferences sf;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button btnLogin;
        Button btnReset;
        ToggleButton tb;

        setContentView(R.layout.activity_login);
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(" ");
        }
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        sf = getSharedPreferences("cookie_data",MODE_PRIVATE);
        boolean cbf = sf.getBoolean("rm",false);

        if(!MakeFolder.makeFolder(Login.this, Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.app_name))){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_login_5), Toast.LENGTH_SHORT).show();
        }

        if(cbf) {
            Intent intent = new Intent(getApplicationContext(), Home.class);
            intent.putExtra("username", sf.getString("username",""));
            intent.putExtra("name", sf.getString("name",""));
            intent.putExtra("phone", sf.getString("phone",""));
            intent.putExtra("folderID", sf.getString("folderID",""));
            intent.putExtra("secureKey", sf.getString("secureKey",""));
            startActivity(intent);
            finish();
        }
        else {
            inputEmail = findViewById(R.id.email);
            inputPassword = findViewById(R.id.password);
            btnLogin = findViewById(R.id.login);
            btnReset = findViewById(R.id.forgotpassword);
            tb = findViewById(R.id.checkBox);
            ch2 = findViewById(R.id.checkBox2);

            inputEmail.setText(sf.getString("username",""));
            inputPassword.setText(sf.getString("password",""));
            ch2.setChecked(sf.getBoolean("checkbox",false));
            tb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    inputPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                else {
                    inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            });

            btnReset.setOnClickListener(view -> {
                Intent myIntent = new Intent(getApplicationContext(), PasswordReset.class);
                startActivity(myIntent);
            });

            btnLogin.setOnClickListener(view -> {
                if ((!inputEmail.getText().toString().equals("")) && (!inputPassword.getText().toString().equals(""))) {
                    new ProcessLogin(Login.this).execute();
                }
                else if ((!inputEmail.getText().toString().equals(""))) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_login_1), Toast.LENGTH_SHORT).show();
                }
                else if ((!inputPassword.getText().toString().equals(""))) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_login_2), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_login_3), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        finish();
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

    private static class ProcessLogin extends AsyncTask<String,Void,JSONObject> {

        private String inputPassword1,inputEmail1;
        private ProgressDialog pDialog;

        private final WeakReference<Login> activityReference;

        // only retain a weak reference to the activity
        ProcessLogin(Login context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Login activity = activityReference.get();
            if(activity==null){
                return;
            }
            pDialog = new ProgressDialog(activity, R.style.MyProgressDialog);
            pDialog.setMessage(activity.getResources().getString(R.string.java_login_7));
            pDialog.setCancelable(false);
            pDialog.show();
            inputEmail1 = activity.inputEmail.getText().toString();
            inputPassword1 = activity.inputPassword.getText().toString();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            final JSONObject jsonIn = new JSONObject();
            final Login activity = activityReference.get();
            if(activity==null){
                return null;
            }
            try {
                jsonIn.put("email", inputEmail1);
                jsonIn.put("password", inputPassword1);
                RequestQueue que = Volley.newRequestQueue(activity);
                String urlString = activity.getResources().getString(R.string.url_signin);
                final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, urlString, jsonIn,
                        response -> {
                            try {
                                String status = response.getString("status");
                                if (status.compareTo("ok") == 0) {
                                    JSONObject tempData =  response.getJSONObject("profile");
                                    String name = tempData.getString("name");
                                    String phone = tempData.getString("phone");
                                    String username = tempData.getString("email");
                                    String folderID = tempData.getString("folderID");
                                    String secureKey = tempData.getString("secureKey");

                                    SharedPreferences.Editor e = activity.sf.edit();
                                    e.clear();
                                    e.apply();
                                    e.commit();
                                    e.putBoolean("rm", true);
                                    e.putString("username", username);
                                    e.putString("name", name);
                                    e.putString("phone", phone);
                                    e.putString("folderID", folderID);
                                    e.putString("secureKey", secureKey);
                                    if (activity.ch2.isChecked()){
                                        e.putString("password", inputPassword1);
                                        e.putBoolean("checkbox", true);
                                    }
                                    else{
                                        e.putString("password", "");
                                        e.putBoolean("checkbox", false);
                                    }
                                    e.apply();
                                    e.commit();

                                    Intent intent = new Intent(activity, Home.class);
                                    intent.putExtra("username", username);
                                    intent.putExtra("name", name);
                                    intent.putExtra("phone", phone);
                                    intent.putExtra("folderID", folderID);
                                    intent.putExtra("secureKey", secureKey);
                                    if (pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                    activity.startActivity(intent);
                                    activity.finish();
                                }
                                else if (status.compareTo("err") == 0) {
                                    String resp = response.getString("message");
                                    if(resp.equalsIgnoreCase("User account is disabled")){
                                        Intent intent = new Intent(activity, ForgotPassword.class);
                                        intent.putExtra("username", inputEmail1);
                                        if (pDialog.isShowing()) {
                                            pDialog.dismiss();
                                        }
                                        activity.startActivity(intent);
                                        Toast.makeText(activity, activity.getResources().getString(R.string.java_login_4), Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        if (pDialog.isShowing()) {
                                            pDialog.dismiss();
                                        }
                                        Toast.makeText(activity, response.getString("message"), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else {
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
        protected void onPostExecute(JSONObject jsonIn) {

        }
    }
}