package me.sunmisc.btree.gui;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.sunmisc.btree.Tree;
import me.sunmisc.btree.cow.ImTree;
import me.sunmisc.btree.heap.Table;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.StreamSupport;

public class UserStatsApp extends Application {
    private final AtomicReference<Tree> tree = new AtomicReference<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final ObservableList<KeyValuePair> tableData = FXCollections.observableArrayList();
    private final Table table = new Table("pets");

    public UserStatsApp() throws IOException {
        table.nodes().tail().ifPresent(e -> {
            tree.set(new ImTree(table, e));
        });
        /*String[] petNames = {
                "Макс",
                "Луна",
                "Белла",
                "Рекс",
                "Мурка",
                "Симба",
                "Тоша",
                "Зефир"
        };
        for (int i = 1; i < 1_000_000; ++i) {
            String value = petNames[ThreadLocalRandom.current().nextInt(0, petNames.length)];
            Tree x = tree.get();
            if (x == null) {
                tree.set(new ImTree(table, i+"", value));
            } else {
                tree.set(x.put(i+"", value));
            }
            table.nodes().tail().ifPresent(root -> {
                table.versions().alloc(root);
            });
        }*/
    }

    @Override
    public void start(Stage primaryStage) {
        // UI Components
        TextField keyField = new TextField();
        keyField.setPromptText("Введите айди (например, pet_id)");
        TextField valueField = new TextField();
        valueField.setPromptText("Введите значение (например, имя)");

        Button addButton = new Button("Добавить");
        Button getButton = new Button("Получить по ключу");
        Button historyButton = new Button("Просмотреть историю");

        Label resultLabel = new Label();

        // Table setup
        TableView<KeyValuePair> tableView = new TableView<>();
        TableColumn<KeyValuePair, String> keyColumn = new TableColumn<>("Key");
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        TableColumn<KeyValuePair, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        tableView.getColumns().addAll(keyColumn, valueColumn);
        tableView.setItems(tableData);
        updateTable();

        // Button actions
        addButton.setOnAction(e -> {
            String key = keyField.getText().trim();
            String value = valueField.getText().trim();
            if (!key.isEmpty() && !value.isEmpty()) {
                long startTime = System.nanoTime();
                put(key, value);
                long endTime = System.nanoTime();
                double durationMs = (endTime - startTime) / 1_000_000.0;
                updateTable();
                resultLabel.setText(String.format("Добавлено за: %s = %s (%.3f миллисекунд)", key, value, durationMs));
                keyField.clear();
                valueField.clear();
            } else {
                resultLabel.setText("Ключ не найден");
            }
        });

        getButton.setOnAction(e -> {
            String key = keyField.getText().trim();
            if (!key.isEmpty()) {
                long startTime = System.nanoTime();
                String text = tree.get().get(key)
                        .map(value -> "Значение: " + value)
                        .orElse("Ключ не найден");
                long endTime = System.nanoTime();
                double durationMs = (endTime - startTime) / 1_000_000.0;
                resultLabel.setText(String.format("%s (%.3f ms)", text, durationMs));
            } else {
                resultLabel.setText("Введите ключ");
            }
        });

        historyButton.setOnAction(e -> showHistoryViewer());

        // Layout
        VBox layout = new VBox(10, keyField, valueField, addButton, getButton, historyButton, resultLabel, tableView);
        layout.setPadding(new Insets(10));
        Scene scene = new Scene(layout, 400, 600);

        // Stage setup
        primaryStage.setTitle("Таблица животных");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateTable() {
        Tree r = tree.get();
        if (r == null) {
            return;
        }
        tableData.clear();
        StreamSupport.stream(r.spliterator(), false).limit(1_000)
                .forEach(e -> tableData.add(new KeyValuePair(e.getKey(), e.getValue())));
    }

    public void put(String key, String value) {
        lock.lock();
        try {
            Tree x = tree.get();
            if (x == null) {
                tree.set(new ImTree(table, key, value));
            } else {
                tree.set(x.put(key, value));
            }
            table.nodes().tail().ifPresent(root -> {
                table.versions().alloc(root);
            });
            System.out.println("Set \"" + key + "\" = \"" + value + "\"");
        } finally {
            lock.unlock();
        }
    }

    private void showHistoryViewer() {
        Stage historyStage = new Stage();
        historyStage.setTitle("История версий");

        // Version list
        ListView<Integer> versionList = new ListView<>();
        ObservableList<Integer> versionIds = FXCollections.observableArrayList();
        for (int i = 0; i < table.versions().count(); i++) {
            versionIds.add(i);
        }
        versionList.setItems(versionIds);

        // Version data table
        TableView<KeyValuePair> versionTable = new TableView<>();
        TableColumn<KeyValuePair, String> keyCol = new TableColumn<>("Date");
        keyCol.setCellValueFactory(new PropertyValueFactory<>("key"));
        TableColumn<KeyValuePair, String> valueCol = new TableColumn<>("List");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        versionTable.getColumns().addAll(keyCol, valueCol);
        ObservableList<KeyValuePair> versionData = FXCollections.observableArrayList();
        versionTable.setItems(versionData);

        // Update table when a version is selected
        versionList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                versionData.clear();
                table.versions().versions().forEach((e) -> {
                    Instant instant = Instant.ofEpochMilli(e.getValue());
                    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                    String formattedDate = dateTime.format(formatter);
                    versionData.add(new KeyValuePair(
                            formattedDate,
                            new ImTree(table, e.getKey()).toString())
                    );
                });
            }
        });

        // Layout
        VBox historyLayout = new VBox(10, new Label("Выбрать версию"), versionList, versionTable);
        historyLayout.setPadding(new Insets(10));
        Scene historyScene = new Scene(historyLayout, 400, 400);
        historyStage.setScene(historyScene);
        historyStage.show();
    }

    public static class KeyValuePair {
        private final String key;
        private final String value;

        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}