package org.mc.perfectmoderation.custom;

import java.sql.Timestamp;

public class Punishment {
    private String ip;
    private String punishment;
    private Timestamp dateOfIssue;
    private Timestamp endDate;
    private String issuedBy;

    public Punishment(String ip, String punishment, Timestamp dateOfIssue, Timestamp endDate, String issuedBy) {
        this.ip = ip;
        this.punishment = punishment;
        this.dateOfIssue = dateOfIssue;
        this.endDate = endDate;
        this.issuedBy = issuedBy;
    }

    public String getIp() {
        return ip;
    }

    public String getPunishment() {
        return punishment;
    }

    public Timestamp getDateOfIssue() {
        return dateOfIssue;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public String getIssuedBy() {
        return issuedBy;
    }
}

