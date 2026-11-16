package com.smartlostfound.model;
public record ActivityLog(int id, Integer userId, String username, String actionType, String description, String timestamp) {}
