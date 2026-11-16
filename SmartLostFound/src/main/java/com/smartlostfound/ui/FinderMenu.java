package com.smartlostfound.ui;

import com.smartlostfound.dao.UserDao;
import com.smartlostfound.model.*;
import java.sql.SQLException;
import java.util.Scanner;

public class FinderMenu extends RoleMenu {
    public FinderMenu(User user,UserDao dao,Scanner scanner){super(user,dao,scanner);}
    protected String title(){return "FINDER MENU";} protected Role requiredRole(){return Role.FINDER;}
    protected String[] options(){return new String[]{"Report Found Item (Create)","View My Found Items (Read)","Update My Found Item","Delete My Found Item"};}
    protected void handleOption(int n){try{switch(n){case 1->create();case 2->printItems(itemDao.getByFinder(user.id()));case 3->update();case 4->delete();default->System.out.println("Invalid choice.");}}catch(SQLException e){System.out.println("Database error: "+e.getMessage());}}
    private void create()throws SQLException{System.out.println("\n--- REPORT FOUND ITEM ---");String name=readRequired("Item name: "),desc=readRequired("Description: "),location=readRequired("Location found: "),date=readRequired("Date found (YYYY-MM-DD): ");if(!date.matches("\\d{4}-\\d{2}-\\d{2}")){System.out.println("Date must use YYYY-MM-DD.");return;}int id=itemDao.create(name,desc,location,date,user.id());System.out.println("Found item saved. Item ID: "+id);}
    private void update()throws SQLException{printItems(itemDao.getByFinder(user.id()));int id=readPositiveInt("Item ID to update: ");String name=readRequired("New item name: "),desc=readRequired("New description: "),loc=readRequired("New location: "),date=readRequired("New date (YYYY-MM-DD): ");if(!date.matches("\\d{4}-\\d{2}-\\d{2}")){System.out.println("Date must use YYYY-MM-DD.");return;}System.out.println(itemDao.update(id,name,desc,loc,date,user.id())?"Item updated.":"Update denied: item not found, not yours, or no longer FOUND.");}
    private void delete()throws SQLException{printItems(itemDao.getByFinder(user.id()));int id=readPositiveInt("Item ID to delete: ");System.out.print("Delete this item? (YES/NO): ");if(scanner.nextLine().trim().equalsIgnoreCase("YES"))System.out.println(itemDao.delete(id,user.id())?"Item deleted.":"Delete denied: item not found, not yours, or no longer FOUND.");else System.out.println("Delete cancelled.");}
}
