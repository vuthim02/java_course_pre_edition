# Domain Specialization — Lesson 2: JavaFX Desktop Applications

> **INTRODUCTORY OVERVIEW** — This section provides a high-level introduction to the domain. Each topic warrants its own dedicated course for professional mastery.

## Why JavaFX?

JavaFX is the modern replacement for Swing — building rich desktop applications with hardware-accelerated graphics, CSS styling, and FXML for UI layout.

```
┌─────────────────────────────────────────────────────────────┐
│                   JAVAFX APPLICATION                          │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                     Stage (Window)                       │ │
│  │  ┌─────────────────────────────────────────────────────┐│ │
│  │  │                   Scene                              ││ │
│  │  │  ┌───────────────────────────────────────────────┐  ││ │
│  │  │  │              Scene Graph                       │  ││ │
│  │  │  │  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐     │  ││ │
│  │  │  │  │Button│  │Label │  │TextField│ │ListView│  │  ││ │
│  │  │  │  └──────┘  └──────┘  └──────┘  └──────┘     │  ││ │
│  │  │  └───────────────────────────────────────────────┘  ││ │
│  │  └─────────────────────────────────────────────────────┘│ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Setup

```xml
<!-- Maven -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.1</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21.0.1</version>
</dependency>
```

## Hello JavaFX

```java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button("Click Me!");
        btn.setOnAction(e -> System.out.println("Hello JavaFX!"));

        StackPane root = new StackPane(btn);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("My First JavaFX App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

## FXML — Declarative UI

```xml
<!-- MainView.fxml -->
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<VBox spacing="10" padding="20" xmlns="http://javafx.com/javafx/21">
    <Label text="User Management" style="-fx-font-size: 24px;" />

    <HBox spacing="10">
        <TextField fx:id="nameField" promptText="Enter name" HBox.hgrow="ALWAYS" />
        <Button text="Add" onAction="#handleAddUser" />
    </HBox>

    <ListView fx:id="userList" VBox.vgrow="ALWAYS" />
</VBox>
```

### Controller

```java
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class MainController implements Initializable {

    @FXML private TextField nameField;
    @FXML private ListView<String> userList;

    private final ObservableList<String> users = FXCollections.observableArrayList();

    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle rb) {
        userList.setItems(users);
    }

    @FXML
    private void handleAddUser() {
        String name = nameField.getText().trim();
        if (!name.isEmpty()) {
            users.add(name);
            nameField.clear();
        }
    }
}
```

## CSS Styling

```css
/* style.css */
.root {
    -fx-font-family: 'Segoe UI', Arial, sans-serif;
    -fx-background-color: #f5f5f5;
}

.button {
    -fx-background-color: #2196F3;
    -fx-text-fill: white;
    -fx-padding: 8 16;
    -fx-background-radius: 4;
}

.button:hover {
    -fx-background-color: #1976D2;
}

.list-view {
    -fx-background-color: white;
    -fx-border-color: #e0e0e0;
    -fx-border-radius: 4;
}

.text-field {
    -fx-background-color: white;
    -fx-border-color: #e0e0e0;
    -fx-border-radius: 4;
    -fx-padding: 8;
}
```

## Charts

```java
@FXML
private void initializeChart() {
    // Create data
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Monthly Sales");
    series.getData().add(new XYChart.Data<>("Jan", 12000));
    series.getData().add(new XYChart.Data<>("Feb", 15000));
    series.getData().add(new XYChart.Data<>("Mar", 18000));
    series.getData().add(new XYChart.Data<>("Apr", 16000));
    series.getData().add(new XYChart.Data<>("May", 22000));

    // Create chart
    BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
    chart.getData().add(series);
    chart.setTitle("Sales Report");
}
```

## Exercises

1. Create a JavaFX application with FXML layout and a controller.
2. Add a ListView with an ObservableList that updates in real-time.
3. Style the UI with an external CSS file.
4. Add a chart to visualize data (bar or pie chart).
5. Build a simple CRUD desktop app for managing tasks.
