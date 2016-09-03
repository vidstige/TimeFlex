package se.vidstige.timeflex;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

class PostPunch extends AsyncTask<String, Void, Integer> {

    private final Punch punch;

    private Exception exception;

    public PostPunch(Punch punch) {
        this.punch = punch;
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

            PunchStore.serialize(punch, urlConn.getOutputStream());

            urlConn.connect();
            int responseCode = urlConn.getResponseCode();

            Log.i("MainActivity", "POST sent... " + responseCode);

        } catch (IOException e) {
            this.exception = e;
            e.printStackTrace();
        } catch (JSONException e) {
            this.exception = e;
            e.printStackTrace();
        }
        return Integer.valueOf(0);
    }

    protected void onPostExecute(Void result) {
        // TODO: check this.exception
    }
}