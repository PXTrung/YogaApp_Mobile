package com.example.yogaapp.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.yogaapp.CoursesActivity;
import com.example.yogaapp.Entities.Course;
import com.example.yogaapp.R;

import java.util.List;

public class CourseAdapter extends ArrayAdapter<Course> {
    private final List<Course> yogaCoursesList;
    private final LayoutInflater inflater;

    public CourseAdapter(Context ctx, List<Course> yogaCourses) {
        super(ctx, 0, yogaCourses);
        this.yogaCoursesList = yogaCourses;
        this.inflater = LayoutInflater.from(ctx);
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public View getView(int pos, View convertedView, @NonNull ViewGroup parentView) {
        if (convertedView == null) {
            convertedView = inflater.inflate(R.layout.course_item, parentView, false);
        }

        Course course = yogaCoursesList.get(pos);

        LinearLayout courseContainer = convertedView.findViewById(R.id.TaskContentLayout);

        TextView courseName = convertedView.findViewById(R.id.courseName);
        TextView courseType = convertedView.findViewById(R.id.courseType);
        TextView courseDay = convertedView.findViewById(R.id.courseDay);
        TextView courseTime = convertedView.findViewById(R.id.courseTime);
        TextView courseDuration = convertedView.findViewById(R.id.courseDuration);
        TextView courseDescription = convertedView.findViewById(R.id.courseDescription);
        TextView coursePrice = convertedView.findViewById(R.id.coursePrice);
        Button deleteButton = convertedView.findViewById(R.id.buttonDeleteCourse);
        Button editButton = convertedView.findViewById(R.id.buttonEditCourse);

        courseName.setText(course.getName());
        courseType.setText(course.getTypeOfClass());
        courseDay.setText(String.format("On: %s", course.getDayOfWeek()));
        courseTime.setText(String.format("Time: %s",course.getTime()));
        courseDuration.setText(String.format("Duration: %d mins", course.getDuration())); // Formats duration as an integer
        coursePrice.setText(String.format("£%.2f", course.getPrice())); // Formats price to 2 decimal places

        // Set the description if provided; otherwise, show "No description"
        if (!course.getDescription().isEmpty()) {
            courseDescription.setText(course.getDescription());
        } else {
            courseDescription.setText(R.string.no_description);
        }

        courseContainer.setOnClickListener(v -> {
            ((CoursesActivity) getContext()).onNavigateToClassesInCourse(course);
        });


        deleteButton.setOnClickListener(v -> {
            ((CoursesActivity) getContext()).onDeleteCourse(course);
        });

        editButton.setOnClickListener(v -> {
            ((CoursesActivity) getContext()).onUpdateCourse(course);
        });

        return convertedView;
    }
}
