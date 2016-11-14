package com.karambit.bookie.helper;


import java.util.Date;

/**
 * Created by orcan on 11/14/16.
 */

public class FileNameGenerator {

    public static String generateBookImageName(String email) {

        return email + "_" + new Date().getTime() + ".png";
    }
}
