package org.youth.overlook.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bestv on 15/7/10.
 */
public class User implements Serializable{

    private String phonenumber;//手机号
    private String password;//密码
    private String nickname;//昵称
    private Date registerTime;//注册时间
    private Date lastLoginTime;//最后登录时间
    private Date contactUploadTime;//最近上传联系人日期
    private int contactAmount;//已上传联系人总数

    public User() {
    }

    public User(String phonenumber, String password, String nickname, Date registerTime, Date lastLoginTime, Date contactUploadTime, int contactAmount) {
        this.phonenumber = phonenumber;
        this.password = password;
        this.nickname = nickname;
        this.registerTime = registerTime;
        this.lastLoginTime = lastLoginTime;
        this.contactUploadTime = contactUploadTime;
        this.contactAmount = contactAmount;
    }

    public Date getContactUploadTime() {
        return contactUploadTime;
    }

    public void setContactUploadTime(Date contactUploadTime) {
        this.contactUploadTime = contactUploadTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Date registerTime) {
        this.registerTime = registerTime;
    }

    public int getContactAmount() {
        return contactAmount;
    }

    public void setContactAmount(int contactAmount) {
        this.contactAmount = contactAmount;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    @Override
    public String toString() {
        return "User{" +
                "phonenumber='" + phonenumber + '\'' +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", registerTime=" + registerTime +
                ", lastLoginTime=" + lastLoginTime +
                ", contactUploadTime=" + contactUploadTime +
                ", contactAmount=" + contactAmount +
                '}';
    }
}
