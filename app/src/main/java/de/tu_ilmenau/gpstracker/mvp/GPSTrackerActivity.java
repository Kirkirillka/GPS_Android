package de.tu_ilmenau.gpstracker.mvp;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.R;
import de.tu_ilmenau.gpstracker.model.SpeedTestTotalResult;
import de.tu_ilmenau.gpstracker.utils.Utils;

/**
 * Main activity method to view model
 */
public class GPSTrackerActivity extends AppCompatActivity implements GPSTrackerView {

    private GPSTrackerPresenter presenter;

    private Button pushManually;
    private Button resetTime;
    private Switch pushContinousSwitch;
    private EditText timeout;
    private TextView xLocation;
    private TextView yLocation;
    private TextView downSpeedText;
    private TextView upSpeedText;
    private TextView deviceIdText;
    private EditText ipAddress;
    private Switch checkboxSpeedTest;
    private EditText speedTestIpAddress;
    /*unique device id */
    private String deviceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check if all permissions are granted
        checkPermissions();
        // initialize View
        init();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Config.REQUEST_CODE_ASK_PERMISSIONS) {
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
        }
    }

    /**
     * initialization process
     */
    @SuppressLint("HardwareIds")
    public void init() {
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
        timeout = findViewById(R.id.timeout);
        pushManually = (Button) findViewById(R.id.pushManually);
        pushManually.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onPushManually();
            }
        });
        resetTime = (Button) findViewById(R.id.resetTime);
        resetTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimeout();
            }
        });
        pushContinousSwitch = (Switch) findViewById(R.id.pushContinuously);
        pushContinousSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onTogglePushContinuousSwitch(isChecked);
            }
        });
        checkboxSpeedTest = findViewById(R.id.checkboxSpeedTest);
        speedTestIpAddress = (EditText) findViewById(R.id.speedTestIpAddress);

        // Bound Presenter and View
        presenter = new GPSTrackerPresenter(this, deviceId);
        presenter.viewIsReady();

    }

    private void onTogglePushContinuousSwitch(boolean isChecked) {
        presenter.toggleServiceDispatching();
    }

    public void resetTimeout() {
        timeout.setText("", TextView.BufferType.EDITABLE);
    }

    @Override
    public void updateLocation(Location location) {
        xLocation.setText(String.valueOf(location.getLatitude()));
        yLocation.setText(String.valueOf(location.getLongitude()));
    }

    @Override
    public void updateSpeedTestIPAddress() {

        String address = speedTestIpAddress.getText().toString();

        boolean res = presenter.setSpeedTestIPAddress(address);

        if (!res) {
            Utils.alertBox("Broker address", "IP is not correct!", this);
        }
    }

    @Override
    public void updateBrokerIPAddress() {

        String address = ipAddress.getText().toString();

        boolean res = presenter.setBrokerIPAddress(address);

        if (!res) {
            Utils.alertBox("Broker address", "Broker IP is not correct!", this);
        }
    }

    @Override
    public void updateSpeedRate(SpeedTestTotalResult speed) {

        if (speed != null) {
            downSpeedText.setText(String.format("Down speed: %s kB/s", speed.getDownSpeed()));
            upSpeedText.setText(String.format("Up speed: %s kB/s", speed.getUpSpeed()));
        }

    }

    @Override
    public void onPushManually() {

        // Check the current broker IP address validity
        String broker_ip = GPSTrackerModel.brokerIPAddress.getValue();

        // Check Broker IP Address
        if (!Utils.isValidIPV4(broker_ip)){
            Utils.alertBox("Broker IP address", "The provided IP address is incorrect!", this);

            return;
        }

        boolean isSpeedTestEnabled = GPSTrackerModel.isSpeedTestEnabled.getValue();
        if (isSpeedTestEnabled){

            String speedTestIPAddress = GPSTrackerModel.speedTestIPAddress.getValue();

            // Check Broker IP Address
            if (!Utils.isValidIPV4(speedTestIPAddress)){
                Utils.alertBox("SpeedTest IP address", "The provided IP address is incorrect!", this);
                return;
            }
        }
        // Check Broker IP Address
        if (!Utils.isValidIPV4(broker_ip)){
            Utils.alertBox("Broker IP address", "The provided IP address is incorrect!", this);

            return;
        }

        boolean res = presenter.pushLocation();

        if (!res){
            Utils.alertBox("Network error", "Network connection is off!", this);
        }

    }

    @Override
    public void togglePushing() {

    }
}