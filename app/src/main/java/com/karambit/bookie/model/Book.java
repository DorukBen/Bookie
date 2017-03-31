package com.karambit.bookie.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Orcan on 8/2/2016.
 * <p/>
 * All Book information will held by this Class
 */
public class Book implements Parcelable {

    private int mID;
    private String mName;
    private String mImageURL;
    private String mThumbnailURL;
    private String mAuthor;
    private State mState;
    private int mGenreCode;
    private User mOwner;

    public Book(int ID, String name, String imageURL, String thumbnailURL, String author, State state, int genreCode, User owner) {
        mID = ID;
        mName = name;
        mImageURL = imageURL;
        mThumbnailURL = thumbnailURL;
        mAuthor = author;
        mState = state;
        mGenreCode = genreCode;
        mOwner = owner;
    }

    protected Book(Parcel in) {
        mID = in.readInt();
        mName = in.readString();
        mImageURL = in.readString();
        mThumbnailURL = in.readString();
        mAuthor = in.readString();
        mState = (State) in.readSerializable();
        mGenreCode = in.readInt();
        mOwner = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mID);
        dest.writeString(mName);
        dest.writeString(mImageURL);
        dest.writeString(mThumbnailURL);
        dest.writeString(mAuthor);
        dest.writeSerializable(mState);
        dest.writeInt(mGenreCode);
        dest.writeParcelable(mOwner, flags);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Book && mID == ((Book) obj).getID();

    }

    //This enum state specifies situation for book
    //Server interprets datas and returns books state as this enum names
    public enum State {

        READING(0),
        OPENED_TO_SHARE(1),
        CLOSED_TO_SHARE(2),
        ON_ROAD(3),
        LOST(4);

        private final int mStateCode;

        State(int code) {
            mStateCode = code;
        }

        public int getStateCode() {
            return mStateCode;
        }

        public static State valueOf(int stateCode) {
            if (stateCode == READING.mStateCode) {
                return READING;
            } else if (stateCode == OPENED_TO_SHARE.mStateCode) {
                return OPENED_TO_SHARE;
            } else if (stateCode == CLOSED_TO_SHARE.mStateCode) {
                return CLOSED_TO_SHARE;
            } else if (stateCode == ON_ROAD.mStateCode) {
                return ON_ROAD;
            } else if (stateCode == LOST.mStateCode) {
                return LOST;
            } else {
                throw new IllegalArgumentException("Invalid Book state");
            }
        }
    }

    public int getID() {
        return mID;
    }

    public void setID(int ID) {
        mID = ID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageURL() {
        return mImageURL;
    }

    public void setImageURL(String imageURL) {
        mImageURL = imageURL;
    }

    public String getThumbnailURL() {
        return mThumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        mThumbnailURL = thumbnailURL;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public State getState() {
        return mState;
    }

    public void setState(State state) {
        mState = state;
    }

    public int getGenreCode() {
        return mGenreCode;
    }

    public void setGenreCode(int genreCode) {
        mGenreCode = genreCode;
    }

    public User getOwner() {
        return mOwner;
    }

    public void setOwner(User owner) {
        mOwner = owner;
    }

    public static Book jsonObjectToBook(JSONObject bookObject) {
        try {
            if (bookObject != null) {
                return new Book(bookObject.isNull("ID") ? -1 : bookObject.getInt("ID"),
                                bookObject.isNull("bookName") || TextUtils.isEmpty(bookObject.getString("bookName")) ? null : bookObject.getString("bookName"),
                                bookObject.isNull("bookPictureURL") || TextUtils.isEmpty(bookObject.getString("bookPictureURL")) ? null : bookObject.getString("bookPictureURL"),
                                bookObject.isNull("bookPictureThumbnailURL") || TextUtils.isEmpty(bookObject.getString("bookPictureThumbnailURL")) ? null : bookObject.getString("bookPictureThumbnailURL"),
                                bookObject.isNull("author") || TextUtils.isEmpty(bookObject.getString("author")) ? null : bookObject.getString("author"),
                                State.valueOf(bookObject.isNull("bookState") ? 2 : bookObject.getInt("bookState")),
                                bookObject.isNull("genreCode") ? 0 : bookObject.getInt("genreCode"),
                                User.jsonObjectToUser(bookObject.isNull("owner") ? null : bookObject.getJSONObject("owner")));
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Book> jsonArrayToBookList(JSONArray bookJsonArray) {
        ArrayList<Book> books = new ArrayList<>(bookJsonArray.length());
        for (int i = 0; i < bookJsonArray.length(); i++) {
            try {
                JSONObject bookObject = bookJsonArray.getJSONObject(i);
                books.add(jsonObjectToBook(bookObject));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return books;
    }

    public static Book.Details jsonObjectToBookDetails(@NonNull JSONObject bookDetailsObject) {
        try {
            Book book = jsonObjectToBook(bookDetailsObject);
            if (book == null) {
                throw new JSONException("Book object is null");
            }

            User addedBy = User.jsonObjectToUser(bookDetailsObject.optJSONObject("addedBy"));
            if (addedBy == null) {
                throw new JSONException("User object is null");
            }

            ArrayList<Book.BookProcess> bookProcesses = new ArrayList<>();

            if (!bookDetailsObject.isNull("bookInteractions")) {
                JSONArray jsonArray = bookDetailsObject.getJSONArray("bookInteractions");
                bookProcesses.addAll(Interaction.jsonArrayToInteractionList(book, jsonArray));
            } else {
                throw new JSONException("bookInteractions key is null");
            }

            if (!bookDetailsObject.isNull("bookTransactions")) {
                JSONArray jsonArray = bookDetailsObject.getJSONArray("bookTransactions");
                bookProcesses.addAll(Transaction.jsonArrayToTransactionList(book, jsonArray));
            } else {
                throw new JSONException("bookTransactions key is null");
            }

            if (!bookDetailsObject.isNull("bookRequests")) {
                JSONArray jsonArray = bookDetailsObject.getJSONArray("bookRequests");
                bookProcesses.addAll(Request.jsonArrayToRequestList(book, jsonArray));
            } else {
                throw new JSONException("bookRequests key is null");
            }

            return book.new Details(
                addedBy,
                bookProcesses);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "\nBook{" +
            "\n\tmID=" + mID + "," +
            "\n\tmName='" + mName + "'," +
            "\n\tmImageURL='" + mImageURL + "'," +
            "\n\tmThumbnailURL='" + mThumbnailURL + "'," +
            "\n\tmAuthor='" + mAuthor + "'," +
            "\n\tmState=" + mState + "," +
            "\n\tmGenreCode=" + mGenreCode + "," +
            "\n\tmOwner=" + mOwner +
            "\n}\n";
    }

    public String toShortString() {
        return "\nBook{" +
            "\n\tmID=" + mID + "," +
            "\n\tmName='" + mName + "'," +
            "\n\tmState=" + mState + "," +
            "\n\tmOwner=" + mOwner.getName() + "," +
            "\n\tmGenreCode=" + mGenreCode +
            "\n}\n";
    }

    public static String listToShortString(ArrayList<Book> books) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("BookList[");

        for (Book book : books) {
            stringBuilder.append(book.toShortString());
        }

        stringBuilder.append("\n]");

        return stringBuilder.toString();
    }

    public class Details {

        private User mAddedBy;
        private ArrayList<BookProcess> mBookProcesses;

        public Details(User addedBy, ArrayList<BookProcess> bookProcesses) {
            mAddedBy = addedBy;
            mBookProcesses = bookProcesses;
        }

        public User getAddedBy() {
            return mAddedBy;
        }

        public void setAddedBy(User addedBy) {
            mAddedBy = addedBy;
        }

        public ArrayList<BookProcess> getBookProcesses() {
            return mBookProcesses;
        }

        public void setBookProcesses(ArrayList<BookProcess> bookProcesses) {
            mBookProcesses = bookProcesses;
        }

        public Book getBook() {
            return Book.this;
        }

        @Override
        public String toString() {
            return "\n" + Book.this.toString() + "\nDetails{" +
                "\nmAddedBy=" + mAddedBy +
                "\n, mBookProcesses=" + mBookProcesses +
                "\n}\n";
        }
    }


    /**
     * Visitor Pattern used. So the different classes can be stored as similar DataType
     * and no need of switch for different operations
     */
    public interface BookProcess {
        void accept(TimelineDisplayableVisitor visitor);

        Calendar getCreatedAt();
    }

    public interface TimelineDisplayableVisitor {
        void visit(Interaction interaction);

        void visit(Transaction transaction);

        void visit(Request request);
    }
}