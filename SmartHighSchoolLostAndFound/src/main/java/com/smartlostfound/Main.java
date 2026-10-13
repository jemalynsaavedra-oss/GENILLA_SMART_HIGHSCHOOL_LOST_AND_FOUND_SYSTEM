package com.smartlostfound;

import com.smartlostfound.dao.DatabaseConnection;
import com.smartlostfound.ui.Menu;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseConnection.initializeDatabase();
            new Menu().start();
        } catch (SQLException e) {
            System.err.println("Unable to initialize database: " + e.getMessage());
        }
    }
}
