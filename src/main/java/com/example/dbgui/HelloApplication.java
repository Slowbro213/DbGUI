package com.example.dbgui;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Scanner;

public class HelloApplication extends Application {
    boolean pressed = false;

    static Connection connection;
    GridPane middleButtons;

    BorderPane borderPane;

    VBox Commands;

    VBox vbox;

    Label xlabel;

    VBox boz;
    RadioButton rb1;
    RadioButton rb2;
    RadioButton rb3;
    RadioButton rb4;
    RadioButton rb5;
    RadioButton rb6;

    StackPane spane; // profile zone
    VBox Boux; // part of the profile zone

    ScrollPane gradesZone; // as the name implies its the grades zone

    public void CreateCommandsZone()
    {

        Label l = new Label("Enter An SQL Command!");
        l.setStyle("-fx-font-weight: bold;");
        l.setFont(new Font("Helvetica",20));
        Button query = new Button("Execute Query");
        TextArea area = new TextArea();
        area.setStyle("-fx-control-inner-background: black; -fx-text-fill: green; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Courier New';");
        area.setMaxWidth(1500);
        TableView tableView = new TableView();
        tableView.setMaxWidth(1500);
        query.setOnAction(e->loadDataFromDatabase(area.getText(),tableView));
        Commands = new VBox(l,area,query,tableView);
        Commands.setSpacing(50);
    }

    public void CreateProfileZone(String name)
    {

        ResultSet rs = null;
        ImageView y = new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\dictionary.gif"));
        ImageView x = new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\search-file.gif"));
        y.setFitHeight(100);
        y.setFitWidth(100);
        x.setFitHeight(95);
        x.setFitWidth(100);
        Button Courses = new Button(" ",y);
        Courses.setStyle("-fx-background-color: white");
        Button Documents = new Button("",x);
        Documents.setStyle("-fx-background-color: white");
        TableView tableView = new TableView();
        tableView.setMinWidth(300);
        HBox buttons = new HBox(Courses,Documents);
        buttons.setSpacing(60);
        Boux = new VBox(buttons , tableView);
        Boux.setPadding(new Insets(100,50,0,0));
        Boux.setSpacing(100);
        Courses.setOnAction(e->{
            LoadCoursesToProfile("SELECT Courses.Code , Courses.Ects  "  +
                    "FROM Students,Interim_Grades,Assignments,Courses " +
                    "WHERE Students.Epoka_ID = Interim_Grades.Epoka_ID " +
                    "AND Interim_Grades.Assignment_ID = Assignments.Assignment_ID " +
                    "AND Assignments.Course_ID = Courses.COurse_ID "+
                    "AND Students.name = '" + name + "';",tableView);
        });

        Documents.setOnAction(e->{
            LoadCoursesToProfile("SELECT Type , info From Student_Documents " +
                    "JOIN Students ON Students.Epoka_ID = Student_Documents.Epoka_ID " +
                    "WHERE Students.name = '" + name + "';",tableView);
        });
        HBox box = new HBox();
        try{
         rs = query("Select * From Students " +
                "Where Name = '" + name + "'");}catch (Exception ee)
        {
            return;
        }
        Rectangle r1 = new Rectangle(670,758,Color.web("#00BFFF"));
        VBox vox = new VBox();
        vox.setPadding(new Insets(101,0,98,0));

        String[] labels = {"Epoka ID No.:","ID Card No.:","Name:", "Surname:", "Birthday:","Birth Place:",
                "Gender:", "Blood Group:", "Marital Status:", "Citizenship:", "Passport No.:"
                , "Primary Email:", "Secondary Email:","Exam ID:","Status:","CGPA:" , "Enrollment Date:"};

        int i = 0;
        for (String labe : labels) {
            StackPane stackPane = new StackPane();
            Rectangle r3 = new Rectangle(660,44);
            if(i%2==0)
            {
                r3.setFill(Color.WHITE);
                r3.setOnMouseEntered(e->r3.setFill(Color.web("#B9F2FF")));
                r3.setOnMouseExited(e->r3.setFill(Color.WHITE));
            }
            else
            {
                r3.setFill(Color.web("#EEEEEE"));
                r3.setOnMouseEntered(e->r3.setFill(Color.web("#B9F2FF")));
                r3.setOnMouseExited(e->r3.setFill(Color.web( "#EEEEEE")));
            }
            String column = null;
            try{
                column = rs.getString(i+1);}catch (SQLException sqle){sqle.printStackTrace();}
            if(i==14)
            {
                if(column.equals("0"))
                    column="Student";
                else
                    column="Graduated";
            }
            else if(i==8)
            {
                if(column.equals("0"))
                    column="Bachelor";
                else
                    column="Married";
            }
            Label labe1 = new Label(labe+ "   " + column);

            labe1.setFont(new Font("Helvetica",16));
            stackPane.getChildren().addAll(r3,labe1);

            vox.getChildren().add(stackPane);


            i++;
        }
        spane = new StackPane(r1,vox);
        try{
            box.getChildren().addAll(Boux,spane);
            box.setPadding(new Insets(0,0,0,200));
            vbox.getChildren().set(1,box);
            vbox.setSpacing(10);
            xlabel.setText("Home >> Profile");
        }catch (NullPointerException npe){rb1.selectedProperty().set(false);}
    }

    public void CreateGradesZone(String name)
    {
        gradesZone = new ScrollPane();
        VBox GradesZone = new VBox();
        ResultSet rs = null;
        try{
        rs = query("SELECT Code , Courses.Course_ID "  +
                "FROM Students,Interim_Grades,Assignments,Courses " +
                "WHERE Students.Epoka_ID = Interim_Grades.Epoka_ID " +
                "AND Interim_Grades.Assignment_ID = Assignments.Assignment_ID " +
                "AND Assignments.Course_ID = Courses.Course_ID "+
                "AND Students.name = '" + name + "';");

        while(rs.next())
            {
                boz = new VBox();
                StackPane hoz = new StackPane();
                Rectangle r = new Rectangle(1000,50,Color.web("#584573"));
                Label laabel = new Label( rs.getString("Code"));
                laabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                laabel.setFont(new Font("Helvetica",20));
                hoz.getChildren().addAll(r,laabel);
                TableView table = new TableView();
                boz.getChildren().addAll(hoz,table);
                loadDataFromDatabase(" SELECT Interim_Grades.grade , Assignments.weight , Assignments.Type , Assignments.Class_Average\n" +
                        "FROM Students,Interim_Grades,Assignments,Courses\n" +
                        "WHERE Students.Epoka_ID = Interim_Grades.Epoka_ID\n" +
                        "AND Interim_Grades.Assignment_ID = Assignments.Assignment_ID\n" +
                        "AND Assignments.Course_ID = Courses.Course_ID\n" +
                        "AND Students.name = '" + name + "'\n" +
                        "AND Courses.Course_ID = '" + rs.getString("Course_ID") + "';",table);
                GradesZone.getChildren().add(boz);
            }

            gradesZone.setContent(GradesZone);
            vbox.getChildren().set(1,gradesZone);
            vbox.setSpacing(100);

        }catch (Exception ee){ee.printStackTrace();}


    }

    public void CreateAttendanceZone(String name)
    {
        gradesZone = new ScrollPane();
        VBox GradesZone = new VBox();
        ResultSet rs = null;
        try{
            rs = query("SELECT Code , Courses.Course_ID "  +
                    "FROM Students,Interim_Grades,Assignments,Courses " +
                    "WHERE Students.Epoka_ID = Interim_Grades.Epoka_ID " +
                    "AND Interim_Grades.Assignment_ID = Assignments.Assignment_ID " +
                    "AND Assignments.Course_ID = Courses.Course_ID "+
                    "AND Students.name = '" + name + "';");

            while(rs.next())
            {
                boz = new VBox();
                StackPane hoz = new StackPane();
                Rectangle r = new Rectangle(1000,50,Color.web("#584573"));
                Label laabel = new Label( rs.getString("Code"));
                laabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                laabel.setFont(new Font("Helvetica",20));
                hoz.getChildren().addAll(r,laabel);
                TableView table = new TableView();
                boz.getChildren().addAll(hoz,table);
                loadDataFromDatabase("SELECT Attendence.* " +
                        "FROM Students,Attendence " +
                        "WHERE Students.name = '" + name + "'\n" +
                        "AND Attendence.Course_ID = '" + rs.getString("Course_ID") + "';",table);
                GradesZone.getChildren().add(boz);
            }

            gradesZone.setContent(GradesZone);
            vbox.getChildren().set(1,gradesZone);
            vbox.setSpacing(100);

        }catch (Exception ee){ee.printStackTrace();}

    }
    public void loadDataFromDatabase(String query,TableView tableView) {
        if(query.equals(""))
            return;
        ObservableList<ObservableList> data = FXCollections.observableArrayList();

        tableView = new TableView();
        tableView.setMaxWidth(1500);
        if(rb2.selectedProperty().get() || rb3.selectedProperty().get())
        {
            boz.getChildren().set(1,tableView);
        }
        else
        Commands.getChildren().set(3,tableView);
        ResultSet rs = null;
        try {

            try {
                rs = query(query);
            }catch (Exception ee) {
                return;
            }


            // Iterate over the ResultSet
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });

                tableView.getColumns().addAll(col);
            }

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }

            tableView.setItems(data);
            tableView.refresh();

        }catch (NullPointerException npe)
        {

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void LoadCoursesToProfile(String query, TableView tableView)
    {
        if(query.equals(""))
            return;
        ObservableList<ObservableList> data = FXCollections.observableArrayList();

        tableView = new TableView();
        tableView.setMinWidth(300);
        tableView.setMaxWidth(300);
        tableView.setStyle("-fx-font: 14px \"Segoe UI\"; -fx-text-fill: black;");
        Boux.getChildren().set(1,tableView);
        ResultSet rs = null;
        try {

            try {
                rs = query(query);
            }catch (Exception ee) {
                return;
            }


            // Iterate over the ResultSet
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellFactory(column -> {
                    return new TableCell() {
                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty ? "" : getItem().toString());
                            setGraphic(null);
                            TableRow currentRow = getTableRow();
                            if (!isEmpty()) {
                                if (getIndex()%2==0)
                                    currentRow.setStyle("-fx-background-color: lightblue;");
                                else
                                    currentRow.setStyle("-fx-background-color:  #90EE90;");
                            }
                        }
                    };
                });
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });

                tableView.getColumns().addAll(col);
            }

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    String s = rs.getString(i);
                    row.add(s);
                }
                data.add(row);
            }

            tableView.setItems(data);
            tableView.refresh();

        }catch (NullPointerException npe)
        {

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public ResultSet query(String query) throws Exception
    {
        ResultSet rs = null;
        try {

            // Execute SQL query
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            if(rs.isClosed())
            {
                rb1.selectedProperty().set(false);
                rb2.selectedProperty().set(false);
                rb3.selectedProperty().set(false);
                rb4.selectedProperty().set(false);
                rb5.selectedProperty().set(false);
               throw new Exception();
            }
        }
        catch (SQLException sqle)
        {}

        return rs;
    }



    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("My Website Replica");

        CreateCommandsZone();
        // Top blue bar
        StackPane sp = new StackPane();
        Image x = new Image("file:" + "C:\\Users\\Perdorues\\Desktop\\lol\\Screenshot 2024-01-10 193319.png");
        ImageView y = new ImageView(x);
        HBox box = new HBox();
        box.setPadding(new Insets(10,10,10,10));
        Rectangle blueBar = new Rectangle(2000, 50, Color.web("#00458C"));
        Label label = new Label("Epoka Interactive System");
        box.getChildren().addAll(y,label);
        box.setSpacing(20);
        label.setFont(new Font("Helvetica",20));
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        sp.getChildren().addAll(blueBar,box);



        // Left part with options
        VBox leftOptions = new VBox();
        leftOptions.setStyle("-fx-background-color: #F5F5F5");
        ImageView zero = new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\home.png"));
        ImageView one = new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\grades.png"));
        ImageView two = new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\profile.jpg"));
        ImageView three = new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\attendences.png"));
        ImageView four= new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\courses.png"));
        ImageView five= new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\Lecturer.png"));
        ImageView six= new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\SQLICON.png"));
        zero.setFitHeight(45);
        zero.setFitWidth(45);
        one.setFitHeight(45);
        one.setFitWidth(45);
        two.setFitWidth(45);
        two.setFitHeight(45);
        three.setFitWidth(45);
        three.setFitHeight(45);
        four.setFitWidth(45);
        four.setFitHeight(45);
        five.setFitWidth(45);
        five.setFitHeight(45);
        six.setFitWidth(45);
        six.setFitHeight(45);
        Label z = new Label("\nHome");
        Label a = new Label("\nGrades");
        Label b = new Label(("\nProfile"));
        Label c = new Label("\nAttendence");
        Label d = new Label("\nCourses");
        Label e_ = new Label("\nLecturers");
        Label f = new Label("\nCommands");
        HBox box0 = new HBox(zero,z);
        HBox box1 = new HBox(one,a);
        HBox box2 = new HBox(two,b);
        HBox box3 = new HBox(three,c);
        HBox box4 = new HBox(four,d);
        HBox box5 = new HBox(five,e_);
        HBox box6 = new HBox(six,f);
        box0.setSpacing(17);
        box1.setSpacing(17);
        box2.setSpacing(17);
        box3.setSpacing(17);
        box4.setSpacing(17);
        box5.setSpacing(17);
        box6.setSpacing(17);
        RadioButton rb0 = new RadioButton();
        rb1 = new RadioButton();
        rb2 = new RadioButton();
        rb3 = new RadioButton();
        rb4 = new RadioButton();
        rb5 = new RadioButton();
        rb6 = new RadioButton();
        ToggleGroup tg = new ToggleGroup();
        rb0.setToggleGroup(tg);
        rb1.setToggleGroup(tg);
        rb2.setToggleGroup(tg);
        rb3.setToggleGroup(tg);
        rb4.setToggleGroup(tg);
        rb5.setToggleGroup(tg);
        rb6.setToggleGroup(tg);
        Button option0 = new Button("",box0);
        option0.setOnAction(e->{
            vbox.getChildren().set(1,middleButtons);
            vbox.setSpacing(100);
            xlabel.setText("Home");
            rb0.selectedProperty().set(true);
        });
        Button option1 = new Button("",box2);
        rb1.selectedProperty().addListener((observable,oldValue,newValue)->{
            if(newValue)
            {
                option1.setStyle("-fx-background-color: blue;");
                b.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Write te name of the student (later,\nit will be Epoka_ID)");
                TextField textField = new TextField();
                alert.getDialogPane().setContent(textField);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        CreateProfileZone(textField.getText());
                    }
                    else
                    {
                        rb1.selectedProperty().set(false);
                    }
                });
               }
            else
            {
                option1.setStyle("");
                b.setStyle("");;
            }
        });
        option1.setOnAction(e->rb1.selectedProperty().set(true));
        Button option2 = new Button("",box1);
        rb2.selectedProperty().addListener((observable,oldValue,newValue) -> {
            if(newValue){
                xlabel.setText("Home >> Grades");
                option2.setStyle("-fx-background-color: blue;");
                a.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Write te name of the student (later,\nit will be Epoka_ID)");
                TextField textField = new TextField();
                alert.getDialogPane().setContent(textField);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        CreateGradesZone(textField.getText());
                    }
                    else
                    {
                        rb2.selectedProperty().set(false);
                    }
                });
            }
            else
            {
                option2.setStyle("");
                a.setStyle("");
            }
        });
        option2.setOnAction(e->rb2.selectedProperty().set(true));
        Button option3 = new Button("",box3);
        rb3.selectedProperty().addListener((observable,oldvalue,newvalue)->{
            if(newvalue){
                xlabel.setText("Home >> Attendance");
                option3.setStyle("-fx-background-color: blue;");
                c.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Write te name of the student (later,\nit will be Epoka_ID)");
                TextField textField = new TextField();
                alert.getDialogPane().setContent(textField);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        CreateAttendanceZone(textField.getText());
                    }
                    else
                    {
                        rb3.selectedProperty().set(false);
                    }
                });

            }
            else
            {
                option3.setStyle("");
                c.setStyle("");
            }
        });
        option3.setOnAction(e->rb3.selectedProperty().set(true));
        Button option4 = new Button("",box4);
        rb4.selectedProperty().addListener((observable,oldvalue,newvalue)->{
            if(newvalue){
                xlabel.setText("Home >> Courses");
                option4.setStyle("-fx-background-color: blue;");
                d.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");}
            else
            {
                option4.setStyle("");
                d.setStyle("");
            }
        });
        option4.setOnAction(e->rb4.selectedProperty().set(true));
        Button option5 = new Button("",box5);
        rb5.selectedProperty().addListener((observable,oldvalue,newvalue)->{
            if(newvalue){
                xlabel.setText("Home >> Lecturers");
                option5.setStyle("-fx-background-color: blue;");
                e_.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");}
            else
            {
                option5.setStyle("");
                e_.setStyle("");
            }
        });
        option5.setOnAction(e->rb5.selectedProperty().set(true));
        Button option6 = new Button("",box6);
        rb6.selectedProperty().addListener((observable,oldvalue,newvalue)->{
            if(newvalue){
                xlabel.setText("SQLite Commands");
                vbox.getChildren().set(1,Commands);
                option6.setStyle("-fx-background-color: blue;");
                f.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");}
            else
            {
                option6.setStyle("");
                f.setStyle("");
            }
        });
        option6.setOnAction(e->rb6.selectedProperty().set(true));
        option0.setPrefSize(300,45);
        option1.setPrefSize(300,45);
        option2.setPrefSize(300,45);
        option3.setPrefSize(300,45);
        option4.setPrefSize(300,45);
        option5.setPrefSize(300,45);
        option6.setPrefSize(300,45);
        leftOptions.setSpacing(30);
        leftOptions.setPadding(new Insets(50,3,20,0));
        leftOptions.getChildren().addAll(option0, option1, option2,option3,option4,option5,option6);

        // Big colorful buttons in the middle
        int size = 90;
        ImageView one1 = new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\grades.png"));
        ImageView two1 = new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\profile.jpg"));
        ImageView three1 = new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\attendences.png"));
        ImageView four1 = new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\courses.png"));
        ImageView five1 = new ImageView(new Image("file:C:\\Users\\Perdorues\\Downloads\\Lecturer.png"));
        one1.setFitHeight(size);
        one1.setFitWidth(size);
        two1.setFitWidth(size);
        two1.setFitHeight(size);
        three1.setFitWidth(size);
        three1.setFitHeight(size);
        four1.setFitWidth(size);
        four1.setFitHeight(size);
        five1.setFitWidth(size);
        five1.setFitHeight(size);
        Label a1 = new Label("\n\n                              Grades");
        Label b1 = new Label(("\n\n                              Profile"));
        Label c1 = new Label("\n\n                            Attendance");//28 for this one only cuz it long
        Label d1 = new Label("\n\n                              Courses");
        Label e1_ = new Label("\n\n                              Lecturers");// 30 spaces
        a1.setFont(new Font("Helvetica",20));
        a1.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        b1.setFont(new Font("Helvetica",20));
        b1.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        c1.setFont(new Font("Helvetica",20));
        c1.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        d1.setFont(new Font("Helvetica",20));
        d1.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        e1_.setFont(new Font("Helvetica",20));
        e1_.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        HBox box11 = new HBox(one1,a1);
        HBox box21 = new HBox(two1,b1);
        HBox box31 = new HBox(three1,c1);
        HBox box41 = new HBox(four1,d1);
        HBox box51 = new HBox(five1,e1_);
        vbox = new VBox();
        vbox.setSpacing(100);
        vbox.setPadding(new Insets(50,50,50,50));
        StackPane grayarea = new StackPane();
        Rectangle bar = new Rectangle(1500,30,Color.web("#F5F5F5"));
        xlabel = new Label("Home");
        xlabel.setStyle(" -fx-font-weight: bold;");
        grayarea.getChildren().addAll(bar,xlabel);
        middleButtons = new GridPane();
        Button button1 = new Button(" ",box21);
        Button button2 = new Button(" ",box11);
        Button button3 = new Button(" ",box31);
        Button button4 = new Button(" ",box41);
        Button button5 = new Button(" ",box51);
        button1.setPrefSize(400,160);
        button2.setPrefSize(400,160);
        button3.setPrefSize(400,160);
        button4.setPrefSize(400,160);
        button5.setPrefSize(400,160);
        button1.setStyle("-fx-background-color:  #00BFFF;"); // DeepSkyBlue color
        button2.setStyle("-fx-background-color: #7B8C3B"); // Green color
        button3.setStyle("-fx-background-color: #FF6347;"); // Tomato color
        button4.setStyle("-fx-background-color: #FFD700;"); // Gold color
        button5.setStyle("-fx-background-color: #673AB7;"); // Violet color
        button1.setOnAction(e->rb1.selectedProperty().set(true));
        button2.setOnAction(e->rb2.selectedProperty().set(true));
        button3.setOnAction(e->rb3.selectedProperty().set(true));
        button4.setOnAction(e->rb4.selectedProperty().set(true));
        button5.setOnAction(e->rb5.selectedProperty().set(true));
        middleButtons.add(button1,0,0);
        middleButtons.add(button2,1,0);
        middleButtons.add(button3,2,0);
        middleButtons.add(button4,0,1);
        middleButtons.add(button5,1,1);
        middleButtons.setHgap(50);
        middleButtons.setVgap(30);
        vbox.getChildren().addAll(grayarea,middleButtons);


        // Layout
        borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: #FFFFFF");
        borderPane.setTop(sp);
        borderPane.setLeft(leftOptions);
        borderPane.setCenter(vbox);

        Scene scene = new Scene(borderPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        String jdbcURl = "jdbc:sqlite:EIS.db";
        try{
            connection = DriverManager.getConnection(jdbcURl);
        }catch (SQLException sqle) {
            System.out.println("error");
            sqle.printStackTrace();
        }
        launch();
    }
}