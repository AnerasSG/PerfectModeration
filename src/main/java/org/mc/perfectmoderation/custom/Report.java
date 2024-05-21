package org.mc.perfectmoderation.custom;

import java.sql.Timestamp;

public class Report {
    private int id;
    private String ip;
    private String reporter;
    private String target;
    private String description;
    private Timestamp dateOfIssue;

    public Report(int id, String ip, String reporter, String target, String description, Timestamp dateOfIssue) {
        this.id = id;
        this.ip = ip;
        this.reporter = reporter;
        this.target = target;
        this.description = description;
        this.dateOfIssue = dateOfIssue;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public String getReporter() {
        return reporter;
    }

    public String getTarget() {
        return target;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getDateOfIssue() {
        return dateOfIssue;
    }
}
