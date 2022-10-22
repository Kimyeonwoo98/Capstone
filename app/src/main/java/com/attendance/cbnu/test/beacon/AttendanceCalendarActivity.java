package com.attendance.cbnu.test.beacon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.attendance.cbnu.test.BaseActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.kizitonwose.calendarview.utils.Size;
import com.yuliwuli.blescan.demo.R;
import com.yuliwuli.blescan.demo.databinding.ActivityAttendanceCalendarBinding;
import com.yuliwuli.blescan.demo.databinding.ItemCalendarDayBinding;

import java.lang.ref.WeakReference;
import java.time.DayOfWeek;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class AttendanceCalendarActivity extends BaseActivity {

    //region Static
    public static void startActivity(Context context, String beaconName, String beaconUuid,
                                     ClassInformation classInformation) {
        Intent intent = getSingleTopIntent(context, AttendanceCalendarActivity.class);
        intent.putExtras(getBundle(beaconUuid, beaconName, null, classInformation));

        context.startActivity(intent);
    }
    //endregion

    private final YearMonth firstMonth = YearMonth.of(2000, 1);
    private final YearMonth lastMonth = YearMonth.now();
    private ActivityAttendanceCalendarBinding binding;
    private YearMonth currentMonth = YearMonth.now();
    private DataFetcher dataFetcher;
    private final ActivityResultLauncher<Intent> attendanceListLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), (result) -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    fetchData();
                }
            });
    private Handler uiHandler;
    private HashMap<Integer, ArrayList<Attendance>> dataSet = new HashMap();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (beaconName == null || beaconUuid == null || classInformation == null) {
//            beaconName = "303";
//            beaconUuid = "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0";
            finish();
            return;
        }

        binding = ActivityAttendanceCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.obj instanceof HashMap) {
                    dataSet = (HashMap<Integer, ArrayList<Attendance>>) msg.obj;
                    binding.calendarView.notifyMonthChanged(currentMonth);
                }

                binding.refreshLayout.setRefreshing(false);
            }
        };

        initUi();
    }

    private void initUi() {
        binding.toolbar.setTitleCentered(true);
        binding.toolbar.setTitle(beaconName + "호 " + classInformation.getName());
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        View cell = getLayoutInflater().inflate(R.layout.item_calendar_day, null);
        cell.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int height = cell.getMeasuredHeight();

        binding.calendarView.setDaySize(new Size(Integer.MIN_VALUE, height));

        binding.calendarView.setMonthScrollListener(calendarMonth -> {
            currentMonth = calendarMonth.getYearMonth();

            String yearMonth = String.format(Locale.KOREA, "%04d년 %02d월", calendarMonth.getYear(), calendarMonth.getMonth());
            binding.calendarHeader.monthTextView.setText(yearMonth);

            binding.calendarHeader.previousMonthButton.setEnabled(currentMonth.isAfter(firstMonth));
            binding.calendarHeader.nextMonthButton.setEnabled(currentMonth.isBefore(lastMonth));

            binding.refreshLayout.setRefreshing(true);
            fetchData();

            return null;
        });

        binding.calendarHeader.previousMonthButton.setOnClickListener(v -> {
            binding.calendarView.scrollToMonth(currentMonth.minusMonths(1));
        });

        binding.calendarHeader.nextMonthButton.setOnClickListener(v -> {
            binding.calendarView.scrollToMonth(currentMonth.plusMonths(1));
        });

        binding.calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer viewContainer, @NonNull CalendarDay day) {
                int textColor;
                boolean isThisMonth = day.getOwner() == DayOwner.THIS_MONTH;
                float alpha = isThisMonth ? 1f : 0.38f;

                DayOfWeek dayOfWeek = day.getDate().getDayOfWeek();

                if (dayOfWeek == DayOfWeek.SUNDAY) {
                    textColor = ContextCompat.getColor(AttendanceCalendarActivity.this, R.color.colorSunday);
                } else if (dayOfWeek == DayOfWeek.SATURDAY) {
                    textColor = ContextCompat.getColor(AttendanceCalendarActivity.this, R.color.colorSaturday);
                } else {
                    textColor = getThemeColor(R.attr.colorOnSurface);
                }

                ItemCalendarDayBinding binding = viewContainer.binding;

                binding.calendarDayText.setTextColor(textColor);
                binding.calendarDayText.setText(String.valueOf(day.getDate().getDayOfMonth()));
                binding.calendarDayText.setAlpha(alpha);

                if (isThisMonth) {
                    ArrayList<Attendance> attendances = dataSet.get(day.getDate().getDayOfMonth());
                    binding.attendanceIndicatorContainer.setVisibility(attendances == null ? View.INVISIBLE : View.VISIBLE);

                    if (attendances != null) {
                        binding.attendanceCountTextView.setText("" + attendances.size());
                        binding.absentCountTextView.setText("" + Math.max(classInformation.getAllMembers() - attendances.size(), 0));
                        binding.getRoot().setOnClickListener(v -> {
                            Intent intent = AttendanceListActivity.getIntent(
                                    AttendanceCalendarActivity.this, beaconName, beaconUuid, classInformation, attendances);
                            attendanceListLauncher.launch(intent);
                        });
                    } else {
                        binding.getRoot().setOnClickListener(null);
                    }
                } else {
                    binding.attendanceIndicatorContainer.setVisibility(View.INVISIBLE);
                    binding.getRoot().setOnClickListener(null);
                }
            }
        });

        binding.calendarView.setup(firstMonth, lastMonth, DayOfWeek.SUNDAY);
        binding.calendarView.scrollToMonth(currentMonth);

        binding.refreshLayout.setOnRefreshListener(this::fetchData);
    }

    private int getThemeColor(@AttrRes int resId) {
        final TypedValue value = new TypedValue();
        getTheme().resolveAttribute(resId, value, true);
        return value.data;
    }

    private void fetchData() {
        if (dataFetcher != null) {
            dataFetcher.exit();
            dataFetcher.interrupt();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(currentMonth.getYear(), currentMonth.getMonthValue() - 1, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long from = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, 1);

        long to = calendar.getTimeInMillis();

        dataFetcher = new DataFetcher(this, beaconUuid, classInformation.getId(), from, to);
        dataFetcher.setDaemon(true);
        dataFetcher.start();
    }

    @Override
    protected void onDestroy() {
        if (dataFetcher != null) {
            dataFetcher.exit();
            dataFetcher.interrupt();
        }

        super.onDestroy();
    }

    private static class DayViewContainer extends ViewContainer {
        ItemCalendarDayBinding binding;

        public DayViewContainer(View view) {
            super(view);

            binding = ItemCalendarDayBinding.bind(view);
        }
    }

    private static class DataFetcher extends Thread {

        private final WeakReference<AttendanceCalendarActivity> activity;
        private final FirebaseDatabase db = FirebaseDatabase.getInstance();

        private final String beaconUuid;
        private final String classId;
        private final long from;
        private final long to;

        private boolean exit = false;

        public DataFetcher(AttendanceCalendarActivity activity, String beaconUuid, String classId, long from, long to) {
            this.activity = new WeakReference(activity);
            this.beaconUuid = beaconUuid;
            this.classId = classId;
            this.from = from;
            this.to = to;
        }

        public void exit() {
            exit = true;
        }

        @Override
        public void run() {
            try {
                DataSnapshot snapshot = Tasks.await(db.getReference()
                        .child("Kimyeonwoopt")
                        .child("Attendance")
                        .child(beaconUuid)
                        .child(classId)
                        .orderByChild("timestamp")
                        .startAt(from)
                        .endBefore(to)
                        .get());

                HashMap<Integer, ArrayList<Attendance>> results = new HashMap<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Attendance attendance = child.getValue(Attendance.class);
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(attendance.getTimestamp());

                    int day = c.get(Calendar.DAY_OF_MONTH);

                    ArrayList<Attendance> attendances = new ArrayList<>();
                    if (results.get(day) != null) {
                        attendances = results.get(day);
                    }

                    attendances.add(attendance);

                    results.put(day, attendances);
                }

                if (activity.get() != null && !exit) {
                    Message message = new Message();
                    message.obj = results;

                    activity.get().uiHandler.sendMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
