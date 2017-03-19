package com.karambit.bookie.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
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
    private LatLng mLocation;

    public User(int ID, String name, String imageUrl, String thumbnailUrl, LatLng location) {
        mID = ID;
        mName = name;
        mImageUrl = imageUrl;
        mThumbnailUrl = thumbnailUrl;
        mLocation = location;
    }

    protected User(Parcel in) {
        mID = in.readInt();
        mName = in.readString();
        mImageUrl = in.readString();
        mThumbnailUrl = in.readString();
        mLocation = in.readParcelable(LatLng.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mID);
        dest.writeString(mName);
        dest.writeString(mImageUrl);
        dest.writeString(mThumbnailUrl);
        dest.writeParcelable(mLocation, flags);
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
            if (userJsonObject != null){
                if (userJsonObject.isNull("latitude") || userJsonObject.isNull("longitude")){
                    return new User(
                            userJsonObject.isNull("ID")? -1: userJsonObject.getInt("ID"),
                            userJsonObject.isNull("nameSurname")|| TextUtils.isEmpty(userJsonObject.getString("nameSurname"))? null: userJsonObject.getString("nameSurname"),
                            userJsonObject.isNull("profilePictureURL")|| TextUtils.isEmpty(userJsonObject.getString("profilePictureURL"))? null: userJsonObject.getString("profilePictureURL"),
                            userJsonObject.isNull("profilePictureThumbnailURL")|| TextUtils.isEmpty(userJsonObject.getString("profilePictureThumbnailURL"))? null: userJsonObject.getString("profilePictureThumbnailURL"),
                            null
                    );
                }else {
                    return new User(
                            userJsonObject.isNull("ID")? -1: userJsonObject.getInt("ID"),
                            userJsonObject.isNull("nameSurname")|| TextUtils.isEmpty(userJsonObject.getString("nameSurname"))? null: userJsonObject.getString("nameSurname"),
                            userJsonObject.isNull("profilePictureURL")|| TextUtils.isEmpty(userJsonObject.getString("profilePictureURL"))? null: userJsonObject.getString("profilePictureURL"),
                            userJsonObject.isNull("profilePictureThumbnailURL")|| TextUtils.isEmpty(userJsonObject.getString("profilePictureThumbnailURL"))? null: userJsonObject.getString("profilePictureThumbnailURL"),
                            new LatLng(userJsonObject.getDouble("latitude"), userJsonObject.getDouble("longitude"))
                    );
                }
            }else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static User.Details jsonObjectToUserDetails(JSONObject userJsonObject) {

        try {
            if (userJsonObject != null){
                User user = jsonObjectToUser(userJsonObject);

                return user.new Details(
                        userJsonObject.isNull("password")|| TextUtils.isEmpty(userJsonObject.getString("password"))? null: userJsonObject.getString("password"),
                        userJsonObject.isNull("email")|| TextUtils.isEmpty(userJsonObject.getString("email"))? null: userJsonObject.getString("email"),
                        !userJsonObject.isNull("emailVerified") && userJsonObject.getBoolean("emailVerified"),
                        userJsonObject.isNull("bio")|| TextUtils.isEmpty(userJsonObject.getString("bio"))? null: userJsonObject.getString("bio"),
                        userJsonObject.isNull("counter")? -1: userJsonObject.getInt("counter"),
                        userJsonObject.isNull("point")? 0: userJsonObject.getInt("point"),
                        userJsonObject.isNull("shared")? 0: userJsonObject.getInt("shared")
                );
            }else {
                return null;
            }
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

    public LatLng getLocation() {
        return mLocation;
    }

    public void setLocation(LatLng location) {
        mLocation = location;
    }


    @Override
    public String toString() {
        return "User{" +
                "mID=" + mID +
                ", mName='" + mName + '\'' +
                ", mImageUrl='" + mImageUrl + '\'' +
                ", mThumbnailUrl='" + mThumbnailUrl + '\'' +
                ", mLocation=" + ((mLocation != null) ? mLocation.toString(): "null") +
                '}';
    }

    public class Details {
        private String mPassword;
        private String mEmail;
        private boolean mVerified;
        private String mBio;
        private int mBookCounter;
        private int mPoint;
        private int mSharedPoint;
        private ArrayList<Book> mCurrentlyReading;
        private ArrayList<Book> mOnRoadBooks;
        private ArrayList<Book> mReadBooks;
        private ArrayList<Book> mBooksOnHand;
        private ArrayList<Book> mSharedBooks;

        public Details(String password, String email, boolean verified, @Nullable String bio, int bookCounter,
                       int point, int sharedPoint, ArrayList<Book> currentlyReading, @Nullable ArrayList<Book> onRoadBooks, @Nullable ArrayList<Book> readBooks,
                       @Nullable ArrayList<Book> booksOnHand, @Nullable ArrayList<Book> sharedBooks) {
            mPassword = password;
            mEmail = email;
            mVerified = verified;
            mBio = bio != null ? bio : "";
            mBookCounter = bookCounter;
            mPoint = point;
            mSharedPoint = sharedPoint;
            mCurrentlyReading = currentlyReading;
            mOnRoadBooks = onRoadBooks;
            mReadBooks = readBooks;
            mBooksOnHand = booksOnHand;
            mSharedBooks = sharedBooks;
        }

        public Details(String password, String email, boolean verified, String bio, int bookCounter, int point, int sharedPoint) {
            mPassword = password;
            mEmail = email;
            mVerified = verified;
            mBio = bio;
            mBookCounter = bookCounter;
            mPoint = point;
            mSharedPoint = sharedPoint;
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

        public int getSharedPoint() {
            return mSharedPoint;
        }

        public void setSharedPoint(int point) {
            mSharedPoint = point;
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

        public ArrayList<Book> getOnRoadBooks() {
            return mOnRoadBooks;
        }

        public int getOnRoadBooksCount(){
            return mOnRoadBooks != null ? mOnRoadBooks.size() : 0;
        }

        public void setOnRoadBooks(ArrayList<Book> mOnRoadBooks) {
            this.mOnRoadBooks = mOnRoadBooks;
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

            StringBuilder stringBuilder = new StringBuilder();

            if (mCurrentlyReading != null && mCurrentlyReading.size() > 0) {
                for (Book book : mCurrentlyReading) {
                    stringBuilder.append(book).append("\n");
                }
            }else {
                stringBuilder.append("null\n");
            }

            String currentlyReading = stringBuilder.toString();

            stringBuilder = new StringBuilder();

            if (mOnRoadBooks != null && mOnRoadBooks.size() > 0) {
                for (Book book : mOnRoadBooks) {
                    stringBuilder.append(book).append("\n");
                }
            } else {
                stringBuilder.append("null\n");
            }

            String onRoadBooks = stringBuilder.toString();

            stringBuilder = new StringBuilder();

            if (mBooksOnHand != null && mBooksOnHand.size() > 0) {
                for (Book book : mBooksOnHand) {
                    stringBuilder.append(book).append("\n");
                }
            } else {
                stringBuilder.append("null\n");
            }

            String booksOnHand = stringBuilder.toString();

            stringBuilder = new StringBuilder();

            if (mReadBooks != null && mReadBooks.size() > 0) {
                for (Book book : mReadBooks) {
                    stringBuilder.append(book).append("\n");
                }
            } else {
                stringBuilder.append("null\n");
            }

            String readBooks = stringBuilder.toString();

            return User.this.toString() + ".Details{" +
                    "\nmPassword='" + mPassword + '\'' +
                    ",\nmEmail='" + mEmail + '\'' +
                    ",\nmVerified=" + mVerified +
                    ",\nmBio='" + mBio + '\'' +
                    ",\nmBookCounter=" + mBookCounter +
                    ",\nmPoint=" + mPoint +
                    ",\nmCurrentlyReading=\n" + currentlyReading +
                    ",mOnRoadBooks=\n" + onRoadBooks +
                    ",mBooksOnHand=\n" + booksOnHand +
                    ",mReadBooks=\n" + readBooks + '}';
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
                    new LatLng(random.nextDouble() + random.nextInt(180) - 90,
                            random.nextDouble() + random.nextInt(180)));
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

            ArrayList<Book> booksOnRoad = Book.GENERATOR.generateBookList(random.nextInt(15));
            ArrayList<Book> booksOnHand = Book.GENERATOR.generateBookList(random.nextInt(15), user);
            ArrayList<Book> booksRead = Book.GENERATOR.generateBookList(random.nextInt(15));
            ArrayList<Book> booksShared = Book.GENERATOR.generateBookList(random.nextInt(15));


            return user.new Details("********", name2Email(user.mName), true, BIO, random.nextInt(5),
                    random.nextInt(300), random.nextInt(300), currentlyReading, booksOnRoad, booksRead, booksOnHand, booksShared);
        }

        private static String name2Email(String name) {
            name = name.replace(" ", "").toLowerCase().trim();
            return name + "@gmail.com";
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof User && this.mID == ((User) obj).getID();
    }
}