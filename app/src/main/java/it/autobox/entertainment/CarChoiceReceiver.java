package it.autobox.entertainment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Private notification action: leave the car's normal Android Auto unchanged. */
public class CarChoiceReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        if(intent!=null &&
           CarConnectionManager.ACTION_KEEP_ANDROID_AUTO.equals(intent.getAction())){
            CarConnectionManager.clearChoiceNotification(context);
        }
    }
}
