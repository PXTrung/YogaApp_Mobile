import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator,
} from "react-native";
import React, { useEffect, useState } from "react";
import { useRouter } from "expo-router";
import { ref, onValue } from "firebase/database";
import { database } from "../../config/firebaseConfig";

interface Course {
  id: number;
  name: string;
  price: number;
  dayOfWeek: string;
  time: string;
  duration: number;
  typeOfClass: "Flow Yoga" | "Aerial Yoga" | "Family Yoga";
  capacity: number;
  description: string;
}

const yogaCourseScreen = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const coursesRef = ref(database, "courses");
    const unsubscribe = onValue(coursesRef, (snapshot) => {
      const data = snapshot.val();
      if (data) {
        const coursesArray = Object.keys(data).map((key) => ({
          id: key,
          ...data[key],
        }));
        setCourses(coursesArray);
      }
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const handleCoursePress = (course: Course) => {
    // Navigate to the ClassDetails screen with the course data
    router.push({
      pathname: "/(courses)/(classes)",
      params: {
        id: course.id,
        courseName: course.name,
        typeOfClass: course.typeOfClass,
        dayOfWeek: course.dayOfWeek,
        time: course.time,
        duration: course.duration,
        description: course.description, // Example data
      },
    });
  };

  if (loading) {
    return (
      <View style={styles.loader}>
        <ActivityIndicator size="large" color="#006400" />
      </View>
    );
  }

  const renderCourseItem = ({ item }: { item: Course }) => {
    return (
      <View style={styles.courseContainer}>
        <TouchableOpacity onPress={() => handleCoursePress(item)}>
          {/* Header Row with Name and Type */}
          <View style={styles.headerRow}>
            <Text style={styles.courseName}>{item.name}</Text>
            <Text
              style={[styles.courseType, typeOfClassStyles[item.typeOfClass]]}
            >
              {item.typeOfClass}
            </Text>
          </View>

          {/* Course Details */}
          <Text style={styles.courseDetail}>Time: {item.time}</Text>
          <Text style={styles.courseDetail}>Day: {item.dayOfWeek}</Text>
          <View style={styles.headerRow}>
            <Text style={styles.courseDetail}>Capacity: {item.capacity}</Text>
            <Text style={styles.coursePrice}>${item.price}</Text>
          </View>
        </TouchableOpacity>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      {/* Logo Section */}
      <View style={styles.logoContainer}>
        <Text style={styles.logoText}>YogaFlow</Text>
      </View>

      <FlatList
        data={courses}
        renderItem={renderCourseItem}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={styles.container}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    padding: 16,
  },
  loader: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  courseContainer: {
    padding: 16,
    marginBottom: 16,
    borderRadius: 8,
    backgroundColor: "#f9f9f9",
    shadowColor: "#000",
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  headerRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 8,
  },
  logoContainer: {
    zIndex: 10, // Ensure the logo stays above other content
    paddingVertical: 5,
    paddingHorizontal: 15,
    borderRadius: 5,
  },
  logoText: {
    fontSize: 28,
    fontWeight: "bold",
    color: "#27AE60",
  },
  courseName: {
    fontSize: 18,
    fontWeight: "bold",
  },
  courseType: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    fontSize: 12,
    fontWeight: "bold",
    color: "#fff",
  },
  FlowYoga: {
    backgroundColor: "#4A90E2", // Blue
  },
  AerialYoga: {
    backgroundColor: "#27AE60", // Green
  },
  FamilyYoga: {
    backgroundColor: "#F2994A", // Orange
  },
  courseDetail: {
    fontSize: 14,
    color: "#555",
  },
  coursePrice: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#E74C3C",
  },
});

const typeOfClassStyles = {
  "Flow Yoga": styles.FlowYoga,
  "Aerial Yoga": styles.AerialYoga,
  "Family Yoga": styles.FamilyYoga,
};

export default yogaCourseScreen;
