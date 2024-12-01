package com.example.yogaapp.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.yogaapp.ClassesActivity;
import com.example.yogaapp.ClassesInCourseActivity;
import com.example.yogaapp.CoursesActivity;
import com.example.yogaapp.R;
import com.example.yogaapp.Entities.Class;

import java.util.List;

public class ClassAdapter extends ArrayAdapter<Class> {
    private Context context;
    private List<Class> classList;

    public ClassAdapter(Context ctx, List<Class> classList) {
        super(ctx, 0, classList);
        this.context = ctx;
        this.classList = classList;
    }

    @Override
    public View getView(int pos, View convertedView, ViewGroup parentView) {
        if (convertedView == null) {
            convertedView = LayoutInflater.from(context).inflate(R.layout.class_item, parentView, false);
        }

        // Get the current class schedule
        Class classSchedule = classList.get(pos);

        // Find views in class_item.xml
        TextView classDate = convertedView.findViewById(R.id.classDate);
        TextView classTeacher = convertedView.findViewById(R.id.classTeacher);
        TextView classComments = convertedView.findViewById(R.id.classComments);
        Button deleteButton = convertedView.findViewById(R.id.buttonDeleteClass);
        Button editButton = convertedView.findViewById(R.id.buttonEditClass);

        // Set data
        classDate.setText(String.format("Date: %s",classSchedule.getDate()));
        classTeacher.setText(String.format("Teacher: %s",classSchedule.getTeacher()));

        // Set comments if available
        if (!classSchedule.getComments().isEmpty()) {
            classComments.setText(String.format("Comments:  %s",classSchedule.getComments()));
            classComments.setVisibility(View.VISIBLE);
        } else {
            classComments.setVisibility(View.GONE);
        }

        // Hide Edit and Delete buttons if in ClassesInCourseActivity
        if (context instanceof ClassesInCourseActivity) {
            deleteButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
        } else {
            // Set actions for other activities if needed
            deleteButton.setOnClickListener(v -> {
                ((ClassesActivity) getContext()).onDeleteClass(classSchedule);
            });

            editButton.setOnClickListener(v -> {
                ((ClassesActivity) getContext()).onNavigateToEditClass(classSchedule);
            });
        }

        return convertedView;
    }

    // Method to update the class list
    public void updateClassList(List<Class> newClassList) {
        classList.clear();
        classList.addAll(newClassList);
        notifyDataSetChanged();
    }
}
