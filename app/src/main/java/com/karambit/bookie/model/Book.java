package com.karambit.bookie.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.karambit.bookie.helper.ImageLinkSource;

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
    private User mOwner;

    public Book(int ID, String name, String imageURL, String thumbnailURL, String author, State state, User owner) {
        mID = ID;
        mName = name;
        mImageURL = imageURL;
        mThumbnailURL = thumbnailURL;
        mAuthor = author;
        mState = state;
        mOwner = owner;
    }

    protected Book(Parcel in) {
        mID = in.readInt();
        mName = in.readString();
        mImageURL = in.readString();
        mThumbnailURL = in.readString();
        mAuthor = in.readString();
        mState = (State) in.readSerializable();
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
        dest.writeParcelable(mOwner, flags);
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

        READ_START(0),
        READ_STOP(1),
        OPEN_TO_SHARE(2),
        CLOSE_TO_SHARE(3),
        ADD(4);                                                 //TODO TODO TODO

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

    public User getOwner() {
        return mOwner;
    }

    public void setOwner(User owner) {
        mOwner = owner;
    }

    @Override
    public String toString() {
        return "Book{" +
                "mID=" + mID +
                ", mName='" + mName + '\'' +
                ", mImageURL='" + mImageURL + '\'' +
                ", mThumbnailURL='" + mThumbnailURL + '\'' +
                ", mAuthor='" + mAuthor + '\'' +
                ", mState=" + mState +
                ", mOwner=" + mOwner +
                '}';
    }

    public class Details {

        private int mGenreCode;
        private User mAddedBy;
        private ArrayList<BookProcess> mBookProcesses;

        public Details(int genreCode, User addedBy, ArrayList<BookProcess> bookProcesses) {
            mGenreCode = genreCode;
            mAddedBy = addedBy;
            mBookProcesses = bookProcesses;
        }

        public int getGenreCode() {
            return mGenreCode;
        }

        public void setGenreCode(int genreCode) {
            mGenreCode = genreCode;
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
                    "mGenreCode=" + mGenreCode +
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
            return Book.this.toString() + ".Transaction{" +
                    "mTransactionType=" + mTransactionType +
                    ", mFromUser=" + mFromUser +
                    ", mToUser=" + mToUser +
                    ", mCreatedAt=" + mCreatedAt + '}';
        }

        @Override
        public void accept(TimelineDisplayableVisitor visitor) {
            visitor.visit(this);
        }
    }

    public class Request implements BookProcess {
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

        @Override
        public String toString() {
            return Book.this.toString() + ".Request{" +
                    "mRequestType=" + mRequestType +
                    ", mFromUser=" + mFromUser +
                    ", mToUser=" + mToUser +
                    ", mCreatedAt=" + mCreatedAt + '}';
        }

        @Override
        public void accept(TimelineDisplayableVisitor visitor) {
            visitor.visit(this);
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

            int randomIndex = random.nextInt(ImageLinkSource.IMAGE_THUMBNAIL_URLS.length);
            String bookImageUrl = ImageLinkSource.IMAGE_URLS[randomIndex];
            String bookThumbnailUrl = ImageLinkSource.IMAGE_THUMBNAIL_URLS[randomIndex];

            return new Book(random.nextInt(), bookName, bookImageUrl, bookThumbnailUrl, author, state, owner);
        }

        public static ArrayList<Book> generateBookList(int count) {
            ArrayList<Book> books = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                books.add(generateBook());
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
            int genre = random.nextInt(100);
            return book.new Details(genre, addedBy, generateBookProcesses(random.nextInt(50)));
        }

        public static ArrayList<BookProcess> generateBookProcesses(int count) {
            ArrayList<BookProcess> bookProcesses = new ArrayList<>();

            for (int i = 0; i < count; i++) {

                Book book = Book.GENERATOR.generateBook();
                User user1 = User.GENERATOR.generateUser();
                User user2 = User.GENERATOR.generateUser();

                Random random = new Random();

                Book.BookProcess bookProcess;

                switch (random.nextInt(11)) {
                    case 0:
                        bookProcess = book.new Interaction(Book.InteractionType.ADD, user1, Calendar.getInstance());
                        break;
                    case 1:
                        bookProcess = book.new Interaction(Book.InteractionType.READ_START, user1, Calendar.getInstance());
                        break;
                    case 2:
                        bookProcess = book.new Interaction(Book.InteractionType.READ_STOP, user1, Calendar.getInstance());
                        break;
                    case 3:
                        bookProcess = book.new Interaction(Book.InteractionType.CLOSE_TO_SHARE, user1, Calendar.getInstance());
                        break;
                    case 4:
                        bookProcess = book.new Interaction(Book.InteractionType.OPEN_TO_SHARE, user1, Calendar.getInstance());
                        break;
                    case 5:
                        bookProcess = book.new Transaction(user1, Book.TransactionType.COME_TO_HAND, user2, Calendar.getInstance());
                        break;
                    case 6:
                        bookProcess = book.new Transaction(user1, Book.TransactionType.DISPACTH, user2, Calendar.getInstance());
                        break;
                    case 7:
                        bookProcess = book.new Transaction(user1, Book.TransactionType.LOST, user2, Calendar.getInstance());
                        break;
                    case 8:
                        bookProcess = book.new Request(Book.RequestType.SEND, user1, user2, Calendar.getInstance());
                        break;
                    case 9:
                        bookProcess = book.new Request(Book.RequestType.ACCEPT, user1, user2, Calendar.getInstance());
                        break;
                    case 10:
                        bookProcess = book.new Request(Book.RequestType.REJECT, user1, user2, Calendar.getInstance());
                        break;

                    default:
                        bookProcess = null;
                }
                bookProcesses.add(bookProcess);
            }
            return bookProcesses;
        }
    }
}