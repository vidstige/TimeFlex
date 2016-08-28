package se.vidstige.timeflex;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static class PostPunch extends AsyncTask<String, Void, Integer> {

        private String direction;
        private Date time;

        private final SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private Exception exception;

        private PostPunch(String direction, Date time) {
            this.direction = direction;
            this.time = time;
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

                //Create JSONObject here
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user", "vidstige");
                jsonParam.put("workplace", "volumental");
                jsonParam.put("direction", direction);
                jsonParam.put("time", dt.format(time)); // TODO: Fix proper ISO 8601 time with TIMEZONE

                // Send POST output.
                DataOutputStream printout = new DataOutputStream(urlConn.getOutputStream());
                printout.writeBytes(jsonParam.toString());
                printout.flush();
                printout.close();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button start_button = (Button) findViewById(R.id.start_button);
        start_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new PostPunch("in", new Date()).execute();
            }
        });

        final Button stop_button = (Button) findViewById(R.id.stop_button);
        stop_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new PostPunch("out", new Date()).execute();
            }
        });

        final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
    }
}
