package se.vidstige.timeflex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vidstige on 23/08/16.
 */
public class WifiLogger extends BroadcastReceiver {
    public WifiLogger() {
        Log.i("WiFiLogger", "Created");
    }

    private String createFilename(int i) {
        return String.format("%05d.scan", i);
    }

    private String getFilename(Context context) {
        List<String> files = Arrays.asList(context.fileList());
        String filename;
        int i = 0 ;
        do {
            filename = createFilename(i);
            i++;
        } while (files.contains(filename));
        return filename;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("WiFiLogger", "Scan results received");
        ShiftStore shiftStore = new ShiftStore(context);
        boolean idle = shiftStore.getActiveShift() == null;

        Set<String> scanSet = shiftStore.getScanSet();
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (idle) {
            // Check if it is time to start shift?
            for (ScanResult scanResult : wifiManager.getScanResults()) {
                if (scanSet != null && scanSet.contains(scanResult.SSID)) {
                    // TODO: Make the listener subscribe to shared prefs
                    shiftStore.startShift();
                }
            }
        } else {
            // Have we stored a scan list?
            if (scanSet == null) {
                HashSet<String> ssids = new HashSet<String>();
                for (ScanResult scanResult : wifiManager.getScanResults()) {
                    ssids.add(scanResult.SSID);
                }
                shiftStore.setScanSet(ssids);
            }

            // Is it time to check out?
            if (scanSet != null) {
                boolean found = false;
                for (ScanResult scanResult : wifiManager.getScanResults()) {
                    if (scanSet.contains(scanResult.SSID)) {
                        found = true;
                    }
                }
                if (!found) {
                    shiftStore.endShift();
                }
            }
        }
    }
}
