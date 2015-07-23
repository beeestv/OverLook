package org.youth.overlook.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bestv on 15/7/22.
 */
public class Location implements Serializable{
    int locationId;//位置ID，自动生成
    String phonenumber;//User主键，手机号
    double longitude;//经度
    double latitude;//纬度
    String address;//位置描述
    String nickname;//User nickname
    Date updateTime;//位置更新时间
    Date createTime;//位置创建时间

    public Location() {
    }

    public Location(String phonenumber, double longitude, double latitude, String address, String nickname, Date updateTime, Date createTime) {
        this.phonenumber = phonenumber;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.nickname = nickname;
        this.updateTime = updateTime;
        this.createTime = createTime;
    }

    public int getLocationId() {
        return locationId;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Location{" +
                "locationId=" + locationId +
                ", phonenumber='" + phonenumber + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", address='" + address + '\'' +
                ", nickname='" + nickname + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}
