package se.vidstige.timeflex;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private PunchStore punchStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        punchStore = new PunchStore(this);

        final Button start_button = (Button) findViewById(R.id.start_button);
        start_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Punch p = Punch.in(new Date());
                try {
                    punchStore.save(p);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //new PostPunch(p).execute();
            }
        });

        final Button stop_button = (Button) findViewById(R.id.stop_button);
        stop_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Punch p = Punch.out(new Date());
                try {
                    punchStore.save(p);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //new PostPunch(p).execute();
            }
        });

        final Button upload_button = (Button) findViewById(R.id.upload_button);
        final Context context = this;
        upload_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    for (String filename : punchStore.getAllFilenames()) {
                        new PostPunch(context, filename).execute();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
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
