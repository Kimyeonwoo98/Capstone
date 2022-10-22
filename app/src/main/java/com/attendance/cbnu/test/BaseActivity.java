package com.attendance.cbnu.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.attendance.cbnu.test.beacon.ClassInformation;
import com.attendance.cbnu.test.login.UserAccount;

public class BaseActivity extends AppCompatActivity {

    private static final String ARG_BEACON_NAME = "ARG_BEACON_NAME";
    private static final String ARG_BEACON_UUID = "ARG_BEACON_UUID";
    private static final String ARG_USER = "ARG_USER";
    private static final String ARG_CLASS_INFORMATION = "ARG_CLASS_INFORMATION";


    protected String beaconUuid;
    protected String beaconName;
    protected UserAccount user;
    protected ClassInformation classInformation;

    protected static Bundle getBundle(String beaconUuid,
                                      String beaconName,
                                      UserAccount userAccount,
                                      ClassInformation classInformation) {
        Bundle bundle = new Bundle();

        bundle.putString(ARG_BEACON_UUID, beaconUuid);
        bundle.putString(ARG_BEACON_NAME, beaconName);
        bundle.putParcelable(ARG_USER, userAccount);
        bundle.putParcelable(ARG_CLASS_INFORMATION, classInformation);

        return bundle;
    }

    protected static Intent getSingleTopIntent(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            beaconName = getIntent().getStringExtra(ARG_BEACON_NAME);
            beaconUuid = getIntent().getStringExtra(ARG_BEACON_UUID);
            user = getIntent().getParcelableExtra(ARG_USER);
            classInformation = getIntent().getParcelableExtra(ARG_CLASS_INFORMATION);
        }

        if (savedInstanceState != null) {
            beaconName = savedInstanceState.getString(ARG_BEACON_NAME);
            beaconUuid = savedInstanceState.getString(ARG_BEACON_UUID);
            user = savedInstanceState.getParcelable(ARG_USER);
            classInformation = savedInstanceState.getParcelable(ARG_CLASS_INFORMATION);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(ARG_BEACON_NAME, beaconName);
        outState.putString(ARG_BEACON_UUID, beaconUuid);
        outState.putParcelable(ARG_USER, user);
        outState.putParcelable(ARG_CLASS_INFORMATION, classInformation);

        super.onSaveInstanceState(outState);
    }
}
