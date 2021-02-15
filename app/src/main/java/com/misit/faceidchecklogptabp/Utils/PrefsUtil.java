package com.misit.faceidchecklogptabp.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.misit.faceidchecklogptabp.R;
import com.misit.faceidchecklogptabp.R;


public class PrefsUtil {
    public static final String FIRST_TIME = "first_time";
    public static final String IS_LOGGED_IN = "is_logged_in";
    public static final String USER_TOKEN = "user_token";
    public static final String USER_ID = "user_id";
    public static final String DEPT = "department";
    public static final String SECTION = "section";
    public static final String SHOW_ABSEN = "section";
    public static final String RULE = "rule";
    public static final String USER_EMAIL = "user_email";
    public static final String NAMA_LENGKAP = "nama_lengkap";
    public static final String NIK = "NIK";
    public static final String USER_NAME = "user_name";
    public static final String LEVEL = "level";
    public static final String USER_PHONE = "user_phone";
    public static final String CURRENT_LAT = "lat";
    public static final String CURRENT_LNG = "lng";
    public static final String LATEST_VERSION_NAME = "latest_version_name";
    public static final String LATEST_VERSION_CODE = "latest_version_code";
    public static final String LATEST_MEDIA_ID = "latest_media_id";
    public static final String LATEST_STORE_ID = "latest_store_id";
    public static final String LATEST_STORE_NAME = "latest_store_name";
    public static final String NUMBER_CUST_SUBMIT = "number_cust_submit";
    public static final String LAST_CUST_SUBMIT = "last_cust_submit";

    private static PrefsUtil instance;
    private Context context;

    public static void initInstance(final Context c) {
        if (instance == null) {
            instance = new PrefsUtil(c);
        }
    }

    public static PrefsUtil getInstance() {
        return instance;
    }

    private PrefsUtil(Context c) {
        this.context = c;
    }

    private String getKey(String mKey) {
        String key = "";
        Resources res = context.getResources();

        //if(FIRST_TIME.equals(mKey)) {
        //    key = res.getString(R.string.pref_first_time);
        //}

        return mKey;
    }

    public boolean setNumberState(String mKey, final int state) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.pref_key),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getKey(mKey), state);
            editor.commit();
            return true;
        } else {
            return false;
        }
    }

    public int getNumberState(String mKey, final int defValue) {
        int state = 0;

        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.pref_key),
                    Context.MODE_PRIVATE);
            state = sharedPref.getInt(getKey(mKey), defValue);
        }

        return state;
    }

    public boolean setNumberState(String mKey, final long state) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.pref_key),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(getKey(mKey), state);
            editor.commit();
            return true;
        } else {
            return false;
        }
    }

    public long getNumberState(String mKey, final long defValue) {
        long state = 0;

        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.pref_key),
                    Context.MODE_PRIVATE);
            state = sharedPref.getLong(getKey(mKey), defValue);
        }

        return state;
    }

    public boolean setStringState(String mKey, String state) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.pref_key),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getKey(mKey), state);
            editor.commit();
            return true;
        } else {
            return false;
        }
    }

    public String getStringState(String mKey, String defValue) {
        String state = "";

        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.pref_key),
                    Context.MODE_PRIVATE);
            state = sharedPref.getString(getKey(mKey), defValue);
        }

        return state;
    }

    public boolean setBooleanState(String mKey, boolean state) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.pref_key),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getKey(mKey), state);
            editor.commit();
            return true;
        } else {
            return false;
        }
    }

    public boolean getBooleanState(String mKey, boolean defValue) {
        boolean state = false;

        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.pref_key),
                    Context.MODE_PRIVATE);
            state = sharedPref.getBoolean(getKey(mKey), defValue);
        }

        return state;
    }
}