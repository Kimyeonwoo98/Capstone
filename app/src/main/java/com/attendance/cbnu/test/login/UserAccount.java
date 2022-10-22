package com.attendance.cbnu.test.login;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * 사용자 게정 정보 모델 클래스
 * Parcelable implement 함. 이유는 다른 Activity 로 UserAccount 클래스 인스턴스를 넘겨주기 위함
 */
@IgnoreExtraProperties
public class UserAccount implements Parcelable {
    private String idToken;     //Firebase uid (고유 토큰정보)
    private String emailId;     //이메일 아이디
    private String password;    // 비밀번호
    private String Number;      // 학번
    private String Name;

    public UserAccount() {
    }

    protected UserAccount(Parcel in) {
        idToken = in.readString();
        emailId = in.readString();
        password = in.readString();
        Number = in.readString();
        Name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idToken);
        dest.writeString(emailId);
        dest.writeString(password);
        dest.writeString(Number);
        dest.writeString(Name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserAccount> CREATOR = new Creator<UserAccount>() {
        @Override
        public UserAccount createFromParcel(Parcel in) {
            return new UserAccount(in);
        }

        @Override
        public UserAccount[] newArray(int size) {
            return new UserAccount[size];
        }
    };

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        this.Number = number;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    /**
     * 학번이 6자리가 아닌 경우 학생, 6자리면 관리자
     */
    @Exclude
    public Boolean isStudent() {
        return Number.trim().length() != 6;
    }
}
