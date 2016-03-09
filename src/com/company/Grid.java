package com.company;

import javax.swing.*;
import java.awt.*;

/**
 * Created by lesliedahlberg on 22/07/14.
 */
public class Grid {

    Point [][] fieldCoordinates;

    BoardSettings boardSettings;

    //GUI
    JFrame frame;
    JPanel panel;
    Snake snake;
    Block block;

    int width;
    int height;



    public Grid(BoardSettings boardSettings) {
        this.boardSettings = boardSettings;
        width = boardSettings.fieldWidth * boardSettings.columns;
        height = boardSettings.fieldHeight * boardSettings.rows;

        fieldCoordinates = newCoordinateArray(boardSettings.columns, boardSettings.rows, boardSettings.fieldWidth, boardSettings.fieldHeight);


        loadFrame();
        loadSnake();
        showFrame();
    }



    //GENERATE NEW COORDINATE ARRAY
    private Point[][] newCoordinateArray(int columns, int rows, int fieldWidth, int fieldHeight){
        Point[][] fieldCoordinates = new Point[columns][rows];
        for(int x = 0; x < columns; x++){
            for(int y = 0; y < rows; y++){
                fieldCoordinates[x][y] = new Point(fieldWidth * x, fieldHeight * y);
            }
        }
        return fieldCoordinates;
    }

    //LOAD GUI
    private void loadFrame(){
        frame = new JFrame(boardSettings.gameTitle);
        panel = new JPanel();

        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(width, height));
        panel.setBackground(boardSettings.background1);
        panel.setFocusable(true);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();

    }
    private void showFrame(){
        frame.setVisible(true);
    }


    //LOAD SNAKE
    private void loadSnake(){
        snake = new Snake(frame, panel, fieldCoordinates, block, boardSettings);
    }






}
