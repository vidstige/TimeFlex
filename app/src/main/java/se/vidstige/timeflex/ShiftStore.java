package se.vidstige.timeflex;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShiftStore {
    private final List<Listener> _listeners = new ArrayList<>();
    private final Context context;
    private final SharedPreferences preferences;
    private static final SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    public ShiftStore(Context context, SharedPreferences preferences) {
        this.context = context;
        this.preferences = preferences;
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
        for (Listener listener : _listeners) {
            listener.onStarted(this, active);
        }
    }

    public void endShift() {
        if (getActiveShift() == null) throw new IllegalStateException("No shift started");
        try {
            Shift ended = getActiveShift();
            ended.setEnd(new Date());
            String filename = getNextFilename(context);
            serialize(ended, context.openFileOutput(filename, context.MODE_PRIVATE));
            setActiveShift(null);
            for (Listener listener : _listeners) {
                listener.onEnded(this, ended);
            }
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
