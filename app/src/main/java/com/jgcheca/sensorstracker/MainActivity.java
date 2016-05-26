package com.jgcheca.sensorstracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.jgcheca.sensorstracker.Preferences.PreferencesAct;
import com.mikepenz.aboutlibraries.Libs;
import com.rey.material.app.Dialog;
import com.rey.material.widget.Button;
import com.rey.material.widget.CheckBox;

import de.cketti.library.changelog.ChangeLog;


public class MainActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private CheckBox gpsBox;
    private CheckBox accBox;
    ChangeLog cl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getResources().getString(R.string.app_name));
            setSupportActionBar(toolbar);
        }
        //toolbar.setTitleTextColor(getResources().getColor(R.color.md_white_1000));

        Button startService_btn = (Button) findViewById(R.id.start_btn);
        Button stopService_btn = (Button) findViewById(R.id.stop_btn);
        gpsBox = (CheckBox) findViewById(R.id.checkbox_gps);
        accBox = (CheckBox) findViewById(R.id.checkbox_accelerometer);

        startService_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final Dialog mDialog = new Dialog(MainActivity.this);
                int slider_value = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getInt("SEEKBAR_VALUE", 50);
                String IP = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("IpOpcion", "");
                String Port = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("PortOpcion","");
               //Start Service
                Intent i= new Intent(MainActivity.this, ReadingSensors.class);
                i.putExtra("SeekBar",slider_value);
                // add data to the intent if you want


                if(!accBox.isChecked() && !gpsBox.isChecked()) {
                    mDialog
                            .title(getResources().getString(R.string.dialog_sensors_unavailable))
                            .positiveAction("OK")
                            .cancelable(true)
                            .elevation(20)
                            .show();

                    mDialog.positiveActionClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                        }
                    });
                }
                else {

                    if (Port.equals("") && IP.equals("")) {
                        mDialog
                                .title(getResources().getString(R.string.dialog_setIPPort))
                                .positiveAction("OK")
                                .cancelable(true)
                                .elevation(20)
                                .show();
                        mDialog.positiveActionClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                            }
                        });

                    } else if (Port.equals("")) {
                        mDialog
                                .title(getResources().getString(R.string.dialog_set_port))
                                .positiveAction("OK")
                                .cancelable(true)
                                .elevation(20)
                                .show();
                        mDialog.positiveActionClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                            }
                        });

                    } else if (IP.equals("")) {
                        mDialog
                                .title(getResources().getString(R.string.dialog_set_IP))
                                .positiveAction("OK")
                                .elevation(20)
                                .cancelable(true)
                                .show();
                        mDialog.positiveActionClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                            }
                        });
                    } else {


                        if (gpsBox.isChecked()) {

                            LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                            boolean enabled = service
                                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
                            if (!enabled) {


                                mDialog
                                        .title(getResources().getString(R.string.dialog_GPS_title))
                                        .positiveAction("OK")
                                        .negativeAction(getResources().getString(R.string.cancel))
                                        .elevation(20)
                                        .cancelable(true)
                                        .show();

                                mDialog.positiveActionClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(intent);
                                        mDialog.dismiss();
                                    }
                                });
                                mDialog.negativeActionClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        mDialog.dismiss();
                                    }
                                });
                            } else {
                                i.putExtra("GPS", "ENABLED");
                                if (accBox.isChecked())
                                    i.putExtra("ACCELEROMETER", "ENABLED");
                                else
                                    i.putExtra("ACCELEROMETER", "DISABLED");

                                startService(i);
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

                                Notification n = new NotificationCompat.Builder(MainActivity.this)
                                        .setContentTitle(getResources().getString(R.string.Notification_title))
                                        .setContentText(getResources().getString(R.string.Notification_subject))
                                        .setSmallIcon(R.mipmap.ic_launcher)

                                        .setAutoCancel(true)
                                        .build();

                                n.flags |= Notification.FLAG_NO_CLEAR;
                                NotificationManager notificationManager =
                                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                                notificationManager.notify(0, n);

                            }


                        } else if (accBox.isChecked()) {

                            i.putExtra("GPS", "DISABLED");
                            i.putExtra("ACCELEROMETER", "ENABLED");


                            startService(i);
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

                            Notification n = new NotificationCompat.Builder(MainActivity.this)
                                    .setContentTitle(getResources().getString(R.string.Notification_title))
                                    .setContentText(getResources().getString(R.string.Notification_subject))
                                    .setSmallIcon(R.mipmap.ic_launcher)

                                    .setAutoCancel(true)
                                    .build();

                            n.flags |= Notification.FLAG_NO_CLEAR;
                            NotificationManager notificationManager =
                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            notificationManager.notify(0, n);
                        }


                    }
                }






            }
        });

        stopService_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Stop Service
                if(!stopService(new Intent(MainActivity.this, ReadingSensors.class)))
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.stopServiceToast), Toast.LENGTH_SHORT).show();

            }
        });

        cl = new ChangeLog(this);
        if (cl.isFirstRun()) {
            cl.getLogDialog().show();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this, ReadingSensors.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
           /* Intent i = new Intent(MainActivity.this, MyPreferenceActivity.class);
            startActivity(i);*/
            Intent intent = new Intent(this, PreferencesAct.class);
            startActivity(intent);
           return true;

        }
        if(id == R.id.action_library){
            new Libs.Builder()
                    //Pass the fields of your application to the lib so it can find all external lib information
                    .withFields(R.string.class.getFields())
                    .withVersionShown(true)
                    .withLicenseShown(true)
                    .withAutoDetect(true)
                    .withActivityTitle(getResources().getString(R.string.action_library))
                    .withAboutAppName(getResources().getString(R.string.app_name))
                    .withAboutIconShown(true)
                    .withAboutVersionShown(true)
                    .withAboutDescription(getResources().getString(R.string.license_description)+"<br /><b>UCLM - ESI</b>")

                    .withAnimations(true)
                    .withAutoDetect(true)
                    .withActivityTheme(R.style.Base_Theme_AppCompat_Light)
                    .start(this);
            return true;
        }

        if(id == R.id.action_changelog){
            new ChangeLog(this).getFullLogDialog().show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
