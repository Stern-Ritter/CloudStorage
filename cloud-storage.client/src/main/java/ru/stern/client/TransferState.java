package ru.stern.client;

public enum TransferState {
    COMMAND,
    NAME_LENGTH,
    NAME,
    FILE_LIST_LENGTH,
    FILE_LENGTH,
    FILE,
    FILE_LIST
}
