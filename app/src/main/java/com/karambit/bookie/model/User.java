package com.karambit.bookie.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.karambit.bookie.helper.ImageLinkSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Orcan on 8/2/2016.
 * <p>
 * All user information will held by this Class.
 */
public class User implements Parcelable {

    private int mID;
    private String mName;
    private String mImageUrl;
    private String mThumbnailUrl;
    private double mLatitude;
    private double mLongitude;

    public User(int ID, String name, String imageUrl, String thumbnailUrl, double latitude, double longitude) {
        mID = ID;
        mName = name;
        mImageUrl = imageUrl;
        mThumbnailUrl = thumbnailUrl;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    protected User(Parcel in) {
        mID = in.readInt();
        mName = in.readString();
        mImageUrl = in.readString();
        mThumbnailUrl = in.readString();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mID);
        dest.writeString(mName);
        dest.writeString(mImageUrl);
        dest.writeString(mThumbnailUrl);
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public static User jsonObjectToUser(JSONObject userJsonObject) {
        try {
            return new User(
                    userJsonObject.getInt("user_id"),
                    userJsonObject.getString("name_surname"),
                    userJsonObject.getString("profile_picture_url"),
                    userJsonObject.getString("profile_picture_thumbnail_url"),
                    userJsonObject.isNull("latitude") ? -1 : userJsonObject.getDouble("latitude"),
                    userJsonObject.isNull("longitude") ? -1 : userJsonObject.getDouble("longitude")
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static User.Details jsonObjectToUserDetails(JSONObject userJsonObject) {

        try {
            User user = new User(
                    userJsonObject.getInt("user_id"),
                    userJsonObject.getString("name_surname"),
                    userJsonObject.getString("profile_picture_url"),
                    userJsonObject.getString("profile_picture_thumbnail_url"),
                    userJsonObject.isNull("latitude") ? -1 : userJsonObject.getDouble("latitude"),
                    userJsonObject.isNull("longitude") ? -1 : userJsonObject.getDouble("longitude")
            );

            return user.new Details(
                    userJsonObject.getString("password"),
                    userJsonObject.getString("email"),
                    (userJsonObject.getInt("email_verified") != 0),
                    userJsonObject.getString("bio"),
                    userJsonObject.getInt("book_counter"),
                    userJsonObject.getInt("point")
            );

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<User> jsonArrayToUserList(JSONArray userJsonArray) {
        ArrayList<User> users = new ArrayList<>(userJsonArray.length());
        for (int i = 0; i < userJsonArray.length(); i++) {
            try {
                JSONObject userObject = userJsonArray.getJSONObject(i);
                users.add(jsonObjectToUser(userObject));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    public void setID(int ID) {
        mID = ID;
    }

    public int getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        mThumbnailUrl = thumbnailUrl;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    @Override
    public String toString() {
        return "User{" +
                "mID=" + mID +
                ", mName='" + mName + '\'' +
                ", mImageUrl='" + mImageUrl + '\'' +
                ", mThumbnailUrl='" + mThumbnailUrl + '\'' +
                ", mLatitude=" + mLatitude +
                ", mLongitude=" + mLongitude +
                '}';
    }

    public class Details {
        private String mPassword;
        private String mEmail;
        private boolean mVerified;
        private String mBio;
        private int mBookCounter;
        private int mPoint;
        private ArrayList<Book> mCurrentlyReading;
        private ArrayList<Book> mReadBooks;
        private ArrayList<Book> mBooksOnHand;
        private ArrayList<Book> mSharedBooks;

        public Details(String password, String email, boolean verified, @Nullable String bio, int bookCounter,
                       int point, ArrayList<Book> currentlyReading, @Nullable ArrayList<Book> readBooks,
                       @Nullable ArrayList<Book> booksOnHand, @Nullable ArrayList<Book> sharedBooks) {
            mPassword = password;
            mEmail = email;
            mVerified = verified;
            mBio = bio != null ? bio : "";
            mBookCounter = bookCounter;
            mPoint = point;
            mCurrentlyReading = currentlyReading;
            mReadBooks = readBooks;
            mBooksOnHand = booksOnHand;
            mSharedBooks = sharedBooks;
        }

        public Details(String password, String email, boolean verified, String bio, int bookCounter, int point) {
            mPassword = password;
            mEmail = email;
            mVerified = verified;
            mBio = bio;
            mBookCounter = bookCounter;
            mPoint = point;
        }

        public String getPassword() {
            return mPassword;
        }

        public void setPassword(String password) {
            mPassword = password;
        }

        public String getEmail() {
            return mEmail;
        }

        public void setEmail(String email) {
            mEmail = email;
        }

        public boolean isVerified() {
            return mVerified;
        }

        public void setVerified(boolean verified) {
            mVerified = verified;
        }

        public String getBio() {
            return mBio;
        }

        public void setBio(String bio) {
            mBio = bio;
        }

        public int getBookCounter() {
            return mBookCounter;
        }

        public void setBookCounter(int bookCounter) {
            mBookCounter = bookCounter;
        }

        public int getPoint() {
            return mPoint;
        }

        public void setPoint(int point) {
            mPoint = point;
        }

        public ArrayList<Book> getCurrentlyReading() {
            return mCurrentlyReading;
        }

        public int getCurrentlyReadingCount() {
            return mCurrentlyReading != null ? mCurrentlyReading.size() : 0;
        }

        public void setCurrentlyReading(ArrayList<Book> currentlyReading) {
            mCurrentlyReading = currentlyReading;
        }

        public ArrayList<Book> getReadBooks() {
            return mReadBooks;
        }

        public int getReadBooksCount() {
            return mReadBooks != null ? mReadBooks.size() : 0;
        }

        public void setReadBooks(ArrayList<Book> readBooks) {
            mReadBooks = readBooks;
        }

        public ArrayList<Book> getBooksOnHand() {
            return mBooksOnHand;
        }

        public int getBooksOnHandCount() {
            return mBooksOnHand != null ? mBooksOnHand.size() : 0;
        }

        public void setBooksOnHand(ArrayList<Book> booksOnHand) {
            mBooksOnHand = booksOnHand;
        }

        public ArrayList<Book> getSharedBooks() {
            return mSharedBooks;
        }

        public int getSharedBooksCount() {
            return mSharedBooks != null ? mSharedBooks.size() : 0;
        }

        public void setSharedBooks(ArrayList<Book> sharedBooks) {
            mSharedBooks = sharedBooks;
        }

        public User getUser() {
            return User.this;
        }

        @Override
        public String toString() {
            return User.this.toString() + ".Details{" +
                    "mPassword='" + mPassword + '\'' +
                    ", mEmail='" + mEmail + '\'' +
                    ", mVerified=" + mVerified +
                    ", mBio='" + mBio + '\'' +
                    ", mBookCounter=" + mBookCounter +
                    ", mPoint=" + mPoint +
                    ",\nmCurrentlyReading=" + mCurrentlyReading +
                    ",\nmReadBooks=" + mReadBooks +
                    ",\nmBooksOnHand=" + mBooksOnHand +
                    ",\nmBooksOnHand=" + mSharedBooks + '}';
        }
    }

    /**
     * This class contains all user generator methods
     * <p>
     * TODO The "ImageLinkSource.java" must be added to project
     */
    public static class GENERATOR {

        private static final String[] NAMES = new String[]{
                "Vinita Vossen",
                "Tran Matamoros",
                "Nilsa Laduke",
                "Blake Stouffer",
                "Zenaida Shuttleworth",
                "Keturah Irey",
                "Justina Tudor",
                "Kamilah Tang",
                "Ross Lukas",
                "Caroline Mondor",
                "Lona Crumb",
                "Evie Meidinger",
                "Torri Ruybal",
                "Armida Skipper",
                "Yvonne Trent",
                "Russ Davy",
                "Letisha Rudder",
                "Sherwood Worsley",
                "Simona Rabun",
                "Rosalba Behnke"
        };

        private static final String BIO = "Lorem ipsum dolor sit amet, consectetur.";

        public static User generateUser() {
            Random random = new Random();

            String name = NAMES[random.nextInt(NAMES.length)];

            int randomIndex = random.nextInt(ImageLinkSource.IMAGE_THUMBNAIL_URLS.length);
            String imageUrl = ImageLinkSource.IMAGE_THUMBNAIL_URLS[randomIndex];
            String imageThumbnailUrl = ImageLinkSource.IMAGE_THUMBNAIL_URLS[randomIndex];

            return new User(random.nextInt(), name, imageUrl, imageThumbnailUrl,
                    random.nextDouble() + random.nextInt(180) - 90,
                    random.nextDouble() + random.nextInt(180));
        }

        public static ArrayList<User> generateUserList(int count) {
            ArrayList<User> users = new ArrayList<>(count);

            for (int i = 0; i < count; i++)
                users.add(generateUser());

            return users;
        }

        public static User.Details generateUserDetails() {
            return generateUserDetails(generateUser());
        }

        /**
         * @param user is referenced for newly created User.Details object
         */
        public static User.Details generateUserDetails(User user) {
            Random random = new Random();

            ArrayList<Book> currentlyReading = Book.GENERATOR.generateBookList(random.nextInt(4), user);

            ArrayList<Book> booksOnHand = Book.GENERATOR.generateBookList(random.nextInt(15), user);
            ArrayList<Book> booksRead = Book.GENERATOR.generateBookList(random.nextInt(15));
            ArrayList<Book> booksShared = Book.GENERATOR.generateBookList(random.nextInt(15));


            return user.new Details("********", name2Email(user.mName), true, BIO, random.nextInt(5),
                    random.nextInt(300), currentlyReading, booksRead, booksOnHand, booksShared);
        }

        private static String name2Email(String name) {
            name = name.replace(" ", "").toLowerCase().trim();
            return name + "@gmail.com";
        }
    }
}