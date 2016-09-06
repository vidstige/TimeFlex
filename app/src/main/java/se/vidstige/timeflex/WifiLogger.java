package se.vidstige.timeflex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WifiLogger extends BroadcastReceiver {
    public WifiLogger() {
        Log.i("WiFiLogger", "Created");
    }

    private static boolean anyMatches(List<ScanResult> scanResults, Set<String> ssids) {
        for (ScanResult scanResult : scanResults) {
            if (ssids.contains(scanResult.SSID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("WiFiLogger", "Scan results received");
        ShiftStore shiftStore = new ShiftStore(context);
        boolean idle = shiftStore.getActiveShift() == null;

        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (idle) {
            if (anyMatches(wifiManager.getScanResults(), shiftStore.getScanSet())) {
                shiftStore.startShift();
            }
        } else {
            // Is it time to check out?
            if (!anyMatches(wifiManager.getScanResults(), shiftStore.getScanSet())) {
                shiftStore.endShift();
            }
        }

        if (shiftStore.getFreezeRequested()) {
            Log.i("WiFiLogger", "Freezing ssids");
            HashSet<String> ssids = new HashSet<>();
            for (ScanResult scanResult : wifiManager.getScanResults()) {
                ssids.add(scanResult.SSID);
            }
            shiftStore.setScanSet(ssids);
            Toast.makeText(context, "Frozen", Toast.LENGTH_LONG).show();
        }
    }
}
