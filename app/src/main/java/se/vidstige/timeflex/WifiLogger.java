package se.vidstige.timeflex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WifiLogger extends BroadcastReceiver {
    private static final String TAG = "WiFiLogger";
    public WifiLogger() {
        Log.i(TAG, "Created");
    }
    private static final SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private static boolean anyMatches(Set<String> a, Set<String> b) {
        for (String element : a) {
            if (b.contains(element)) {
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

    private String createFilename(int i) {
        return String.format("%05d.scan", i);
    }

    private String getFilename(Context context) {
        List<String> files = Arrays.asList(context.fileList());
        String filename;
        int i = 0;
        do {
            filename = createFilename(i);
            i++;
        } while (files.contains(filename));
        return filename;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Scan results received");
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        Date time = new Date();

        // Save scan
        try {
            JSONObject root = new JSONObject();
            JSONArray ssids = new JSONArray();
            for (ScanResult scanResult : wifiManager.getScanResults()) {
                JSONObject ssid = new JSONObject();
                ssid.put("ssid", scanResult.SSID);
                ssid.put("level", scanResult.level);
                ssids.put(ssid);
            }
            root.put("time", dt.format(time)); // TODO: Fix proper ISO 8601 time with TIMEZONE
            root.put("access_points", ssids);

            FileOutputStream outputStream = context.openFileOutput(getFilename(context), Context.MODE_PRIVATE);

            OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
            Log.i(TAG, "Writing: " + root.toString());
            osw.write(root.toString());
            osw.flush();
            osw.close();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Compute shits on the fly
        ShiftStore shiftStore = new ShiftStore(context);
        boolean idle = shiftStore.getActiveShift() == null;

        Set<String> scanResults = toSet(wifiManager.getScanResults());
        scanResults.remove("");
        Set<String> frozen = shiftStore.getScanSet();
        frozen.remove("");
        Log.i(TAG, "Frozen: ");
        for (String ssid : frozen) {
            Log.i(TAG, "- " + ssid);
        }

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

        // Re-freeze
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
