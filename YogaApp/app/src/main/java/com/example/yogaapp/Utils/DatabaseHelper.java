package com.example.yogaapp.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

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
    private static final String DBNAME = "yogaApp.db";
    private static final int DBVERSION = 1;
    private static final String TABLECOURSES = "Courses";
    private Context context;

    private static final String CO_COURSEID = "id";
    private static final String CO_COURSENAME = "name";
    private static final String CO_COURSEDAYOFWEEK = "dayOfWeek";
    private static final String CO_COURSETIME = "time";
    private static final String CO_COURSECAPACITY = "capacity";
    private static final String CO_COURSEDURATION = "duration";
    private static final String CO_COURSEPRICE = "price";
    private static final String CO_COURSETYPEOFCLASS = "typeOfClass";
    private static final String CO_COURSEDESCRIPTION = "description";


    // Table name and column names for class schedule
    private static final String TABLECLASSES = "classes";
    private static final String CO_CLASSID = "id";
    private static final String CO_CLASSDATE = "date";
    private static final String CO_CLASSCOURSEID = "course_id";
    private static final String CO_CLASSTEACHER = "teacher";
    private static final String CO_CLASSCOMMENTS = "comments";

    public DatabaseHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableCourses = "CREATE TABLE " + TABLECOURSES + " (" +
                CO_COURSEID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CO_COURSENAME + " TEXT NOT NULL, " +
                CO_COURSEDAYOFWEEK + " TEXT NOT NULL, " +
                CO_COURSETIME + " TEXT NOT NULL, " +
                CO_COURSECAPACITY + " INTEGER NOT NULL, " +
                CO_COURSEDURATION + " INTEGER NOT NULL, " +
                CO_COURSEPRICE + " REAL NOT NULL, " +
                CO_COURSETYPEOFCLASS + " TEXT NOT NULL, " +
                CO_COURSEDESCRIPTION + " TEXT)";
        db.execSQL(createTableCourses);

        // Classes table creation
        String CREATE_CLASSES_TABLE = "CREATE TABLE " + TABLECLASSES + "("
                + CO_CLASSID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CO_CLASSDATE  + " TEXT,"
                + CO_CLASSCOURSEID + " INTEGER,"
                + CO_CLASSTEACHER + " TEXT NOT NULL,"
                + CO_CLASSCOMMENTS + " TEXT,"
                + "FOREIGN KEY(" + CO_CLASSCOURSEID + ") REFERENCES " + TABLECOURSES + "(id)"
                + ")";
        db.execSQL(CREATE_CLASSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    // Insert a new course
    public void addCourse(Course course) {
        SQLiteDatabase sqlDb = this.getWritableDatabase();
        ContentValues conValues = new ContentValues();
        conValues.put(CO_COURSENAME, course.getName());
        conValues.put(CO_COURSEDAYOFWEEK, course.getDayOfWeek());
        conValues.put(CO_COURSETIME, course.getTime());
        conValues.put(CO_COURSECAPACITY, course.getCapacity());
        conValues.put(CO_COURSEDURATION, course.getDuration());
        conValues.put(CO_COURSEPRICE, course.getPrice());
        conValues.put(CO_COURSETYPEOFCLASS, course.getTypeOfClass());
        conValues.put(CO_COURSEDESCRIPTION, course.getDescription());

        // Insert in SQLite and retrieve the inserted row ID
        long id = sqlDb.insert(TABLECOURSES, null, conValues);
        course.setId((int) id); // Set the course ID for Firebase
        sqlDb.close();

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
        SQLiteDatabase sqlDb = this.getWritableDatabase();
        ContentValues conValues = new ContentValues();
        conValues.put(CO_COURSEDAYOFWEEK, course.getDayOfWeek());
        conValues.put(CO_COURSENAME, course.getName());
        conValues.put(CO_COURSETIME, course.getTime());
        conValues.put(CO_COURSECAPACITY, course.getCapacity());
        conValues.put(CO_COURSEDURATION, course.getDuration());
        conValues.put(CO_COURSEPRICE, course.getPrice());
        conValues.put(CO_COURSETYPEOFCLASS, course.getTypeOfClass());
        conValues.put(CO_COURSEDESCRIPTION, course.getDescription());

        int rowsAfftected = sqlDb.update(TABLECOURSES, conValues, CO_COURSEID + " = ?", new String[]{String.valueOf(course.getId())});
        sqlDb.close();

        // Update in Firebase Realtime Database if update in SQLite was successful
        if (rowsAfftected > 0){
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("courses");
            firebaseRef.child(String.valueOf(course.getId())).setValue(course);
        }

    }

    // Delete a course
    public void deleteCourse(int courseId) {
        SQLiteDatabase sqlDb = this.getWritableDatabase();
        int deletedRowCourse = sqlDb.delete(TABLECOURSES, CO_COURSEID + " = ?", new String[]{String.valueOf(courseId)});
        sqlDb.close();

        // Delete from Firebase Realtime Database if delete from SQLite was successful
        if (deletedRowCourse > 0) {
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("courses");
            firebaseRef.child(String.valueOf(courseId)).removeValue();
        }

    }

    // Retrieve all courses
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase sqlDb = this.getReadableDatabase();
        Cursor cur = sqlDb.rawQuery("SELECT * FROM " + TABLECOURSES, null);

        if (cur.moveToFirst()) {
            do {
                Course course = new Course(
                        cur.getInt(cur.getColumnIndexOrThrow(CO_COURSEID)),
                        cur.getString(cur.getColumnIndexOrThrow(CO_COURSENAME)),
                        cur.getString(cur.getColumnIndexOrThrow(CO_COURSEDAYOFWEEK)),
                        cur.getString(cur.getColumnIndexOrThrow(CO_COURSETIME)),
                        cur.getInt(cur.getColumnIndexOrThrow(CO_COURSECAPACITY)),
                        cur.getInt(cur.getColumnIndexOrThrow(CO_COURSEDURATION)),
                        cur.getDouble(cur.getColumnIndexOrThrow(CO_COURSEPRICE)),
                        cur.getString(cur.getColumnIndexOrThrow(CO_COURSETYPEOFCLASS)),
                        cur.getString(cur.getColumnIndexOrThrow(CO_COURSEDESCRIPTION))
                );
                courses.add(course);
            } while (cur.moveToNext());
        }
        cur.close();
        sqlDb.close();
        return courses;
    }

    public Course getCourseById(int courseId) {
        SQLiteDatabase sqlDb = this.getReadableDatabase();
        Course course = null;

        Cursor cur = sqlDb.query(
                TABLECOURSES,
                new String[]{"id", "name", "dayOfWeek", "time", "capacity", "duration", "price", "typeOfClass", "description"},
                "id = ?",
                new String[]{String.valueOf(courseId)},
                null,
                null,
                null
        );

        if (cur != null && cur.moveToFirst()) {
            String name = cur.getString(cur.getColumnIndexOrThrow("name"));
            String dayOfWeek = cur.getString(cur.getColumnIndexOrThrow("dayOfWeek"));
            String time = cur.getString(cur.getColumnIndexOrThrow("time"));
            int capacity = cur.getInt(cur.getColumnIndexOrThrow("capacity"));
            int duration = cur.getInt(cur.getColumnIndexOrThrow("duration"));
            double price = cur.getDouble(cur.getColumnIndexOrThrow("price"));
            String typeOfClass = cur.getString(cur.getColumnIndexOrThrow("typeOfClass"));
            String description = cur.getString(cur.getColumnIndexOrThrow("description"));

            // Initialize the course object with retrieved data
            course = new Course(courseId, name, dayOfWeek, time, capacity, duration, price, typeOfClass, description);
        }

        if (cur != null) {
            cur.close();
        }
        sqlDb.close();

        return course;
    }


    // ===================================================== Class ===================================================
    public void addClass(Class classSchedule) {
        SQLiteDatabase sqlDb = this.getWritableDatabase();
        ContentValues conValues = new ContentValues();
        conValues.put(CO_CLASSDATE , classSchedule.getDate());
        conValues.put(CO_CLASSCOURSEID, classSchedule.getCourseId());
        conValues.put(CO_CLASSTEACHER, classSchedule.getTeacher());
        conValues.put(CO_CLASSCOMMENTS, classSchedule.getComments());

        long classId = sqlDb.insert(TABLECLASSES, null, conValues); // Insert into SQLite
        sqlDb.close();

        // Insert into Firebase Realtime Database
        // Only sync if SQLite insertion was successful
        if (classId != -1) {
            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("classes");
            classSchedule.setId((int) classId); // Set the SQLite ID for the Firebase entry
            firebaseDatabase.child(String.valueOf(classId)).setValue(classSchedule);
        }
    }

    public void deleteClass(int classId) {
        SQLiteDatabase sqlDb = this.getWritableDatabase();
        int deletedRowsClass = sqlDb.delete(TABLECLASSES, "id = ?", new String[]{String.valueOf(classId)});
        sqlDb.close();

        // Delete from Firebase Realtime Database if delete from SQLite was successful
        if (deletedRowsClass > 0) {
            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("classes");
            firebaseDatabase.child(String.valueOf(classId)).removeValue();
        }
    }

    public List<Class> getAllClasses() {
        List<Class> yogaClasses = new ArrayList<>();
        SQLiteDatabase sqlDb = this.getReadableDatabase();
        Cursor cur = sqlDb.rawQuery("SELECT * FROM " + TABLECLASSES, null);

        if (cur.moveToFirst()) {
            do {
                Class classSchedule = new Class();
                classSchedule.setId(cur.getInt(cur.getColumnIndexOrThrow(CO_CLASSID)));
                classSchedule.setDate(cur.getString(cur.getColumnIndexOrThrow(CO_CLASSDATE )));
                classSchedule.setCourseId(cur.getInt(cur.getColumnIndexOrThrow(CO_CLASSCOURSEID)));
                classSchedule.setTeacher(cur.getString(cur.getColumnIndexOrThrow(CO_CLASSTEACHER)));
                classSchedule.setComments(cur.getString(cur.getColumnIndexOrThrow(CO_CLASSCOMMENTS)));
                yogaClasses.add(classSchedule);
            } while (cur.moveToNext());
        }
        cur.close();
        sqlDb.close();
        return yogaClasses;
    }

    public Class getClassById(int classId) {
        SQLiteDatabase sqlDb = this.getReadableDatabase();
        Cursor cur = sqlDb.query(TABLECLASSES, null, "id = ?", new String[]{String.valueOf(classId)}, null, null, null);

        if (cur != null) {
            cur.moveToFirst();
            Class classSchedule = new Class(
                    cur.getInt(cur.getColumnIndexOrThrow(CO_CLASSID)),
                    cur.getString(cur.getColumnIndexOrThrow(CO_CLASSDATE )),
                    cur.getInt(cur.getColumnIndexOrThrow(CO_CLASSCOURSEID)),
                    cur.getString(cur.getColumnIndexOrThrow(CO_CLASSTEACHER)),
                    cur.getString(cur.getColumnIndexOrThrow(CO_CLASSCOMMENTS))
            );
            cur.close();
            return classSchedule;
        }
        return null;
    }

    public void updateClass(Class classSchedule) {
        SQLiteDatabase sqlDb = this.getWritableDatabase();
        ContentValues conValues = new ContentValues();
        conValues.put(CO_CLASSDATE , classSchedule.getDate());
        conValues.put(CO_CLASSCOURSEID, classSchedule.getCourseId());
        conValues.put(CO_CLASSTEACHER, classSchedule.getTeacher());
        conValues.put(CO_CLASSCOMMENTS, classSchedule.getComments());


        int rowsAffected = sqlDb.update(TABLECLASSES, conValues, "id = ?", new String[]{String.valueOf(classSchedule.getId())});
        sqlDb.close();

        // Update in Firebase Realtime Database if update in SQLite was successful
        if (rowsAffected > 0) {
            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("classes");
            firebaseDatabase.child(String.valueOf(classSchedule.getId())).setValue(classSchedule);
        }
    }

    public List<Class> getClassesByCourseId(int courseId) {
        List<Class> classSchedules = new ArrayList<>();
        SQLiteDatabase sqlDb = this.getReadableDatabase();

        Cursor cur = sqlDb.query(
                TABLECLASSES,
                new String[]{CO_CLASSID, CO_CLASSDATE , CO_CLASSCOURSEID, CO_CLASSTEACHER, CO_CLASSCOMMENTS},
                CO_CLASSCOURSEID + "=?",
                new String[]{String.valueOf(courseId)},
                null,
                null,
                CO_CLASSDATE  + " ASC"
        );

        if (cur.moveToFirst()) {
            do {
                int tempId = cur.getInt(cur.getColumnIndexOrThrow(CO_CLASSID));
                String tempDate = cur.getString(cur.getColumnIndexOrThrow(CO_CLASSDATE ));
                String tempTeacher = cur.getString(cur.getColumnIndexOrThrow(CO_CLASSTEACHER));
                String tempComments = cur.getString(cur.getColumnIndexOrThrow(CO_CLASSCOMMENTS));

                classSchedules.add(new Class(tempId, tempDate, courseId, tempTeacher, tempComments));
            } while (cur.moveToNext());
        }

        cur.close();
        sqlDb.close();

        return classSchedules;
    }

    public List<Class> searchClasses(String searchText) {
        List<Class> classList = new ArrayList<>();
        SQLiteDatabase sqlDb = this.getReadableDatabase();


        String selection = "teacher LIKE ? OR date LIKE ?";
        String[] selectionArgs = new String[]{
                "%" + searchText + "%", // For partial matching in teacher
                "%" + searchText + "%"  // For partial matching in date
        };

        Cursor cur = sqlDb.query(
                TABLECLASSES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cur != null) {
            while (cur.moveToNext()) {
                Class classSchedule = new Class(
                        cur.getInt(cur.getColumnIndexOrThrow(CO_CLASSID)),
                        cur.getString(cur.getColumnIndexOrThrow(CO_CLASSDATE )),
                        cur.getInt(cur.getColumnIndexOrThrow(CO_CLASSCOURSEID)),
                        cur.getString(cur.getColumnIndexOrThrow(CO_CLASSTEACHER)),
                        cur.getString(cur.getColumnIndexOrThrow(CO_CLASSCOMMENTS))

                );
                classList.add(classSchedule);
            }
            cur.close();
        }
        sqlDb.close();
        return classList;
    }


    // =========================================== FireBase ==========================================

    // Check for internet connection
    public boolean isConnectedToInternet() {
        ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInternet = connectManager.getActiveNetworkInfo();
        return activeInternet != null && activeInternet.isConnected();
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
            public void onCancelled(DatabaseError err) {
                Log.e("FirebaseSync", "Database error: " + err.getMessage());
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
