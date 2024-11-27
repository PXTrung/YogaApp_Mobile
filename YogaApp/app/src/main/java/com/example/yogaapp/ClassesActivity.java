package com.example.yogaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaapp.Entities.Course;
import com.example.yogaapp.Utils.ClassAdapter;
import com.example.yogaapp.Entities.Class;
import com.example.yogaapp.Utils.CourseAdapter;
import com.example.yogaapp.Utils.DatabaseHelper;

import java.util.List;

public class ClassesActivity extends AppCompatActivity {

    private SearchView searchTeacherNameView;
    private ListView listViewClasses;
    private ClassAdapter classAdapter;

    private List<Class> classList;
    private DatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);

        // Enable the Up button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        searchTeacherNameView = findViewById(R.id.SearchView);
        listViewClasses = findViewById(R.id.listViewClasses);
        dbHelper = new DatabaseHelper(this);

        // Set up search functionality
        searchTeacherNameView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchClassesByTeacher(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchClassesByTeacher(newText);
                return true;
            }
        });
    }

    @Override
    protected  void onStart(){
        super.onStart();
        loadClasses();
    }

    public void onNavigateToAddClass(View v){
        Intent intent = new Intent(this, AddClassActivity.class);
        startActivity(intent);
    }

    public void onNavigateToEditClass(Class classSchedule){
        editClass(classSchedule);
    }

    public void onDeleteClass(Class classSchedule){
        deleteClass(classSchedule);
    }

    private void deleteClass(Class classSchedule) {
        dbHelper.deleteClass(classSchedule.getId());
        loadClasses();
        Toast.makeText(this, "Class deleted", Toast.LENGTH_SHORT).show();
    }

    private void editClass(Class classSchedule){
        Intent intent = new Intent(this, EditCLassActivity.class);
        intent.putExtra("classId", classSchedule.getId());
        startActivity(intent);
    }

    private void loadClasses() {
        List<Class> classList = dbHelper.getAllClasses();
        classAdapter = new ClassAdapter(this, classList);
        listViewClasses.setAdapter(classAdapter);
    }

    private void searchClassesByTeacher(String teacherName) {
        List<Class> filteredList;
        if (teacherName.isEmpty()) {
            // If search input is empty, show all classes
            filteredList = dbHelper.getAllClasses();
        } else {
            // Get filtered list based on teacher name
            filteredList = dbHelper.searchClasses(teacherName);
        }

        // Update adapter with filtered list
        classAdapter.updateClassList(filteredList);
    }
}