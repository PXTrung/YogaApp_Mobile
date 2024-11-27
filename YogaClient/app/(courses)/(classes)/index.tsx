import { View, Text, StyleSheet, TextInput, FlatList } from "react-native";
import React, { useEffect, useState } from "react";
import { Stack, useLocalSearchParams, useRouter } from "expo-router";
import { database } from "../../../config/firebaseConfig"; // Adjust the path if needed
import { ref, query, orderByChild, equalTo, onValue } from "firebase/database";

interface Class {
  id: string;
  teacher: string;
  date: string;
  courseId: string;
  comments: string;
}

const ClassDetailsScreen = () => {
  const [classes, setClasses] = useState<Class[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const {
    id,
    courseName,
    typeOfClass,
    dayOfWeek,
    time,
    duration,
    description,
  } = useLocalSearchParams();

  useEffect(() => {
    const fetchClasses = async () => {
      try {
        // Ensure courseId is a number
        const validCourseId = typeof id === "number" ? id : Number(id);
        const classesRef = ref(database, "classes");
        const classesQuery = query(
          classesRef,
          orderByChild("courseId"),
          equalTo(validCourseId)
        );

        onValue(classesQuery, (snapshot) => {
          const data = snapshot.val();
          console.log("Raw Firebase Data:", data); // Log raw data from Firebase
          if (data) {
            const formattedClasses = Object.keys(data).map((key) => ({
              id: key,
              ...data[key],
            }));
            setClasses(formattedClasses);
          } else {
            setClasses([]);
          }
          setLoading(false);
        });
      } catch (error) {
        console.error("Error fetching classes:", error);
        setLoading(false);
      }
    };

    fetchClasses();
  }, [id]);

  const filteredClasses = classes.filter(
    (cls) =>
      cls.teacher.toLowerCase().includes(searchTerm.toLowerCase()) ||
      cls.date.includes(searchTerm)
  );

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerTitle: "Yoga Class" }} />
      <View style={styles.detailsSection}>
        <View style={styles.headerRow}>
          <Text style={styles.header}>{courseName}</Text>
        </View>

        <Text style={styles.subHeader}>
          Day: {dayOfWeek} | Time: {time} | Duration: {duration} mins
        </Text>
        <Text style={styles.description}>{description}</Text>
      </View>

      <View style={styles.separator} />

      <View style={styles.classList}>
        <Text style={styles.classListHeader}>Classes</Text>

        {/* Search Bar */}
        <TextInput
          style={styles.searchBar}
          placeholder="Search by teacher or date"
          value={searchTerm}
          onChangeText={setSearchTerm}
        />

        {/* List of Classes */}
        <FlatList
          data={filteredClasses}
          keyExtractor={(item) => item.id}
          renderItem={({ item }) => (
            <View style={styles.classItem}>
              <Text style={styles.classDate}>{item.date}</Text>
              <Text style={styles.classTeacher}>{item.teacher}</Text>
              <Text style={styles.classComments}>{item.comments}</Text>
            </View>
          )}
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
  detailsSection: {
    marginBottom: 16,
  },
  header: {
    fontSize: 22,
    fontWeight: "bold",
  },
  headerRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 8,
  },
  courseType: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    fontSize: 12,
    fontWeight: "bold",
    color: "#fff",
  },
  subHeader: {
    fontSize: 16,
    color: "#555",
  },
  description: {
    fontSize: 14,
    color: "#888",
    marginTop: 8,
  },
  separator: {
    borderBottomWidth: 1,
    marginVertical: 16,
  },
  classList: {
    flex: 1,
  },
  classListHeader: {
    fontSize: 20,
    fontWeight: "bold",
    alignSelf: "flex-end",
    color: "#6bd731",
    marginBottom: 10,
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
  searchBar: {
    height: 40,
    borderWidth: 1,
    borderColor: "#ccc",
    borderRadius: 20,
    paddingHorizontal: 15,
    fontSize: 16,
    marginBottom: 20,
    backgroundColor: "#fff",
  },
  classItem: {
    padding: 16,
    marginBottom: 16,
    borderRadius: 8,
    backgroundColor: "#f9f9f9",
    shadowColor: "#000",
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  classDate: {
    fontSize: 16,
    color: "#54e382",
    fontWeight: "bold",
  },
  classTeacher: {
    fontSize: 14,
    fontWeight: "bold",
    color: "#555",
  },
  classComments: {
    fontSize: 12,
    color: "#777",
    marginTop: 5,
  },
});

const typeOfClassStyles = {
  "Flow Yoga": styles.FlowYoga,
  "Aerial Yoga": styles.AerialYoga,
  "Family Yoga": styles.FamilyYoga,
};

export default ClassDetailsScreen;
