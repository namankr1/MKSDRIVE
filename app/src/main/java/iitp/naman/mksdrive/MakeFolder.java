package iitp.naman.mksdrive;

import android.app.Activity;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by naman on 19-12-2017.
 * Creates folder
 */

class MakeFolder {

    static Boolean makeFolder(Activity activity, String folderName){

        if(CheckPermission.checkPermission(activity)) {
            File folder = new File(folderName);
            if (folder.exists()) {
                return true;
            }
            else {
                try {
                    return folder.mkdirs();
                }
                catch (Exception ecp) {
                    ecp.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    static Boolean makeFile(Activity activity, String path, String fileName, byte[] data){

        if(!makeFolder(activity, path)){
            return false;
        }
        File file = new File(path, fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            fileOutputStream.write(data);
            fileOutputStream.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
