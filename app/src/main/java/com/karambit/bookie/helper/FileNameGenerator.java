package com.karambit.bookie.helper;


import java.util.Date;

/**
 * Created by orcan on 11/14/16.
 */

public class FileNameGenerator {

    public static String generateBookImageName(String email) {
        return email + "_" + new Date().getTime() + ".png";
    }

    public static String generateBookThumbnailName(String bookImageName) {
        String[] splitted = bookImageName.split("_");
        String email = splitted[0];
        String date = splitted[1].substring(0, splitted[1].length() - 4);
        return email + "_" + date + "_thumbnail.png";
    }
}
