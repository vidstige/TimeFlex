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

    private static boolean anyMatches(Set<String> a, Set<String> b) {
        for (String ssid : a) {
            if (b.contains(a)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> toSet(List<ScanResult> scanResults) {
        HashSet<String> result = new HashSet<>();
        for (ScanResult scanResult : scanResults) {
            result.add(scanResult.SSID);
        }
        return result;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("WiFiLogger", "Scan results received");
        ShiftStore shiftStore = new ShiftStore(context);
        boolean idle = shiftStore.getActiveShift() == null;

        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        Set<String> scanResults = toSet(wifiManager.getScanResults());
        scanResults.remove("");
        Set<String> frozen = shiftStore.getScanSet();
        frozen.remove("");
        if (idle) {
            if (anyMatches(scanResults, frozen)) {
                shiftStore.startShift();
            }
        } else {
            // Is it time to check out?
            if (!anyMatches(scanResults, frozen)) {
                shiftStore.endShift();
            }
        }

        if (shiftStore.getFreezeRequested()) {
            Log.i("WiFiLogger", "Freezing ssids");
            HashSet<String> ssids = new HashSet<>();
            for (String ssid : scanResults) {
                ssids.add(ssid);
            }
            shiftStore.setScanSet(ssids);
            Toast.makeText(context, "Frozen", Toast.LENGTH_LONG).show();
        }
    }
}
