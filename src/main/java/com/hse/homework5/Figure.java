package com.hse.homework5;

import java.util.Random;

/**
 * Класс, реализующий фигуры, которые могут появляться.
 */
public class Figure {
    /**
     * Массив, отвечающий за расположение клеток фигуры.
     */
    int[][] location;
    /**
     * Центральная точка фигуры.
     */
    Point centerPoint;

    Figure() {
        location = new int[3][3];
        centerPoint = new Point(1, 1);
    }

    /**
     * Метод, создающий одну из 31 новых фигур.
     *
     * @return созданную фигуру
     */
    public static Figure getRandomFigure() {
        Random rnd = new Random(System.currentTimeMillis());
        int figureNumber = 1 + rnd.nextInt(31);
        return createFigure(figureNumber);
    }

    public static Figure getFigureByNumber(int figureNumber) {
        return createFigure(figureNumber);
    }

    private static Figure createFigure(int figureNumber) {
        Figure figure = new Figure();
        switch (figureNumber) {
            case 1 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[i][1] = 1;
                }
                figure.location[0][2] = 1;
            }
            case 2 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[1][i] = 1;
                }
                figure.location[0][0] = 1;
            }
            case 3 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[i][1] = 1;
                }
                figure.location[2][0] = 1;
            }
            case 4 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[1][i] = 1;
                }
                figure.location[2][2] = 1;
            }
            case 5 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[i][1] = 1;
                }
                figure.location[0][0] = 1;
            }
            case 6 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[1][i] = 1;
                }
                figure.location[0][2] = 1;
            }
            case 7 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[i][1] = 1;
                }
                figure.location[2][2] = 1;
            }
            case 8 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[1][i] = 1;
                }
                figure.location[2][0] = 1;
            }
            case 9 -> {
                figure.location[0][0] = 1;
                figure.location[1][0] = 1;
                figure.location[1][1] = 1;
                figure.location[2][1] = 1;
            }
            case 10 -> {
                figure.location[0][2] = 1;
                figure.location[1][0] = 1;
                figure.location[1][1] = 1;
                figure.location[0][1] = 1;
            }
            case 11 -> {
                figure.location[0][1] = 1;
                figure.location[1][1] = 1;
                figure.location[1][0] = 1;
                figure.location[2][0] = 1;
            }
            case 12 -> {
                figure.location[0][0] = 1;
                figure.location[1][2] = 1;
                figure.location[1][1] = 1;
                figure.location[0][1] = 1;
            }
            case 13 -> {
                figure.centerPoint = new Point(0, 0);
                for (int i = 0; i < 3; i++) {
                    figure.location[i][0] = 1;
                }
                figure.location[0][1] = 1;
                figure.location[0][2] = 1;
            }
            case 14 -> {
                figure.centerPoint = new Point(2, 0);
                for (int i = 0; i < 3; i++) {
                    figure.location[2][i] = 1;
                }
                figure.location[0][0] = 1;
                figure.location[1][0] = 1;
            }
            case 15 -> {
                figure.centerPoint = new Point(2, 2);
                for (int i = 0; i < 3; i++) {
                    figure.location[i][2] = 1;
                }
                figure.location[2][0] = 1;
                figure.location[2][1] = 1;
            }
            case 16 -> {
                figure.centerPoint = new Point(0, 2);
                for (int i = 0; i < 3; i++) {
                    figure.location[0][i] = 1;
                }
                figure.location[1][2] = 1;
                figure.location[2][2] = 1;
            }
            case 17 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[i][0] = 1;
                }
                figure.location[1][1] = 1;
                figure.location[1][2] = 1;
            }
            case 18 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[2][i] = 1;
                }
                figure.location[0][1] = 1;
                figure.location[1][1] = 1;
            }
            case 19 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[i][2] = 1;
                }
                figure.location[1][0] = 1;
                figure.location[1][1] = 1;
            }
            case 20 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[0][i] = 1;
                }
                figure.location[1][1] = 1;
                figure.location[2][1] = 1;
            }
            case 21 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[1][i] = 1;
                }
            }
            case 22 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[i][1] = 1;
                }
            }
            case 23 -> figure.location[1][1] = 1;
            case 24 -> {
                figure.location[1][1] = 1;
                figure.location[1][2] = 1;
                figure.location[2][1] = 1;
            }
            case 25 -> {
                figure.location[1][0] = 1;
                figure.location[1][1] = 1;
                figure.location[2][1] = 1;
            }
            case 26 -> {
                figure.location[1][0] = 1;
                figure.location[0][1] = 1;
                figure.location[1][1] = 1;
            }
            case 27 -> {
                figure.location[0][1] = 1;
                figure.location[1][1] = 1;
                figure.location[1][2] = 1;
            }
            case 28 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[i][1] = 1;
                }
                figure.location[1][2] = 1;
            }
            case 29 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[1][i] = 1;
                }
                figure.location[0][1] = 1;
            }
            case 30 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[i][1] = 1;
                }
                figure.location[1][0] = 1;
            }
            case 31 -> {
                for (int i = 0; i < 3; i++) {
                    figure.location[1][i] = 1;
                }
                figure.location[2][1] = 1;
            }
        }
        return figure;
    }

}
