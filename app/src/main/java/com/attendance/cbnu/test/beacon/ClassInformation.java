package com.attendance.cbnu.test.beacon;

import android.os.Parcel;
import android.os.Parcelable;

public class ClassInformation implements Parcelable {

    /**
     * 각 수업마다 ID 는 전부 달라야 합니다.
     */
    private String id;

    /**
     * 수업 이름
     */
    private String name;

    /**
     * 수업 시간
     */
    private String time;

    /**
     * 총원
     */
    private int allMembers;


    public ClassInformation(String id, String name, String time, int allMembers) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.allMembers = allMembers;
    }

    protected ClassInformation(Parcel in) {
        id = in.readString();
        name = in.readString();
        time = in.readString();
        allMembers = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(time);
        dest.writeInt(allMembers);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ClassInformation> CREATOR = new Creator<ClassInformation>() {
        @Override
        public ClassInformation createFromParcel(Parcel in) {
            return new ClassInformation(in);
        }

        @Override
        public ClassInformation[] newArray(int size) {
            return new ClassInformation[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getAllMembers() {
        return allMembers;
    }

    public void setAllMembers(int allMembers) {
        this.allMembers = allMembers;
    }
}
