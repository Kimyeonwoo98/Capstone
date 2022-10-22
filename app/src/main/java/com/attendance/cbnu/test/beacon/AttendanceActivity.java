package com.attendance.cbnu.test.beacon;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.attendance.cbnu.test.BaseActivity;
import com.attendance.cbnu.test.login.UserAccount;
import com.yuliwuli.blescan.demo.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AttendanceActivity extends BaseActivity {

    //region Static
    private static final String ARG_TIME_STAMP = "ARG_TIME_STAMP";

    public static void startActivity(Context context, String beaconName, String beaconuuid,
                                     UserAccount user, ClassInformation classInformation, Long timestamp) {
        Intent intent = getSingleTopIntent(context, AttendanceActivity.class);
        intent.putExtras(getBundle(beaconuuid, beaconName, user, classInformation));
        intent.putExtra(ARG_TIME_STAMP, timestamp);

        context.startActivity(intent);
    }
    //endregion

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
    private long timestamp = 0L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            timestamp = getIntent().getLongExtra(ARG_TIME_STAMP, 0);
        }

        if (savedInstanceState != null) {
            timestamp = savedInstanceState.getLong(ARG_TIME_STAMP, 0);
        }

        if (beaconName == null || classInformation == null || user == null) {
            finish();
            return;
        }

        // 사용자가 학생인 경우
        setContentView(R.layout.activity_attendance);
        initForStudent();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(ARG_TIME_STAMP, timestamp);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void initForStudent() {
        findViewById(R.id.close_button).setOnClickListener(v -> onBackPressed());

        TextView messageTextView = findViewById(R.id.message_text_view);
        String dateTime = dateFormatter.format(new Date(timestamp));

        messageTextView.setText(String.format(Locale.getDefault(),
                "%s %s 학생\n%s호 %s\n%s\n출석완료",
                user.getNumber(), user.getName(), beaconName, classInformation.getName(), dateTime));
    }
}