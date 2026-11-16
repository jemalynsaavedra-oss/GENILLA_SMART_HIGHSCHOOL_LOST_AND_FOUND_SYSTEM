package com.smartlostfound.ui;

import com.smartlostfound.dao.UserDao;
import com.smartlostfound.model.FoundItem;
import com.smartlostfound.model.Role;
import com.smartlostfound.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/** Owner actions for finding, claiming, and confirming receipt of found belongings. */
public class OwnerMenu extends RoleMenu {
    public OwnerMenu(User user, UserDao dao, Scanner scanner) {
        super(user, dao, scanner);
    }

    @Override protected String title() { return "OWNER MENU"; }
    @Override protected Role requiredRole() { return Role.OWNER; }

    @Override
    protected String[] options() {
        return new String[]{
                "View Available Found Items",
                "Search Available Found Items",
                "Claim a Found Item",
                "View My Items Awaiting Collection",
                "Receive / Confirm Returned Item"
        };
    }

    @Override
    protected void handleOption(int choice) {
        try {
            switch (choice) {
                case 1 -> viewAvailableItems();
                case 2 -> searchAvailableItems();
                case 3 -> claimFoundItem();
                case 4 -> printItems(itemDao.getClaimedByOwner(user.id()));
                case 5 -> confirmReceipt();
                default -> System.out.println("Invalid choice. Select an option shown in the menu.");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    /** Displays all Finder reports that are still available to claim. */
    private void viewAvailableItems() throws SQLException {
        System.out.println("\n--- AVAILABLE FOUND ITEMS ---");
        printItems(itemDao.getAvailableItems());
    }

    private void searchAvailableItems() throws SQLException {
        System.out.println("\n--- SEARCH AVAILABLE FOUND ITEMS ---");
        String query = readRequired("Enter an item name, description, or location: ");
        printItems(itemDao.search(query));
    }

    private void claimFoundItem() throws SQLException {
        System.out.println("\n--- CLAIM A FOUND ITEM ---");
        List<FoundItem> available = itemDao.getAvailableItems();
        printItems(available);
        if (available.isEmpty()) return;

        int itemId = readPositiveInt("Enter the item ID to claim (0 to cancel): ", true);
        if (itemId == 0) {
            System.out.println("Claim cancelled.");
            return;
        }

        FoundItem selected = available.stream().filter(item -> item.id() == itemId).findFirst().orElse(null);
        if (selected == null) {
            System.out.println("Invalid item ID. Choose an ID from the available items listed above.");
            return;
        }

        System.out.println("Selected item: " + selected.itemName());
        System.out.print("Confirm that you want to claim this item? (YES/NO): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("YES")) {
            System.out.println("Claim cancelled.");
            return;
        }

        if (itemDao.claim(selected.id(), user.id())) {
            System.out.println("Item successfully claimed. The claim and its activity log have been saved.");
            System.out.println("Please coordinate collection with the Finder or administrator.");
        } else {
            System.out.println("Claim failed. This item may have just been claimed by another user. Refresh the list and try again.");
        }
    }

    private void confirmReceipt() throws SQLException {
        System.out.println("\n--- CONFIRM ITEM RECEIPT ---");
        List<FoundItem> claimed = itemDao.getClaimedByOwner(user.id());
        printItems(claimed);
        if (claimed.isEmpty()) return;

        int itemId = readPositiveInt("Enter the ID of the item you physically received (0 to cancel): ", true);
        if (itemId == 0) {
            System.out.println("Receipt confirmation cancelled.");
            return;
        }
        boolean belongsToOwner = claimed.stream().anyMatch(item -> item.id() == itemId);
        if (!belongsToOwner) {
            System.out.println("Invalid item ID. You can only confirm items claimed under your account.");
            return;
        }

        System.out.print("Confirm you have physically received this item? (YES/NO): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("YES")) {
            System.out.println("Receipt confirmation cancelled.");
            return;
        }
        if (itemDao.receive(itemId, user.id())) {
            System.out.println("Receipt confirmed. Item marked RETURNED and the activity log has been saved.");
        } else {
            System.out.println("Unable to confirm receipt. The item may no longer be assigned to your account.");
        }
    }
}
