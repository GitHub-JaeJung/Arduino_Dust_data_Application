package com.example.dust_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {
    public static final int ALPHA_REQ_CODE = 100;
    public static final int ALPHA_DEVICE_CODE = 110;
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    String[] array;
    BluetoothSPP alpha_Bt;
    TextView Dust, H, T;
    ImageView imageView;

    float imgEvent = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alpha_Bt = new BluetoothSPP(this);
        setTitle("???????????? ?????????");
        imageView = (ImageView) findViewById(R.id.imageView2);

        setupBluetooth();

        alphaConnectState();

        // GPS ??????
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermisson();
        }

        final TextView textView_address = (TextView)findViewById(R.id.Gps);

        Button ShowLocationButton = (Button) findViewById(R.id.Gpsbtn);
        ShowLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsTracker = new GpsTracker(MainActivity.this);

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                String address = getCurrentAddress(latitude, longitude);
                textView_address.setText(address);

                Toast.makeText(MainActivity.this, "???????????? \n??????" + latitude + "\n?????? " + longitude, Toast.LENGTH_LONG).show();
            }
        });
    }
    public void onRequestPermissionsResult (int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????

            boolean check_result = true;

            // ?????? ???????????? ??????????????? ???????????????.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                ;
            } else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????. 2?????? ????????? ????????????.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(MainActivity.this, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermisson() {
        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. ?????? ???????????? ????????? ?????????(??????????????? 6.0 ?????? ????????? ????????? ???????????? ?????? ?????? ????????? ?????? ????????? ?????? ???????????????.)
            // 3. ?????? ?????? ????????? ??? ??????
        } else {  // 2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ????????? ????????????.
            // 3-1 ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(MainActivity.this, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????. ", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    public String getCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude,longitude,7);
        } catch (IOException ioException) {
            Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????. \n" + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public void alphaConnectState() {
        //????????? ??????
        alpha_Bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {

            TextView Dust = (TextView) findViewById(R.id.dust);
            TextView H = (TextView) findViewById(R.id.humd);
            TextView T = (TextView) findViewById(R.id.tmep);


            @Override
            public void onDataReceived(byte[] data, String message) {
                try {

                array = message.split(",");   // ,??? ??????



                Dust.setText(array[0]);
                H.setText(array[1]);
                T.setText(array[2]);

                imgEvent = Float.parseFloat(array[0]);
                if(imgEvent > 0 && imgEvent < 30.00 ){
                    imageView.setImageResource(R.drawable.smile);
                } else if(imgEvent > 30.00 && imgEvent < 80.00){
                    imageView.setImageResource(R.drawable.blank);
                } else if(imgEvent > 81.00 && imgEvent < 150.00){
                    imageView.setImageResource(R.drawable.sad);
                }else if(imgEvent > 151.00){
                    imageView.setImageResource(R.drawable.angry);
                }

            }
            catch (NumberFormatException e){
                e.printStackTrace();
            }
            }

        });
        alpha_Bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            @Override
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Second Bluetooth Setting
    public void setup() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alpha_Bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if (!alpha_Bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, ALPHA_REQ_CODE);
        } else {
            if (!alpha_Bt.isServiceAvailable()) {
                alpha_Bt.setupService();
                alpha_Bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID??? ??????????????? ?????? ??????
                setup();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ALPHA_DEVICE_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    alpha_Bt.connect(data);
                }
                break;
            case ALPHA_REQ_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    alpha_Bt.setupService();
                    alpha_Bt.startService(BluetoothState.DEVICE_OTHER);
//                setup();
                } else {
                    Toast.makeText(getApplicationContext()
                            , "Bluetooth was not enabled."
                            , Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

        }
        /*switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                if (checkLocationServicesStatus()){
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS ????????? ?????????");
                        checkRunTimePermisson();
                        return;
                    }
                }
                break;
        }*/
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(locationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent(getApplicationContext(), DeviceList.class);
        switch (id) {
            case R.id.action_scan1:
                if (alpha_Bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    alpha_Bt.disconnect();
                } else {
                    startActivityForResult(intent, ALPHA_DEVICE_CODE);

                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void setupBluetooth() {
        if (!alpha_Bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}