package se.vidstige.timeflex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

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
        WifiManager w = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        try {
            PrintWriter scan_results = new PrintWriter(context.openFileOutput(getFilename(context), Context.MODE_PRIVATE));
            for (ScanResult r : w.getScanResults()) {
                scan_results.println(r.SSID + ":" + r.level);
            }
            scan_results.close();
            Log.i("WiFiLogger", "...file written");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
