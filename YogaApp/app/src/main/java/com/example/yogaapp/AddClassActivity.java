package com.example.yogaapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.example.yogaapp.Entities.Class;
import com.example.yogaapp.Utils.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddClassActivity extends AppCompatActivity {

    private EditText editTextTeacher, editTextComments;
    private TextView textViewPickDate;
    private String selectedDate;
    private Spinner spinnerCourse;

    private List<Course> courseList;
    private ArrayAdapter<String> courseAdapter;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        dbHelper = new DatabaseHelper(this);

        spinnerCourse = findViewById(R.id.spinnerCourse);
        textViewPickDate= findViewById(R.id.btnPickDate);
        editTextTeacher = findViewById(R.id.editTextTeacher);
        editTextComments = findViewById(R.id.editTextComments);

        // Enable the Up button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected  void onStart(){
        super.onStart();
        loadCoursesIntoSpinner();
    }

    public void onOpenDatePicker(View v){
        showDatePickerDialog();
    }

    public void onSaveClass(View v){
        if(validateInputs()){
            saveClass();
        }
    }

    private void loadCoursesIntoSpinner() {
        Executors.newSingleThreadExecutor().execute(() -> {
            courseList = dbHelper.getAllCourses();
            List<String> courseNames = new ArrayList<>();

            for (Course course : courseList) {
                courseNames.add(course.getName());
            }

            runOnUiThread(() -> {
                courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseNames);
                courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCourse.setAdapter(courseAdapter);
            });
        });
    }

    private Course getSelectedCourse() {
        int selectedPosition = spinnerCourse.getSelectedItemPosition();
        return courseList.get(selectedPosition); // Retrieve the Course object
    }

    private void showDatePickerDialog() {
        Calendar classCalendar = Calendar.getInstance();
        Course course = getSelectedCourse();

        // Get the required day of the week for the course
        int requiredDayOfWeek = getCalendarDayOfWeek(course.getDayOfWeek());

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {

            classCalendar.set(year, month, dayOfMonth);
            // Check if the selected day matches the course day
            if (classCalendar.get(Calendar.DAY_OF_WEEK) != requiredDayOfWeek) {
                // Show a message to the user if the selected day is incorrect
                Toast.makeText(this, "Please select a " + course.getDayOfWeek(), Toast.LENGTH_SHORT).show();
            } else {
                // If the date is valid, update the selected date
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                selectedDate = format.format(classCalendar.getTime());
                textViewPickDate.setText(selectedDate);
            }
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));


        datePickerDialog.show();
    }

    private int getCalendarDayOfWeek(String dayOfWeek) {
        switch (dayOfWeek) {
            case "Monday":
                return Calendar.MONDAY;
            case "Tuesday":
                return Calendar.TUESDAY;
            case "Wednesday":
                return Calendar.WEDNESDAY;
            case "Thursday":
                return Calendar.THURSDAY;
            case "Friday":
                return Calendar.FRIDAY;
            case "Saturday":
                return Calendar.SATURDAY;
            case "Sunday":
                return Calendar.SUNDAY;
            default:
                throw new IllegalArgumentException("Invalid day of the week: " + dayOfWeek);
        }
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(textViewPickDate.getText())) {
            textViewPickDate.setError("Date is required");
            return false;
        }

        if (TextUtils.isEmpty( editTextTeacher.getText())) {
            editTextTeacher.setError("Teacher name is required");
            return false;
        }

        // Validation for other fields can be added here if necessary
        return true;
    }

    private void saveClass() {
        Course course = getSelectedCourse();
        int courseId = course.getId();
        String teacher = editTextTeacher.getText().toString().trim();
        String comments = editTextComments.getText().toString().trim();


        executorService.execute(() -> {
            Class classSchedule = new Class(0, selectedDate, courseId, teacher, comments);
            dbHelper.addClass(classSchedule);

            runOnUiThread(() -> {
                Toast.makeText(this, "Class added successfully", Toast.LENGTH_SHORT).show();
            });
        });

    }
}