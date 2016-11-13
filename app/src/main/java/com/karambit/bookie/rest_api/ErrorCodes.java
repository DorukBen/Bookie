package com.karambit.bookie.rest_api;

/**
 * Created by orcan on 11/12/16.
 */

public class ErrorCodes {

    public static final int EMPTY_POST = 0;
    public static final int MISSING_POST_ELEMENT = 1;
    public static final int INVALID_EMAIL = 2;
    public static final int INVALID_NAME_SURNAME = 3;
    public static final int INVALID_LATITUDE_OR_LONGITUDE = 4;
    public static final int EMAIL_TAKEN = 5;
    public static final int SHORT_PASSWORD = 6;
    public static final int LONG_PASSWORD = 7;
    public static final int FALSE_COMBINATION = 8;
    public static final int QUERY = 9;
    public static final int EMAIL_NOT_FOUND = 10;
    public static final int INVALID_REQUEST = 11;
    public static final int VERIFICATION_HASH_NOT_FOUND = 12;
    public static final int UNKNOWN = -1;

}
