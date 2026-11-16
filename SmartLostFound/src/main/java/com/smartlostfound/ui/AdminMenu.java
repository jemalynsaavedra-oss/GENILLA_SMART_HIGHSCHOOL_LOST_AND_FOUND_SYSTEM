package com.smartlostfound.ui;

import com.smartlostfound.dao.UserDao;
import com.smartlostfound.model.Role;
import com.smartlostfound.model.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu extends RoleMenu {
    /** Statuses an admin may assign through the update feature. */
    private static final String[] VALID_STATUSES = {"APPROVED", "PENDING"};

    public AdminMenu(User user, UserDao userDao, Scanner scanner) {
        super(user, userDao, scanner);
    }

    @Override
    protected String title() {
        return "ADMIN MENU";
    }

    @Override
    protected String[] options() {
        return new String[]{
                "View All System Users",
                "View Pending Registrations",
                "Approve User Account",
                "Update User Role & Status"
        };
    }

    @Override
    protected void handleOption(int index) {
        try {
            switch (index) {
                case 1 -> viewAllUsers();
                case 2 -> viewPendingUsers();
                case 3 -> approveUserAccount();
                case 4 -> updateUserRoleAndStatus();
                default -> System.out.println("Invalid choice.");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void viewAllUsers() throws SQLException {
        System.out.println("\n--- ALL SYSTEM USERS ---");
        printUserTable(userDao.getAllUsers());
    }

    private void viewPendingUsers() throws SQLException {
        System.out.println("\n--- PENDING REGISTRATIONS ---");
        printUserTable(userDao.getPendingUsers());
    }

    private void approveUserAccount() throws SQLException {
        System.out.println("\n--- APPROVE USER ACCOUNT ---");
        List<User> pending = userDao.getPendingUsers();
        printUserTable(pending);
        if (pending.isEmpty()) {
            return;
        }

        while (true) {
            int id = readIntInput("Enter Target User ID to approve (0 to cancel): ");

            if (id == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            User target = pending.stream().filter(u -> u.id() == id).findFirst().orElse(null);
            if (target == null) {
                System.out.println("User ID " + id + " is not in the pending list.");
                continue;
            }

            if (userDao.approveUser(id)) {
                System.out.println("Success! " + target.username() + " (" + target.roles()
                        + ") has been APPROVED.");
            } else {
                System.out.println("Approval failed. Please try again.");
            }
            return;
        }
    }

    /** Lets the admin change another user's role and status. Enter 0 at any step to cancel. */
    private void updateUserRoleAndStatus() throws SQLException {
        System.out.println("\n--- UPDATE USER ROLE & STATUS ---");
        List<User> users = userDao.getAllUsers();
        printUserTable(users);
        if (users.isEmpty()) {
            return;
        }

        // Step 1: choose the target user.
        User target;
        while (true) {
            int id = readIntInput("Enter Target User ID to update (0 to cancel): ");

            if (id == 0) {
                System.out.println("Update cancelled.");
                return;
            }
            if (id == user.id()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                continue;
            }

            target = users.stream().filter(u -> u.id() == id).findFirst().orElse(null);
            if (target == null) {
                System.out.println("User ID " + id + " does not exist.");
                continue;
            }
            break;
        }

        System.out.println("Selected: " + target.username() + " (Current role: " + target.roles()
                + ", Current status: " + target.status() + ")");

        // Step 2: choose the new role.
        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        // Step 3: choose the new status.
        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(target.id(), newRole, newStatus)) {
            System.out.println("Success! " + target.username() + " is now " + newRole
                    + " with status " + newStatus + ".");
        } else {
            System.out.println("Update failed. Please try again.");
        }
    }

    // ---------------------------------------------------------------------
    // Input validation wrappers
    // ---------------------------------------------------------------------

    /** Keeps asking until the input is a whole number, so letters/symbols never crash the app. */
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    /**
     * Keeps asking until a valid role (ADMIN, FINDER, OWNER) is entered.
     * @return the role in upper case, or null if the admin entered 0 to cancel.
     */
    private String readValidRole() {
        StringBuilder names = new StringBuilder();
        for (Role role : Role.values()) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(role.name());
        }

        while (true) {
            System.out.print("Enter new role (" + names + ") or 0 to cancel: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            for (Role role : Role.values()) {
                if (role.name().equals(input)) {
                    return role.name();
                }
            }
            System.out.println("Invalid role. Valid roles are: " + names + ".");
        }
    }

    /**
     * Keeps asking until a valid status (APPROVED, PENDING) is entered.
     * @return the status in upper case, or null if the admin entered 0 to cancel.
     */
    private String readValidStatus() {
        String names = String.join(", ", VALID_STATUSES);

        while (true) {
            System.out.print("Enter new status (" + names + ") or 0 to cancel: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            for (String status : VALID_STATUSES) {
                if (status.equals(input)) {
                    return status;
                }
            }
            System.out.println("Invalid status. Valid statuses are: " + names + ".");
        }
    }

    /** Prints any list of users as an aligned, bordered table. */
    private void printUserTable(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No users to display.");
            return;
        }
        String border = "+-" + "-".repeat(5) + "-+-" + "-".repeat(15) + "-+-"
                + "-".repeat(12) + "-+-" + "-".repeat(10) + "-+";
        String row = "| %-5s | %-15s | %-12s | %-10s |%n";

        System.out.println(border);
        System.out.printf(row, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);
        for (User u : users) {
            System.out.printf(row, u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println(border);
        System.out.println("Total: " + users.size());
    }
}
