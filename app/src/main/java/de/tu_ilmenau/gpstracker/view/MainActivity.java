package de.tu_ilmenau.gpstracker.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.R;
import de.tu_ilmenau.gpstracker.model.SpeedTestTotalResult;
import de.tu_ilmenau.gpstracker.presenter.GpsLocationViewModel;
import de.tu_ilmenau.gpstracker.utils.Utils;

/**
 * Main activity method to view model
 */
public class MainActivity extends AppCompatActivity implements OnClickListener {

    private Logger LOG = LoggerFactory.getLogger(MainActivity.class);
    private GpsLocationViewModel viewModel;
    //    private LocationManager locationManager = null;
//    private LocationListener locationListener = null;
    private Button btnGetLocation;
    private Button pushManually;
    private Button resetTime;
    private Switch pushContinuously;
    private Switch checkbox_http_use;
    private EditText timeout;
    private TextView xLocation;
    private TextView yLocation;
    private TextView downSpeedText;
    private TextView upSpeedText;
    private TextView deviceIdText;
    private EditText ipAddress;
    /*unique device id */
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

    /**
     * Check and ask permissions
     */
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : Config.REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, Config.REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[Config.REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(Config.REQUEST_CODE_ASK_PERMISSIONS, Config.REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    /**
     * Alert about missing permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Config.REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" +
                                permissions[index] + "' is not granted, exiting ...", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();
                break;
        }
    }

    /**
     * initialization process
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    private void initialize() {
        setContentView(R.layout.main);
        deviceId = Settings.Secure.getString(getApplicationContext()
                .getContentResolver(), Settings.Secure.ANDROID_ID);
        //if you want to lock screen for always Portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        ipAddress = (EditText) findViewById(R.id.editIpAddress);
        deviceIdText = (TextView) findViewById(R.id.deviceId);
        deviceIdText.setText(String.format("Device id: %s", deviceId));
        xLocation = (TextView) findViewById(R.id.X);
        yLocation = (TextView) findViewById(R.id.Y);
        downSpeedText = findViewById(R.id.speed);
        upSpeedText = findViewById(R.id.upSpeed);
        btnGetLocation = (Button) findViewById(R.id.btnLocation);
        btnGetLocation.setOnClickListener(this);
        pushManually = (Button) findViewById(R.id.pushManually);
        pushManually.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                pushManually();
            }
        });
        resetTime = (Button) findViewById(R.id.resetTime);
        resetTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimeout();
            }
        });
        resetTime.setOnClickListener(this);
        pushContinuously = (Switch) findViewById(R.id.pushContinuously);
        pushContinuously.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pushContListener(isChecked);
            }
        });
        viewModel = ViewModelProviders.of(this).get(GpsLocationViewModel.class);
        viewModel.init(this, deviceId);
        viewModel.locationStorage.observe(this, new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if (location != null) {
                    xLocation.setText(String.valueOf(location.getLatitude()));
                    yLocation.setText(String.valueOf(location.getLongitude()));
                }
            }
        });
        viewModel.speedStorage.observe(this, new Observer<SpeedTestTotalResult>() {
            @Override
            public void onChanged(SpeedTestTotalResult speed) {
                if (speed != null) {
                    downSpeedText.setText(String.format("Down speed: %s Kbs", speed.getDownSpeed()));
                    upSpeedText.setText(String.format("Up speed: %s Kbs", speed.getUpSpeed()));
                }
            }
        });
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private boolean setIp() {
        String ipAdd = ipAddress.getText().toString();
        if (!ipAdd.isEmpty() && Utils.isValidIPV4(ipAdd)) {
            viewModel.setIpV4(ipAdd);
            return true;
        } else {
            Utils.alertBox("Broker address", "Broker IP is not correct!", this);
            return false;
        }
    }

    private void pushContListener(boolean isChecked) {
        if (!setIp()) {
            return;
        }
        if (!isChecked && Utils.serviceIsRunning(this)) {
            viewModel.stopService();
        }
        if (isChecked) {
            String timeoutStr = timeout.getText().toString();
            try {
                int timeoutVal = 1; // default value
                if (!timeoutStr.isEmpty()) {
                    timeoutVal = Integer.parseInt(timeoutStr);
                }
                viewModel.startService(timeoutVal);
            } catch (Exception e) {
                pushContinuously.setChecked(false);
                Utils.alertBox("Incorrect time-out", "Time-out should be an integer value!", this);
            }
        }
    }

    private void resetTimeout() {
        timeout.setText("", TextView.BufferType.EDITABLE);
    }
   /* public void enableMqtt() {
        ipAdd = ipAddress.getText().toString();
        if (!ipAdd.isEmpty() && isValidIPV4(ipAdd)) { //todo !isEmpty()
            clientWrapper = MqttClientWrapper.getInstance(getApplicationContext(), ipAdd, buffer);
            clientWrapper.setHttpSender(httpPostReq);
            clientWrapper.connect();
            enableMqtt = true;
        } else {
            alertbox("Broker address", "Broker IP is not correct!");
        }
    }*/

    private void pushManually() {
        if (!setIp()) {
            return;
        }
        boolean flag = displayGpsStatus();
        if (!flag) {
            Utils.alertBox("GPS Status", "Your GPS is off!", this);
        }
        /*if (!enableMqtt) {
            alertbox("MQTT error", "You are not connected to MQTT!");
        }*/
        if (flag) {
            LOG.info("onClick");
            viewModel.pushLocation();
        }
    }


    /**
     * Method to Check GPS is enable or disable
     *
     * @return true id gps status enabled and false otherwise
     */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver, Config.LOC_MANAGER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }
}