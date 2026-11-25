package it.autobox.entertainment;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Opt-in car connection chooser on the Samsung PHONE.
 *
 * This class never attempts to replace the Android Auto projection UI on the
 * car display. Android Auto is allowed to continue operating independently.
 */
public final class CarConnectionManager {
    public static final String PREFS = "autobox_car_connection";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_ADDRESS = "selected_device";
    public static final String KEY_NAME = "selected_name";
    public static final String CHANNEL_ID = "autobox_car_connection_v1";
    public static final int NOTIFICATION_ID = 9024;
    public static final String ACTION_OPEN_AUTOBOX = "it.autobox.entertainment.OPEN_PHONE_HOME";
    public static final String ACTION_OPEN_CHOOSER = "it.autobox.entertainment.OPEN_CAR_CHOOSER";
    public static final String ACTION_KEEP_ANDROID_AUTO = "it.autobox.entertainment.KEEP_ANDROID_AUTO";
    private CarConnectionManager() {}

    public static boolean bluetoothGranted(Context context) {
        return Build.VERSION.SDK_INT < 31
                || context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean notificationGranted(Context context) {
        return Build.VERSION.SDK_INT < 33
                || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
    }

    public static String getSetupJson(Context context) {
        JSONObject object = new JSONObject();
        JSONArray devices = new JSONArray();
        try {
            object.put("bluetoothGranted", bluetoothGranted(context));
            object.put("notificationGranted", notificationGranted(context));
            object.put("enabled", context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
                    .getBoolean(KEY_ENABLED,false));
            object.put("name", context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
                    .getString(KEY_NAME,""));
            object.put("address", context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
                    .getString(KEY_ADDRESS,""));
            if (bluetoothGranted(context)) {
                BluetoothManager manager=(BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter adapter=manager==null?null:manager.getAdapter();
                if(adapter!=null && adapter.isEnabled()) {
                    for(BluetoothDevice device:adapter.getBondedDevices()) {
                        JSONObject item=new JSONObject();
                        item.put("name",device.getName()==null?"Bluetooth senza nome":device.getName());
                        item.put("address",device.getAddress());
                        devices.put(item);
                    }
                }
            }
            object.put("devices",devices);
        } catch(Exception ignored) {
            try{object.put("devices",devices);}catch(Exception alsoIgnored){}
        }
        return object.toString();
    }

    public static boolean saveDevice(Context context,String selected) {
        if(!bluetoothGranted(context)||selected==null||selected.isEmpty())return false;
        try {
            BluetoothManager manager=(BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter=manager==null?null:manager.getAdapter();
            if(adapter==null||!adapter.isEnabled())return false;
            for(BluetoothDevice device:adapter.getBondedDevices()) {
                if(selected.equals(device.getAddress())) {
                    String name=device.getName();
                    context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
                        .putString(KEY_ADDRESS,selected)
                        .putString(KEY_NAME,name==null?"Veicolo Bluetooth":name)
                        .putBoolean(KEY_ENABLED,true)
                        .apply();
                    return true;
                }
            }
        }catch(Exception ignored){}
        return false;
    }

    public static void disable(Context context){
        context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED,false).apply();
    }

    public static void clearChoiceNotification(Context context){
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager!=null)manager.cancel(NOTIFICATION_ID);
    }

    public static void showChoiceNotification(Context context,String deviceName,boolean test) {
        if(!notificationGranted(context))return;
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager==null)return;
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel channel=new NotificationChannel(CHANNEL_ID,
                "Scelta alla connessione della Kia",NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Scegli tra Android Auto originale e AutoBox sul telefono");
            manager.createNotificationChannel(channel);
        }
        Intent openBox=new Intent(context,MainActivity.class);
        openBox.setAction(ACTION_OPEN_AUTOBOX);
        openBox.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent box=PendingIntent.getActivity(context,11,openBox,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        Intent openChooser=new Intent(context,MainActivity.class);
        openChooser.setAction(ACTION_OPEN_CHOOSER);
        openChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent chooser=PendingIntent.getActivity(context,12,openChooser,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        Intent keep=new Intent(context,CarChoiceReceiver.class);
        keep.setAction(ACTION_KEEP_ANDROID_AUTO);
        PendingIntent standard=PendingIntent.getBroadcast(context,13,keep,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        String name=deviceName==null||deviceName.isEmpty()?"auto":deviceName;
        Notification.Builder notification=Build.VERSION.SDK_INT>=26?
                new Notification.Builder(context,CHANNEL_ID):new Notification.Builder(context);
        notification.setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle(test?"Prova scelta AutoBox":"Collegato a "+name)
            .setContentText("Scegli: Android Auto sulla Kia oppure AutoBox sul Samsung")
            .setContentIntent(chooser)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_RECOMMENDATION)
            .setPriority(Notification.PRIORITY_HIGH)
            .addAction(android.R.drawable.ic_menu_compass,"Android Auto normale",standard)
            .addAction(android.R.drawable.ic_menu_view,"AutoBox sul Samsung",box);
        try{manager.notify(NOTIFICATION_ID,notification.build());}
        catch(SecurityException ignored){}
    }
}
