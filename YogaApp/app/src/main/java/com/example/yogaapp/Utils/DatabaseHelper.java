package com.example.yogaapp.Utils;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.example.yogaapp.Entities.Course;
import com.example.yogaapp.Entities.Class;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "yogaApp.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_COURSES = "Courses";
    private Context context;

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DAY_OF_WEEK = "dayOfWeek";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_CAPACITY = "capacity";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_TYPE_OF_CLASS = "typeOfClass";
    private static final String COLUMN_DESCRIPTION = "description";


    // Table name and column names for class schedule
    private static final String TABLE_CLASSES = "classes";
    private static final String COLUMN_CLASS_ID = "id";
    private static final String COLUMN_CLASS_DATE = "date";
    private static final String COLUMN_CLASS_COURSE_ID = "course_id"; // Foreign key to Course
    private static final String COLUMN_CLASS_TEACHER = "teacher";
    private static final String COLUMN_CLASS_COMMENTS = "comments";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableCourses = "CREATE TABLE " + TABLE_COURSES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_DAY_OF_WEEK + " TEXT NOT NULL, " +
                COLUMN_TIME + " TEXT NOT NULL, " +
                COLUMN_CAPACITY + " INTEGER NOT NULL, " +
                COLUMN_DURATION + " INTEGER NOT NULL, " +
                COLUMN_PRICE + " REAL NOT NULL, " +
                COLUMN_TYPE_OF_CLASS + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT)";
        db.execSQL(createTableCourses);

        // Classes table creation
        String CREATE_CLASSES_TABLE = "CREATE TABLE " + TABLE_CLASSES + "("
                + COLUMN_CLASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CLASS_DATE + " TEXT,"
                + COLUMN_CLASS_COURSE_ID + " INTEGER,"
                + COLUMN_CLASS_TEACHER + " TEXT NOT NULL,"
                + COLUMN_CLASS_COMMENTS + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_CLASS_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(id)"
                + ")";
        db.execSQL(CREATE_CLASSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    // Insert a new course
    public void addCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, course.getName());
        values.put(COLUMN_DAY_OF_WEEK, course.getDayOfWeek());
        values.put(COLUMN_TIME, course.getTime());
        values.put(COLUMN_CAPACITY, course.getCapacity());
        values.put(COLUMN_DURATION, course.getDuration());
        values.put(COLUMN_PRICE, course.getPrice());
        values.put(COLUMN_TYPE_OF_CLASS, course.getTypeOfClass());
        values.put(COLUMN_DESCRIPTION, course.getDescription());

        // Insert in SQLite and retrieve the inserted row ID
        long id = db.insert(TABLE_COURSES, null, values);
        course.setId((int) id); // Set the course ID for Firebase
        db.close();

        // Insert into Firebase Realtime Database
        // Only sync if SQLite insertion was successful
        if (id != -1) {
            // Add to Firebase
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("courses");
            firebaseRef.child(String.valueOf(course.getId())).setValue(course);
        }
    }

    // Update a course
    public void updateCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DAY_OF_WEEK, course.getDayOfWeek());
        values.put(COLUMN_NAME, course.getName());
        values.put(COLUMN_TIME, course.getTime());
        values.put(COLUMN_CAPACITY, course.getCapacity());
        values.put(COLUMN_DURATION, course.getDuration());
        values.put(COLUMN_PRICE, course.getPrice());
        values.put(COLUMN_TYPE_OF_CLASS, course.getTypeOfClass());
        values.put(COLUMN_DESCRIPTION, course.getDescription());

        int rowsAfftected = db.update(TABLE_COURSES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(course.getId())});
        db.close();

        // Update in Firebase Realtime Database if update in SQLite was successful
        if (rowsAfftected > 0){
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("courses");
            firebaseRef.child(String.valueOf(course.getId())).setValue(course);
        }

    }

    // Delete a course
    public void deleteCourse(int courseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_COURSES, COLUMN_ID + " = ?", new String[]{String.valueOf(courseId)});
        db.close();

        // Delete from Firebase Realtime Database if delete from SQLite was successful
        if (rowsDeleted > 0) {
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("courses");
            firebaseRef.child(String.valueOf(courseId)).removeValue();
        }

    }

    // Retrieve all courses
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COURSES, null);

        if (cursor.moveToFirst()) {
            do {
                Course course = new Course(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_OF_WEEK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE_OF_CLASS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                );
                courses.add(course);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return courses;
    }

    public Course getCourseById(int courseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Course course = null;

        Cursor cursor = db.query(
                TABLE_COURSES,
                new String[]{"id", "name", "dayOfWeek", "time", "capacity", "duration", "price", "typeOfClass", "description"},
                "id = ?",
                new String[]{String.valueOf(courseId)},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow("dayOfWeek"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            int capacity = cursor.getInt(cursor.getColumnIndexOrThrow("capacity"));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration"));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
            String typeOfClass = cursor.getString(cursor.getColumnIndexOrThrow("typeOfClass"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));

            // Initialize the course object with retrieved data
            course = new Course(courseId, name, dayOfWeek, time, capacity, duration, price, typeOfClass, description);
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return course;
    }


    // ===================================================== Class ===================================================
    public void addClass(Class classSchedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_DATE, classSchedule.getDate());
        values.put(COLUMN_CLASS_COURSE_ID, classSchedule.getCourseId());
        values.put(COLUMN_CLASS_TEACHER, classSchedule.getTeacher());
        values.put(COLUMN_CLASS_COMMENTS, classSchedule.getComments());

        long classId = db.insert(TABLE_CLASSES, null, values); // Insert into SQLite
        db.close();

        // Insert into Firebase Realtime Database
        // Only sync if SQLite insertion was successful
        if (classId != -1) {
            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("classes");
            classSchedule.setId((int) classId); // Set the SQLite ID for the Firebase entry
            firebaseDatabase.child(String.valueOf(classId)).setValue(classSchedule);
        }
    }

    public void deleteClass(int classId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_CLASSES, "id = ?", new String[]{String.valueOf(classId)});
        db.close();

        // Delete from Firebase Realtime Database if delete from SQLite was successful
        if (rowsDeleted > 0) {
            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("classes");
            firebaseDatabase.child(String.valueOf(classId)).removeValue();
        }
    }

    public List<Class> getAllClasses() {
        List<Class> classes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CLASSES, null);

        if (cursor.moveToFirst()) {
            do {
                Class classSchedule = new Class();
                classSchedule.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                classSchedule.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_DATE)));
                classSchedule.setCourseId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLASS_COURSE_ID)));
                classSchedule.setTeacher(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_TEACHER)));
                classSchedule.setComments(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_COMMENTS)));
                classes.add(classSchedule);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return classes;
    }

    public Class getClassById(int classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CLASSES, null, "id = ?", new String[]{String.valueOf(classId)}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            Class classSchedule = new Class(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLASS_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLASS_COURSE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_TEACHER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_COMMENTS))
            );
            cursor.close();
            return classSchedule;
        }
        return null;
    }

    public void updateClass(Class classSchedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_DATE, classSchedule.getDate());
        values.put(COLUMN_CLASS_COURSE_ID, classSchedule.getCourseId());
        values.put(COLUMN_CLASS_TEACHER, classSchedule.getTeacher());
        values.put(COLUMN_CLASS_COMMENTS, classSchedule.getComments());


        int rowsAffected = db.update(TABLE_CLASSES, values, "id = ?", new String[]{String.valueOf(classSchedule.getId())});
        db.close();

        // Update in Firebase Realtime Database if update in SQLite was successful
        if (rowsAffected > 0) {
            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("classes");
            firebaseDatabase.child(String.valueOf(classSchedule.getId())).setValue(classSchedule);
        }
    }

    public List<Class> getClassesByCourseId(int courseId) {
        List<Class> classSchedules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_CLASSES,
                new String[]{COLUMN_CLASS_ID, COLUMN_CLASS_DATE, COLUMN_CLASS_COURSE_ID, COLUMN_CLASS_TEACHER, COLUMN_CLASS_COMMENTS},
                COLUMN_CLASS_COURSE_ID + "=?",
                new String[]{String.valueOf(courseId)},
                null,
                null,
                COLUMN_CLASS_DATE + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLASS_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_DATE));
                String teacher = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_TEACHER));
                String comments = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_COMMENTS));

                classSchedules.add(new Class(id, date, courseId, teacher, comments));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return classSchedules;
    }

    public List<Class> searchClasses(String searchText) {
        List<Class> classList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();


        String selection = "teacher LIKE ? OR date LIKE ?";
        String[] selectionArgs = new String[]{
                "%" + searchText + "%", // For partial matching in teacher
                "%" + searchText + "%"  // For partial matching in date
        };

        Cursor cursor = db.query(
                TABLE_CLASSES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Class classSchedule = new Class(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLASS_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLASS_COURSE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_TEACHER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_COMMENTS))

                );
                classList.add(classSchedule);
            }
            cursor.close();
        }
        db.close();
        return classList;
    }


    // =========================================== FireBase ==========================================

    // Check for internet connection
    public boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    // Sync data if online
    public void checkAndSyncDataIfOnline() {
        if (isConnectedToInternet()) {
            syncDataToFirebase();
        }
    }

    private void syncDataToFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference coursesRef = database.getReference("courses");
        DatabaseReference classesRef = database.getReference("classes");

        // Check if Firebase has data
        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Firebase has no data; perform sync
                    syncCoursesToFirebase(coursesRef);
                    syncClassesToFirebase(classesRef);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseSync", "Database error: " + error.getMessage());
            }
        });
    }

    private void syncCoursesToFirebase(DatabaseReference coursesRef) {
        List<Course> courseList = getAllCourses(); // Retrieve all courses from SQLite
        for (Course course : courseList) {
            String courseId = coursesRef.push().getKey();
            coursesRef.child(courseId).setValue(course);
        }
    }

    private void syncClassesToFirebase(DatabaseReference classesRef) {
        List<Class> classList = getAllClasses(); // Retrieve all classes from SQLite
        for (Class classSchedule : classList) {
            String classId = classesRef.push().getKey();
            classesRef.child(classId).setValue(classSchedule);
        }
    }
}
