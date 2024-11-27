package com.example.yogaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaapp.Entities.Course;
import com.example.yogaapp.Utils.CourseAdapter;
import com.example.yogaapp.Utils.DatabaseHelper;

import java.util.List;

public class CoursesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView listView;
    private CourseAdapter courseAdapter;
    private List<Course> courseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        dbHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.courses_container);

        // Enable the Up button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override protected void onStart() {
        super.onStart();
        loadCourses();
    }

    public void onNavigateToAddCourse(View v) {
        Intent intent = new Intent(this, AddCourseActivity.class);
        startActivity(intent);
    }

    public void onDeleteCourse(Course course){
        deleteCourse(course);
    }

    public void onUpdateCourse(Course course){
        editCourse(course);
    }

    public void onNavigateToClassesInCourse(Course course) {
        Intent intent = new Intent(this, ClassesInCourseActivity.class);
        intent.putExtra("id", course.getId());
        startActivity(intent);
    }

    private void loadCourses() {
        courseList = dbHelper.getAllCourses();
        courseAdapter = new CourseAdapter(this, courseList);
        listView.setAdapter(courseAdapter);
    }

    private void editCourse(Course course) {
        Intent intent = new Intent(this, EditCourseActivity.class);
        intent.putExtra("courseId", course.getId());
        startActivity(intent);
    }


    private void deleteCourse(Course course) {
        dbHelper.deleteCourse(course.getId());
        loadCourses();
        Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show();
    }
}