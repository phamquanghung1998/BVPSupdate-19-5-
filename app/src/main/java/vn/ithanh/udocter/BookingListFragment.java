package vn.ithanh.udocter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import vn.ithanh.udocter.model.BOOKING_INFO;

/**
 * Created by iThanh on 12/7/2017.
 */

public class BookingListFragment extends Fragment {
    /* Data */
    private ArrayList<BOOKING_INFO> booking = new ArrayList<BOOKING_INFO>();

    public void setItems(ArrayList<BOOKING_INFO> lst) {
        this.booking = lst;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.fragment_booking_list, container, false);
        setupRecyclerView(rv);
        return rv;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new BookingRecyclerViewAdapter(getActivity(), this.booking));
    }
}
