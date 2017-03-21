package com.karambit.bookie.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

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
            switch (stateCode) {
                case 0:
                    return State.READING;
                case 1:
                    return State.OPENED_TO_SHARE;
                case 2:
                    return State.CLOSED_TO_SHARE;
                case 3:
                    return State.ON_ROAD;
                case 4:
                    return State.LOST;
                default:
                    return null;
            }
        }
    }

    public enum InteractionType {

        ADD(0),
        READ_START(1),
        READ_STOP(2),
        OPEN_TO_SHARE(3),
        CLOSE_TO_SHARE(4);

        private final int mInteractionCode;

        InteractionType(int code) {
            mInteractionCode = code;
        }

        public int getInteractionCode() {
            return mInteractionCode;
        }
    }

    public enum TransactionType {

        DISPACTH(8),
        COME_TO_HAND(9),
        LOST(10);

        private final int mTransactionCode;

        TransactionType(int code) {
            mTransactionCode = code;
        }

        public int getTransactionCode() {
            return mTransactionCode;
        }
    }

    public enum RequestType {

        SEND(5),
        ACCEPT(6),
        REJECT(7);

        private final int mRequestCode;

        RequestType(int code) {
            mRequestCode = code;
        }

        public int getRequestCode() {
            return mRequestCode;
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
            if (bookObject != null){
                return new Book(bookObject.isNull("ID")? -1: bookObject.getInt("ID"),
                        bookObject.isNull("bookName")|| TextUtils.isEmpty(bookObject.getString("bookName"))? null: bookObject.getString("bookName"),
                        bookObject.isNull("bookPictureURL")|| TextUtils.isEmpty(bookObject.getString("bookPictureURL"))? null: bookObject.getString("bookPictureURL"),
                        bookObject.isNull("bookPictureThumbnailURL")|| TextUtils.isEmpty(bookObject.getString("bookPictureThumbnailURL"))? null: bookObject.getString("bookPictureThumbnailURL"),
                        bookObject.isNull("author")|| TextUtils.isEmpty(bookObject.getString("author"))? null: bookObject.getString("author"),
                        State.valueOf(bookObject.isNull("bookState")? 2: bookObject.getInt("bookState")),
                        bookObject.isNull("genreCode")? 0: bookObject.getInt("genreCode"),
                        User.jsonObjectToUser(bookObject.isNull("owner")? null: bookObject.getJSONObject("owner")));
            }else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Book.Details jsonObjectToBookDetails(JSONObject bookDetailsObject) {
        try {
            if (bookDetailsObject != null){
                Book book = jsonObjectToBook(bookDetailsObject);
                User added_by = User.jsonObjectToUser(bookDetailsObject.isNull("addedBy")? null: bookDetailsObject.getJSONObject("addedBy"));

                ArrayList<Book.BookProcess> bookProcesses = new ArrayList<>();

                if (!bookDetailsObject.isNull("bookInteractions")){
                    JSONArray jsonArray = bookDetailsObject.getJSONArray("bookInteractions");
                    for (int i = 0; i < jsonArray.length(); i++){
                        bookProcesses.add(book.new Interaction(InteractionType.values()[jsonArray.getJSONObject(i).getInt("interactionType") - Book.InteractionType.values()[0].getInteractionCode()],
                                User.jsonObjectToUser(jsonArray.getJSONObject(i).getJSONObject("user")),
                                jsonArray.getJSONObject(i).getString("createdAt")));
                    }
                }

                if (!bookDetailsObject.isNull("bookTransactions")){
                    JSONArray jsonArray = bookDetailsObject.getJSONArray("bookTransactions");
                    for (int i = 0; i < jsonArray.length(); i++){
                        bookProcesses.add(book.new Transaction(User.jsonObjectToUser(jsonArray.getJSONObject(i).getJSONObject("fromUser")),
                                Book.TransactionType.values()[jsonArray.getJSONObject(i).getInt("transactionType") - Book.TransactionType.values()[0].getTransactionCode()],
                                User.jsonObjectToUser(jsonArray.getJSONObject(i).getJSONObject("toUser")),
                                jsonArray.getJSONObject(i).getString("createdAt")));
                    }
                }

                if (!bookDetailsObject.isNull("bookRequests")){
                    JSONArray jsonArray = bookDetailsObject.getJSONArray("bookRequests");
                    for (int i = 0; i < jsonArray.length(); i++){
                        bookProcesses.add(book.new Request(Book.RequestType.values()[jsonArray.getJSONObject(i).getInt("requestType") - Book.RequestType.values()[0].getRequestCode()],
                                User.jsonObjectToUser(jsonArray.getJSONObject(i).getJSONObject("toUser")),
                                User.jsonObjectToUser(jsonArray.getJSONObject(i).getJSONObject("fromUser")),
                                jsonArray.getJSONObject(i).getString("createdAt")));
                    }
                }

                return book.new Details(
                        added_by,
                        bookProcesses);
            }else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Book.Request> jsonObjectToBookRequests(JSONObject jsonObject, Book book){
        ArrayList<Book.Request> bookRequests = new ArrayList<>();
        try {
            if (!jsonObject.isNull("bookRequests")){
                JSONArray jsonArray = jsonObject.getJSONArray("bookRequests");
                for (int i = 0; i < jsonArray.length(); i++){
                    bookRequests.add(book.new Request(Book.RequestType.values()[jsonArray.getJSONObject(i).getInt("requestType") - Book.RequestType.values()[0].getRequestCode()],
                            User.jsonObjectToUser(jsonArray.getJSONObject(i).getJSONObject("toUser")),
                            User.jsonObjectToUser(jsonArray.getJSONObject(i).getJSONObject("fromUser")),
                            jsonArray.getJSONObject(i).getString("createdAt")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bookRequests;

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

    @Override
    public String toString() {
        return "Book{" +
                "mID=" + mID +
                ", mName='" + mName + '\'' +
                ", mState=" + mState + '}';
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
            return Book.this.toString() + ".Details{" +
                    ", mAddedBy=" + mAddedBy + '}';
        }
    }

    public class Interaction implements BookProcess {

        private InteractionType mInteractionType;
        private User mUser;
        private Calendar mCreatedAt;


        public Interaction(InteractionType interactionType, User user, Calendar createdAt) {
            mInteractionType = interactionType;
            mUser = user;
            mCreatedAt = createdAt;
        }

        public Interaction(InteractionType interactionType, User user, String createdAt) {
            mInteractionType = interactionType;
            mUser = user;
            Timestamp timestamp = Timestamp.valueOf(createdAt);
            mCreatedAt = Calendar.getInstance();
            mCreatedAt.setTimeInMillis(timestamp.getTime());
        }

        public User getUser() {
            return mUser;
        }

        public Book getBook() {
            return Book.this;
        }

        public InteractionType getInteractionType() {
            return mInteractionType;
        }

        public Calendar getCreatedAt() {
            return mCreatedAt;
        }

        @Override
        public String toString() {
            return Book.this.toString() + ".Interaction{" +
                    "mInteractionType=" + mInteractionType +
                    ", mUser=" + mUser +
                    ", mCreatedAt=" + mCreatedAt + '}';
        }

        @Override
        public void accept(TimelineDisplayableVisitor visitor) {
            visitor.visit(this);
        }
    }

    public class Transaction implements BookProcess {
        private TransactionType mTransactionType;
        private User mFromUser;
        private User mToUser;
        private Calendar mCreatedAt;

        public Transaction(User fromUser, TransactionType transactionType, User toUser, Calendar createdAt) {
            mFromUser = fromUser;
            mTransactionType = transactionType;
            mToUser = toUser;
            mCreatedAt = createdAt;
        }

        public Transaction(User fromUser, TransactionType transactionType, User toUser, String createdAt) {
            mFromUser = fromUser;
            mTransactionType = transactionType;
            mToUser = toUser;
            Timestamp timestamp = Timestamp.valueOf(createdAt);
            mCreatedAt = Calendar.getInstance();
            mCreatedAt.setTimeInMillis(timestamp.getTime());
        }

        public TransactionType getTransactionType() {
            return mTransactionType;
        }

        public User getToUser() {
            return mToUser;
        }

        public User getFromUser() {
            return mFromUser;
        }

        public Calendar getCreatedAt() {
            return mCreatedAt;
        }

        public Book getBook() {
            return Book.this;
        }

        @Override
        public String toString() {
            return Book.this.getName() + ".Transaction{" +
                    "mTransactionType=" + mTransactionType +
                    ", mFromUser=" + mFromUser.getName() +
                    ", mToUser=" + mToUser.getName() +
                    ", mCreatedAt=" + mCreatedAt.getTimeInMillis() + '}';
        }

        @Override
        public void accept(TimelineDisplayableVisitor visitor) {
            visitor.visit(this);
        }
    }

    public class Request implements BookProcess, Comparable<Book.Request> {
        private RequestType mRequestType;
        private User mFromUser;
        private User mToUser;
        private Calendar mCreatedAt;

        public Request(RequestType requestType, User toUser, User fromUser, Calendar createdAt) {
            mRequestType = requestType;
            mToUser = toUser;
            mFromUser = fromUser;
            mCreatedAt = createdAt;
        }

        public Request(RequestType requestType, User toUser, User fromUser, String createdAt) {
            mRequestType = requestType;
            mToUser = toUser;
            mFromUser = fromUser;
            Timestamp timestamp = Timestamp.valueOf(createdAt);
            mCreatedAt = Calendar.getInstance();
            mCreatedAt.setTimeInMillis(timestamp.getTime());
        }

        public RequestType getRequestType() {
            return mRequestType;
        }

        public User getToUser() {
            return mToUser;
        }

        public User getFromUser() {
            return mFromUser;
        }

        public Calendar getCreatedAt() {
            return mCreatedAt;
        }

        public Book getBook() {
            return Book.this;
        }

        public void setRequestType(RequestType requestType) {
            mRequestType = requestType;
        }

        public void setFromUser(User fromUser) {
            mFromUser = fromUser;
        }

        public void setToUser(User toUser) {
            mToUser = toUser;
        }

        public void setCreatedAt(Calendar createdAt) {
            mCreatedAt = createdAt;
        }

        @Override
        public String toString() {
            return Book.this.getName() + ".Request{" +
                    "mRequestType=" + mRequestType +
                    ", mFromUser=" + mFromUser.getName() +
                    ", mToUser=" + mToUser.getName() +
                    ", mCreatedAt=" + mCreatedAt.getTimeInMillis() + '}';
        }

        @Override
        public void accept(TimelineDisplayableVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public int compareTo(@NonNull Request otherRequest) {
            int ordinalDifference = this.mRequestType.ordinal() - otherRequest.getRequestType().ordinal();
            if (ordinalDifference != 0) {
                return ordinalDifference;
            } else {
                return (int) (otherRequest.getCreatedAt().getTimeInMillis() - this.mCreatedAt.getTimeInMillis());
            }
        }
    }


    /**
     * Visitor Pattern used. So the different classes can be stored as similar DataType
     * and no need of switch for different operations
     */
    public interface BookProcess {
        void accept(TimelineDisplayableVisitor visitor);
    }

    public interface TimelineDisplayableVisitor {
        void visit(Interaction interaction);

        void visit(Transaction transaction);

        void visit(Request request);
    }


    /**
     * This class contains all user generator methods
     * <p/>
     * TODO The "ImageLinkSource.java" must be added to project
     */
    public static class GENERATOR {

        private static final String[] BOOK_IMAGE_URLS = new String[] {
                "https://s-media-cache-ak0.pinimg.com/236x/0b/25/55/0b2555245e181c4ddee446d42c830eb1.jpg",
                "https://s-media-cache-ak0.pinimg.com/236x/6b/8a/ab/6b8aab839ccb7b6221a4f885eacaa2d0.jpg",
                "https://s-media-cache-ak0.pinimg.com/236x/cb/35/ce/cb35ce56998dd787890b6424677eb7f9.jpg",
                "http://i1.nyt.com/images/2015/12/09/books/review/09cover-kl/09cover-kl-blog480-v3.jpg",
                "http://tgoodman.com/images/portfolio/wolf2.jpg",
                "http://tgoodman.com/images/portfolio/wolf2.jpg",
                "https://image.freepik.com/free-vector/book-cover-with-polygonal-design_1048-1413.jpg",
                "http://designgrapher.com/wp-content/uploads/2013/10/book-cover-design-ideas3.jpg",
                "https://s-media-cache-ak0.pinimg.com/originals/f2/a8/fe/f2a8fe32c3a11ffbe26a1ccf7c119b38.jpg",
                "https://s-media-cache-ak0.pinimg.com/236x/c1/52/56/c15256d64cce734ece94c5011a1ef6a6.jpg",
                "https://www.wired.com/wp-content/uploads/2014/08/pm-frac.jpg",
                "https://www.wired.com/wp-content/uploads/2014/08/pm-nesbo.jpg",
                "https://www.wired.com/wp-content/uploads/2014/08/pm-foucault.jpg",
                "https://www.wired.com/wp-content/uploads/2014/08/pm-crime.jpg",
                "https://s-media-cache-ak0.pinimg.com/236x/46/3c/cf/463ccf4a27abba252e57259b9b0fef7c.jpg",
                "http://67.media.tumblr.com/tumblr_lhge766TWo1qz6f9yo1_500.jpg",
                "https://s-media-cache-ak0.pinimg.com/236x/aa/4c/4c/aa4c4c4c952202a0ba1fd4b2eaf6d74c.jpg",
                "https://mir-s3-cdn-cf.behance.net/projects/404/f3533838512253.Y3JvcCwxNzExLDEzMzgsNDA2LDU1.jpg",
                "http://ecx.images-amazon.com/images/I/41HZAjABEPL._SL500_AA300_.jpg",
                "https://d3by36x8sj6cra.cloudfront.net/assets/images/book/large/9780/7493/9780749397050.jpg",
                "https://d2npbuaakacvlz.cloudfront.net/images/uploaded/large-present/2013/3/8/umberto-eco-1362765138.jpg"
        };

        private static final String[] NAMES = new String[] {
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

        private static final String[] BOOK_NAMES = new String[] {
                "Raven Of The Forsaken",
                "Raven With Gold",
                "Snakes Without Sin",
                "Officers Of The Banished",
                "Agents And Humans",
                "Horses And Wolves",
                "Union Of Nightmares",
                "Beginning Without Honor",
                "Learning From The Shadows",
                "Eliminating The WorldStranger",
                "Butterfly Of Wood",
                "Blacksmiths Of Freedom",
                "Gods Of The South",
                "Rebels And Swindlers",
                "Lords And Cats",
                "Hope Of The North",
                "Design Without Flaws",
                "Vanishing Into Nightmares",
                "Going To The North"
        };

        public static Book generateBook() {
            Random random = new Random();

            User owner = User.GENERATOR.generateUser();
            String author = NAMES[random.nextInt(NAMES.length)];
            String bookName = BOOK_NAMES[random.nextInt(BOOK_NAMES.length)];
            State state = State.values()[random.nextInt(State.values().length)];

            int randomIndex = random.nextInt(BOOK_IMAGE_URLS.length);
            String bookImageUrl = BOOK_IMAGE_URLS[randomIndex];
            String bookThumbnailUrl = BOOK_IMAGE_URLS[randomIndex];

            int genre = random.nextInt(100);
            return new Book(random.nextInt(), bookName, bookImageUrl, bookThumbnailUrl, author, state, genre, owner);
        }

        public static Book generateBook(User user) {
            Random random = new Random();

            String author = NAMES[random.nextInt(NAMES.length)];
            String bookName = BOOK_NAMES[random.nextInt(BOOK_NAMES.length)];
            State state = State.values()[random.nextInt(State.values().length)];

            int randomIndex = random.nextInt(BOOK_IMAGE_URLS.length);
            String bookImageUrl = BOOK_IMAGE_URLS[randomIndex];
            String bookThumbnailUrl = BOOK_IMAGE_URLS[randomIndex];

            int genre = random.nextInt(100);
            return new Book(random.nextInt(), bookName, bookImageUrl, bookThumbnailUrl, author, state, genre, user);
        }

        public static ArrayList<Book> generateBookList(int count) {
            return generateBookList(count, User.GENERATOR.generateUser());
        }

        public static ArrayList<Book> generateBookList(int count, User user) {
            ArrayList<Book> books = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                books.add(generateBook(user));
            }

            return books;
        }

        public static Book.Details generateBookDetails() {
            return generateBookDetails(generateBook());
        }

        /**
         * @param book is referenced for newly created User.Details object
         */
        public static Book.Details generateBookDetails(Book book) {
            User addedBy = User.GENERATOR.generateUser();
            Random random = new Random();
            return book.new Details(addedBy, generateBookProcesses(random.nextInt(50)));
        }

        public static ArrayList<BookProcess> generateBookProcesses(int count) {
            ArrayList<BookProcess> bookProcesses = new ArrayList<>();

            Book book = generateBook();
            User user1 = User.GENERATOR.generateUser();

            Calendar createBookCalendar = Calendar.getInstance();
            createBookCalendar.setTimeInMillis(createBookCalendar.getTimeInMillis() - 15000000000L);
            createBookCalendar.setTimeInMillis(createBookCalendar.getTimeInMillis() - 100000);
            bookProcesses.add(book.new Interaction(InteractionType.ADD, user1, createBookCalendar));

            Calendar calendar = Calendar.getInstance();

            for (int i = 0; i < count; i++) {

                book = GENERATOR.generateBook();
                user1 = User.GENERATOR.generateUser();
                User user2 = User.GENERATOR.generateUser();

                Random random = new Random();

                Book.BookProcess bookProcess;

                switch (random.nextInt(10)) {
                    case 0:
                        bookProcess = book.new Interaction(Book.InteractionType.READ_START, user1, calendar);
                        break;
                    case 1:
                        bookProcess = book.new Interaction(Book.InteractionType.READ_STOP, user1, calendar);
                        break;
                    case 2:
                        bookProcess = book.new Interaction(Book.InteractionType.CLOSE_TO_SHARE, user1, calendar);
                        break;
                    case 3:
                        bookProcess = book.new Interaction(Book.InteractionType.OPEN_TO_SHARE, user1, calendar);
                        break;
                    case 4:
                        bookProcess = book.new Transaction(user1, Book.TransactionType.COME_TO_HAND, user2, calendar);
                        break;
                    case 5:
                        bookProcess = book.new Transaction(user1, Book.TransactionType.DISPACTH, user2, calendar);
                        break;
                    case 6:
                        bookProcess = book.new Transaction(user1, Book.TransactionType.LOST, user2, calendar);
                        break;
                    case 7:
                        bookProcess = book.new Request(Book.RequestType.SEND, user1, user2, calendar);
                        break;
                    case 8:
                        bookProcess = book.new Request(Book.RequestType.ACCEPT, user1, user2, calendar);
                        break;
                    case 9:
                        bookProcess = book.new Request(Book.RequestType.REJECT, user1, user2, calendar);
                        break;

                    default:
                        bookProcess = null;
                }
                bookProcesses.add(bookProcess);

                calendar = (Calendar) calendar.clone();
                calendar.setTimeInMillis(calendar.getTimeInMillis() - random.nextInt(1000000000));
            }
            return bookProcesses;
        }
    }
}