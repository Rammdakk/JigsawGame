package com.hse.homework5;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameControlTest {

    @Test
    void updateMethodTest() {
        GameControl.field = new int[9][9];
        GameControl.field[2][4] = 1;
        GameControl.field[6][5] = 1;
        GameControl.update();
        boolean isRight = true;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (GameControl.field[i][j] != 0) {
                    isRight = false;
                    break;
                }
            }
        }
        assertTrue(isRight);
    }

    @Test
    void tryToInsertToFreeSpace() {
        GameControl.update();
        assertTrue(GameControl.tryToInsert(new Point(4, 4), Figure.getRandomFigure()));
    }

    @Test
    void tryToInsertToNotFreeSpace() {
        GameControl.update();
        Figure figure = Figure.getRandomFigure();
        GameControl.field[1 + (int) figure.centerPoint.getX()][1 + (int) figure.centerPoint.getY()] = 1;
        assertFalse(GameControl.tryToInsert(new Point(4, 4), figure));
    }
}