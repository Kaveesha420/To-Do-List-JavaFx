package model.Entity;

import javafx.scene.control.CheckBox;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class toDoList {
    private int id;
    private String title;
    private String description;
    private String date;
    private String status;
    private CheckBox selectCheckBox;


    public toDoList(int id, String title, String description, String date, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.status = status;
        this.selectCheckBox = new CheckBox();
    }
}