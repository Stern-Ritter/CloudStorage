package ru.stern.server;

public enum TransferState {
    COMMAND,
    NAME_LENGTH,
    NAME_LENGTH_TO_DELETE,
    NAME_LENGTH_TO_SEND,
    NAME,
    NAME_TO_DELETE,
    NAME_TO_SEND,
    FILE_LENGTH,
    FILE
}
