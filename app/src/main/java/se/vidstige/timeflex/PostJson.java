package se.vidstige.timeflex;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class PostJson extends AsyncTask<String, Void, Integer> {

    private final Context context;
    private final String filename;
    private final URL target;

    private Exception exception;

    public PostJson(Context context, String filename, URL target) {
        this.context = context;
        this.filename = filename;
        this.target = target;
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
            HttpURLConnection urlConnection = (HttpURLConnection)target.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            copyStream(context.openFileInput(filename), urlConnection.getOutputStream());

            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();

            Log.i("PostJson", "POST sent... " + responseCode);
            if (responseCode == 200) {
                context.deleteFile(filename);
                Log.i("PostJson", "...deleting " + filename);
            }

        } catch (IOException e) {
            this.exception = e;
            e.printStackTrace();
        }
        return 0;
    }

    protected void onPostExecute(Void result) {
        // TODO: check this.exception
    }
}