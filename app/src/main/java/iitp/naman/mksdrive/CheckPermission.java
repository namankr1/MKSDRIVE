package iitp.naman.mksdrive;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by naman on 19-12-2017.
 * Checks write permission.
 */

class CheckPermission {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    static Boolean checkPermission(final Activity activity){

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)){
            return false;
        }

        // request permission when it is not granted.
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialog);
                builder.setMessage(activity.getResources().getString(R.string.java_login_5))
                        .setCancelable(true)
                        .setPositiveButton(activity.getResources().getString(R.string.java_login_6), (dialog, id) -> ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE));
                AlertDialog alert = builder.create();
                alert.show();
            }
            else{
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            return false;
        }
        else {
            return true;
        }
    }
}
