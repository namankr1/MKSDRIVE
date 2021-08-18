package iitp.naman.mksdrive;

import android.content.Context;
import android.content.Intent;

/**
 * Created by naman on 18-12-2017.
 * Exits the app
 */

class Exit {
    Exit(Context activity){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }
}
