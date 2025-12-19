package model.Entity;

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
}