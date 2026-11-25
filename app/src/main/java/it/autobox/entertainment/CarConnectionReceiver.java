package it.autobox.entertainment;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;

/** Receives the protected Bluetooth ACL_CONNECTED broadcast from Android. */
public class CarConnectionReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        if(intent==null || !BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction()))
            return;
        if(!CarConnectionManager.bluetoothGranted(context))return;
        android.content.SharedPreferences prefs=context.getSharedPreferences(
                CarConnectionManager.PREFS,Context.MODE_PRIVATE);
        if(!prefs.getBoolean(CarConnectionManager.KEY_ENABLED,false))return;
        String selected=prefs.getString(CarConnectionManager.KEY_ADDRESS,"");
        if(selected.isEmpty())return;
        try {
            BluetoothDevice connected=Build.VERSION.SDK_INT>=33
                    ? intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE,BluetoothDevice.class)
                    : intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(connected==null || !selected.equals(connected.getAddress()))return;
            // A single connection can emit repeated broadcasts within seconds.
            long elapsed=SystemClock.elapsedRealtime();
            long last=prefs.getLong("last_prompt_elapsed",0L);
            if(last!=0 && elapsed>=last && elapsed-last<90_000L)return;
            prefs.edit().putLong("last_prompt_elapsed",elapsed).apply();
            CarConnectionManager.showChoiceNotification(context,
                    prefs.getString(CarConnectionManager.KEY_NAME,"Auto"),false);
        }catch(SecurityException ignored){}
    }
}
