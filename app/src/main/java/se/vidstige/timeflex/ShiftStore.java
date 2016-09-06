package se.vidstige.timeflex;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ShiftStore implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final List<Listener> _listeners = new ArrayList<>();
    private final Context context;
    private final SharedPreferences preferences;
    private static final SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    public ShiftStore(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("Shifts", Context.MODE_PRIVATE);
        this.preferences.registerOnSharedPreferenceChangeListener(this);
    }

    public Set<String> getScanSet() {
        return preferences.getStringSet("scan", Collections.<String>emptySet());
    }

    public void setScanSet(Set<String> scanSet) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("scan", scanSet);
        editor.putBoolean("freeze_requested", false);
        editor.apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("shift_start".equals(key)) {
            Shift active = getActiveShift();
            if (active == null) {
                for (Listener listener : _listeners) {
                    listener.onEnded(this, null);
                }

            } else {
                for (Listener listener : _listeners) {
                    listener.onStarted(this, active);
                }
            }
        }
    }

    public void setFreezeRequested() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("freeze_requested", true);
        editor.apply();
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
    }

    public boolean getFreezeRequested() {
        return preferences.getBoolean("freeze_requested", false);
    }

    public interface Listener {
        void onStarted(ShiftStore store, Shift shift);
        void onEnded(ShiftStore store, Shift shift);
    }

    private static String createFilename(int i) {
        return String.format("%05d.shift", i);
    }

    private static String getNextFilename(Context context) {
        List<String> files = Arrays.asList(context.fileList());
        String filename;
        int i = 0;
        do {
            filename = createFilename(i);
            i++;
        } while (files.contains(filename));
        return filename;
    }

    public static void serialize(Shift shift, OutputStream outputStream) throws JSONException, IOException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("user", "vidstige");
        jsonParam.put("workplace", "volumental");
        jsonParam.put("start", dt.format(shift.getStart())); // TODO: Fix proper ISO 8601 time with TIMEZONE
        jsonParam.put("end", dt.format(shift.getEnd())); // TODO: Fix proper ISO 8601 time with TIMEZONE

        OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
        osw.write(jsonParam.toString());
        osw.flush();
        osw.close();
    }
    public void startShift() {
        if (getActiveShift() != null) throw new IllegalStateException("Shift already started");
        Shift active = new Shift(new Date());
        setActiveShift(active);
    }

    public void endShift() {
        if (getActiveShift() == null) throw new IllegalStateException("No shift started");
        try {
            Shift ended = getActiveShift();
            ended.setEnd(new Date());
            String filename = getNextFilename(context);
            serialize(ended, context.openFileOutput(filename, context.MODE_PRIVATE));
            setActiveShift(null);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addListener(Listener listener) {
        _listeners.add(listener);
    }

    public Shift getActiveShift() {
        String shift_start = preferences.getString("shift_start", null);
        if (shift_start == null) {
            return null;
        }
        try {
            return new Shift(dt.parse(shift_start));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setActiveShift(Shift shift) {
        SharedPreferences.Editor editor = preferences.edit();
        if (shift == null) {
            editor.remove("shift_start");
        } else {
            editor.putString("shift_start", dt.format(shift.getStart()));
        }
        editor.apply();
    }
}
