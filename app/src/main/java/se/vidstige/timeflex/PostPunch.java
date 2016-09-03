package se.vidstige.timeflex;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class PostPunch extends AsyncTask<String, Void, Integer> {


    private final Context context;
    private final String filename;

    private Exception exception;

    public PostPunch(Context context, String filename) {
        this.context = context;
        this.filename = filename;
    }

    private void copyStream(InputStream source, OutputStream target) throws IOException {
        byte[] buffer = new byte[1024];
        int len = source.read(buffer);
        while (len != -1) {
            target.write(buffer, 0, len);
            len = source.read(buffer);
        }
    }

    protected Integer doInBackground(String... urls) {
        try {
            URL url = new URL("http://timeflex-vidstige.rhcloud.com/api/punch/");
            HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-Type", "application/json");
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);

            // PunchStore.serialize(punch, urlConn.getOutputStream());
            copyStream(context.openFileInput(filename), urlConn.getOutputStream());

            urlConn.connect();
            int responseCode = urlConn.getResponseCode();

            Log.i("MainActivity", "POST sent... " + responseCode);
            if (responseCode == 200) {
                context.deleteFile(filename);
                Log.i("MainActivity", "...deleting " + filename);
            }

        } catch (IOException e) {
            this.exception = e;
            e.printStackTrace();
        }
        return Integer.valueOf(0);
    }

    protected void onPostExecute(Void result) {
        // TODO: check this.exception
    }
}