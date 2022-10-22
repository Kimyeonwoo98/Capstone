package com.attendance.cbnu.test.beacon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.attendance.cbnu.test.BaseActivity;
import com.google.firebase.database.FirebaseDatabase;
import com.yuliwuli.blescan.demo.R;
import com.yuliwuli.blescan.demo.databinding.ActivityAttendanceListBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class AttendanceListActivity extends BaseActivity {

    //region Static
    private static final String ARG_ATTENDANCES = "ARG_ATTENDANCES";

    public static Intent getIntent(Context context, String beaconName, String beaconUuid,
                                   ClassInformation classInformation, ArrayList<Attendance> attendances) {
        Intent intent = getSingleTopIntent(context, AttendanceListActivity.class);
        intent.putExtras(getBundle(beaconUuid, beaconName, null, classInformation));
        intent.putParcelableArrayListExtra(ARG_ATTENDANCES, attendances);

        return intent;
    }
    //endregion

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
    private ArrayList<Attendance> attendances;

    private ActivityAttendanceListBinding binding;
    private AttendanceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            attendances = getIntent().getParcelableArrayListExtra(ARG_ATTENDANCES);
        }

        if (savedInstanceState != null) {
            attendances = savedInstanceState.getParcelableArrayList(ARG_ATTENDANCES);
        }

        if (beaconName == null || beaconUuid == null || classInformation == null || attendances == null || attendances.isEmpty()) {
            finish();
            return;
        }

        binding = ActivityAttendanceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUi();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(ARG_ATTENDANCES, attendances);
        super.onSaveInstanceState(outState);
    }

    private void initUi() {
        binding.toolbar.setTitleCentered(true);
        binding.toolbar.setTitle(beaconName + "호 " + classInformation.getName());
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear_all) {
                new AlertDialog.Builder(this)
                        .setMessage("초기화 하시겠습니까? 초기화시 모든 출석 데이터가 삭제됩니다.")
                        .setPositiveButton("확인", (dialogInterface, i) -> {
                            clearAll();
                        })
                        .setNegativeButton("취소", null)
                        .create()
                        .show();

                return true;
            }

            return false;
        });

        adapter = new AttendanceAdapter(dateFormatter);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL));

        adapter.submitList(attendances);
    }

    /**
     * 출석 데이터 초기화 함수
     */
    private void clearAll() {
        HashMap<String, Object> update = new HashMap<>();

        for (Attendance attendance : adapter.getCurrentList()) {
            update.put(attendance.getIdToken(), null);
        }

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference()
                .child("Kimyeonwoopt")
                .child("Attendance")
                .child(beaconUuid)
                .child(classInformation.getId())
                .updateChildren(update);

        setResult(Activity.RESULT_OK);

        Toast.makeText(this, "초기화 되었습니다.", Toast.LENGTH_SHORT).show();
        onBackPressed();
    }


    private static class AttendanceAdapter extends ListAdapter<Attendance, AttendanceAdapter.AttendanceViewHolder> {

        private final SimpleDateFormat dateFormatter;

        protected AttendanceAdapter(SimpleDateFormat dateFormatter) {
            super(new DiffUtil.ItemCallback<Attendance>() {
                @Override
                public boolean areItemsTheSame(@NonNull Attendance oldItem, @NonNull Attendance newItem) {
                    return oldItem.getIdToken().trim().equals(newItem.getIdToken().trim());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Attendance oldItem, @NonNull Attendance newItem) {
                    return oldItem.getEmailId().trim().equals(newItem.getEmailId().trim()) &&
                            oldItem.getNumber().trim().equals(newItem.getNumber().trim()) &&
                            oldItem.getName().trim().equals(newItem.getName().trim()) &&
                            oldItem.getTimestamp() == newItem.getTimestamp();
                }
            });

            this.dateFormatter = dateFormatter;
        }

        @NonNull
        @Override
        public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new AttendanceViewHolder(inflater.inflate(R.layout.item_attendance, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
            Attendance attendance = getItem(position);
            holder.nameTextView.setText(attendance.getNumber() + " " + attendance.getName());
            holder.dateTimeTextView.setText(dateFormatter.format(new Date(attendance.getTimestamp())) + " 출석");
        }

        private static class AttendanceViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            TextView dateTimeTextView;

            public AttendanceViewHolder(@NonNull View itemView) {
                super(itemView);

                nameTextView = itemView.findViewById(R.id.name_text_view);
                dateTimeTextView = itemView.findViewById(R.id.date_time_text_view);
            }
        }
    }
}