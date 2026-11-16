package com.smartlostfound;

import com.smartlostfound.dao.DatabaseInitializer;
import com.smartlostfound.dao.UserDao;
import com.smartlostfound.ui.Authentication;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try { DatabaseInitializer.initialize(); }
        catch (SQLException e) { System.err.println("Unable to initialize database: " + e.getMessage()); return; }
        System.out.println("Database ready: smart_lost_found.db");
        try (Scanner scanner = new Scanner(System.in)) {
            new Authentication(new UserDao(), scanner).start();
        }
    }
}
