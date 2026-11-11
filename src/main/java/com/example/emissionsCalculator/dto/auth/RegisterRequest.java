package com.example.emissionsCalculator.dto.auth;

public record RegisterRequest(String username, String password, boolean isAdmin) {}