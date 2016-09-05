package se.vidstige.timeflex;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements ShiftStore.Listener {

    private ShiftStore shiftStore;
    private Button stop_button;
    private Button start_button;
    private TextView status_label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shiftStore = new ShiftStore(this, this.getPreferences(MODE_PRIVATE));
        final Context context = this;

        status_label = (TextView) findViewById(R.id.status);

        start_button = (Button) findViewById(R.id.start_button);
        start_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                shiftStore.startShift();
                Toast.makeText(context, "Punched in", Toast.LENGTH_LONG).show();
            }
        });

        stop_button = (Button) findViewById(R.id.stop_button);
        stop_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                shiftStore.endShift();
                Toast.makeText(context, "Punched out", Toast.LENGTH_LONG).show();
            }
        });

        final Button upload_button = (Button) findViewById(R.id.upload_button);
        upload_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                for (String filename : context.fileList()) {
                    try {
                        if (filename.endsWith(".shift")) {
                            URL url = new URL("http://timeflex-vidstige.rhcloud.com/api/shift/");
                            new PostJson(context, filename, url).execute();
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(context, "Uploaded", Toast.LENGTH_LONG).show();
            }
        });

        shiftStore.addListener(this);

        final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
    }

    @Override
    public void onStarted(ShiftStore store, Shift shift) {
        start_button.setEnabled(false);
        stop_button.setEnabled(true);
        status_label.setText(R.string.working_status);
    }

    @Override
    public void onEnded(ShiftStore store, Shift shift) {
        start_button.setEnabled(true);
        stop_button.setEnabled(false);
        status_label.setText(R.string.idle_status);
    }
}
