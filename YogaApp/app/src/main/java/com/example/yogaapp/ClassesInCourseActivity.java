package com.example.yogaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yogaapp.Entities.Class;
import com.example.yogaapp.Utils.ClassAdapter;
import com.example.yogaapp.Utils.DatabaseHelper;

import java.util.List;

public class ClassesInCourseActivity extends AppCompatActivity {

    private ListView listViewClasses;
    private ClassAdapter classAdapter;
    private DatabaseHelper dbHelper;
    private int courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes_in_course);

        // Enable the Up button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get the course ID from the intent
        courseId = getIntent().getIntExtra("id", -1);

        listViewClasses = findViewById(R.id.classesInCourse);
        dbHelper = new DatabaseHelper(this);
    }

    @Override
    protected  void onStart(){
        super.onStart();
        loadClasses();
    }


    private void loadClasses() {
        List<Class> classList = dbHelper.getClassesByCourseId(courseId);
        classAdapter = new ClassAdapter(this, classList);
        listViewClasses.setAdapter(classAdapter);
    }
}