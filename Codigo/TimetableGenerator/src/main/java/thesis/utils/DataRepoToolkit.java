package thesis.utils;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.ClassUnit;
import thesis.model.domain.elements.Room;
import thesis.model.domain.elements.Teacher;

import java.util.ArrayList;
import java.util.List;

public class DataRepoToolkit {
    private DataRepoToolkit() {}

    public static List<Room> getRooms(InMemoryRepository model, ClassUnit cls) {
        List<Room> rooms = new ArrayList<>();

        for(String roomId : cls.getClassRoomPenalties().keySet()) {
            Room room = model.getRoom(roomId);
            if(room != null) {
                rooms.add(room);
            }
        }

        return rooms;
    }

    public static List<Teacher> getTeacherList(InMemoryRepository model, ClassUnit cls) {
        List<Teacher> teachers = new ArrayList<>();

        for(int teacherId : cls.getTeacherIdList()) {
            Teacher teacher = model.getTeacher(teacherId);
            if(teacher != null) {
                teachers.add(teacher);
            }
        }

        return teachers;
    }
}
