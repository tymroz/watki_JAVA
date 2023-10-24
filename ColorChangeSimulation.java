import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Random;

/**
 * @author Tymoteusz Roźmiarek
 */

public class ColorChangeSimulation extends Application {

    private static final int RECTANGLE_SIZE = 30;

    private int n; // liczba wierszy
    private int m; // liczba kolumn
    private int k; // szybkość działania
    private double p; // prawdopodobieństwo zmiany koloru

    private Random random = new Random();

    private Rectangle[][] rectangles;
    private ColorChangeThread[][] threads;
    private boolean[][] isThreadSuspended;

    private TextField nField;
    private TextField mField;
    private TextField kField;
    private TextField pField;

    /**
     * Ta metoda jest wywoływana przy uruchamianiu aplikacji JavaFX. Tworzy interfejs użytkownika, w tym pola tekstowe i przycisk do rozpoczęcia symulacji.
     * @param primaryStage scena, na której wyświetlane są pola tekstowe i przycisk do rozpoczęcia symulacji
     */
    @Override
    public void start(Stage primaryStage) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(5);
        gridPane.setHgap(10);

        Label nLabel = new Label("Liczba wierszy (n):");
        nField = new TextField();
        gridPane.add(nLabel, 0, 0);
        gridPane.add(nField, 1, 0);

        Label mLabel = new Label("Liczba kolumn (m):");
        mField = new TextField();
        gridPane.add(mLabel, 0, 1);
        gridPane.add(mField, 1, 1);

        Label kLabel = new Label("Szybkość działania (k):");
        kField = new TextField();
        gridPane.add(kLabel, 0, 2);
        gridPane.add(kField, 1, 2);

        Label pLabel = new Label("Prawdopodobieństwo zmiany koloru (p):");
        pField = new TextField();
        gridPane.add(pLabel, 0, 3);
        gridPane.add(pField, 1, 3);

        Button startButton = new Button("Wykonaj");
        startButton.setOnAction(event -> startSimulation());
        gridPane.add(startButton, 0, 4);

        VBox root = new VBox(gridPane);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Color Change Simulation");
        primaryStage.setOnCloseRequest(event -> stopSimulation());
        primaryStage.show();
    }

    /**
     * Ta metoda jest wywoływana po kliknięciu przycisku "Wykonaj". Pobiera wartości z pól tekstowych, sprawdza poprawność wprowadzonych danych, tworzy planszę, wątki i uruchamia symulację.
     */
    private void startSimulation() {
        try {
            n = Integer.parseInt(nField.getText());
            m = Integer.parseInt(mField.getText());
            k = Integer.parseInt(kField.getText());
            p = Double.parseDouble(pField.getText());

            if (m <= 0 || n <= 0 || k <= 0 || p < 0 || p > 1) {
                throw new IllegalArgumentException("Nieprawidłowe wartości parametrów!");
            }

            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(10));
            gridPane.setVgap(2);
            gridPane.setHgap(2);

            rectangles = new Rectangle[n][m];
            threads = new ColorChangeThread[n][m];
            isThreadSuspended = new boolean[n][m];

            for (int row = 0; row < n; row++) {
                for (int col = 0; col < m; col++) {
                    Rectangle rectangle = new Rectangle(RECTANGLE_SIZE, RECTANGLE_SIZE);
                    rectangle.setFill(getRandomColor());
                    int rowtemp = row;
                    int coltemp = col;
                    rectangle.setOnMouseClicked(event -> toggleThreadSuspended(rowtemp, coltemp));
                    rectangles[row][col] = rectangle;
                    gridPane.add(rectangle, col, row);
                }
            }

            Scene scene = new Scene(gridPane);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Color Change Simulation");
            stage.setOnCloseRequest(event -> stopSimulation());
            stage.show();

            for (int row = 0; row < n; row++) {
                for (int col = 0; col < m; col++) {
                    ColorChangeThread thread = new ColorChangeThread(row, col);
                    threads[row][col] = thread;
                    thread.start();
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Nieprawidłowy format parametrów!");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Ta metoda generuje losowy kolor o wartościach składowych RGB.
     * @return obiekt klasy Color.
     */
    private Color getRandomColor() {
        double red = random.nextDouble();
        double green = random.nextDouble();
        double blue = random.nextDouble();
        return new Color(red, green, blue, 1.0);
    }

    /**
     * Jest to klasa wewnętrzna reprezentująca wątek odpowiedzialny za zmianę kolorów pojedynczego prostokąta na planszy. Przechowuje informacje o wierszu i kolumnie, w którym znajduje się prostokąt. Wywołanie metody run() rozpoczyna działanie wątku.
     */
    private class ColorChangeThread extends Thread {
        private int row;
        private int col;

        /**
         * Jest to konstruktor klasy 'ColorChangeThread'
         * @param row
         * @param col
         */
        public ColorChangeThread(int row, int col) {
            this.row = row;
            this.col = col;
        }

        /**
         * Jest to metoda reprezentująca logikę działania wątku. W pętli sprawdza, czy wątek został przerwany. Następnie generuje losowe opóźnienie i usypia wątek na ten czas. Jeśli wątek nie jest wstrzymany, to na podstawie prawdopodobieństwa p zmienia kolor prostokąta na planszy.
         */
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    int delay = (int) (0.5 * k + random.nextDouble() * k);
                    Thread.sleep(delay);

                    if (isThreadSuspended[row][col]) {
                        continue;
                    }

                    if (random.nextDouble() < p) {
                        Platform.runLater(() -> {
                            rectangles[row][col].setFill(getRandomColor());
                            System.out.println("Start: " + getName());
                            System.out.println("End: " + getName());
                        });
                    } else {
                        Platform.runLater(() -> {
                            Color averageColor = calculateAverageColor(row, col);
                            rectangles[row][col].setFill(averageColor);
                            System.out.println("Start: " + getName());
                            System.out.println("End: " + getName());
                        });
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Ta metoda oblicza średni kolor na podstawie kolorów sąsiadujących prostokątów.
         * @param row indeks wiersza
         * @param col indeks kolumny
         * @return obiekt klasy Color.
         */
        private Color calculateAverageColor(int row, int col) {
            Color[] neighborColors = new Color[4];
            neighborColors[0] = getColorFromNeighbor(row - 1, col);
            neighborColors[1] = getColorFromNeighbor(row + 1, col);
            neighborColors[2] = getColorFromNeighbor(row, col - 1);
            neighborColors[3] = getColorFromNeighbor(row, col + 1);

            double redSum = 0.0;
            double greenSum = 0.0;
            double blueSum = 0.0;

            int validNeighbors = 0;

            for (Color neighborColor : neighborColors) {
                if (neighborColor != null) {
                    redSum += neighborColor.getRed();
                    greenSum += neighborColor.getGreen();
                    blueSum += neighborColor.getBlue();
                    validNeighbors++;
                }
            }

            if (validNeighbors > 0) {
                double redAverage = redSum / validNeighbors;
                double greenAverage = greenSum / validNeighbors;
                double blueAverage = blueSum / validNeighbors;
                return new Color(redAverage, greenAverage, blueAverage, 1.0);
            }

            return null;
        }

        /**
         *  Ta metoda pobiera kolor sąsiada danego prostokąta.
         * @param row indeks wiersza
         * @param col indeks kolumny
         * @return obiekt klasy Color.
         */
        private Color getColorFromNeighbor(int row, int col) {
            int wrappedRow = (row + n) % n;
            int wrappedCol = (col + m) % m;
            return (Color) rectangles[wrappedRow][wrappedCol].getFill();
        }
    }

    /**
     * Ta metoda przełącza stan wstrzymania wątku.
     * @param row indeks wiersza
     * @param col indeks kolumny
     */
    private void toggleThreadSuspended(int row, int col) {
        isThreadSuspended[row][col] = !isThreadSuspended[row][col];
    }

    /**
     * Ta metoda kończy symulację. Przerywa wszystkie wątki, zamyka aplikację JavaFX i kończy działanie programu.
     */
    private void stopSimulation() {
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < m; col++) {
                threads[row][col].interrupt();
            }
        }
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
