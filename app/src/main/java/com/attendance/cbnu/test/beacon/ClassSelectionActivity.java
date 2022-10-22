package com.attendance.cbnu.test.beacon;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.attendance.cbnu.test.BaseActivity;
import com.attendance.cbnu.test.login.UserAccount;
import com.google.firebase.database.FirebaseDatabase;
import com.yuliwuli.blescan.demo.R;
import com.yuliwuli.blescan.demo.databinding.ActivityClassSelectionBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassSelectionActivity extends BaseActivity {

    //region Static
    public static void startActivity(Context context, String beaconName, String beaconUuid, UserAccount user) {
        Intent intent = getSingleTopIntent(context, ClassSelectionActivity.class);
        intent.putExtras(getBundle(beaconUuid, beaconName, user, null));

        context.startActivity(intent);
    }
    //endregion

    private final HashMap<String, ArrayList<ClassInformation>> classes = new HashMap<>();
    private ActivityClassSelectionBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (beaconName == null || beaconUuid == null || user == null) {
            finish();
            return;
        }

        binding = ActivityClassSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initClasses();

        if (!classes.containsKey(beaconUuid)) {
            Toast.makeText(this, "선택하신 비콘의 수업 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initUi();
    }

    private void initClasses() {
        // 수업 데이터 초기화 (ID 는 전부 달라야 합니다.)
        // ID, 수업 이름, 수업 시간, 총원 순
        ArrayList<ClassInformation> c1 = new ArrayList<>();
        c1.add(new ClassInformation("class1", "수업 1", "09:00~10:00", 40));
        c1.add(new ClassInformation("class2", "수업 2", "10:00~11:00", 30));
        c1.add(new ClassInformation("class3", "수업 3", "11:00~12:00", 50));

        classes.put("E2C56DB5-DFFB-48D2-B060-D0F5A71096E0", c1);
    }

    private void initUi() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ClassAdapter adapter = new ClassAdapter(classes.get(beaconUuid));
        adapter.setOnItemClickListener(item -> {
            if (user.isStudent()) {
                Attendance attendance = new Attendance(user);
                FirebaseDatabase.getInstance().getReference()
                        .child("Kimyeonwoopt")
                        .child("Attendance")
                        .child(beaconUuid)
                        .child(item.getId())
                        .child(attendance.getIdToken())
                        .setValue(attendance);

                AttendanceActivity.startActivity(this, beaconName, beaconUuid,
                        user, item, attendance.getTimestamp());

            } else {
                AttendanceCalendarActivity.startActivity(this, beaconName, beaconUuid, item);
            }
        });

        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL));
    }


    private static class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

        private final List<ClassInformation> items;
        private Consumer<ClassInformation> onItemClickListener;

        public ClassAdapter(List<ClassInformation> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ClassViewHolder(inflater.inflate(R.layout.item_class, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
            ClassInformation item = items.get(position);
            holder.nameTextView.setText(item.getName());
            holder.timeTextView.setText(item.getTime());

            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.accept(item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void setOnItemClickListener(Consumer<ClassInformation> onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        private static class ClassViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            TextView timeTextView;

            public ClassViewHolder(@NonNull View itemView) {
                super(itemView);

                nameTextView = itemView.findViewById(R.id.name_text_view);
                timeTextView = itemView.findViewById(R.id.time_text_view);
            }
        }
    }
}
