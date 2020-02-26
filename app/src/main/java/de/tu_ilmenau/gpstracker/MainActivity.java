package de.tu_ilmenau.gpstracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;

//import android.util.Log;

public class MainActivity extends Activity implements OnClickListener {

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private Logger LOG =
            LoggerFactory.getLogger(MainActivity.class);

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    private Button btnGetLocation;
    private Button pushManually;
    private Button resetTime;
    TextView downSpeedText;
    TextView upSpeedText;
    private TextView deviceIdText;

    private String deviceId;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();
        ContextInitializer ci = new ContextInitializer(loggerContext);
        try {
            ci.autoConfig();
        } catch (JoranException e) {
            e.printStackTrace();
        }
        LOG = LoggerFactory.getLogger(MainActivity.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onClick(View v) {
    }


    /*----------Method to create an AlertBox ------------- */
    protected void alertbox(String title, String mymessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mymessage)
                .setCancelable(false)
                .setTitle(title)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // finish the current activity
                                // AlertBoxAdvance.this.finish();
                                /*Intent myIntent = new Intent(
                                        Settings.ACTION_SECURITY_SETTINGS);
                                startActivity(myIntent);*/
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' is not granted, exiting ...", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();
                break;
        }


    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private void initialize() {
        setContentView(R.layout.main);
        deviceId = Settings.Secure.getString(getApplicationContext()
                .getContentResolver(), Settings.Secure.ANDROID_ID);

        //if you want to lock screen for always Portrait mode
        setRequestedOrientation(ActivityInfo
                .SCREEN_ORIENTATION_UNSPECIFIED);

        deviceIdText = (TextView) findViewById(R.id.deviceId);
        deviceIdText.setText(String.format("Device id: %s", deviceId));
        downSpeedText = findViewById(R.id.speed);
        upSpeedText = findViewById(R.id.upSpeed);

        btnGetLocation = (Button) findViewById(R.id.btnLocation);
        btnGetLocation.setOnClickListener(this);
        pushManually = (Button) findViewById(R.id.pushManually);
        pushManually.setOnClickListener(this);
        resetTime = (Button) findViewById(R.id.resetTime);
        resetTime.setOnClickListener(this);
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
    }
}