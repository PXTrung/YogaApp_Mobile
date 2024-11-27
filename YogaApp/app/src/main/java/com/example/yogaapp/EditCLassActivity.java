package com.example.yogaapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaapp.Entities.Class;

import com.example.yogaapp.Entities.Course;
import com.example.yogaapp.Utils.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditCLassActivity extends AppCompatActivity {

    private EditText editTextTeacher, editTextComments;
    private TextView textViewPickDate;
    private String selectedDate;
    private Spinner spinnerCourse;

    private DatabaseHelper dbHelper;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private List<Course> courseList;
    private ArrayAdapter<String> courseAdapter;

    private int classId;
    private Class classSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class);

        dbHelper = new DatabaseHelper(this);

        // Get the class ID from the intent
        classId = getIntent().getIntExtra("classId", -1);

        spinnerCourse = findViewById(R.id.editSpinnerCourse);
        textViewPickDate= findViewById(R.id.editPickDate);
        editTextTeacher = findViewById(R.id.editTeacher);
        editTextComments = findViewById(R.id.editComments);

        // Enable the Up button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected  void onStart(){
        super.onStart();
        // Load the class data for editing
        if (classId != -1) {
            classSchedule = dbHelper.getClassById(classId);
            if (classSchedule != null) {
                // Populate fields with current class data
                textViewPickDate.setText(classSchedule.getDate());
                editTextTeacher.setText(classSchedule.getTeacher());
                editTextComments.setText(classSchedule.getComments());

                // Set up the course spinner with available courses (using course ID)
                setupCourseSpinner();
            }
        }
    }

    public void onOpenEditedDatePicker(View v){
        showDatePickerDialog();
    }

    public void onUpdateClass(View v){
        if (validateInputs()) {
            editClass();
        }
    }


    private void setupCourseSpinner() {
        courseList = dbHelper.getAllCourses();
        List<String> courseNames = new ArrayList<>();
        for (Course course : courseList) {
            courseNames.add(course.getName());
        }

        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseNames);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(courseAdapter);

        // Set the selected course based on current class data
        int selectedPosition = -1;
        for (int i = 0; i < courseList.size(); i++) {
            if (courseList.get(i).getId() == classSchedule.getCourseId()) {
                selectedPosition = i;
                break;
            }
        }
        spinnerCourse.setSelection(selectedPosition);
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

    private Course getSelectedCourse() {
        int selectedPosition = spinnerCourse.getSelectedItemPosition();
        return courseList.get(selectedPosition); // Retrieve the Course object
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        Course course = getSelectedCourse();

        // Get the required day of the week for the course
        int requiredDayOfWeek = getCalendarDayOfWeek(course.getDayOfWeek());

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {

            calendar.set(year, month, dayOfMonth);
            // Check if the selected day matches the course day
            if (calendar.get(Calendar.DAY_OF_WEEK) != requiredDayOfWeek) {
                // Show a message to the user if the selected day is incorrect
                Toast.makeText(this, "Please select a " + course.getDayOfWeek(), Toast.LENGTH_SHORT).show();
            } else {
                // If the date is valid, update the selected date
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                selectedDate = format.format(calendar.getTime());
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

    private void editClass() {
        Course course = getSelectedCourse();
        int courseId = course.getId();
        String teacher = editTextTeacher.getText().toString().trim();
        String comments = editTextComments.getText().toString().trim();


        executorService.execute(() -> {
            Class updatedClass = new Class(classId, selectedDate, courseId, teacher, comments);
            dbHelper.updateClass(updatedClass);

            runOnUiThread(() -> {
                Toast.makeText(this, "Class edited successfully", Toast.LENGTH_SHORT).show();
            });
        });
    }
}