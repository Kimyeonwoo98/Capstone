package com.attendance.cbnu.test.beacon;


import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.MinewBeacon;
import com.yuliwuli.blescan.demo.R;

import java.util.ArrayList;
import java.util.List;


public class BeaconListAdapter extends RecyclerView.Adapter<BeaconListAdapter.MyViewHolder> {

    private List<MinewBeacon> mMinewBeacons = new ArrayList<>();
    private Consumer<MinewBeacon> onClickListener = null;


    /**
     * Item click 리스너 설정 함수
     *
     * @param onClickListener 등록할 클릭 리스너
     */
    public void setOnClickListener(Consumer<MinewBeacon> onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_main, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.setDataAndUi(mMinewBeacons.get(position));

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    int position = holder.getAdapterPosition();
                    if (position >= 0 && position < mMinewBeacons.size()) {
                        onClickListener.accept(mMinewBeacons.get(position));
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mMinewBeacons != null) {
            return mMinewBeacons.size();
        }
        return 0;
    }

    public void setData(List<MinewBeacon> minewBeacons) {
        this.mMinewBeacons = minewBeacons;

//        notifyItemRangeChanged(0,minewBeacons.size());
        notifyDataSetChanged();

    }

    public void setItems(List<MinewBeacon> newItems) {
//        validateItems(newItems);


        int startPosition = 0;
        int preSize = 0;
        if (this.mMinewBeacons != null) {
            preSize = this.mMinewBeacons.size();

        }
        if (preSize > 0) {
            this.mMinewBeacons.clear();
            notifyItemRangeRemoved(startPosition, preSize);
        }
        this.mMinewBeacons.addAll(newItems);
        notifyItemRangeChanged(startPosition, newItems.size());
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {


        private final TextView mDevice_name;
        private MinewBeacon mMinewBeacon;


        public MyViewHolder(View itemView) {
            super(itemView);
            mDevice_name = itemView.findViewById(R.id.device_name);
        }

        public void setDataAndUi(MinewBeacon minewBeacon) {
            mMinewBeacon = minewBeacon;
            mDevice_name.setText(mMinewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue());

            String battery = mMinewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_BatteryLevel).getStringValue();
            int batt = Integer.parseInt(battery);
            if (batt > 100) {
                batt = 100;
            }


        }
    }
}
