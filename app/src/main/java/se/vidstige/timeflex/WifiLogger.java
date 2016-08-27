package se.vidstige.timeflex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by vidstige on 23/08/16.
 */
public class WifiLogger extends BroadcastReceiver {
    public WifiLogger() {
        Log.i("WiFiLogger", "Created");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager w = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        Log.i("WiFiLogger", "SSIDs:");
        for (ScanResult r : w.getScanResults()) {
            Log.i("WiFiLogger", "SSID: " + r.SSID + ", level: " + r.level);
            //a.setText(r.SSID + "" + r.level + "\r\n");
        }
    }
}
