package org.youth.overlook.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2015/5/5 0005.
 */
public class Action implements Serializable{

    int actionId;//活动ID，自动生成，自增
    String actionname;//活动名
    String builer;//活动创建者手机号
    String destination;//集合点
    Date actiontime;//约定时间
    Date buildingtime;//活动创建时间

    public Action() {
    }

    public Action(String actionname, Date actiontime, Date buildingtime, String builer, String destination) {
        this.actionname = actionname;
        this.actiontime = actiontime;
        this.buildingtime = buildingtime;
        this.builer = builer;
        this.destination = destination;
    }

    public int getActionId() {
        return actionId;
    }

    public Date getActiontime() {
        return actiontime;
    }

    public void setActiontime(Date actiontime) {
        this.actiontime = actiontime;
    }

    public Date getBuildingtime() {
        return buildingtime;
    }

    public void setBuildingtime(Date buildingtime) {
        this.buildingtime = buildingtime;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getActionname() {
        return actionname;
    }

    public void setActionname(String actionname) {
        this.actionname = actionname;
    }

    public String getBuiler() {
        return builer;
    }

    public void setBuiler(String builer) {
        this.builer = builer;
    }

    @Override
    public String toString() {
        return "Action{" +
                "actionId=" + actionId +
                ", actionname='" + actionname + '\'' +
                ", builer='" + builer + '\'' +
                ", destination='" + destination + '\'' +
                ", actiontime=" + actiontime +
                ", buildingtime=" + buildingtime +
                '}';
    }
}
