package com.hse.homework5;

/**
 * Класс, отвечающий за контроль над полем.
 */
public class GameControl {
    /**
     * Поле игры.
     */
    public static int[][] field = new int[9][9];

    /**
     * Очистка поля.
     */
    public static void update() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                field[i][j] = 0;
            }
        }
    }

    /**
     * Проверка на возможность вставки фигуры в поле 9х9.
     *
     * @param centerPoint   центральная точка фигуры
     * @param currentFigure текущая фигуры, вставка которой происходит
     * @return true - если вставка возможна, иначе - false
     */
    public static boolean tryToInsert(Point centerPoint, Figure currentFigure) {
        int[][] tempCord = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(field[i], 0, tempCord[i], 0, 9);
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (currentFigure.location[i][j] == 1) {
                    int X = (int) (centerPoint.getX() + 2 * i);
                    int Y = (int) (centerPoint.getY() + 2 * j);
                    if (X < 2 || X > 18 || Y < 2 || Y > 18 || tempCord[(X / 2) - 1][(Y / 2) - 1] == 1) {
                        for (int k = 0; k < 9; k++) {
                            System.arraycopy(tempCord[k], 0, field[k], 0, 9);
                        }
                        return false;
                    } else {
                        field[(X / 2) - 1][(Y / 2) - 1] = 1;
                    }
                }
            }
        }
        return true;
    }
}
