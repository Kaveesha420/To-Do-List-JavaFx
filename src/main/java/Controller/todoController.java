package Controller;

import DB.DbConnection;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import model.Entity.toDoList;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class todoController implements Initializable {

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESC = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_STATUS = "status";

    @FXML private JFXTextField txtTask1;
    @FXML private JFXTextField txtTask2;
    @FXML private JFXTextField txtTask3;
    @FXML private JFXTextField txtTask4;
    @FXML private JFXTextField txtTask5;
    @FXML private JFXTextField txtTask6;
    @FXML private JFXTextField txtTask7;

    @FXML private JFXCheckBox chkTask1;
    @FXML private JFXCheckBox chkTask2;
    @FXML private JFXCheckBox chkTask3;
    @FXML private JFXCheckBox chkTask4;
    @FXML private JFXCheckBox chkTask5;
    @FXML private JFXCheckBox chkTask6;
    @FXML private JFXCheckBox chkTask7;

    @FXML private Label lblRemove1;
    @FXML private Label lblRemove2;
    @FXML private Label lblRemove3;
    @FXML private Label lblRemove4;
    @FXML private Label lblRemove5;
    @FXML private Label lblRemove6;
    @FXML private Label lblRemove7;

    private JFXTextField[] taskFields;
    private JFXCheckBox[] taskCheckBoxes;
    private Label[] removeLabels;

    @FXML private TableView<toDoList> tblCompletedTasks;
    @FXML private TableColumn<toDoList, Integer> colId;
    @FXML private TableColumn<toDoList, String> colTitle;
    @FXML private TableColumn<toDoList, String> colDesc;
    @FXML private TableColumn<toDoList, String> colDate;

    @FXML private JFXTextField txtTitle;
    @FXML private JFXTextField txtDescription;
    @FXML private DatePicker datePicker;
    @FXML private Label lblDate;
    @FXML private Label lblTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        taskFields = new JFXTextField[]{txtTask1, txtTask2, txtTask3, txtTask4, txtTask5, txtTask6, txtTask7};
        taskCheckBoxes = new JFXCheckBox[]{chkTask1, chkTask2, chkTask3, chkTask4, chkTask5, chkTask6, chkTask7};
        removeLabels = new Label[]{lblRemove1, lblRemove2, lblRemove3, lblRemove4, lblRemove5, lblRemove6, lblRemove7};

        colId.setCellValueFactory(new PropertyValueFactory<>(COLUMN_ID));
        colTitle.setCellValueFactory(new PropertyValueFactory<>(COLUMN_TITLE));
        colDesc.setCellValueFactory(new PropertyValueFactory<>(COLUMN_DESC));
        colDate.setCellValueFactory(new PropertyValueFactory<>(COLUMN_DATE));

        loadAllTasks();
        setupRemoveLabelActions();
        loadDateAndTime();
    }

    private void loadDateAndTime() {
        lblDate.setText(LocalDate.now().toString());

        Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
            lblTime.setText(currentTime.format(formatter));
        }), new KeyFrame(Duration.seconds(1)));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void setupRemoveLabelActions() {
        for (int i = 0; i < removeLabels.length; i++) {
            final int index = i;
            removeLabels[i].setOnMouseClicked((MouseEvent event) -> {
                if (taskCheckBoxes[index].getUserData() != null) {
                    int id = (int) taskCheckBoxes[index].getUserData();
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this task?", ButtonType.YES, ButtonType.NO);
                    if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                        deleteTaskFromDB(id);
                    }
                }
            });
        }
    }

    @FXML
    void btnAddTaskOnAction(ActionEvent event) {
        String title = txtTitle.getText();
        String desc = txtDescription.getText();
        String date = (datePicker.getValue() != null) ? datePicker.getValue().toString() : "";

        if (title.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Title is required!").show();
            return;
        }

        String sql = "INSERT INTO todo_tasks (title, description, date, status) VALUES (?, ?, ?, ?)";

        try {
            Connection connection = DbConnection.getInstance().getConnection();
            try (PreparedStatement pstm = connection.prepareStatement(sql)) {
                pstm.setString(1, title);
                pstm.setString(2, desc);
                pstm.setString(3, date);
                pstm.setString(4, "PENDING");

                if (pstm.executeUpdate() > 0) {
                    clearInputs();
                    loadAllTasks();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnReloadOnAction(ActionEvent event) {
        String sql = "UPDATE todo_tasks SET status = 'COMPLETED' WHERE id = ?";

        try {
            Connection connection = DbConnection.getInstance().getConnection();
            try (PreparedStatement pstm = connection.prepareStatement(sql)) {
                boolean isUpdated = false;

                for (JFXCheckBox checkBox : taskCheckBoxes) {
                    if (checkBox.isSelected() && checkBox.getUserData() != null) {
                        int id = (int) checkBox.getUserData();
                        pstm.setInt(1, id);
                        pstm.addBatch();
                        isUpdated = true;
                    }
                }

                if (isUpdated) {
                    pstm.executeBatch();
                    loadAllTasks();
                } else {
                    new Alert(Alert.AlertType.INFORMATION, "Select a task to complete!").show();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnClearTaskOnAction(ActionEvent event) {
        toDoList selectedTask = tblCompletedTasks.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete completed task?", ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                deleteTaskFromDB(selectedTask.getId());
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Select a task from the table!").show();
        }
    }

    private void deleteTaskFromDB(int id) {
        try {
            Connection connection = DbConnection.getInstance().getConnection();

            String deleteSql = "DELETE FROM todo_tasks WHERE id = ?";
            try (PreparedStatement pstm = connection.prepareStatement(deleteSql)) {
                pstm.setInt(1, id);
                pstm.executeUpdate();
            }

            String reorderSql = "SET @count = 0";
            String updateSql = "UPDATE todo_tasks SET id = @count:= @count + 1";
            String resetAutoIncSql = "ALTER TABLE todo_tasks AUTO_INCREMENT = 1";

            try (java.sql.Statement stmt = connection.createStatement()) {
                stmt.execute(reorderSql);
                stmt.execute(updateSql);
                stmt.execute(resetAutoIncSql);
            }

            loadAllTasks();
            new Alert(Alert.AlertType.INFORMATION, "Task Deleted & IDs Reordered!").show();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAllTasks() {
        loadPendingTasksToLeft();
        loadCompletedTasksToRight();
    }

    private void loadPendingTasksToLeft() {

        for (int i = 0; i < 7; i++) {
            taskFields[i].setText("");
            taskCheckBoxes[i].setSelected(false);
            taskCheckBoxes[i].setUserData(null);
            taskFields[i].setDisable(true);
            taskCheckBoxes[i].setDisable(true);
            removeLabels[i].setVisible(false);
        }

        String sql = "SELECT id, title, description, date, status FROM todo_tasks WHERE status = 'PENDING' LIMIT 7";

        try {
            Connection connection = DbConnection.getInstance().getConnection();
            try (PreparedStatement pstm = connection.prepareStatement(sql);
                 ResultSet resultSet = pstm.executeQuery()) {

                int index = 0;
                while (resultSet.next() && index < 7) {
                    int id = resultSet.getInt(COLUMN_ID);
                    String title = resultSet.getString(COLUMN_TITLE);

                    taskFields[index].setText(title);
                    taskFields[index].setDisable(false);

                    taskCheckBoxes[index].setDisable(false);
                    taskCheckBoxes[index].setUserData(id); // Store ID

                    removeLabels[index].setVisible(true);
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCompletedTasksToRight() {
        ObservableList<toDoList> completedList = FXCollections.observableArrayList();
        String sql = "SELECT id, title, description, date, status FROM todo_tasks WHERE status = 'COMPLETED'";

        try {
            Connection connection = DbConnection.getInstance().getConnection();
            try (PreparedStatement pstm = connection.prepareStatement(sql);
                 ResultSet resultSet = pstm.executeQuery()) {

                while (resultSet.next()) {
                    completedList.add(new toDoList(
                            resultSet.getInt(COLUMN_ID),
                            resultSet.getString(COLUMN_TITLE),
                            resultSet.getString(COLUMN_DESC),
                            resultSet.getString(COLUMN_DATE),
                            resultSet.getString(COLUMN_STATUS)
                    ));
                }
            }
            tblCompletedTasks.setItems(completedList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearInputs() {
        txtTitle.clear();
        txtDescription.clear();
        datePicker.setValue(null);
    }
}