package de.marvinleiers.finditems.utils;

import com.zaxxer.hikari.HikariDataSource;
import de.marvinleiers.finditems.FindItems;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQL
{
    private final HikariDataSource hikari;
    private final String database;
    private final String password;
    private final String host;
    private final String user;

    public MySQL(String host, String database, String user, String password)
    {
        this.hikari = new HikariDataSource();
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    public void connect()
    {
        this.hikari.setMaximumPoolSize(10);
        this.hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        this.hikari.addDataSourceProperty("serverName", this.host);
        this.hikari.addDataSourceProperty("port", 3306);
        this.hikari.addDataSourceProperty("databaseName", this.database);
        this.hikari.addDataSourceProperty("user", this.user);
        this.hikari.addDataSourceProperty("password", this.password);
        this.hikari.addDataSourceProperty("zeroDateTimeBehavior", "convertToNull");
    }

    public void createTable(String tableName, String content)
    {
        if (!(tableName.isEmpty() || content.isEmpty()))
            update("CREATE TABLE IF NOT EXISTS " + tableName + " " + content);
    }

    public void update(String query)
    {
        Connection connection = null;

        try
        {
            connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
            statement.close();
            connection.close();
        }
        catch (SQLException | NullPointerException e)
        {
            try
            {
                connection.close();
            }
            catch (SQLException | NullPointerException ignored)
            {

            }

            e.printStackTrace();
            FindItems.getInstance().log("§4Fehler beim Schreiben in die Datenbank aufgetreten!");
        }
    }

    public boolean exists(OfflinePlayer player, String table)
    {
        try
        {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM " + table + " WHERE uuid='"
                    + player.getUniqueId().toString() + "';");

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
            {
                boolean bool = resultSet.getInt("id") != 0;
                resultSet.close();
                preparedStatement.close();
                connection.close();
                return bool;
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public Connection getConnection()
    {
        try
        {
            return hikari.getConnection();
        }
        catch (SQLException e)
        {
            FindItems.getInstance().log("§4Verbindung zur Datenbank konnte nicht hergestellt werden!");
        }

        return null;
    }

    public void close()
    {
        try
        {
            this.hikari.close();
            FindItems.getInstance().log("MySQL-Verbindung erfolgreich getrennt!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
