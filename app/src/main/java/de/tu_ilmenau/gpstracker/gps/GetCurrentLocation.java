package de.tu_ilmenau.gpstracker.gps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
//import android.util.Log;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.R;
import de.tu_ilmenau.gpstracker.database.BufferValue;
import de.tu_ilmenau.gpstracker.database.SqliteBuffer;
import de.tu_ilmenau.gpstracker.dbModel.ClientDeviceMessage;
import de.tu_ilmenau.gpstracker.mqtt.MqttClientService;
import de.tu_ilmenau.gpstracker.mqtt.MqttClientWrapper;
import de.tu_ilmenau.gpstracker.storage.LastLocationStorage;

public class GetCurrentLocation extends Activity implements OnClickListener {

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private Logger LOG =
            LoggerFactory.getLogger(GetCurrentLocation.class);

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};


    public static final String SERVICE_CLASSNAME = MqttClientService.class.getName();
    static private final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
    static private Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private boolean isPushCont;
    private int timeoutVal;

    private Button btnGetLocation;
    private Button pushManually;
    private Button resetTime;
    private Switch pushContinuously;
    private Switch pushHttp;
    private EditText timeout;
    private TextView xLocation;
    private TextView yLocation;
    TextView downSpeedText;
    TextView upSpeedText;
    private TextView deviceIdText;

    private EditText ipAddress;
    //    private ProgressBar pb;
    private String deviceId;
    private MqttClientWrapper clientWrapper;
    private Boolean flag = false;
    private boolean enableMqtt = false;
    private String ipAdd;
    private SqliteBuffer buffer;
    private boolean httpPostReq;

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
        LOG = LoggerFactory.getLogger(GetCurrentLocation.class);
    }


    @SuppressLint("MissingPermission")
    public void enableMqtt() {
        ipAdd = ipAddress.getText().toString();
        if (!ipAdd.isEmpty() && isValidIPV4(ipAdd)) { //todo !isEmpty()
            clientWrapper = MqttClientWrapper.getInstance(getApplicationContext(), ipAdd, buffer);
            clientWrapper.setHttpSender(httpPostReq);
            clientWrapper.connect();
            enableMqtt = true;
        } else {
            alertbox("Broker address", "Broker IP is not correct!");
        }
    }

    private static boolean isValidIPV4(final String s) {
        return IPV4_PATTERN.matcher(s).matches();
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLocation:
                enableMqtt();
                break;
            case R.id.pushManually:
                pushManually();
                break;
            case R.id.resetTime:
                timeout.setText("", TextView.BufferType.EDITABLE);
                timeoutVal = 0;
                break;
        }
    }

    private void pushManually() {
        flag = displayGpsStatus();
        if (!flag) {
            alertbox("GPS Status", "Your GPS is off!");
        }
        if (!enableMqtt) {
            alertbox("MQTT error", "You are not connected to MQTT!");
        }
        if (flag) {
            LOG.info("onClick");
            pushLocation();
        }
    }

    private void bufferLocation(ClientDeviceMessage message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String payload = mapper.writeValueAsString(message);
            buffer.insertValue(payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    private void pushLocation() {
        //Download your image
        new AsyncCaller().execute();
    }

    /*----Method to Check GPS is enable or disable ----- */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        System.out.println(Config.LOC_MANAGER + "------------------------------------------------------------------------------");
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        Config.LOC_MANAGER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
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

    /*----------Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            LastLocationStorage instance = LastLocationStorage.getInstance();
            synchronized (instance) {
                if (instance.isChanged()) {
                    xLocation.setText(instance.getLattitude());
                    yLocation.setText(instance.getLongitude());
                    downSpeedText.setText(String.format("Down speed: %s Kbs", instance.getDownSpeed()));
                    upSpeedText.setText(String.format("Up speed: %s Kbs", instance.getUpSpeed()));
                    instance.setChanged(false);
                }
            }

           /* xLocation.setText("");
            yLocation.setText("");
//            pb.setVisibility(View.INVISIBLE);
            Toast.makeText(getBaseContext(), "Location changed : Lat: " +
                            loc.getLatitude() + " Lng: " + loc.getLongitude(),
                    Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            Log.v(TAG, longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.v(TAG, latitude);
            String s = longitude + "\n" + latitude;
            xLocation.setText(latitude);
            yLocation.setText(longitude);*/
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }


    private void startService(int timeoutVal) {

        final Intent intent = new Intent(this, MqttClientService.class);
        intent.putExtra("IP", ipAdd);
        intent.getStringExtra("IP");
        intent.putExtra("device", deviceId);
        intent.putExtra("timeout", timeoutVal);
        startService(intent);

    }

    private void stopService() {

        final Intent intent = new Intent(this, MqttClientService.class);
        stopService(intent);
    }

    private boolean serviceIsRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SERVICE_CLASSNAME.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @SuppressLint("NewApi")
    private class AsyncCaller extends BackgroundSpeedTester {
        private Location loc;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... params) {

            try {
                speedTest();
//                speed = SpeedTester.test();
                loc = locationManager.getLastKnownLocation(Config.LOC_MANAGER);
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiInfo = wifiManager.getConnectionInfo();
                ClientDeviceMessage message = MessageBuilder.buildMessage(loc, wifiInfo, deviceId, downSpeed, upSpeed);
                if (enableMqtt) {
                    clientWrapper.publish(message);
                } else {
                    bufferLocation(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                downSpeed = 0;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            xLocation.setText(loc.getLatitude() + "");
            yLocation.setText(loc.getLongitude() + "");
            if (wifiInfo == null) {
                alertbox("Network error", "Network connection is off!");
                return;
            }
            downSpeedText.setText(String.format("Down speed: %s Kbs", this.downSpeed));
            upSpeedText.setText(String.format("Up speed: %s Kbs", this.upSpeed));
            //this method will be running on UI thread
        }

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

    @SuppressLint("MissingPermission")
    private void initialize() {
        setContentView(R.layout.main);
        deviceId = Settings.Secure.getString(getApplicationContext()
                .getContentResolver(), Settings.Secure.ANDROID_ID);

        //if you want to lock screen for always Portrait mode
        setRequestedOrientation(ActivityInfo
                .SCREEN_ORIENTATION_PORTRAIT);

//        pb = (ProgressBar) findViewById(R.id.progressBar1);
        ipAddress = (EditText) findViewById(R.id.editIpAddress);
//        pb.setVisibility(View.INVISIBLE);
        deviceIdText = (TextView) findViewById(R.id.deviceId);
        deviceIdText.setText(String.format("Device id: %s", deviceId));
        xLocation = (TextView) findViewById(R.id.X);
        yLocation = (TextView) findViewById(R.id.Y);
        downSpeedText = findViewById(R.id.speed);
        upSpeedText = findViewById(R.id.upSpeed);

        btnGetLocation = (Button) findViewById(R.id.btnLocation);
        btnGetLocation.setOnClickListener(this);
        pushManually = (Button) findViewById(R.id.pushManually);
        pushManually.setOnClickListener(this);
        resetTime = (Button) findViewById(R.id.resetTime);
        resetTime.setOnClickListener(this);
        pushContinuously = (Switch) findViewById(R.id.pushContinuously);
        pushHttp = (Switch) findViewById(R.id.pushHttp);
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        pushContinuously.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && serviceIsRunning()) {
                    stopService();
                }
                if (isChecked) {
                    String timeoutStr = timeout.getText().toString();
                    try {
                        if (!timeoutStr.isEmpty()) {
                            timeoutVal = Integer.parseInt(timeoutStr);
                        }
                        startService(timeoutVal);
                    } catch (Exception e) {
                        pushContinuously.setChecked(false);
                        alertbox("Incorrect time-out", "Time-out should be an integer value!");
                    }
                }

            }
        });

        pushHttp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                httpPostReq = isChecked;
                if (clientWrapper != null) {
                    clientWrapper.setHttpSender(isChecked);
                }
            }
        });

        locationListener = new MyLocationListener();
        if (locationManager.getLastKnownLocation(Config.LOC_MANAGER) == null) {
            Config.LOC_MANAGER = LocationManager.NETWORK_PROVIDER;
            if (locationManager.getLastKnownLocation(Config.LOC_MANAGER) == null) {
                Config.LOC_MANAGER = LocationManager.PASSIVE_PROVIDER;
            }
        }
        locationManager.requestLocationUpdates(Config.LOC_MANAGER,
                2 * 1000, 0.1f, locationListener);
        timeout = (EditText) findViewById(R.id.timeout);
        buffer = new SqliteBuffer(this);
        if (buffer.getCount() > 0) {
            List<BufferValue> all = buffer.getAll();
            BufferValue bufferValue = all.get(all.size() - 1);
            try {
                ClientDeviceMessage message = new ObjectMapper().readValue(bufferValue.getValue(), ClientDeviceMessage.class);
                downSpeedText.setText(String.format("Down speed: %s Kbs", message.getPayload().getDownSpeed()));
                downSpeedText.setText(String.format("Up speed: %s Kbs", message.getPayload().getUpSpeed()));
                xLocation.setText(message.getLatitude() + "");
                yLocation.setText(message.getLongitude() + "");
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }
    }
}
