package com.jgcheca.sensorstracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ReadingSensors extends Service implements SensorEventListener, LocationListener {
    /** indicates how to behave if the service is killed */
    int mStartMode;
    /** interface for clients that bind */
    IBinder mBinder;
    /** indicates whether onRebind should be used */


    private Socket socket;

    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorGPS;

    private boolean gpsChecked ,acceChecked;

    private String SERVERIP = "";
    private int SERVERPORT;
    private Exception exception;
    private SharedPreferences preferences;
    private int raiserValueAcc=0;
    private int raiserValueGPS=0;
    private Integer seekbar_value=0;

    private LocationManager locationManager;
    private String provider;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful

        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        handleCommand(intent);
        return Service.START_NOT_STICKY;
    }

    public void handleCommand(Intent i) {
        Bundle b = i.getExtras();
        if(b.get("GPS").equals("ENABLED"))
            gpsChecked = true;
        else
            gpsChecked = false;
        if(b.get("ACCELEROMETER").equals("ENABLED"))
            acceChecked = true;
        else
            acceChecked = false;

        seekbar_value = b.getInt("SeekBar");
        System.out.println(seekbar_value);

        if(acceChecked)
            mSensorManager.registerListener(this,mSensorAccelerometer,setSensorSpeed());

        /*locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);*/
        // Initialize the location fields
        /*if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
            locationManager.requestLocationUpdates(provider, 1000, 1, this);
        } else {
            Toast.makeText(ReadingSensors.this, "Location unavailable", Toast.LENGTH_SHORT).show();

        }*/
        if(gpsChecked)
            setGPS();

    }

    public Integer setSensorSpeed(){
        double speed = 0;

        double speeed = (Integer.MAX_VALUE/100);

        double division = speed * seekbar_value;


        speed =  (Integer.MAX_VALUE/100)*(seekbar_value)-10;




        return (int) speed;

    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i("ReadingSensors", "Hello!!");
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        try {
            SERVERIP = preferences.getString("IpOpcion", "");
            SERVERPORT = Integer.parseInt(preferences.getString("PortOpcion", ""));
        }
        catch(Exception e){
            stopSelf();
        }

        new Thread()
        {
            public void run()
            {
                try
                {

                    socket = new Socket(SERVERIP,SERVERPORT);
                }
                catch (java.net.ConnectException e){

                    exception = e;
                    Toast.makeText(ReadingSensors.this, "Host Unreachable", Toast.LENGTH_SHORT).show();
                    stopSelf();
                    Log.e(MainActivity.class.toString(), e.getMessage(), e);
                }
                catch (Exception ex)
                {


                    Log.e(MainActivity.class.toString(), ex.getMessage(), ex);

                }
            }

        }.start();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (acceChecked)
            mSensorManager.unregisterListener(this, mSensorAccelerometer);
        if(gpsChecked)
            locationManager.removeUpdates(this);
        NotificationManager myNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myNotificationManager.cancel(0);
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        if(exception instanceof java.net.ConnectException)
            Toast.makeText(ReadingSensors.this, "Host Unreachable", Toast.LENGTH_SHORT).show();

        try {
            if (!socket.isClosed())
                socket.close();
        } catch (IOException e) {
            Toast.makeText(this, "Could not close socket correctly", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        final float alpha = (float) 0.8;

        double[] gravity = new double[3];
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];


        double[] linear_acceleration = new double[3];
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];


        System.out.println("x " + linear_acceleration[0] + " y " + linear_acceleration[1] + " z " + linear_acceleration[2]);
        new SendSensorsData(socket).execute("Value Accelerometer " + raiserValueAcc + "-  x: " + linear_acceleration[0] + "  y: " + linear_acceleration[1] + " z:  " + linear_acceleration[2] + "  "+"\n\n");
        raiserValueAcc++;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        int lat = (int) (location.getLatitude());
        int lng = (int) (location.getLongitude());

        new SendSensorsData(socket).execute("Value GPS " + raiserValueGPS + "-  Latitude: " + lat + "  Longitude: " + lng +"  "+"\n\n");
        raiserValueGPS++;

        System.out.println(raiserValueGPS);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:

                Toast.makeText(this, "Status Changed: Out of Service",
                        Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:

                Toast.makeText(this, "Status Changed: Temporarily Unavailable",
                        Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.AVAILABLE:

                Toast.makeText(this, "Status Changed: Available",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(ReadingSensors.this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(ReadingSensors.this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    public void setGPS(){
        if(locationManager==null)
            locationManager = (LocationManager) ReadingSensors.this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled=false;
        try{
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){}
        boolean network_enabled=false;
        try{
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){}

        if(!gps_enabled && !network_enabled){
            Toast.makeText(ReadingSensors.this, "Activate GPS",
                    Toast.LENGTH_SHORT).show();
        }
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);


        Criteria crit = new Criteria();
        crit.setPowerRequirement(Criteria.POWER_LOW);
        crit.setAccuracy(Criteria.ACCURACY_COARSE);
        String provider = locationManager.getBestProvider(crit, false);

        Criteria criteria = new Criteria();

        provider = locationManager.getBestProvider(criteria, false);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        Location location = locationManager.getLastKnownLocation(provider);


        locationManager.requestLocationUpdates(provider, Integer.parseInt(preferences.getString("MinTimeOpcion","")), Integer.parseInt(preferences.getString("MinDistanceOpcion","")), this);
        if (location!=null)
        {
            onLocationChanged(location);

        }


    }
}

class SendSensorsData extends AsyncTask<String, Integer, Void> {
    Socket socket;
    private Exception exception;

    public SendSensorsData(Socket socket){
        this.socket = socket;
    }

    protected Void doInBackground(String... urls) {
        try {
            DataOutputStream DOS = new DataOutputStream(socket.getOutputStream());
            DOS.writeUTF(urls[0]);
        } catch (Exception e) {
            this.exception = e;
            return null;
        }
        return null;
    }
}
