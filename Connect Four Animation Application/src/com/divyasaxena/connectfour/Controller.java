package com.divyasaxena.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int CIRCLE_DIAMETER = 80;
    private static final String discColor1 = "#24303E";
    private static final String discColor2 = "#4CAABB";

    private static  String PLAYER_ONE = "Player One";
    private static String PLAYER_TWO = "Player Two";

    private boolean isPlayerOneTurn = true;

    private Disc[][] insertedDiscsArray = new Disc[ROWS][COLUMNS];

    @FXML
    public GridPane RootGridPane;

    @FXML
    public Pane insertedDiscsPane;

    @FXML
    public Label PlayerNameLabel;

    @FXML
    public TextField playerOneName;

    @FXML
    public TextField playerTwoName;

    @FXML
    public Button setNamesBtn;

    private boolean isAllowedToInsert = true;  // Flag to avoid same color disc being added..

    public  void createPlayground(){

        Shape rectangleWithHoles = CreateGameStructuralGrid();
        RootGridPane.add(rectangleWithHoles, 0,1);

        List<Rectangle> rectangleList = createClickableColumns();

        for(Rectangle rectangle: rectangleList){
            RootGridPane.add(rectangle,0,1);
        }
    }
    private Shape CreateGameStructuralGrid(){
        Shape rectangleWithHoles = new Rectangle((COLUMNS + 1) * CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);

        for(int row = 0; row < ROWS; row++){
            for(int col = 0; col < COLUMNS; col++){

                Circle circle = new Circle();
                circle.setRadius(CIRCLE_DIAMETER/2);
                circle.setCenterX(CIRCLE_DIAMETER/2);
                circle.setCenterY(CIRCLE_DIAMETER/2);
                circle.setSmooth(true);

                circle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);
                circle.setTranslateY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);
                rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle);
            }
        }
        rectangleWithHoles.setFill(Color.WHITE);

        return rectangleWithHoles;
    }

    private List<Rectangle> createClickableColumns(){

        List<Rectangle> rectangleList = new ArrayList<>();

        for(int col = 0; col < COLUMNS; col++) {
            Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX( col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);

            rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
            rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.TRANSPARENT));

            final int column = col;
            rectangle.setOnMouseClicked(event -> {
                if(isAllowedToInsert) {
                    isAllowedToInsert = false; // When disc is being dropped then no more disc will be inserted..
                    insertDisc(new Disc(isPlayerOneTurn), column);
                }
            });

            rectangleList.add(rectangle);
        }

        return rectangleList;
    }

    private void insertDisc(Disc disc, int column) {

        int row = ROWS - 1;
        while (row >= 0) {

            if(getDiscIfPresent(row, column) == null)
                break;
             row--;
        }

        if(row < 0)
            return;

        insertedDiscsArray[row][column] = disc; // For Structural Changes
        insertedDiscsPane.getChildren().add(disc);

        disc.setTranslateX(column * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);

        int CurrentRow = row;
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5), disc);
        translateTransition.setToY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);
        translateTransition.setOnFinished(event -> {

            isAllowedToInsert = true; // Finally, When the disc is dropped allow next player to insert disc..
            if(gameEnded(CurrentRow,column)){
                gameOver();
                return;
            }

            isPlayerOneTurn =! isPlayerOneTurn;
            PlayerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);
        });
        translateTransition.play();
    }

    private void gameOver() {
           String Winner = isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO;
           System.out.println("Winner is: " + Winner);

           Alert alert = new Alert(Alert.AlertType.INFORMATION);
           alert.setTitle("Connect Four");
           alert.setHeaderText("The Winner is: " + Winner);
           alert.setContentText("Want to play again? ");

           ButtonType yesBtn = new ButtonType("Yes");
           ButtonType noBtn = new ButtonType("No, Exit");
           alert.getButtonTypes().setAll(yesBtn, noBtn);

           Platform.runLater( () -> {

               Optional<ButtonType> btnClicked =  alert.showAndWait();
               if(btnClicked.isPresent() && btnClicked.get() == yesBtn ){
                   // User choose yes so reset game...
                   resetGame();
               }else{
                   Platform.exit();
                   System.exit(0);
               }
           });
    }

    public void resetGame() {
        insertedDiscsPane.getChildren().clear(); //Remove all inserted Disc from pane..
        for (int row = 0; row < insertedDiscsArray.length; row++){

            for(int col = 0; col < insertedDiscsArray[row].length; col++){
                insertedDiscsArray[row][col] = null;
            }
        }

        isPlayerOneTurn = true; // Let player start the game..
        PlayerNameLabel.setText(PLAYER_ONE);

        createPlayground(); // prepare the fresh playground..
    }

    private boolean gameEnded(int row,int column){

        //Vertical points..row = 2, col = 3
        //range of row value= 0 to 5
        List<Point2D> verticalPoints =
                IntStream.rangeClosed(row - 3, row + 3).mapToObj(r -> new Point2D(r, column)).collect(Collectors.toList());

        List<Point2D> HorizontalPoints =
                IntStream.rangeClosed(column - 3, column + 3).mapToObj(col -> new Point2D(row, col)).collect(Collectors.toList());

        Point2D startPoint1 = new Point2D(row - 3, column + 3);
        List<Point2D> diagonal1Points =
                IntStream.rangeClosed(0, 6).mapToObj(i -> startPoint1.add(i, -i)).collect(Collectors.toList());

        Point2D startPoint2 = new Point2D(row - 3, column - 3);
        List<Point2D> diagonal2Points =
                IntStream.rangeClosed(0, 6).mapToObj(i -> startPoint2.add(i, i)).collect(Collectors.toList());

        boolean isEnded = checkConnections(verticalPoints) || checkConnections(HorizontalPoints)
                             || checkConnections(diagonal1Points) || checkConnections(diagonal2Points);

        return isEnded;
    }

    private boolean checkConnections(List<Point2D>Points) {

        int chain = 0;
        for (Point2D point: Points) {

            int rowIndexForArray = (int) point.getX();
            int columnIndexForArray = (int) point.getY();

            Disc disc = getDiscIfPresent(rowIndexForArray, columnIndexForArray);
            if(disc != null && disc.isPlayerOneMove == isPlayerOneTurn){

                chain++;
                if(chain == 4){
                    return true;
                }
            }else {
               chain = 0;
            }
        }
        return  false;
    }

    private  Disc getDiscIfPresent(int row, int columns){            // To Prevent array OutOfBoundException

        if(row >= ROWS || row < 0 || columns >= COLUMNS || columns < 0)   // If row and column index is invalid
            return null;

        return  insertedDiscsArray[row][columns];
    }

    private static class Disc extends Circle {

        private final boolean isPlayerOneMove;

        public Disc(boolean isPlayerOneMove) {

            this.isPlayerOneMove = isPlayerOneMove;
            setRadius(CIRCLE_DIAMETER / 2);
            setFill(isPlayerOneMove? Color.valueOf(discColor1): Color.valueOf(discColor2));
            setCenterX(CIRCLE_DIAMETER/2);
            setCenterY(CIRCLE_DIAMETER/2);
        }
    }

    @Override

    public void initialize(URL location, ResourceBundle resources) {

        setNamesBtn.setOnAction(event->{

            String input1 = playerOneName.getText();
            String input2 = playerTwoName.getText();

            PLAYER_ONE = input1 ;
            PLAYER_TWO = input2 ;

            if (input1.isEmpty())
                PLAYER_ONE = "Player One's";
            if(input2.isEmpty())
                PLAYER_TWO = "Player Two's";

            PlayerNameLabel.setText(isPlayerOneTurn ? PLAYER_ONE: PLAYER_TWO);
        });
    }
}
