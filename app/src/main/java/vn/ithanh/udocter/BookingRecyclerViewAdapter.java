package vn.ithanh.udocter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.text.ParseException;
import java.util.ArrayList;

import vn.ithanh.udocter.model.BOOKING_INFO;
import vn.ithanh.udocter.util.Config;
import vn.ithanh.udocter.util.TimeUtils;
import vn.ithanh.udocter.util.Utils;

/**
 * Created by iThanh on 12/7/2017.
 */

public class BookingRecyclerViewAdapter extends RecyclerView.Adapter<BookingRecyclerViewAdapter.ViewHolder> {

    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private ArrayList<BOOKING_INFO> mValues;

    private ImageLoader uilImageLoader;
    private DisplayImageOptions options;

    Context mcontext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public String mBoundString;

        public final View mView;
        public final ImageView mImageView;
        public final TextView mTextView;
        public final TextView mTextViewDes;
        public final TextView tvItem_price;

        public final LinearLayout llStatus;
        public final TextView tvStatus;

        public final ImageView ivstar1;
        public final ImageView ivstar2;
        public final ImageView ivstar3;
        public final ImageView ivstar4;
        public final ImageView ivstar5;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.ivItem_pic);
            mTextView = (TextView) view.findViewById(R.id.tvItem_name);
            mTextViewDes = (TextView) view.findViewById(R.id.tvItem_dec);
            tvItem_price = (TextView) view.findViewById(R.id.tvItem_price);
            llStatus = (LinearLayout) view.findViewById(R.id.llStatus);
            tvStatus = (TextView) view.findViewById(R.id.tvStatus);

            ivstar1 = view.findViewById(R.id.ivstar1);
            ivstar2 = view.findViewById(R.id.ivstar2);
            ivstar3 = view.findViewById(R.id.ivstar3);
            ivstar4 = view.findViewById(R.id.ivstar4);
            ivstar5 = view.findViewById(R.id.ivstar5);

            ivstar1.setVisibility(View.GONE);
            ivstar2.setVisibility(View.GONE);
            ivstar3.setVisibility(View.GONE);
            ivstar4.setVisibility(View.GONE);
            ivstar5.setVisibility(View.GONE);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }
    }

    public BookingRecyclerViewAdapter(Context context, ArrayList<BOOKING_INFO> item) {
        mcontext = context;
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        mValues = item;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.booking_docter_row, parent, false);
        view.setBackgroundResource(mBackground);


        uilImageLoader = MyApplication.uilImageLoader;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.img_loadding)
                .showImageForEmptyUri(R.drawable.img_loadding)
                .showImageOnFail(R.drawable.img_loadding)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(
                        new RoundedBitmapDisplayer(Config.CORNER_RADIUS_PIXELS))
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        return new ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final  BOOKING_INFO booking_info = mValues.get(position);

        int status = booking_info.getStatus();
        holder.mBoundString = booking_info.getId() + "";
        holder.mTextView.setText(booking_info.getPatient_name());

        if (position%2 == 0)
            holder.itemView.setBackgroundResource(R.drawable.row_booking_backgroud);
        else
            holder.itemView.setBackgroundColor(Color.WHITE);

        try {
            holder.tvItem_price.setText(TimeUtils.millisToLongDHMS(booking_info.getCreated()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bookingIntent = new Intent(mcontext, BookingDetailActivity.class);
                bookingIntent.putExtra(BookingDetailActivity.EXTRA_BOOKING_ID, booking_info.getId() + "");
                mcontext.startActivity(bookingIntent);

            }
        });
            if ( status == 0) {
                holder.llStatus.setBackgroundResource(R.drawable.choxacnhan_backgroud);
                holder.tvStatus.setText("Chờ xác nhận");
                holder.mTextViewDes.setText("Xác nhận để lấy số điện thoại");
            }
            if ( status == 1) {
                holder.llStatus.setBackgroundResource(R.drawable.xacnhan_backgroud);
                holder.tvStatus.setText("BS xác nhận");
                holder.mTextViewDes.setText("ĐT : " + booking_info.getPatient_phone());
            }
            if ( status == 2) {
                holder.llStatus.setBackgroundResource(R.drawable.hoanthanh_backgroud);
                holder.tvStatus.setText("Hoàn thành");
            }
            if ( status == 3) {
                holder.llStatus.setBackgroundResource(R.drawable.huy_backgroud);
                holder.tvStatus.setText("Đã hủy");
            }
            if ( status == 4) {
                holder.llStatus.setBackgroundResource(R.drawable.huy_backgroud);
                holder.tvStatus.setText("BS Đã hủy");
            }

        if (booking_info.getRate() > 0){
            holder.ivstar1.setVisibility(View.VISIBLE);
        }
        if (booking_info.getRate() > 1){
            holder.ivstar2.setVisibility(View.VISIBLE);
        }
        if (booking_info.getRate() > 2){
            holder.ivstar3.setVisibility(View.VISIBLE);
        }
        if (booking_info.getRate() > 3){
            holder.ivstar4.setVisibility(View.VISIBLE);
        }
        if (booking_info.getRate() > 4){
            holder.ivstar5.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}