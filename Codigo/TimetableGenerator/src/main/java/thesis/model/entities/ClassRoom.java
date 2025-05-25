package thesis.model.entities;

import jakarta.persistence.*;
import thesis.model.entities.EmbeddableIds.ClassRoomPK;

@Entity
@Table(name = "class_room")
public class ClassRoom {
    @EmbeddedId
    private ClassRoomPK id;

    @ManyToOne
    @MapsId("classUnitPK")
    @JoinColumns({
            @JoinColumn(name = "course_id", referencedColumnName = "course_id"),
            @JoinColumn(name = "config_id", referencedColumnName = "config_id"),
            @JoinColumn(name = "subpart_id", referencedColumnName = "subpart_id"),
            @JoinColumn(name = "class_id", referencedColumnName = "class_id")
    })
    private ClassUnit classUnit;

    @ManyToOne
    @MapsId("roomId")
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private Room room;

    @Column(nullable = false)
    private int penalty;

    public ClassRoom() {}

    public ClassRoom(ClassUnit classUnit, Room room, int penalty) {
        this.id = new ClassRoomPK(classUnit.getId(), room.getId());
        this.classUnit = classUnit;
        this.room = room;
        this.penalty = penalty;
    }

    public ClassRoomPK getId() {
        return id;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public ClassUnit getClassUnit() {
        return classUnit;
    }

    public void setClassUnit(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.id.setClassUnitPK(classUnit.getId());
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
        this.id.setRoomId(room.getId());
    }
}
