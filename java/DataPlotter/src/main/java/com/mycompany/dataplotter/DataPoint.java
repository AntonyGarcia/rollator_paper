package com.mycompany.dataplotter;

import java.util.Date;

public class DataPoint {

    private double acc_x;
    private double acc_y;
    private double acc_z;
    private double gy_x;
    private double gy_y;
    private double gy_z;
    private Date timestampDate;
    private String timestamp;

    public DataPoint(double acc_x, double acc_y, double acc_z, double gy_x, double gy_y, double gy_z, String timestamp) {
        this.acc_x = acc_x;
        this.acc_y = acc_y;
        this.acc_z = acc_z;
        this.gy_x = gy_x;
        this.gy_y = gy_y;
        this.gy_z = gy_z;
        this.timestamp = timestamp;
        this.timestampDate = parseTimestamp(Double.parseDouble(timestamp));
    }

    private Date parseTimestamp(double timestampDouble) {
        return new Date((long) (timestampDouble * 1000));
    }

    public double getAcc_x() {
        return acc_x;
    }

    public void setAcc_x(double acc_x) {
        this.acc_x = acc_x;
    }

    public double getAcc_y() {
        return acc_y;
    }

    public void setAcc_y(double acc_y) {
        this.acc_y = acc_y;
    }

    public double getAcc_z() {
        return acc_z;
    }

    public void setAcc_z(double acc_z) {
        this.acc_z = acc_z;
    }

    public double getGy_x() {
        return gy_x;
    }

    public void setGy_x(double gy_x) {
        this.gy_x = gy_x;
    }

    public double getGy_y() {
        return gy_y;
    }

    public void setGy_y(double gy_y) {
        this.gy_y = gy_y;
    }

    public double getGy_z() {
        return gy_z;
    }

    public void setGy_z(double gy_z) {
        this.gy_z = gy_z;
    }

    public Date getTimestampDate() {
        return timestampDate;
    }

    public void setTimestampDate(Date timestampDate) {
        this.timestampDate = timestampDate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
