package ru.stern.common;

public class Commands {
    public static final byte REG_REQUEST = 6;           //User registration request
    public static final byte REG_SUCCESS = 7;           //User registration was successful
    public static final byte REG_FAILED = 8;            //User registration failed
    public static final byte AUTH_REQUEST = 9;          //User authentication request
    public static final byte AUTH_SUCCESS = 10;         //User authentication was successful
    public static final byte AUTH_FAILED = 11;          //User authentication failed
    public static final byte FILE_REQUEST = 14;         //Request to get a file from the server
    public static final byte FILE_TRANSFER = 15;        //Request to transfer a file to the server
    public static final byte FILE_LIST_REQUEST = 16;    //Request to get a list of files on the server
    public static final byte FILE_DELETE = 17;          //Request to delete a file on the server
    public static final byte DISCONNECT_REQUEST = 99;   //Request to disconnect from the server
}
