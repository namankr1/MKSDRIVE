package iitp.naman.mksdrive;

import android.app.ProgressDialog;
import android.net.Uri;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by naman on 19-12-2017.
 * First screen
 */

public class StartScreen extends AppCompatActivity {
    static int MAX_TIMEOUT = 20*1000;
    static int MAX_RETRY = 0;
    static float BACKOFF_MULT= 1f;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new NetCheckVersion(StartScreen.this).execute();
    }

    private static class NetCheckVersion extends AsyncTask<String, Void, Boolean> {
        private final WeakReference<StartScreen> activityReference;

        // only retain a weak reference to the activity
        NetCheckVersion(StartScreen context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            StartScreen activity = activityReference.get();
            if(activity==null){
                return;
            }
            activity.pDialog = new ProgressDialog(activity, R.style.MyProgressDialog);
            activity.pDialog.setMessage(activity.getResources().getString(R.string.java_please_wait));
            activity.pDialog.setCancelable(false);
            activity.pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... args) {
            final StartScreen activity = activityReference.get();
            if(activity==null){
                return null;
            }
            ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm==null){
                return false;
            }

            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL(activity.getResources().getString(R.string.url_home));
                    HttpURLConnection urlC = (HttpURLConnection) url.openConnection();
                    urlC.setConnectTimeout(3000);
                    urlC.connect();

                    if (urlC.getResponseCode() == 200) {
                        urlC.disconnect();
                        return true;
                    }
                    else{
                        urlC.disconnect();
                        return false;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean th) {
            final StartScreen activity = activityReference.get();
            if(activity==null){
                return;
            }
            if(th) {
                new ProcessRegisterCheckVersion(activity).execute();
            }
            else {
                AlertDialogNoInternet(activity);
            }
        }
    }

    private static class ProcessRegisterCheckVersion extends AsyncTask<String,Void,JSONObject> {
        private final WeakReference<StartScreen> activityReference;

        // only retain a weak reference to the activity
        ProcessRegisterCheckVersion(StartScreen context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            final StartScreen activity = activityReference.get();
            if(activity==null){
                return null;
            }
            try {
                JSONObject jsonIn = new JSONObject();
                RequestQueue que = Volley.newRequestQueue(activity);
                String urlString = activity.getResources().getString(R.string.url_checkversion);
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, urlString, jsonIn,
                        response -> {
                            try {
                                String status = response.getString("status");
                                if (status.compareTo("ok") == 0) {
                                    String serverVersion =  response.getString("message");
                                    String localVersion = activity.getResources().getString(R.string.version_info);
                                    if(serverVersion.equalsIgnoreCase(localVersion)){
                                        Intent intent = new Intent(activity, Login.class);
                                        activity.startActivity(intent);
                                        if (activity.pDialog.isShowing()) {
                                            activity.pDialog.dismiss();
                                        }
                                        activity.finish();
                                    }
                                    else{
                                        AlertDialogUpdate(activity);
                                    }
                                }else if(status.compareTo("err") == 0){
                                    AlertDialogNoInternet(activity);
                                }
                                else{
                                    AlertDialogNoInternet(activity);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                AlertDialogNoInternet(activity);
                            }
                        }, error -> {
                            error.printStackTrace();
                            AlertDialogNoInternet(activity);
                        });
                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(StartScreen.MAX_TIMEOUT,StartScreen.MAX_RETRY,StartScreen.BACKOFF_MULT));
                que.add(jsonObjReq);
                return jsonIn;
            }
            catch (Exception e) {
                e.printStackTrace();
                AlertDialogNoInternet(activity);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject json) {
        }
    }

    private static void AlertDialogNoInternet(final StartScreen activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialog);
        builder.setMessage(activity.getResources().getString(R.string.java_startscreen_4))
                .setCancelable(false)
                .setPositiveButton(activity.getResources().getString(R.string.java_startscreen_5), (dialog, id) -> new NetCheckVersion(activity).execute())
                .setNegativeButton(activity.getResources().getString(R.string.java_startscreen_6), (dialog, id) -> {
                    activity.finish();
                    new Exit(activity);
                });
        AlertDialog alert = builder.create();
        if (activity.pDialog.isShowing()) {
            activity.pDialog.dismiss();
        }
        alert.show();
    }

    private static void AlertDialogUpdate(final StartScreen activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialog);
        builder.setMessage(activity.getResources().getString(R.string.java_startscreen_1))
                .setCancelable(false)
                .setPositiveButton(activity.getResources().getString(R.string.java_startscreen_2), (dialog, id) -> {
                    String url1 = activity.getResources().getString(R.string.url_play_store);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    i.setData(Uri.parse(url1));
                    activity.startActivity(i);
                    activity.finish();
                })
                .setNegativeButton(activity.getResources().getString(R.string.java_startscreen_3), (dialog, id) -> {
                    activity.finish();
                    new Exit(activity);
                });
        AlertDialog alert = builder.create();
        if (activity.pDialog.isShowing()) {
            activity.pDialog.dismiss();
        }
        alert.show();
    }
}
