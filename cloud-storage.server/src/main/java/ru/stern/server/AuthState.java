package ru.stern.server;

public enum AuthState {
    COMMAND,
    REG_LOGIN_LENGTH,
    REG_LOGIN,
    REG_PASS_LENGTH,
    REG_PASS,
    AUTH_LOGIN_LENGTH,
    AUTH_LOGIN,
    AUTH_PASS_LENGTH,
    AUTH_PASS
}