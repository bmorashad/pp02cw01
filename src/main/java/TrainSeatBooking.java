import com.mongodb.client.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TrainSeatBooking extends Application {
    final int NUM_SEATS = 42;
    final List<Button> SEATS = makeSeats();

    List<Button> reserved = new ArrayList<>();
    List<String> names = new ArrayList<>();

    MongoDatabase database;
    MongoCollection<Document> collection;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        connectToMongoCollection("mongodb://localhost:27017", "trainBooking", "trainSeatReservation");
        displayInstructions();
        menu();
    }

    // Common tasks
    public void displayInstructions() {
        String instructions = "Enter 'Q': exit the program\nEnter 'A': add customer to seat\n" +
                "Enter 'V': add customer to view all the seats \nEnter 'E': view empty(available) seats\nEnter 'D': delete a customer from seat\n" +
                "Enter 'F': find the seat of a given customer\nEnter 'S': write data to a database collection(overwite if already exist)" +
                "Enter 'L': load from a database collection\nEnter 'O': display booked seats in alphabeticle order";
        System.out.println("Follow these instructions to navitage the menu properly");
        System.out.println(instructions);
    }
    public void connectToMongoCollection(String url, String databaseName, String collectionName) {
        database = MongoClients.create(url).getDatabase(databaseName);
        collection = database.getCollection(collectionName);
    }
    public Label makeLabel(String lblTxt) {
        Label lbl = new Label(lblTxt);
        lbl.setTextFill(Color.web("blue"));
        GridPane.setConstraints(lbl, 0, 0);
        GridPane.setColumnSpan(lbl,6);
        return lbl;
    }
    public GridPane makeGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(10);
        grid.setHgap(10);
        return grid;
    }
    public List<Button> makeSeats() {
        List<Button> seats = new ArrayList<>();
        for(int i = 0, c = 0, r = 0; i < NUM_SEATS; i++) {
            Button seat = new Button("" + (i+1));
            seat.setMaxSize(100, 100);
            seat.setMinSize(40, 40);
            seat.setStyle("-fx-font-size: 14px; -fx-background-color: #ddd; -fx-font-family: 'Clear Sans';");
            if(i % 7 == 0) {
                r += 1;
                c = 0;
            }
            GridPane.setConstraints(seat, c, r);
            seats.add(seat);
            c++;
        }
        return seats;
    }
    public void onQuit(Stage stage) {
            stage.close();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    menu();
                }
            });
    }

    // Customer options
    public void menu() {
        Scanner sc = new Scanner(System.in);
        String[] options = {"A", "V", "E", "D", "F", "S", "L", "O", "Q"};
        // to check weather the list contains given option
        List<String> optionsArrLst = Arrays.asList(options);

        System.out.print("Enter an option to proceed: ");
        String option = sc.nextLine().toUpperCase();
        if (optionsArrLst.contains(option)) {
            switch (option) {
                case "A":
                    addSeats();
                    break;
                case "E":
                    emptySeats();
                    break;
                case "V":
                    viewSeat();
                    break;
                case "D":
                    deleteSeat();
                    break;
                case "F":
                    findSeat();
                    break;
                case "S":
                    saveToDatabase();
                    break;
                case "L":
                    loadFromDatabase();
                    break;
                default:
                    Platform.exit();
                    break;
            }
        } else {
            System.out.println("Invalid Option('q' to exit)");
            menu();
        }
    }
    public void viewSeat() {
        Stage stage = new Stage();

        GridPane grid = makeGrid();
        Label lbl = makeLabel("View Empty Seats");

        Button quit = new Button("Quit");
        quit.setMinWidth(80);
        quit.setMaxWidth(150);
        GridPane.setConstraints(quit, 5, 8);
        GridPane.setColumnSpan(quit, 2);
        grid.getChildren().add(quit);

        grid.getChildren().add(lbl);
        for(Button seat : SEATS) {
            seat.setOnAction(null);
            if(!reserved.contains(seat)) {
                seat.setStyle("-fx-background-color: #5cff9d");
            } else {
                seat.setStyle("-fx-background-color: #ff9ca7");
            }
            grid.getChildren().add(seat);
        }
        Scene scene = new Scene(grid, 500, 500);
        stage.setScene(scene);
        stage.setTitle("Book A Seat");
        stage.show();
        quit.setOnAction(e -> {
            onQuit(stage);
        });
        stage.setOnCloseRequest(e -> {
            onQuit(stage);
        });
    }
    public void emptySeats() {
        Stage stage = new Stage();
        GridPane grid = makeGrid();
        Button quit = new Button("Quit");
        quit.setMinWidth(80);
        quit.setMaxWidth(150);
        GridPane.setConstraints(quit, 5, 8);
        GridPane.setColumnSpan(quit, 2);
        grid.getChildren().add(quit);

        int i = 0; // to count reserved seats
        for(Button seat : SEATS) {
            if(!reserved.contains(seat)) {
                seat.setOnAction(null);
                seat.setStyle("");
                grid.getChildren().add(seat);
                i++;
            }
        }
        Label lbl = makeLabel("");
        // different label when all seats are booked
        if(i > 0) {
            lbl.setText("View Empty Seats");
            lbl.setStyle("-fx-font-size: 18px");
        } else {
            lbl.setText("Sorry, All Seats Are Booked");
            lbl.setStyle("-fx-font-size: 26px");
        }
        grid.getChildren().add(lbl);

        Scene scene = new Scene(grid, 500, 500);
        stage.setScene(scene);
        stage.setTitle("Book A Seat");
        stage.show();
        quit.setOnAction(e -> {
            onQuit(stage);
        });
        stage.setOnCloseRequest(event -> {
            onQuit(stage);
        });
    }
    public void addSeats() {
        Stage stage = new Stage();

        Label lbl = makeLabel("Book A Seat");
        GridPane grid = makeGrid();
        grid.getChildren().add(lbl);

        TextField name = new TextField();
        GridPane.setConstraints(name, 0, 8);
        GridPane.setColumnSpan(name, 5);
        grid.getChildren().add(name);

        Button add = new Button("Add");
        GridPane.setConstraints(add, 5, 8);
        grid.getChildren().add(add);

        Button quit = new Button("Quit");
        GridPane.setConstraints(quit, 6, 8);
        grid.getChildren().add(quit);

        final Button[] toBeReserved = {null};
        for(Button seat : SEATS) {
            if(reserved.contains(seat)) {
                seat.setOnAction(null);
                seat.setStyle("-fx-background-color: #ff9ca7");
            } else {
                seat.setStyle("");
                seat.setOnAction(e ->  {
                    if(toBeReserved[0] != null){
                        toBeReserved[0].setStyle("");}
                    if( toBeReserved[0] != seat) {
                        seat.setStyle("-fx-background-color: #5cff9d");
                        toBeReserved[0] = seat;
                    }
                    else { seat.setStyle("");
                        toBeReserved[0] = null;};
                });
            }
            grid.getChildren().add(seat);
            // make seats reservable

        }
        Scene scene = new Scene(grid, 500, 500);
        stage.setScene(scene);
        stage.setTitle("Book A Seat");
        stage.show();
        add.setOnAction(e -> {
            if(toBeReserved[0] != null){
                if(!(name.getText().trim().isEmpty()) && name.getText().length() != 1) { // to stop giving q as the name
                    name.setStyle("-fx-border-color: silver");
                    reserved.add(toBeReserved[0]);
                    names.add(name.getText());
                    toBeReserved[0] = null;
                    stage.close();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            menu();
                        }
                    });
                }
            }
        });
        quit.setOnAction(e -> {
            onQuit(stage);
        });
        stage.setOnCloseRequest(e -> {
            onQuit(stage);
        });
    }
    public void deleteSeat() {
        Scanner sc = new Scanner(System.in);
            System.out.print("Enter your name: ");
            String getName = sc.nextLine();
            if(!getName.toLowerCase().equals("q")) {
                if (names.indexOf(getName) != -1) {
                    reserved.remove(names.indexOf(getName));
                    names.remove(getName);
                    menu();
                } else {
                    System.out.println("No Seat Booked For The Provided Name!");
                    deleteSeat();
                }
            } else {
                menu();
            }
    }
    public void findSeat() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your name: ");
        String getName = sc.nextLine();
        if(!getName.toLowerCase().equals("q")) {
            if (names.indexOf(getName) != -1) {
                String seat = reserved.get(names.indexOf(getName)).getText();
                System.out.println("Your Seat Is: " + seat);
                menu();
            } else {
                System.out.println("No Seat Booked On Given Info");
                findSeat();
            }
        }
        menu();
    }
    public void saveToDatabase() {
        collection.drop();
        for (Button seat : reserved) {
            String name = names.get(reserved.indexOf(seat));
            String seatNum = seat.getText();
            Document doc = new Document("name", name).append("seat", seatNum);
            collection.insertOne(doc);
        }
        menu();
    }
    public void loadFromDatabase() {
        MongoCursor<Document> cursor = collection.find().iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            Button seat = SEATS.get(Integer.parseInt((String) doc.get("seat")) - 1); // doc value is obj
            String name = (String) doc.get("name");
            if(!reserved.contains(seat)) {
                reserved.add(seat);
                names.add(name);
            }
        }
        menu();
    }
}
