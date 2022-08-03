package com.partko.matchthree;

import static java.lang.Math.abs;

import android.util.Log;

import java.util.Arrays;
import java.util.Random;

public class GameLogicT {

    private static final String TAG = "myLogs";

    int[][] matrix = new int[8][8];
    int[][][] searchMasks = new int[16][4][4];
    int[][] maskSizes = new int[16][2];
    private static final Random random = new Random();
    int anlz = 0;
    int score = 0;

    GameLogicT() {
        //---------------- маски поиска ----------------//
        searchMasks[0][0][0] = searchMasks[0][0][1] = searchMasks[0][1][2] = 1;
        searchMasks[1][1][0] = searchMasks[1][1][1] = searchMasks[1][0][2] = 1;
        searchMasks[2][0][0] = searchMasks[2][1][1] = searchMasks[2][0][2] = 1;
        searchMasks[3][1][0] = searchMasks[3][0][1] = searchMasks[3][0][2] = 1;
        searchMasks[4][0][0] = searchMasks[4][1][1] = searchMasks[4][1][2] = 1;
        searchMasks[5][1][0] = searchMasks[5][0][1] = searchMasks[5][1][2] = 1;
        searchMasks[6][2][0] = searchMasks[6][0][1] = searchMasks[6][1][1] = 1;
        searchMasks[7][0][0] = searchMasks[7][1][0] = searchMasks[7][2][1] = 1;
        searchMasks[8][0][0] = searchMasks[8][1][1] = searchMasks[8][2][1] = 1;
        searchMasks[9][1][0] = searchMasks[9][2][0] = searchMasks[9][0][1] = 1;
        searchMasks[10][1][0] = searchMasks[10][0][1] = searchMasks[10][2][1] = 1;
        searchMasks[11][0][0] = searchMasks[11][2][0] = searchMasks[11][1][1] = 1;
        searchMasks[12][0][0] = searchMasks[12][0][1] = searchMasks[12][0][3] = 1;
        searchMasks[13][0][0] = searchMasks[13][0][2] = searchMasks[13][0][3] = 1;
        searchMasks[14][0][0] = searchMasks[14][1][0] = searchMasks[14][3][0] = 1;
        searchMasks[15][0][0] = searchMasks[15][2][0] = searchMasks[15][3][0] = 1;

        //---------------- размеры масок поиска ----------------//
        maskSizes[0][0] = maskSizes[1][0] = maskSizes[2][0] = maskSizes[3][0] = maskSizes[4][0] = maskSizes[5][0] = 2;
        maskSizes[0][1] = maskSizes[1][1] = maskSizes[2][1] = maskSizes[3][1] = maskSizes[4][1] = maskSizes[5][1] = 3;
        maskSizes[6][0] = maskSizes[7][0] = maskSizes[8][0] = maskSizes[9][0] = maskSizes[10][0] = maskSizes[11][0] = 3;
        maskSizes[6][1] = maskSizes[7][1] = maskSizes[8][1] = maskSizes[9][1] = maskSizes[10][1] = maskSizes[11][1] = 2;
        maskSizes[12][0] = maskSizes[13][0] = 1;
        maskSizes[12][1] = maskSizes[13][1] = 4;
        maskSizes[14][0] = maskSizes[15][0] = 4;
        maskSizes[14][1] = maskSizes[15][1] = 1;

        newMatrix(); //создание массива
    }


    protected void newMatrix() {
        Log.d(TAG, "Вызван newMatrix()");
        do {
            createMatrix(); // создается массив из случайных элементов

            Log.d(TAG, "созданная матрица:");
            displayMatrix(matrix);

            while (searchMatches(false)) { // совпавшие в ряд 3+ элементы заменяются на отрицательные
                Log.d(TAG, "измененная матрица:");
                displayMatrix(matrix);

                gemReplacement(); // отрицательные элементы заменяются на новые случайные

                Log.d(TAG, "измененная матрица с заменой:");
                displayMatrix(matrix);

            }
        } while (analysisForExchanges() == 0); // если нет ходов, массив создается заново
        score = 0; // обнуляются очки, т.к. матрица менялась без участия игрока
    }


    //---------------- создание массива из случайных элементов ----------------//
    protected void createMatrix() {
        Log.d(TAG, "Вызван createMatrix()");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                matrix[i][j] = random.nextInt(8) + 1; //////gems.length
            }
        }
    }


    //---------------- проверка массива на совпадения 3+ в ряд ----------------//
    protected boolean searchMatches(boolean addToScore) {
        Log.d(TAG, "Вызван searchMatches()");

        boolean isMatch = false; // флаг "есть ли хоть одно готовое совпадение"
        int NChBlock;
        int ChBlock = 0;
        int tempScore = 0;
        anlz = 0;

        //Log.d(TAG, "isMatch в начале checkMatrix()" + isMatch);

        //---------------- анализ по горизонтали ----------------//
        for (int y = 0; y < 8; y++) { // поле 8х8
            NChBlock = 0; // число фишек в текущей группе
            for (int x = 0; x < 8; x++) {
                if (x == 0) {
                    ChBlock = abs(matrix[x][y]);
                }
                if (abs(matrix[x][y]) == ChBlock) { // ChBlock - число группы
                    NChBlock += 1;
                } else {
                    if (NChBlock > 2) { // Найдена группа из NChBlock фишек ChBlock (>=3)
                        isMatch = true;
                        anlz++;
                        tempScore += NChBlock * 10 + NChBlock / 4 * 10 + NChBlock / 5 * 20;
                        //Log.d(TAG, "Метка 1");
                        for (int l = 0; l < NChBlock; l++) { // Выделение группы
                            matrix[x - NChBlock + l][y] = (-1) * abs(matrix[x - NChBlock + l][y]); // Замена знака у готовых групп
                        }
                    }
                    ChBlock = abs(matrix[x][y]); // Сброс группы на новую
                    NChBlock = 1;
                }
                if (x == 7 && NChBlock > 2) { // Концевая группа
                    isMatch = true;
                    anlz++;
                    tempScore += NChBlock * 10 + NChBlock / 4 * 10 + NChBlock / 5 * 20;
                    //Log.d(TAG, "Метка 2");
                    for (int l = 1; l < NChBlock+1; l++) { //Выделение группы:
                        matrix[x - NChBlock + l][y] = (-1) * abs(matrix[x - NChBlock + l][y]);
                    }
                }
            }
        }

        //---------------- анализ по вертикали ----------------//
        for (int x = 0; x < 8; x++) { // поле 8х8
            NChBlock = 0; // число фишек в текущей группе
            for (int y = 0; y < 8; y++) {
                if (y == 0) {
                    ChBlock = abs(matrix[x][y]);
                }
                if (abs(matrix[x][y]) == ChBlock) { // ChBlock - число группы
                    NChBlock += 1;
                } else {
                    if (NChBlock > 2) { // Найдена группа из NChBlock фишек ChBlock (>=3)
                        isMatch = true;
                        anlz++;
                        tempScore += NChBlock * 10 + NChBlock / 4 * 10 + NChBlock / 5 * 20;
                        //Log.d(TAG, "Метка 3");
                        for (int l = 0; l < NChBlock; l++) { // Выделение группы
                            matrix[x][y - NChBlock + l] = (-1) * abs(matrix[x][y - NChBlock + l]); // Замена знака у готовых групп
                        }
                    }
                    ChBlock = abs(matrix[x][y]); // Сброс группы на новую
                    NChBlock = 1;
                }
                if (y == 7 && NChBlock > 2) { // Концевая группа
                    isMatch = true;
                    anlz++;
                    tempScore += NChBlock * 10 + NChBlock / 4 * 10 + NChBlock / 5 * 20;
                    //Log.d(TAG, "Метка 4");
                    for (int l = 1; l < NChBlock+1; l++) { // Выделение группы:
                        matrix[x][y - NChBlock + l] = (-1) * abs(matrix[x][y - NChBlock + l]);
                    }
                }
            }
        }
        if (addToScore) {
            score += tempScore;
        }
        //Log.d(TAG, "isMatch в конце checkMatrix()" + isMatch);
        return isMatch;
    }


    //---------------- замена отрицательных элементов массива ----------------//
    protected void gemReplacement() {
        Log.d(TAG, "Вызван gemReplacement()");
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                if (matrix[i][j] < 0){
                    matrix[i][j] = random.nextInt(8) + 1; //////gems.length
                }
            }
        }
    }


    //---------------- анализ на обмены ----------------//
    protected int analysisForExchanges() {
        int NChange = 0; // число возможных обменов на слития
        int Nmask = 0; // Совпадения с маской

        for (int i = 1; i <= 8; i++) {
            for (int mask_number = 0; mask_number < 16; mask_number++) {
                for (int y = 0; y < (8 - maskSizes[mask_number][1] + 1); y++) {
                    for (int x = 0; x < (8 - maskSizes[mask_number][0] + 1); x++) {
                        Nmask = 0;
                        for (int My = 0; My < maskSizes[mask_number][1]; My++) {
                            for (int Mx = 0; Mx < maskSizes[mask_number][0]; Mx++) {
                                if (searchMasks[mask_number][Mx][My] == 1 && matrix[x + Mx][y + My] == i) {
                                    Nmask += 1;
                                }
                            }
                        }
                        if (Nmask == 3) {
                            NChange += 1;
                        }
                    }
                }
            }
        }
        Log.d(TAG, "Вызван analysisForExchanges(), NChange = " + NChange);
        return NChange;
    }


    //---------------- падение элементов ----------------//
    protected void fallingGems() {
        Log.d(TAG, "Вызван fallingGems()");
        int NHole = 0;
        int YHole = 0;

        for (int x = 0; x < 8; x++) {
            NHole = 0; // Число незакрытых пропусков в текущем столбце
            for (int y = 7; y > -1; y--) {
                if (matrix[x][y] < 0) { // Пропуск
                    NHole += 1; // Подсчет вертикали пропусков
                    if (NHole == 1) { // Самый нижний пропуск
                        YHole = y;
                    }
                }
                if (matrix[x][y] > 0 && NHole > 0) { // Фишка над пропуском
                    matrix[x][YHole] = matrix[x][y]; // Упавшая фишка
                    YHole -= 1; // Перемещение самого нижнего пропуска
                    matrix[x][y] = -1; // Вместо упавшей фишки пропуск
                    Log.d(TAG, "Произошело падение");
                }
            }
        }
    }


    //---------------- обмен соседних элементов ----------------//
    protected void gemExchange(int posX1, int posY1, int posX2, int posY2) {
        int tmp = matrix[posX1][posY1];
        matrix[posX1][posY1] = matrix[posX2][posY2];
        matrix[posX2][posY2] = tmp;
    }


    //---------------- вывод матрицы для проверки ----------------//
    protected void displayMatrix(int[][] array) {
        for (int i = 0; i < 8; i++) {
            Log.d(TAG, " " + Arrays.toString(array[i]));
        }
    }

}
