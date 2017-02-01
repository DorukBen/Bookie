package com.karambit.bookie.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karambit.bookie.BookActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.infinite_viewpager.HorizontalInfiniteCycleViewPager;
import com.karambit.bookie.helper.infinite_viewpager.InfiniteCyclePagerAdapter;
import com.karambit.bookie.model.Book;

import java.util.ArrayList;


/**
 * Created by GIGAMOLE on 7/27/16.
 */
public class HorizontalPagerAdapter extends InfiniteCyclePagerAdapter {

    private ArrayList<Book> mBooks = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mLayoutInflater;

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
    public int getItemPosition(final Object object) {
        return POSITION_NONE;
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

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, BookActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("book", mBooks.get(position));
                intent.putExtras(bundle);
                mContext.startActivity(intent);

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

    public void setBooks(ArrayList<Book> books){
        mBooks.clear();
        mBooks.addAll(books);
    }
}
