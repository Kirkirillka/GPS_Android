package de.tu_ilmenau.gpstracker.gps;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.fasterxml.jackson.core.JsonProcessingException;


import de.tu_ilmenau.gpstracker.R;
import de.tu_ilmenau.gpstracker.mqtt.MqttClientService;
import de.tu_ilmenau.gpstracker.mqtt.MqttClientWrapper;

public class GetCurrentLocation extends Activity implements OnClickListener {

    public static final String SERVICE_CLASSNAME = MqttClientService.class.getName();

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private boolean isPushCont;
    private int timeoutVal;

    private Button btnGetLocation;
    private Button pushManually;
    private Button resetTime;
    private Switch pushContinuously;
    private EditText timeout;
    private EditText xLocation;
    private EditText yLocation;

    private EditText ipAddress;
    //    private ProgressBar pb;
    private String deviceId;
    private MqttClientWrapper clientWrapper;
    private static final String TAG = "Debug";
    private Boolean flag = false;
    private boolean enableMqtt = false;
    private String ipAdd;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        deviceId = Settings.Secure.getString(getApplicationContext()
                .getContentResolver(), Settings.Secure.ANDROID_ID);

        //if you want to lock screen for always Portrait mode
        setRequestedOrientation(ActivityInfo
                .SCREEN_ORIENTATION_PORTRAIT);

//        pb = (ProgressBar) findViewById(R.id.progressBar1);
        ipAddress = (EditText) findViewById(R.id.editIpAddress);
//        pb.setVisibility(View.INVISIBLE);

        xLocation = (EditText) findViewById(R.id.X);
        yLocation = (EditText) findViewById(R.id.Y);


        btnGetLocation = (Button) findViewById(R.id.btnLocation);
        btnGetLocation.setOnClickListener(this);
        pushManually = (Button) findViewById(R.id.pushManually);
        pushManually.setOnClickListener(this);
        resetTime = (Button) findViewById(R.id.resetTime);
        resetTime.setOnClickListener(this);
        pushContinuously = (Switch) findViewById(R.id.pushContinuously);
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        pushContinuously.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && serviceIsRunning()) {
                    stopService();
                }
                if (isChecked) {
                    String timeoutStr = timeout.getText().toString();
                    if (!timeoutStr.isEmpty()) {
                        timeoutVal = Integer.parseInt(timeoutStr);
                    }
                    startService(timeoutVal);
                   /* locationListener = new MyLocationListener();
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            timeoutVal * 1000, 0.5f, locationListener);*/
                }
            }
        });
        timeout = (EditText) findViewById(R.id.timeout);
    }

    @SuppressLint("MissingPermission")
    public void enableMqtt() {
        ipAdd = ipAddress.getText().toString();
        if (!ipAdd.isEmpty()) { //todo !isEmpty()
            clientWrapper = MqttClientWrapper.getInstance(getApplicationContext(), ipAdd);
            clientWrapper.connect();
            enableMqtt = true;
        } else {
            alertbox("broker address", "broker ip is not set");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLocation:
                if (!enableMqtt) {
                    enableMqtt();
                }
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
            alertbox("Gps Status!!", "Your GPS is Off");
        }
        if (!enableMqtt) {
            alertbox("Mqtt error!!", "Your do not connected to mqtt");
        }
        if (flag && enableMqtt) {
            Log.v(TAG, "onClick");
            @SuppressLint("MissingPermission")
            Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            try {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                @SuppressLint("MissingPermission") WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo == null) {
                    alertbox("Network error", "Network connection is off");
                    return;
                }
                clientWrapper.publish(MessgeBuilder.buildMessage(loc, wifiInfo, deviceId));
                xLocation.setText(loc.getLatitude() + "");
                yLocation.setText(loc.getLongitude() + "");
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

//            pb.setVisibility(View.VISIBLE);
        }
    }


    /*----Method to Check GPS is enable or disable ----- */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.NETWORK_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    /*----------Method to create an AlertBox ------------- */
    protected void alertbox(String title, String mymessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your Device's GPS is Disable")
                .setCancelable(false)
                .setTitle("** Gps Status **")
                .setPositiveButton("Gps On",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // finish the current activity
                                // AlertBoxAdvance.this.finish();
                                Intent myIntent = new Intent(
                                        Settings.ACTION_SECURITY_SETTINGS);
                                startActivity(myIntent);
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
            xLocation.setText("");
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
            yLocation.setText(longitude);
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
}
