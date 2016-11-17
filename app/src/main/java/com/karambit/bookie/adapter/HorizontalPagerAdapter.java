package com.karambit.bookie.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karambit.bookie.R;
import com.karambit.bookie.model.Book;

import java.util.ArrayList;

/**
 * Created by doruk on 16.11.2016.
 */
public class HorizontalPagerAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private ArrayList<Book> mBooks;

    public HorizontalPagerAdapter(final Context context, ArrayList<Book> books) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mBooks = books;
    }

    @Override
    public int getCount() {
        return mBooks.size();
    }

    @Override
    public int getItemPosition(final Object object) {
        return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        View view = mLayoutInflater.inflate(R.layout.item_infinite_cycle_view_pager, container, false);

        ImageView bookImage = (ImageView) view.findViewById(R.id.bookImageInfiniteCycle);

        Glide.with(mContext)
                .load(mBooks.get(position).getThumbnailURL())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.placeholder_book)
                .into(bookImage);

        ((TextView)view.findViewById(R.id.bookNameInfiniteCycleTextView)).setText(mBooks.get(position).getName());
        ((TextView)view.findViewById(R.id.authorInfiniteCycleTextView)).setText(mBooks.get(position).getAuthor());


        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        container.removeView((View) object);
    }


    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }
}
