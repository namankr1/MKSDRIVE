package iitp.naman.mksdrive;

import android.content.SharedPreferences;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TextView;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by naman on 14-12-2017.
 * list of items in a folder
 */

public class FolderView extends AppCompatActivity {
    private GridView gridView;
    private String username;
    private String secureKey;
    private String folderID;
    private String mimeType;
    private String downloadPath;
    private String name;
    private String[] file_Id = new String[] {};
    private String[] file_mimeType = new String[] {};
    private String[] file_name = new String[] {};
    private String[] file_size = new String[] {};

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folderview);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            username = extras.getString("username");
            secureKey = extras.getString("secureKey");
            folderID = extras.getString("folderID");
            mimeType = extras.getString("mimeType");
            name = extras.getString("name");
            downloadPath = extras.getString("downloadPath");
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_error), Toast.LENGTH_SHORT).show();
            finish();
        }

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        gridView = findViewById(R.id.gridView1);
        new ProcessFetchFiles(FolderView.this).execute();
        gridView.setOnItemClickListener((parent, v, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), FolderView.class);
            intent.putExtra("username", username);
            intent.putExtra("secureKey", secureKey);
            intent.putExtra("folderID", ((TextView) v.findViewById(R.id.folderId)).getText() + "");
            intent.putExtra("mimeType", ((TextView) v.findViewById(R.id.folderMimeType)).getText() + "");
            intent.putExtra("name", ((TextView) v.findViewById(R.id.folderName)).getText() + "");
            intent.putExtra("downloadPath", downloadPath + File.separator + name);
            startActivity(intent);
        });
        gridView.setLongClickable(true);
        gridView.setOnItemLongClickListener((parent, v, position, id) -> {
            String mimeType1 = ((TextView) v.findViewById(R.id.folderMimeType)).getText() + "";
            if(mimeType1.equalsIgnoreCase("application/vnd.google-apps.folder")){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_folderview_4), Toast.LENGTH_SHORT).show();
            }
            else{
                Intent intent = new Intent(getApplicationContext(), FileRequestByEmail.class);
                intent.putExtra("username", username);
                intent.putExtra("secureKey", secureKey);
                intent.putExtra("folderID", ((TextView) v.findViewById(R.id.folderId)).getText() + "");
                intent.putExtra("mimeType", mimeType1);
                intent.putExtra("name", ((TextView) v.findViewById(R.id.folderName)).getText() + "");
                startActivity(intent);
            }
            return true;
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
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

    private static class ProcessFetchFiles extends AsyncTask<String,Void,Boolean> {
        private ProgressDialog pDialog;
        private final WeakReference<FolderView> activityReference;

        // only retain a weak reference to the activity
        ProcessFetchFiles(FolderView context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            FolderView activity = activityReference.get();
            if(activity==null){
                return;
            }
            pDialog = new ProgressDialog(activity, R.style.MyProgressDialog);
            pDialog.setMessage(activity.getResources().getString(R.string.java_folderview_3));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... args) {
            JSONObject jsonIn = new JSONObject();
            final FolderView activity = activityReference.get();
            if(activity==null){
                return null;
            }
            try {
                jsonIn.put("email",activity.username);
                jsonIn.put("secureKey",activity.secureKey);
                jsonIn.put("folderID",activity.folderID);
                jsonIn.put("mimeType",activity.mimeType);
                jsonIn.put("name",activity.name);
                RequestQueue que = Volley.newRequestQueue(activity);
                String urlString = activity.getResources().getString(R.string.url_getdata);
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, urlString, jsonIn,
                        response -> {
                            try {
                                String status = response.getString("status");
                                if (status.compareTo("okfolder") == 0) {
                                    JSONArray tempData =  response.getJSONArray("filelist");
                                    int len=tempData.length();
                                    activity.file_Id = new String[len];
                                    activity.file_mimeType = new String[len];
                                    activity.file_name = new String[len];
                                    activity.file_size = new String[len];
                                    for (int i = 0; i < len; i++) {
                                        activity.file_Id[i] = tempData.getJSONObject(i).getString("id");
                                        activity.file_mimeType[i] = tempData.getJSONObject(i).getString("mimeType");
                                        activity.file_name[i] = tempData.getJSONObject(i).getString("name");
                                        try{
                                            long s1 = tempData.getJSONObject(i).getLong("size");
                                            double s = Long.valueOf(s1).doubleValue();
                                            if(s>1024){
                                                s = s/1024;
                                                if(s>1024){
                                                    s = s/1024;
                                                    if(s>1024){
                                                        s = s/1024;
                                                        activity.file_size[i] = round(s,3) + " GB";
                                                    }
                                                    else{
                                                        activity.file_size[i] = round(s,2) + " MB";
                                                    }
                                                }
                                                else{
                                                    activity.file_size[i] = round(s,2) + " KB";
                                                }
                                            }
                                            else{
                                                activity.file_size[i] = round(s,2) + " B";
                                            }
                                        }
                                        catch (Exception e){
                                            activity.file_size[i] = "";
                                        }
                                    }
                                    activity.gridView.setAdapter(new AdapterFolderView(activity,activity.file_Id,activity.file_mimeType,activity.file_name,activity.file_size));
                                    if(len >0) {
                                        if (pDialog.isShowing()) {
                                            pDialog.dismiss();
                                        }
                                    }
                                    else {
                                        AlertDialogError(activity.getResources().getString(R.string.java_folderview_1));
                                    }
                                }
                                else if (status.compareTo("okfile") == 0) {
                                    String encodedData = response.getString("filedata");
                                    byte[] decodedData = Base64.decode(encodedData, Base64.NO_WRAP);
                                    if(!MakeFolder.makeFile(activity, activity.downloadPath , activity.name, decodedData)){
                                        AlertDialogError(activity.getResources().getString(R.string.java_folderview_5));
                                    }
                                    else{
                                        try {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            Uri fileUri = FileProvider.getUriForFile(activity, activity.getString(R.string.url_host), new File(activity.downloadPath, activity.name));
                                            intent.setDataAndType(fileUri, activity.mimeType);
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                            if (pDialog.isShowing()) {
                                                pDialog.dismiss();
                                            }
                                            activity.startActivity(intent);
                                            Toast.makeText(activity,"File saved at " + activity.downloadPath+ File.separator+ activity.name, Toast.LENGTH_SHORT).show();
                                            activity.finish();
                                        }
                                        catch (Exception e){
                                            e.printStackTrace();
                                            AlertDialogError(activity.getResources().getString(R.string.java_folderview_6) + " "+ activity.downloadPath+ File.separator+ activity.name);
                                        }
                                    }
                                }
                                else if(status.compareTo("err") == 0){
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
                            } catch (JSONException e) {
                                AlertDialogError(activity.getResources().getString(R.string.connection_fail));
                            }
                        }, error -> AlertDialogError(activity.getResources().getString(R.string.connection_fail)));
                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(StartScreen.MAX_TIMEOUT,StartScreen.MAX_RETRY,StartScreen.BACKOFF_MULT));
                que.add(jsonObjReq);

            } catch (JSONException e) {
                e.printStackTrace();
                AlertDialogError(activity.getResources().getString(R.string.connection_fail));
                return false;
            }
            return null;

        }
        @Override
        protected void onPostExecute(Boolean json) {

        }
        private void AlertDialogError(String resp){
            final FolderView activity = activityReference.get();
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
            final FolderView activity = activityReference.get();
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
