package com.jp.onebuttonsendmail;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;


public class PreferenceFragmentUnit extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        //read current settings
        readPreference();
    }


    //listener and active read preference
    private SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {

                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    readPreference();
                }
    };

    //listener
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }



    //配列化してまとめたい
    public void readPreference() {

        //category2
        EditTextPreference mail_to = (EditTextPreference)getPreferenceScreen().findPreference("mail_to");
        mail_to.setSummary(mail_to.getText());

        EditTextPreference mail_to_name = (EditTextPreference)getPreferenceScreen().findPreference("mail_to_name");
        mail_to_name.setSummary(mail_to_name.getText());

        //category3
        EditTextPreference mail_from = (EditTextPreference)getPreferenceScreen().findPreference("mail_from");
        mail_from.setSummary(mail_from.getText());

        EditTextPreference firstname = (EditTextPreference)getPreferenceScreen().findPreference("firstname");
        firstname.setSummary(firstname.getText());

        EditTextPreference title_prefix = (EditTextPreference)getPreferenceScreen().findPreference("title_prefix");
        title_prefix.setSummary(title_prefix.getText());

        //category4
        EditTextPreference host = (EditTextPreference)getPreferenceScreen().findPreference("host");
        host.setSummary(host.getText());

        EditTextPreference connection_timeout = (EditTextPreference)getPreferenceScreen().findPreference("connection_timeout");
        connection_timeout.setSummary(connection_timeout.getText());

        EditTextPreference smtp_timeout = (EditTextPreference)getPreferenceScreen().findPreference("smtp_timeout");
        smtp_timeout.setSummary(smtp_timeout.getText());

        EditTextPreference mail_protocol = (EditTextPreference)getPreferenceScreen().findPreference("mail_protocol");
        mail_protocol.setSummary(mail_protocol.getText());

        EditTextPreference smtp_host = (EditTextPreference)getPreferenceScreen().findPreference("smtp_host");
        smtp_host.setSummary(smtp_host.getText());

        EditTextPreference smtp_port = (EditTextPreference)getPreferenceScreen().findPreference("smtp_port");
        smtp_port.setSummary(smtp_port.getText());
    }

}
