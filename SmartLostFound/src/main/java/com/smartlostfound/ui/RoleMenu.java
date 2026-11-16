package com.smartlostfound.ui;

import com.smartlostfound.dao.*;
import com.smartlostfound.model.*;
import java.sql.SQLException;
import java.util.Scanner;

public abstract class RoleMenu {
    protected final User user; protected final UserDao userDao; protected final Scanner scanner;
    protected final FoundItemDao itemDao=new FoundItemDao();
    protected RoleMenu(User user,UserDao userDao,Scanner scanner){this.user=user;this.userDao=userDao;this.scanner=scanner;}
    protected abstract String title();
    protected abstract Role requiredRole();
    protected String[] options(){return new String[0];}
    protected void handleOption(int index){System.out.println("Invalid menu option.");}
    public boolean show(){
        if(!requiredRole().name().equals(user.roles())){System.out.println("Access denied: this menu is for "+requiredRole()+" only.");return false;}
        String[] extra=options();int logoutNo=extra.length+1,exitNo=extra.length+2;
        while(true){System.out.println("\n--- "+title()+" ---\nLogged in as: "+user.username()+" ["+user.roles()+"]");for(int i=0;i<extra.length;i++)System.out.println((i+1)+". "+extra[i]);System.out.println(logoutNo+". Logout\n"+exitNo+". Exit Application\nChoose an option: ");
            String in=scanner.nextLine().trim();int choice;try{choice=Integer.parseInt(in);}catch(NumberFormatException e){System.out.println("Enter a valid menu number.");continue;}
            if(choice>=1&&choice<=extra.length){handleOption(choice);}else if(choice==logoutNo){try{userDao.logLogout(user);}catch(SQLException e){System.out.println("Could not save logout audit record: "+e.getMessage());}System.out.println("You have logged out.");return false;}else if(choice==exitNo){try{userDao.logLogout(user);}catch(SQLException e){System.out.println("Could not save logout audit record: "+e.getMessage());}return true;}else System.out.println("Invalid choice.");
        }
    }
    protected String readRequired(String prompt){while(true){System.out.print(prompt);String v=scanner.nextLine().trim();if(!v.isBlank())return v;System.out.println("This field is required.");}}
    protected int readPositiveInt(String prompt){return readPositiveInt(prompt,false);}
    protected int readPositiveInt(String prompt,boolean allowZero){while(true){System.out.print(prompt);try{int n=Integer.parseInt(scanner.nextLine().trim());if((allowZero&&n==0)||n>0)return n;}catch(NumberFormatException ignored){}System.out.println(allowZero?"Enter zero to cancel or a positive whole number.":"Enter a positive whole number.");}}
    protected void printItems(java.util.List<FoundItem> items){if(items.isEmpty()){System.out.println("No matching items found.");return;}System.out.printf("%-5s | %-20s | %-28s | %-22s | %-12s | %-10s%n","ID","ITEM","DESCRIPTION","LOCATION","DATE FOUND","STATUS");System.out.println("----------------------------------------------------------------------------------------------------------------------------------------");for(FoundItem i:items)System.out.printf("%-5d | %-20.20s | %-28.28s | %-22.22s | %-12s | %-10s%n",i.id(),i.itemName(),i.description(),i.location(),i.dateFound(),i.status());}
}
