package vn.ithanh.udocter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import vn.ithanh.udocter.model.SERVICES_UD;
import vn.ithanh.udocter.util.Utils;

public class ServiceRecyclerViewAdapter
        extends RecyclerView.Adapter<ServiceRecyclerViewAdapter.ViewHolder> {

    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private ArrayList<SERVICES_UD> mValues;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public String mBoundString;

        public final View mView;
        public final TextView tvUnit_name;
        public final TextView tvUnit_price;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvUnit_name = (TextView) view.findViewById(R.id.tvUnit_name);
            tvUnit_price = (TextView) view.findViewById(R.id.tvUnit_price);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + tvUnit_name.getText();
        }
    }

    public ServiceRecyclerViewAdapter(Context context, ArrayList<SERVICES_UD> items) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.services_row, parent, false);
        view.setBackgroundResource(mBackground);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mBoundString = mValues.get(position).getName();
        holder.tvUnit_name.setText(mValues.get(position).getName());
        holder.tvUnit_price.setText(Utils.priceWithout(mValues.get(position).getPrice()));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
