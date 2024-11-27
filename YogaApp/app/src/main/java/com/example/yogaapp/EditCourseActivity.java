package com.example.yogaapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yogaapp.Entities.Course;
import com.example.yogaapp.Utils.DatabaseHelper;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditCourseActivity extends AppCompatActivity {

    private int courseId = -1;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DatabaseHelper dbHelper;

    private Spinner spinnerDayOfWeek, spinnerTypeOfClass;
    private EditText editTextCourseName, editTextCapacity, editTextDuration, editTextPrice, editTextDescription;
    private TextView textViewSelectedTime;

    private int hourOfDay, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        dbHelper = new DatabaseHelper(this);

        // Initialize views
        spinnerDayOfWeek = findViewById(R.id.edit_day_of_week);
        editTextCourseName = findViewById(R.id.edit_course_name);
        textViewSelectedTime = findViewById(R.id.edit_selected_time);
        editTextCapacity = findViewById(R.id.edit_capacity);
        editTextDuration = findViewById(R.id.edit_duration);
        editTextPrice = findViewById(R.id.edit_price);
        spinnerTypeOfClass = findViewById(R.id.edit_type_of_class);
        editTextDescription = findViewById(R.id.edit_description);

        // Populate spinners
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this, R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayOfWeek.setAdapter(dayAdapter);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.types_of_class, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeOfClass.setAdapter(typeAdapter);

        // Enable the Up button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        courseId = intent.getIntExtra("courseId", -1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadCourseData(courseId);
    }

    private void loadCourseData(int courseId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Course course = dbHelper.getCourseById(courseId); // Assuming you have this method
            runOnUiThread(() -> {
                if (course != null) {
                    // Set data in fields
                    spinnerDayOfWeek.setSelection(getSpinnerIndex(spinnerDayOfWeek, course.getDayOfWeek()));
                    textViewSelectedTime.setText(course.getTime());
                    editTextCourseName.setText(course.getName());
                    editTextCapacity.setText(String.valueOf(course.getCapacity()));
                    editTextDuration.setText(String.valueOf(course.getDuration()));
                    editTextPrice.setText(String.format(Locale.getDefault(), "%.2f", course.getPrice()));
                    spinnerTypeOfClass.setSelection(getSpinnerIndex(spinnerTypeOfClass, course.getTypeOfClass()));
                    editTextDescription.setText(course.getDescription());
                }
            });
        });
    }

    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    public void onEditCourse(View v) {
        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();
        String name = editTextCourseName.getText().toString();
        String time = textViewSelectedTime.getText().toString();
        String capacityStr = editTextCapacity.getText().toString();
        String durationStr = editTextDuration.getText().toString();
        String priceStr = editTextPrice.getText().toString();
        String typeOfClass = spinnerTypeOfClass.getSelectedItem().toString();
        String description = editTextDescription.getText().toString();


        // Input validation
        if (name.isEmpty()) {
            editTextCourseName.setError("Course name is required.");
            editTextCourseName.requestFocus();
            return;
        }

        if (time.equals("No time selected")) {
            Toast.makeText(this, "Please select a time for the course.", Toast.LENGTH_SHORT).show();
            textViewSelectedTime.requestFocus();
            return;
        }

        if (capacityStr.isEmpty()) {
            editTextCapacity.setError("Capacity is required.");
            editTextCapacity.requestFocus();
            return;
        }

        if (durationStr.isEmpty()) {
            editTextDuration.setError("Duration is required.");
            editTextDuration.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            editTextPrice.setError("Price is required.");
            editTextPrice.requestFocus();
            return;
        }

        if (typeOfClass.equals("Select Type")) {
            Toast.makeText(this, "Please select a type of class.", Toast.LENGTH_SHORT).show();
            spinnerTypeOfClass.requestFocus();
            return;
        }

        int capacity = Integer.parseInt(capacityStr);
        int duration = Integer.parseInt(durationStr);
        double price = Double.parseDouble(priceStr);

        // Perform database operation in background
        executorService.execute(() -> {
            Course course = new Course(courseId, name, dayOfWeek, time, capacity, duration, price, typeOfClass, description);
            dbHelper.updateCourse(course);

            runOnUiThread(() -> {
                Toast.makeText(EditCourseActivity.this, "Course edited successfully", Toast.LENGTH_SHORT).show();
            });
        });
    }

    public void onOpenTimePicker(View v){
        showTimePicker();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    hourOfDay = selectedHour;
                    minute = selectedMinute;
                    textViewSelectedTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                }, hourOfDay, minute, true);
        timePickerDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}