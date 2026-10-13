package com.smartlostfound.ui;

import com.smartlostfound.dao.UserDao;
import com.smartlostfound.model.User;
import java.sql.SQLException;
import java.util.Scanner;

public class Menu {
    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\\n==================================");
            System.out.println(" SMART HIGH SCHOOL LOST AND FOUND");
            System.out.println("==================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> registerAccount();
                case "2" -> loginAccount();
                case "3" -> {
                    running = false;
                    System.out.println("Thank you for using the system!");
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void registerAccount() {
        try {
            System.out.println("\\n--- REGISTER ACCOUNT ---");
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            if (username.isBlank() || password.isBlank()) {
                System.out.println("Username and password are required.");
                return;
            }
            if (userDao.usernameExists(username)) {
                System.out.println("Username already exists.");
                return;
            }

            System.out.println("Select role:");
            System.out.println("1. TEACHER");
            System.out.println("2. STUDENT");
            System.out.print("Enter choice: ");

            String role = switch (scanner.nextLine().trim()) {
                case "1" -> "TEACHER";
                case "2" -> "STUDENT";
                default -> null;
            };
            if (role == null) {
                System.out.println("Invalid role selection.");
                return;
            }

            User user = new User(0, username, password, role, "PENDING");
            if (userDao.register(user)) {
                System.out.println("Registration successful!");
                System.out.println("Account status: PENDING");
                System.out.println("Wait for account approval.");
            }
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void loginAccount() {
        try {
            System.out.println("\\n--- LOGIN ---");
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            User user = userDao.login(username, password);
            if (user == null) {
                System.out.println("Invalid username or password.");
                return;
            }
            if (!"APPROVED".equals(user.status())) {
                System.out.println("Your account is " + user.status() + ".");
                System.out.println("Please wait for approval.");
                return;
            }

            System.out.println("\\nLogin successful!");
            System.out.println("Welcome, " + user.username());
            System.out.println("Role: " + user.roles());
            userMenu(user);
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private void userMenu(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\\n--- USER MENU ---");
            System.out.println("Logged in as: " + user.username());
            System.out.println("Role: " + user.roles());
            System.out.println("1. Logout");
            System.out.println("2. Exit Application");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> {
                    loggedIn = false;
                    System.out.println("You have logged out.");
                }
                case "2" -> {
                    System.out.println("Thank you for using the system!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}
