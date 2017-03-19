package com.karambit.bookie.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.infinite_viewpager.InfiniteCyclePagerAdapter;
import com.karambit.bookie.model.Book;

import java.util.ArrayList;


/**
 * Created by GIGAMOLE on 7/27/16.
 */
public class HorizontalPagerAdapter extends InfiniteCyclePagerAdapter {

    public static final String TAG = HorizontalPagerAdapter.class.getSimpleName();

    private ArrayList<Book> mBooks = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private BookClickListener mBookClickListener;

    public HorizontalPagerAdapter(final Context context, ArrayList<Book> books) {
        mContext = context;
        mBooks = books;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return mBooks.indexOf(object);
    }

    @Override
    public Object instantiateItemView(final ViewGroup container, final int position) {
        View view = mLayoutInflater.inflate(R.layout.item_infinite_cycle_view_pager, container, false);

        ImageView bookImage = (ImageView) view.findViewById(R.id.bookImageInfiniteCycle);

        Glide.with(mContext)
                .load(mBooks.get(position).getThumbnailURL())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.placeholder_192dp)
                .error(R.drawable.error_192dp)
                .into(bookImage);

        ((TextView)view.findViewById(R.id.bookNameInfiniteCycleTextView)).setText(mBooks.get(position).getName());
        ((TextView)view.findViewById(R.id.authorInfiniteCycleTextView)).setText(mBooks.get(position).getAuthor());

        view.findViewById(R.id.horizontalPagerCardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBookClickListener != null){
                    mBookClickListener.onBookClick(mBooks.get(position));
                }
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItemView(final ViewGroup container, final int position, final Object object) {
        container.removeView((View) object);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }

    void setBooks(ArrayList<Book> books){
        mBooks = books;
        Log.i(TAG, "Books set: " + books);
    }

    public interface BookClickListener {
        void onBookClick(Book book);
    }

    public BookClickListener getBookClickListener() {
        return mBookClickListener;
    }

    public void setBookClickListener(BookClickListener bookClickListener) {
        mBookClickListener = bookClickListener;
    }
}
