package com.smartlostfound.model;
public record FoundItem(int id, String itemName, String description, String location, String dateFound, int finderId, Integer ownerId, String status) {}
