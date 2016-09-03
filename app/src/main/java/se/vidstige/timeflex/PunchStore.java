package se.vidstige.timeflex;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PunchStore {
    private static final SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private Context context;

    public PunchStore(Context context) {
        this.context = context;
    }

    private String createFilename(int i) {
        return String.format("%05d.punch", i);
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

    public static void serialize(Punch punch, OutputStream outputStream) throws JSONException, IOException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("user", punch.getUser());
        jsonParam.put("workplace", punch.getWorkplace());
        jsonParam.put("direction", punch.getDirection() == Punch.Direction.IN ? "in" : "out");
        jsonParam.put("time", dt.format(punch.getTimestamp())); // TODO: Fix proper ISO 8601 time with TIMEZONE

        OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
        osw.write(jsonParam.toString());
        osw.flush();
        osw.close();
    }

    public void save(Punch punch) throws IOException, JSONException {
        FileOutputStream fileOutputStream = context.openFileOutput(getFilename(context), Context.MODE_PRIVATE);
        serialize(punch, fileOutputStream);
    }
}
