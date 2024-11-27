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
    private final List<Course> courses;
    private final LayoutInflater inflater;

    public CourseAdapter(Context context, List<Course> courses) {
        super(context, 0, courses);
        this.courses = courses;
        this.inflater = LayoutInflater.from(context);
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.course_item, parent, false);
        }

        Course course = courses.get(position);

        LinearLayout courseContainer = convertView.findViewById(R.id.TaskContentLayout);

        TextView courseName = convertView.findViewById(R.id.courseName);
        TextView courseType = convertView.findViewById(R.id.courseType);
        TextView courseDay = convertView.findViewById(R.id.courseDay);
        TextView courseTime = convertView.findViewById(R.id.courseTime);
        TextView courseDuration = convertView.findViewById(R.id.courseDuration);
        TextView courseDescription = convertView.findViewById(R.id.courseDescription);
        TextView coursePrice = convertView.findViewById(R.id.coursePrice);
        Button deleteButton = convertView.findViewById(R.id.buttonDeleteCourse);
        Button editButton = convertView.findViewById(R.id.buttonEditCourse);

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

        return convertView;
    }
}
