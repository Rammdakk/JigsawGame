package com.hse.homework5;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.System.exit;


public class MainApplication extends Application {
    private static PrintStream printStream;
    private static InputStream inputStream;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    long time;
    int gameCounter = 0;
    Point centerPoint;
    Figure currentFigure;
    GridPane sourceGrid;
    GridPane targetField;
    private Stage mainStage;
    int maxTime = 1000;
    Timeline timeline;
    PauseTransition delay;

    public static void main(String[] args) throws IOException {
        String serverHostName = "localhost";
        int serverPortNumber = 5001;
        Socket clientSocket = null;
        InetAddress serverHost = InetAddress.getByName(serverHostName);
        try {
            clientSocket = new Socket(serverHost, serverPortNumber);
            printStream = new PrintStream(clientSocket.getOutputStream());
            inputStream = clientSocket.getInputStream();
        } catch (ConnectException ex) {
            System.out.println("Server is not available");
            exit(0);
        }
        launch();
        System.out.println("Closing socket");
        inputStream.close();
        printStream.close();
        clientSocket.close();
    }


    @Override

    public void start(Stage stage) {
        GameControl.update();
        time = System.currentTimeMillis() / 1000;
        centerPoint = new Point(2, 2);
        currentFigure = Figure.getRandomFigure();
        SplitPane splitPane = new SplitPane();
        Scene scene = new Scene(splitPane, 300, 300);
        sourceGrid = createGrid(3, "000000", 0.3);
        targetField = createGrid(9, "0000FF", 0.2);
        sourceGrid.setOnMousePressed(event -> {
            sourceGrid.setMouseTransparent(true);
            event.setDragDetect(true);
        });
        sourceGrid.setOnMouseReleased(event -> sourceGrid.setMouseTransparent(false));
        sourceGrid.setOnMouseDragged(event -> event.setDragDetect(false));
        sourceGrid.setOnDragDetected(event -> sourceGrid.startFullDrag());
        targetField.setOnMouseDragEntered(event -> {
        });
        // Обработка перемещения мышки над targetField.
        targetField.setOnMouseDragOver(this::drawingContour);
        // Действие после того, как мышка отпущена.
        targetField.setOnMouseDragReleased(this::addFigureToTheField);
        targetField.setOnMouseDragExited(event -> {
        });
        mainStage = stage;
        mainStage.setOnCloseRequest(windowEvent -> finishedGameByUser());
        getUserName(splitPane, scene);
    }

    /**
     * Метод, заканчивающий приложение при закрытии пользователем
     */
    private void finishedGameByUser() {
        timeline.stop();
        printStream.println(-1);
        printStream.println(gameCounter);
        printStream.println((System.currentTimeMillis() / 1000 - time));
        printStream.close();
    }

    /**
     * Метод, заканчивающий приложение
     */
    private void finishedGame() throws IOException {
        timeline.stop();
        printStream.println(-2);
        printStream.println(gameCounter);
        printStream.println((System.currentTimeMillis() / 1000 - time));
        parseCommand(sourceGrid, -2);
    }

    /**
     * Метод, получающий команду.
     */
    private void parseCommand(GridPane gridPane, int val) {
        Scanner in = new Scanner(inputStream);
        int commandNumber = 1;
        try {
            printStream.println(val);
            commandNumber = in.nextInt();
        } catch (NoSuchElementException ex) {
            timeline.stop();
            createWarning(mainStage, "Сервер не отвечает или \nвторой игрок отказался играть. Игра закончена", true);
        }
        if (commandNumber > 0 && commandNumber < 32) {
            fillFigure(gridPane, commandNumber);
        } else if (commandNumber == -1) {
            timeline.stop();
            System.out.println("Finishing by user");
            in.nextLine();
            String res = "You win! Enemy left game" + "\n" + "Total points: " + gameCounter + "\nSpendTime: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(System.currentTimeMillis() / 1000 - time), ZoneId.systemDefault()).minusHours(3).format(formatter) +
                    "\nEnemy points: " + in.nextLine() + "\nEnemy time: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(in.nextLong()), ZoneId.systemDefault()).minusHours(3).format(formatter);
            createWarning(mainStage, res, true);
        } else if (commandNumber == -2) {
            System.out.println("Finishing game");
            in.nextLine();
            long playerTime = System.currentTimeMillis() / 1000 - time;
            int enemyScore = in.nextInt();
            long enemyTime = in.nextLong();
            String res = createInfoMessage(playerTime, enemyScore, enemyTime);
            finishGameWindow(mainStage, res);
        } else if (commandNumber == 130) {
            System.out.println("Response from server received\n");
        }
    }

    /**
     * Метод, печатающий информацию о результатах игры
     *
     * @param playerTime время, затраченное игроком
     * @param enemyScore очки, набранные противником
     * @param enemyTime  время, затраченное игроком
     * @return строку с результатом
     */
    private String createInfoMessage(long playerTime, int enemyScore, long enemyTime) {
        String res;
        if (enemyScore == -1 || enemyTime == -1) {
            res = "You've played without enemies !" + "\n" + "Total points: " + gameCounter + "\nSpendTime: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(playerTime), ZoneId.systemDefault()).minusHours(3).format(formatter);
        } else {
            if (enemyScore < gameCounter) {
                res = "You win!" + "\n" + "Total points: " + gameCounter + "\nSpendTime: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(playerTime), ZoneId.systemDefault()).minusHours(3).format(formatter) + "\nEnemy points: " +
                        enemyScore + "\nEnemy time: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(enemyTime), ZoneId.systemDefault()).minusHours(3).format(formatter);
            } else if (enemyScore == gameCounter) {
                if (enemyTime > playerTime) {
                    res = "You win!" + "\n" + "Total points: " + gameCounter + "\nSpendTime: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(playerTime), ZoneId.systemDefault()).minusHours(3).format(formatter) + "\nEnemy points: " +
                            enemyScore + "\nEnemy time: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(enemyTime), ZoneId.systemDefault()).minusHours(3).format(formatter);
                } else if (enemyScore == playerTime) {
                    res = "Draw!" + "\n" + "Total points: " + gameCounter + "\nSpendTime: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(playerTime), ZoneId.systemDefault()).minusHours(3).format(formatter) + "\nEnemy points: " +
                            enemyScore + "\nEnemy time: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(enemyTime), ZoneId.systemDefault()).minusHours(3).format(formatter);
                } else {
                    res = "You lose((" + "\n" + "Total points: " + gameCounter + "\nSpendTime: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(playerTime), ZoneId.systemDefault()).minusHours(3).format(formatter) + "\nEnemy points: " +
                            enemyScore + "\nEnemy time: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(enemyTime), ZoneId.systemDefault()).minusHours(3).format(formatter);
                }
            } else {
                res = "You lose((" + "\n" + "Total points: " + gameCounter + "\nSpendTime: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(playerTime), ZoneId.systemDefault()).minusHours(3).format(formatter) + "\nEnemy points: " +
                        enemyScore + "\nEnemy time: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(enemyTime), ZoneId.systemDefault()).minusHours(3).format(formatter);
            }
        }
        return res;
    }

    /**
     * Метод, создающий окно с при окончании игры.
     *
     * @param curStage       объект, являющий родителем для нового окна
     * @param warningMessage Строка с сообщением предупреждения
     */
    private void finishGameWindow(Stage curStage, String warningMessage) {
        delay.stop();
        final Stage dialog = new Stage();
        dialog.setTitle("Game Info");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(curStage);
        VBox dialogVbox = new VBox(20);
        Button OKButton = new Button();
        OKButton.setText(" OK ");
        OKButton.setMinSize(200, 20);
        OKButton.setOnMouseClicked(mouseEvent13 -> finishingGame(dialog));
        dialog.setOnCloseRequest(windowEvent -> finishingGame(dialog));
        Button newGameButton = new Button();
        newGameButton.setText(" Новая игра ");
        newGameButton.setMinSize(200, 20);
        newGameButton.setOnMouseClicked(mouseEvent13 -> launchNewGame(dialog));
        dialogVbox.setAlignment(Pos.CENTER);
        Text warning = new Text(warningMessage);
        warning.setTextAlignment(TextAlignment.CENTER);
        dialogVbox.getChildren().addAll(warning, OKButton, newGameButton);
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    /**
     * Запуск новой игры
     *
     * @param dialog окно с предложением начать новую игру.
     */
    private void launchNewGame(Stage dialog) {
        //Запуск новой игры
        printStream.println(10);
        gameCounter = 0;
        time = System.currentTimeMillis() / 1000;
        centerPoint = new Point(2, 2);
        targetField.getChildren().clear();
        GameControl.update();
        fillGrid(9, "0000FF", targetField);
        dialog.close();
        StartTimer();
        timeline.play();
        parseCommand(sourceGrid, 2);
    }

    /**
     * Завершение игры
     *
     * @param dialog окно с предложением начать новую игру.
     */
    private void finishingGame(Stage dialog) {
        printStream.println(9);
        dialog.close();
        mainStage.close();
    }

    /**
     * Окно для получения имени пользователя.
     */
    private void getUserName(SplitPane splitPane, Scene scene) {
        final Stage userName = new Stage();
        Button OKButton = new Button();
        Text message = new Text("Enter your name. \nThe game will launch after all players enter their names");
        message.setTextAlignment(TextAlignment.CENTER);
        TextField info = new TextField("Your name");
        info.setPrefColumnCount(11);
        userName.setOnCloseRequest(windowEvent -> {
            getUserName(splitPane, scene);
            createWarning(new Stage(), "Name is not closable. \nIf you want close app, \nenter name and close main window", false);
        });
        OKButton.setOnMouseClicked(mouseEvent12 -> {
            String name = info.getText();
            if (Objects.equals(name, "")) {
                createWarning(userName, "Name can't be empty ", false);
            } else {
                printStream.println(name);
                printStream.flush();
                try {
                    displaySettings(splitPane, scene);
                } catch (NoSuchElementException ex) {
                    createWarning(mainStage, "Сервер не отвечает или \nвторой игрок отказался играть. Игра закончена", true);
                    printStream.println(9);
                    userName.close();
                    return;
                }
                userName.close();
                mainStage.show();
                StartTimer();
                for (Node divider : splitPane.lookupAll(".split-pane-divider")) {
                    if (divider != null) {
                        divider.setStyle("-fx-background-color: transparent;");
                    }
                }
                parseCommand(sourceGrid, 2);
            }
        });
        userName.setTitle("Name");
        userName.initModality(Modality.APPLICATION_MODAL);
        userName.initOwner(mainStage);
        VBox dialogVbox = new VBox(20);
        OKButton.setText(" OK ");
        info.setMinSize(200, 20);
        info.setMaxSize(200, 20);
        OKButton.setMinSize(200, 20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.getChildren().addAll(message, info, OKButton);
        Scene dialogScene = new Scene(dialogVbox, 340, 200);
        userName.setScene(dialogScene);
        userName.show();
    }

    /**
     * Запуск отсчета времени игры.
     */
    private void StartTimer() {
        time = System.currentTimeMillis() / 1000;
        delay = new PauseTransition(Duration.seconds(maxTime));
        delay.setOnFinished(event -> {
            try {
                finishedGame();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        delay.play();
    }

    /**
     * Метод, создающий окно с предупреждением.
     *
     * @param curStage       объект, являющий родителем для нового окна
     * @param warningMessage Строка с сообщением предупреждения
     * @param isClosing      true, если надо после закрыть curStage
     */
    private void createWarning(Stage curStage, String warningMessage, boolean isClosing) {
        final Stage dialog = new Stage();
        dialog.setTitle("Game Warning");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(curStage);
        VBox dialogVbox = new VBox(20);
        Button OKButton1 = new Button();
        OKButton1.setText(" OK ");
        OKButton1.setMinSize(200, 20);
        OKButton1.setOnMouseClicked(mouseEvent13 -> {
            dialog.close();
            if (isClosing) {
                mainStage.close();
            }
        });
        dialog.setOnCloseRequest(windowEvent -> {
            dialog.close();
            if (isClosing) {
                mainStage.close();
            }
        });
        dialogVbox.setAlignment(Pos.CENTER);
        Text warning = new Text(warningMessage);
        warning.setTextAlignment(TextAlignment.CENTER);
        dialogVbox.getChildren().addAll(warning, OKButton1);
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    /**
     * Метод, параметры отображения основного окна.
     */
    private void displaySettings(SplitPane splitPane, Scene scene) {
        SplitPane rightSplitPane = addButtonsAndText();
        sourceGrid.setMinSize(250, 250);
        sourceGrid.setMaxSize(250, 250);
        targetField.setMaxSize(570, 570);
        targetField.setMinSize(570, 570);
        splitPane.getItems().addAll(targetField, rightSplitPane);
        splitPane.setDividerPositions();
        mainStage.setScene(scene);
        mainStage.setTitle("Jigsaw");
        mainStage.setMinHeight(600);
        mainStage.setMinWidth(900);
    }

    /**
     * Метод, переносящий фигуру на поле 9*9
     */
    private void addFigureToTheField(MouseDragEvent event) {
        centerPoint = findPos(event.getX(), event.getY(), targetField);
        if ((int) centerPoint.getX() > 1 && (int) centerPoint.getY() > 1) {
            ConfigureCoordinates();
            boolean isAbleToInsert = GameControl.tryToInsert(centerPoint, currentFigure);
            // Вставка в поле.
            if (isAbleToInsert) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (currentFigure.location[i][j] == 1) {
                            int X = (int) (centerPoint.getX() + 2 * i);
                            int Y = (int) (centerPoint.getY() + 2 * j);
                            Pane pn = new Pane();
                            pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "FFFF00"), CornerRadii.EMPTY, Insets.EMPTY)));
                            targetField.add(pn, X, Y);
                        }
                    }
                }
                gameCounter++;
                parseCommand(sourceGrid, 2);
            }
        }
        fillGridBorders(9, "0000FF", targetField);
    }

    /**
     * Метод, отображающий контур.
     */
    private void drawingContour(MouseDragEvent event) {
        System.out.println("Making borders");
        fillSomeGridBorders(targetField);
        centerPoint = findPos(event.getX(), event.getY(), targetField);
        if ((int) centerPoint.getX() > 1 && (int) centerPoint.getY() > 1) {
            ConfigureCoordinates();
            // Вычисление координат относительно текущего положения курсора.
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (currentFigure.location[i][j] == 1) {
                        int X = (int) (centerPoint.getX() + 2 * i);
                        int Y = (int) (centerPoint.getY() + 2 * j);
                        makeBordersOfFigure(X, Y, targetField);
                    }
                }
            }
        }
    }

    /**
     * Метод, отрисовывающий правую часть основного игрового поля
     *
     * @return правую часть поля
     */
    private SplitPane addButtonsAndText() {
        SplitPane splitPane1 = new SplitPane();
        splitPane1.setOrientation(Orientation.VERTICAL);
        Button finishButton = new Button();
        finishButton.setText("  Finish Game  ");
        finishButton.setMinSize(200, 20);
        finishButton.setOnMouseClicked(mouseEvent -> {
            try {
                finishedGame();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Text gameInfoText = new Text("\n\nGame duration");
        gameInfoText.setStyle("-fx-font: 24 arial;");
        gameInfoText.setX(130);
        gameInfoText.setY(50);
        Scanner in = new Scanner(inputStream);
        Text enemyName = new Text(in.nextLine() + "\n" + in.nextLine());
        maxTime = in.nextInt();
        enemyName.setStyle("-fx-font: 16 arial;");
        enemyName.setX(130);
        enemyName.setY(50);
        final Label timerText = new Label();
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    timerText.setText("Max time:  " + LocalDateTime.ofInstant(Instant.ofEpochSecond(maxTime), ZoneId.systemDefault()).minusHours(3).format(formatter) +
                            "\nGame time: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(System.currentTimeMillis() / 1000 - time), ZoneId.systemDefault()).minusHours(3).format(formatter) + "\nTime left: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(-System.currentTimeMillis() / 1000 + maxTime + time), ZoneId.systemDefault()).minusHours(3).format(formatter));
                    parseCommand(sourceGrid, 130);
                }
                ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        splitPane1.getItems().addAll(gameInfoText, enemyName, timerText, sourceGrid, /*newGameButton,*/ finishButton);
        splitPane1.setDividerPositions(0.7f, 0, 8f);
        return splitPane1;
    }

    /**
     * Изменение координат, если они попали на границу.
     */
    private void ConfigureCoordinates() {
        centerPoint.setX(centerPoint.getX() - 2 * currentFigure.centerPoint.getX());
        centerPoint.setY(centerPoint.getY() - 2 * currentFigure.centerPoint.getY());
        if ((int) centerPoint.getX() % 2 == 1) {
            centerPoint.setX(centerPoint.getX() + 1);
        }
        if ((int) centerPoint.getY() % 2 == 1) {
            centerPoint.setY(centerPoint.getY() + 1);
        }
    }

    /**
     * Метод, отвечающий за размещение рамки для понятия, куда будет вставлена фигура.
     *
     * @param X           - x coordinate of cell
     * @param Y           - y coordinate of cell
     * @param targetField - insert Field.
     */
    private void makeBordersOfFigure(int X, int Y, GridPane targetField) {
        if (X > 1 && Y > 1 && X < 19 && Y < 19) {
            Pane pn = new Pane();
            pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "FF0000"), CornerRadii.EMPTY, Insets.EMPTY)));
            targetField.add(pn, X - 1, Y - 1);
            pn = new Pane();
            pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "FF0000"), CornerRadii.EMPTY, Insets.EMPTY)));
            targetField.add(pn, X - 1, Y + 1);
            pn = new Pane();
            pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "FF0000"), CornerRadii.EMPTY, Insets.EMPTY)));
            targetField.add(pn, X - 1, Y);
            pn = new Pane();
            pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "FF0000"), CornerRadii.EMPTY, Insets.EMPTY)));
            targetField.add(pn, X, Y - 1);
            pn = new Pane();
            pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "FF0000"), CornerRadii.EMPTY, Insets.EMPTY)));
            targetField.add(pn, X + 1, Y - 1);
            pn = new Pane();
            pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "FF0000"), CornerRadii.EMPTY, Insets.EMPTY)));
            targetField.add(pn, X, Y + 1);
            pn = new Pane();
            pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "FF0000"), CornerRadii.EMPTY, Insets.EMPTY)));
            targetField.add(pn, X + 1, Y + 1);
            pn = new Pane();
            pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "FF0000"), CornerRadii.EMPTY, Insets.EMPTY)));
            targetField.add(pn, X + 1, Y);
        }
    }

    /**
     * Поиск клетки, по координатам.
     *
     * @param x           координата по оси X
     * @param y           координата по оси Y
     * @param searchField поле, в котором осуществляется поиск
     * @return координаты клетки поля
     */
    private Point findPos(double x, double y, GridPane searchField) {
        for (Node node : searchField.getChildren()) {
            Bounds bounds = node.getBoundsInParent();
            if (bounds.getMinX() < x && bounds.getMaxX() > x && bounds.getMinY() < y && bounds.getMaxY() > y) {
                return new Point(GridPane.getColumnIndex(node), GridPane.getRowIndex(node));
            }
        }
        return new Point();
    }

    /**
     * Метод, создающий поле необходимого размера.
     *
     * @param size      размер поля
     * @param colorCode цвет границ
     * @param lineSize  относительная толщина границ
     * @return созданное поле
     */
    private GridPane createGrid(int size, String colorCode, double lineSize) {
        double cellSize = 100.00 / (size + 1);
        cellSize -= lineSize;
        GridPane dp = new GridPane();
        RowConstraints rw1 = new RowConstraints();
        rw1.setPercentHeight(cellSize / 2 + lineSize / 2);
        dp.getRowConstraints().add(rw1);
        RowConstraints rw = new RowConstraints();
        rw.setPercentHeight(lineSize);
        dp.getRowConstraints().add(rw);
        for (int i = 0; i < size; i++) {
            rw = new RowConstraints();
            rw.setPercentHeight(cellSize);
            dp.getRowConstraints().add(rw);
            rw = new RowConstraints();
            rw.setPercentHeight(lineSize);
            dp.getRowConstraints().add(rw);
        }
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(cellSize / 2 + lineSize / 2);
        dp.getColumnConstraints().add(cc);
        cc = new ColumnConstraints();
        cc.setPercentWidth(lineSize);
        dp.getColumnConstraints().add(cc);
        for (int i = 0; i < size; i++) {
            cc = new ColumnConstraints();
            cc.setPercentWidth(cellSize);
            dp.getColumnConstraints().add(cc);
            cc = new ColumnConstraints();
            cc.setPercentWidth(lineSize);
            dp.getColumnConstraints().add(cc);
        }
        fillGrid(size, colorCode, dp);
        return dp;
    }

    /**
     * Метод, заполняющий поля.
     *
     * @param size      размер поля
     * @param colorCode цвет границ
     * @param dp        поле
     */
    private void fillGrid(int size, String colorCode, GridPane dp) {
        fillGridBorders(size, colorCode, dp);
        if (size > 3) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    Pane pane = new Pane();
                    pane.setBackground(new Background(new BackgroundFill(Color.web("#" + "FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
                    dp.add(pane, 2 * (i + 1), 2 * (j + 1));
                }
            }
        }
    }

    /**
     * Метод, восстанавливающий цвет границ, после его изменения.
     *
     * @param dp поле
     */
    private void fillSomeGridBorders(GridPane dp) {
        int x = Math.max((int) centerPoint.getX() / 2 - 2, 0);
        int y = Math.max((int) centerPoint.getY() / 2 - 2, 2);
        for (int i = 0; i < 4; i++) {
            for (int j = -2; j < 4; j++) {
                Pane pn = new Pane();
                pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "0000FF"), CornerRadii.EMPTY, Insets.EMPTY)));
                dp.add(pn, 2 * (x + i + 1) + 1, 2 * (y + j + 1) + 1);
                {
                    pn = new Pane();
                    pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "0000FF"), CornerRadii.EMPTY, Insets.EMPTY)));
                    dp.add(pn, 2 * (x + i + 1) + 1, 2 * (y + j + 1));
                }
                {
                    pn = new Pane();
                    pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "0000FF"), CornerRadii.EMPTY, Insets.EMPTY)));
                    dp.add(pn, 2 * (x + i + 1), 2 * (y + j + 1) + 1);
                }
            }
        }
        for (int i = 2; i < 19; i++) {
            Pane pn = new Pane();
            pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "0000FF"), CornerRadii.EMPTY, Insets.EMPTY)));
            dp.add(pn, 1, i);
            pn = new Pane();
            pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "0000FF"), CornerRadii.EMPTY, Insets.EMPTY)));
            dp.add(pn, i, 1);
        }
    }

    /**
     * Метод, отвечающий за окраску границ клеток поля (чтоб видно было границы клеток).
     *
     * @param size      размер поля
     * @param colorCode цвет границ
     * @param dp        поле
     */
    private void fillGridBorders(int size, String colorCode, GridPane dp) {
        for (int i = -1; i < size; i++) {
            for (int j = -1; j < size; j++) {
                Pane pn = new Pane();
                pn.setBackground(new Background(new BackgroundFill(Color.web("#" + colorCode), CornerRadii.EMPTY, Insets.EMPTY)));
                dp.add(pn, 2 * (i + 1) + 1, 2 * (j + 1) + 1);
                if (j > -1) {
                    pn = new Pane();
                    pn.setBackground(new Background(new BackgroundFill(Color.web("#" + colorCode), CornerRadii.EMPTY, Insets.EMPTY)));
                    dp.add(pn, 2 * (i + 1) + 1, 2 * (j + 1));
                }
                if (i > -1) {
                    pn = new Pane();
                    pn.setBackground(new Background(new BackgroundFill(Color.web("#" + colorCode), CornerRadii.EMPTY, Insets.EMPTY)));
                    dp.add(pn, 2 * (i + 1), 2 * (j + 1) + 1);
                }
            }
        }
    }

    /**
     * Метод, отвечающий за отображение сгенерированной фигуры.
     *
     * @param gridPane поле для отображения
     */
    private void fillFigure(GridPane gridPane, int figureNumber) {
        currentFigure = Figure.getFigureByNumber(figureNumber);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Pane pn = new Pane();
                if (currentFigure.location[i][j] == 1) {
                    pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "FFFF00"), CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    pn.setBackground(new Background(new BackgroundFill(Color.web("#" + "FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
                }
                gridPane.add(pn, 2 * (i + 1), 2 * (j + 1));
            }
        }
    }
}