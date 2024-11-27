package com.example.yogaapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaapp.Entities.Course;
import com.example.yogaapp.Utils.DatabaseHelper;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddCourseActivity extends AppCompatActivity {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DatabaseHelper dbHelper;

    private Spinner spinnerDayOfWeek, spinnerTypeOfClass;
    private EditText editTextCourseName, editTextCapacity, editTextDuration, editTextPrice, editTextDescription;
    private TextView textViewSelectedTime;
    private Button  buttonSaveCourse;

    private int hourOfDay, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        dbHelper = new DatabaseHelper(this);

        // Initialize views
        spinnerDayOfWeek = findViewById(R.id.spinner_day_of_week);
        editTextCourseName = findViewById(R.id.editText_course_name);
        textViewSelectedTime = findViewById(R.id.textView_selected_time);
        editTextCapacity = findViewById(R.id.editText_capacity);
        editTextDuration = findViewById(R.id.editText_duration);
        editTextPrice = findViewById(R.id.editText_price);
        spinnerTypeOfClass = findViewById(R.id.spinner_type_of_class);
        editTextDescription = findViewById(R.id.editText_description);
        buttonSaveCourse = findViewById(R.id.button_save_course);

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
    }

    public void onSaveCourse(View v) {
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
            Course course = new Course(0, name, dayOfWeek, time, capacity, duration, price, typeOfClass, description);
            dbHelper.addCourse(course);

            runOnUiThread(() -> {
                Toast.makeText(AddCourseActivity.this, "Course added successfully", Toast.LENGTH_SHORT).show();
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