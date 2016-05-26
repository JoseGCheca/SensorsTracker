package com.jgcheca.sensorstracker.Preferences;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;


import com.jgcheca.sensorstracker.R;


public class PreferenceFrag extends android.preference.PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SeekBarPreference _seekBarPref;
    SharedPreferences prefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final Preference IPPref = findPreference("IpOpcion");
        final Preference PortPref = findPreference("PortOpcion");

        final Preference MinDistancePref = findPreference("MinDistanceOpcion");
        final Preference MinTimePref = findPreference("MinTimeOpcion");


        if(!MinDistancePref.getSharedPreferences().getString("MinDistanceOpcion","").equals(""))
            MinDistancePref.setSummary(MinDistancePref.getSharedPreferences().getString("MinDistanceOpcion", ""));

        if(!MinTimePref.getSharedPreferences().getString("MinTimeOpcion","").equals(""))
            MinTimePref.setSummary(MinTimePref.getSharedPreferences().getString("MinTimeOpcion", ""));

        if(!IPPref.getSharedPreferences().getString("IpOpcion","").equals(""))
            IPPref.setSummary(IPPref.getSharedPreferences().getString("IpOpcion", ""));

        if(!PortPref.getSharedPreferences().getString("PortOpcion","").equals(""))
            PortPref.setSummary(PortPref.getSharedPreferences().getString("PortOpcion",""));


        EditText editTextDistance = ((EditTextPreference) findPreference("MinDistanceOpcion"))
                .getEditText();
        EditText editTextTime = ((EditTextPreference) findPreference("MinTimeOpcion"))
                .getEditText();

        EditText editTextIP = ((EditTextPreference) findPreference("IpOpcion"))
                .getEditText();

        EditText editTextPort = ((EditTextPreference) findPreference("PortOpcion"))
                .getEditText();


        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
                    if (!resultingTxt.matches ("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                        return "";
                    } else {
                        String[] splits = resultingTxt.split("\\.");
                        for (int i=0; i<splits.length; i++) {
                            if (Integer.valueOf(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
                return null;
            }
        };

        editTextIP.setFilters(filters);
        editTextPort.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "65535")});

        editTextDistance.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "2147483647")});
        editTextTime.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "2147483647")});


        MinDistancePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                MinDistancePref.setSummary(newValue.toString());

                //do something
                return true;
            }

        });

        MinTimePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                MinTimePref.setSummary(newValue.toString());

                //do something
                return true;
            }

        });

        IPPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                IPPref.setSummary(newValue.toString());

                //do something
                return true;
            }

        });

        PortPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                PortPref.setSummary(newValue.toString());

                //do something
                return true;
            }

        });
        // Get widgets :
        _seekBarPref = (SeekBarPreference) this.findPreference("SEEKBAR_VALUE");

        // Set listener :
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // Set seekbar summary :
        int radius = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("SEEKBAR_VALUE", 50);
        _seekBarPref.setSummary(this.getString(R.string.settings_summary).replace("$1", "" + radius));


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Set seekbar summary :
        int radius = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("SEEKBAR_VALUE", 50);
        _seekBarPref.setSummary(this.getString(R.string.settings_summary).replace("$1", ""+radius));
    }
}