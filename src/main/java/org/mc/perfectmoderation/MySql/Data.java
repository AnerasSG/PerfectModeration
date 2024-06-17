package org.mc.perfectmoderation.MySql;

import org.mc.perfectmoderation.PerfectModeration;
import org.mc.perfectmoderation.custom.Punishment;
import org.mc.perfectmoderation.custom.Report;

import java.sql.*;
import java.util.*;

public class Data {
    PerfectModeration plugin;
    public Data(PerfectModeration perfectModeration) {
        plugin = perfectModeration;
    }
    public void createPunishmentsTable(){
        PreparedStatement preparedStatement;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Punishments "
                    + "(IP VARCHAR(100), Punishment VARCHAR(100), Date_of_issue TIMESTAMP DEFAULT CURRENT_TIMESTAMP, End_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, Issued VARCHAR(100))");
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void addPunishment(String ip, String punishment, Timestamp dateOfIssue, Timestamp endDate, String Name) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("INSERT INTO Punishments (IP, Punishment, Date_of_issue, End_date, Issued) VALUES (?,?,?,?,?)");
            ps.setString(1, ip);
            ps.setString(2, punishment);
            ps.setTimestamp(3, dateOfIssue);
            ps.setTimestamp(4, endDate);
            ps.setString(5, Name);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void removeLastPunishment(String ip) {
        PreparedStatement selectStatement = null;
        PreparedStatement deleteStatement = null;
        ResultSet resultSet = null;

        try {
            selectStatement = plugin.SQL.getConnection().prepareStatement("SELECT MAX(Date_of_issue) AS last_date FROM Punishments WHERE IP = ?");
            selectStatement.setString(1, ip);
            resultSet = selectStatement.executeQuery();

            Timestamp lastDate = null;
            if (resultSet.next()) {
                lastDate = resultSet.getTimestamp("last_date");
            }

            if (lastDate != null) {
                deleteStatement = plugin.SQL.getConnection().prepareStatement("DELETE FROM Punishments WHERE IP = ? AND Date_of_issue = ?");
                deleteStatement.setString(1, ip);
                deleteStatement.setTimestamp(2, lastDate);
                deleteStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
        public List<Punishment> getPunishmentsByIP(String ip) {
        List<Punishment> punishments = new ArrayList<>();
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM Punishments WHERE IP = ?");
            ps.setString(1, ip);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Punishment punishment = new Punishment(
                        rs.getString("IP"),
                        rs.getString("Punishment"),
                        rs.getTimestamp("Date_of_issue"),
                        rs.getTimestamp("End_date"),
                        rs.getString("Issued")
                );
                punishments.add(punishment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createWarnTable() {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Warn_Table "
                    + "(UUID VARCHAR(100), NAME VARCHAR(100), Reason VARCHAR(100), Issue_Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, Expiry_Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, Time BIGINT, PRIMARY KEY (UUID, Issue_Date))");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addWarn(UUID uuid, String name, String reason, Timestamp expiryDate, long time) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("INSERT INTO Warn_Table (UUID, NAME, Reason, Issue_Date, Expiry_Date, Time) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?)");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, reason);
            preparedStatement.setTimestamp(4, expiryDate);
            preparedStatement.setLong(5, time);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public long getTimeFromWarnTable(UUID uuid) {
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT Time FROM Warn_Table WHERE UUID = ?");
            preparedStatement.setString(1, uuid.toString());
            resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                return resultSet.getLong("Time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public void removeWarnFromWarnTable(UUID uuid) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("DELETE FROM Warn_Table WHERE UUID = ?");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean hasWarnings(UUID uuid) {
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT COUNT(*) AS count FROM Warn_Table WHERE UUID = ?");
            preparedStatement.setString(1, uuid.toString());
            resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                int count = resultSet.getInt("count");
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public int getWarnCount(UUID uuid) {
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT COUNT(*) AS total_warns FROM Warn_Table WHERE UUID = ?");
            preparedStatement.setString(1, uuid.toString());
            resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                return resultSet.getInt("total_warns");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Возвращаем 0, если произошла ошибка или не найдено ни одной записи для указанного UUID
    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createBanTable() {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS TempBan_Table "
                    + "(NAME VARCHAR(100) PRIMARY KEY, UUID VARCHAR(100), Time BIGINT, Reason VARCHAR(100), Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, Operator VARCHAR(100))");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addPlayerToBanTable(String name, UUID uuid, long time, String reason, String operator) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "INSERT INTO TempBan_Table (NAME, UUID, Time, Reason, Operator) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.setLong(3, time);
            preparedStatement.setString(4, reason);
            preparedStatement.setString(5, operator);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public String getDateFromBanTable(UUID playerUUID) {
        String endDate = null;
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "SELECT Date FROM TempBan_Table WHERE UUID = ?");
            preparedStatement.setString(1, playerUUID.toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                endDate = resultSet.getString("Date");
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return endDate;
    }

    public String getOperatorFromBanTable(UUID playerUUID) {
        String operator = null;
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "SELECT Operator FROM TempBan_Table WHERE UUID = ?");
            preparedStatement.setString(1, playerUUID.toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                operator = resultSet.getString("Operator");
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return operator;
    }
    public boolean isPlayerInBanTable(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "SELECT * FROM TempBan_Table WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public long getTimeFromBanTable(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "SELECT Time FROM TempBan_Table WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("Time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getReasonFromBanTable(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "SELECT Reason FROM TempBan_Table WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("Reason");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void removePlayerFromBanTable(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "DELETE FROM TempBan_Table WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createMuteTable() {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS TempMute_Table "
                    + "(NAME VARCHAR(100) PRIMARY KEY, UUID VARCHAR(100), Time BIGINT, Reason VARCHAR(100))");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addPlayerToMuteTable(String name, UUID uuid, long time, String reason) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "INSERT INTO TempMute_Table (NAME, UUID, Time, Reason) VALUES (?, ?, ?, ?)");
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.setLong(3, time);
            preparedStatement.setString(4, reason);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean isPlayerInMuteTable(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "SELECT * FROM TempMute_Table WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public long getTimeFromMuteTable(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "SELECT Time FROM TempMute_Table WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("Time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getReasonFromMuteTable(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "SELECT Reason FROM TempMute_Table WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("Reason");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void removePlayerFromMuteTable(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "DELETE FROM TempMute_Table WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createAccountsTable(){
        PreparedStatement preparedStatement;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS Accounts " +
                            "(IP VARCHAR(100), Name VARCHAR(100), UUID VARCHAR(100), serverName VARCHAR(100), " +
                            "vanished BOOLEAN DEFAULT FALSE)"
            );
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void createPlayer(String ip, String name, UUID uuid, String serverName) {
        if (playerExists(ip, name, uuid)) {
            return;
        }

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "INSERT INTO Accounts (IP, Name, UUID, serverName, vanished) VALUES (?, ?, ?, ?, ?)"
            );
            preparedStatement.setString(1, ip);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, uuid.toString());
            preparedStatement.setString(4, serverName);
            preparedStatement.setBoolean(5, false);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean isVanished(UUID uuid) {
        PreparedStatement preparedStatement;
        boolean vanished = false;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "SELECT vanished FROM Accounts WHERE UUID = ?"
            );
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                vanished = resultSet.getBoolean("vanished");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vanished;
    }
    public void setVanished(UUID uuid, boolean vanished) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement(
                    "UPDATE Accounts SET vanished = ? WHERE UUID = ?"
            );
            preparedStatement.setBoolean(1, vanished);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public String getServerNameByPlayerName(String playerName){
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        String serverName = null;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT serverName FROM Accounts WHERE Name = ?");
            preparedStatement.setString(1, playerName);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                serverName = resultSet.getString("serverName");
            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
        return serverName;
    }
    public String getIPByUUID(UUID uuid) {
        String ip = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT IP FROM Accounts WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                ip = resultSet.getString("IP");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ip;
    }
    private boolean playerExists(String ip, String name, UUID uuid) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT * FROM Accounts WHERE IP = ? AND Name = ? AND UUID = ?");
            preparedStatement.setString(1, ip);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, uuid.toString());
            resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void updateServerName(UUID uuid, String newServerName) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("UPDATE Accounts SET serverName = ? WHERE UUID = ?");
            preparedStatement.setString(1, newServerName);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getNamesByIP(String ip) {
        List<String> names = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT Name FROM Accounts WHERE IP = ?");
            preparedStatement.setString(1, ip);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                names.add(resultSet.getString("Name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createReportsTable(){
        PreparedStatement preparedStatement;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Reports "
                    + "(ID INT AUTO_INCREMENT PRIMARY KEY, IP VARCHAR(100), Reporter VARCHAR(100), Target VARCHAR(100), Description TEXT, ServerName TEXT, Date_of_issue TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void addReport(String ip, String reporter, String target, String description, String serverName, Timestamp dateOfIssue) {
        try {
            int nextId = getNextAvailableId();
            if (nextId != -1) {
                PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("INSERT INTO Reports (ID, IP, Reporter, Target, Description, ServerName, Date_of_issue) VALUES (?,?,?,?,?,?,?)");
                ps.setInt(1, nextId);
                ps.setString(2, ip);
                ps.setString(3, reporter);
                ps.setString(4, target);
                ps.setString(5, description);
                ps.setString(6, serverName);
                ps.setTimestamp(7, dateOfIssue);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("INSERT INTO Reports (IP, Reporter, Target, Description, ServerName, Date_of_issue) VALUES (?,?,?,?,?,?)");
                ps.setString(1, ip);
                ps.setString(2, reporter);
                ps.setString(3, target);
                ps.setString(4, description);
                ps.setString(5, serverName);
                ps.setTimestamp(6, dateOfIssue);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<Report> getReportsFromDatabase() {
        List<Report> reports = new ArrayList<>();
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM Reports");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Report report = new Report(
                        rs.getInt("ID"),
                        rs.getString("IP"),
                        rs.getString("Reporter"),
                        rs.getString("Target"),
                        rs.getString("Description"),
                        rs.getString("ServerName"),
                        rs.getTimestamp("Date_of_issue")
                );
                reports.add(report);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }
    public Report getReportByID(int ID) {
        Report report = null;
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT * FROM Reports WHERE ID = ?");
            preparedStatement.setInt(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String ip = resultSet.getString("IP");
                String reporter = resultSet.getString("Reporter");
                String target = resultSet.getString("Target");
                String description = resultSet.getString("Description");
                String serverName = resultSet.getString("ServerName");
                Timestamp dateOfIssue = resultSet.getTimestamp("Date_of_issue");

                report = new Report(ID, ip, reporter, target, description, serverName, dateOfIssue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public void deleteReportById(int reportId) {
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("DELETE FROM Reports WHERE ID = ?");
            preparedStatement.setInt(1, reportId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private int getNextAvailableId() {
        int nextId = -1;
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT ID + 1 FROM Reports WHERE NOT EXISTS (SELECT 1 FROM Reports r2 WHERE r2.ID = Reports.ID + 1)");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nextId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nextId;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
