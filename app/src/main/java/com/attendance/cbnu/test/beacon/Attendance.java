package com.attendance.cbnu.test.beacon;

import android.os.Parcel;
import android.os.Parcelable;

import com.attendance.cbnu.test.login.UserAccount;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class Attendance implements Parcelable {
    public static final Creator<Attendance> CREATOR = new Creator<Attendance>() {
        @Override
        public Attendance createFromParcel(Parcel in) {
            return new Attendance(in);
        }

        @Override
        public Attendance[] newArray(int size) {
            return new Attendance[size];
        }
    };
    private String idToken;     // Firebase uid (고유 토큰정보)
    private String emailId;     // 이메일 아이디
    private String number;      // 학번
    private String name;
    private long timestamp;

    public Attendance() {
    }

    public Attendance(UserAccount account) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);

        this.idToken = dateFormat.format(now) + "_" + account.getIdToken();
        this.emailId = account.getEmailId();
        this.number = account.getNumber();
        this.name = account.getName();
        this.timestamp = now.getTime();
    }

    protected Attendance(Parcel in) {
        idToken = in.readString();
        emailId = in.readString();
        number = in.readString();
        name = in.readString();
        timestamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idToken);
        dest.writeString(emailId);
        dest.writeString(number);
        dest.writeString(name);
        dest.writeLong(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
