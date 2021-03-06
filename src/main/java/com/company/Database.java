package com.company;


import com.company.models.Vartotojas;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private BasicDataSource dataSource;

    public Database() {

        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUsername("root");
        dataSource.setPassword("");
        dataSource.setUrl("jdbc:mysql://localhost:3306/duombaze?useUnicode=yes&characterEncoding=UTF-8");
        dataSource.setValidationQuery("SELECT 1");
    }

    public boolean isUserValid(String username, String password) {
        String query = "SELECT * FROM vartotojai WHERE username=? AND password=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet results = preparedStatement.executeQuery();

            int counter = 0;
            while (results.next()) {
                counter++;
            }

            return counter == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void insertUser(String vardas, String pavarde, int amzius) {
        String query = "INSERT INTO vartotojai (vardas, pavarde, amzius)"
                + " VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, vardas);
            statement.setString(2, pavarde);
            statement.setInt(3, amzius);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Vartotojas> gautiVartotojus() {
        List<Vartotojas> vartotojai = new ArrayList<>();
        String query = "SELECT * FROM vartotojai";
        String turinys = "";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Vartotojas vartotojas = new Vartotojas();
                vartotojas.setId(resultSet.getInt("id"));
                vartotojas.setVardas(resultSet.getString("vardas"));
                vartotojas.setPavarde(resultSet.getString("pavarde"));
                vartotojas.setAmzius(resultSet.getInt("amzius"));
                vartotojas.setUsername(resultSet.getString("username"));
                vartotojas.setPassword(resultSet.getString("password"));
                vartotojas.setEmail(resultSet.getString("email"));
                vartotojai.add(vartotojas);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vartotojai;
    }


    public boolean register(String username, String password, String email) {
        String sql = "INSERT INTO vartotojai (username, password, email) " + "VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);

            int rowsAffected = statement.executeUpdate();

            return rowsAffected == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getPasswordForEmail(String email) {
        String query = "SELECT password FROM vartotojai WHERE email=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            ResultSet results = preparedStatement.executeQuery();

            results.next();
            String password = results.getString("password");
            return password;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}

