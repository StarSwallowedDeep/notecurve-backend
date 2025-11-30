package com.notecurve.auth.dto;

public record LoginRequest(
    String loginId,
    String password
) {}
