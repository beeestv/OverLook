package org.youth.overlook.bean;

import java.io.Serializable;

/**
 * Created by bestv on 15/7/22.
 */
public class UserAction implements Serializable{
    int id;//自动生成ID
    String phonenumber;//user主键，手机号
    int actionId;//用户ID
    int didJoin;//flag，是否同意加入活动
    int locationId;//位置ID

    public UserAction() {
    }

    public UserAction(String phonenumber, int actionId, int didJoin, int locationId) {
        this.phonenumber = phonenumber;
        this.actionId = actionId;
        this.didJoin = didJoin;
        this.locationId = locationId;
    }

    public int getId() {
        return id;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public int getDidJoin() {
        return didJoin;
    }

    public void setDidJoin(int didJoin) {
        this.didJoin = didJoin;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    @Override
    public String toString() {
        return "UserAction{" +
                "id=" + id +
                ", phonenumber='" + phonenumber + '\'' +
                ", actionId=" + actionId +
                ", didJoin=" + didJoin +
                ", locationId=" + locationId +
                '}';
    }
}
