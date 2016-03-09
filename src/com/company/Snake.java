package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lesliedahlberg on 22/07/14.
 */
public class Snake {
    JFrame frame;
    JPanel panel;
    Square head;
    Block block;

    Square[] tail;
    Point[] tailSquaresLocations;


    boolean removedHoveredBlock = false;
    //int moves = 0;
    String movingInDirection = "left";
    boolean snakeMoving = true;
    boolean firstMoveInitiated = false;
    boolean aiIsPlaying;

    int tailSquareNumber = 0;
    int blocksEaten = 0;
    BoardSettings boardSettings;
    int numberOfTailSquares = boardSettings.numberOfTailsSquares;

    boolean gameHasStarted = false;


    Point[][] fieldCoordinates;
    public Snake(JFrame frame, JPanel panel, Point[][] fieldCoordinates, Block block, BoardSettings boardSettings){
        this.frame = frame;
        this.panel = panel;
        this.block = block;
        this.fieldCoordinates = fieldCoordinates;
        this.boardSettings = boardSettings;



        loadHead();
        loadTail();
        listen();
        //JOptionPane.showMessageDialog(null, "Press 1 for AI, press 2 for HUMAN!");

    }

    private void loadHead() {
        head = new Square(fieldCoordinates[BoardSettings.defaultHeadIndexX][BoardSettings.defaultHeadIndexY], new Dimension(BoardSettings.fieldWidth, BoardSettings.fieldHeight), BoardSettings.color1);
        panel.add(head);
    }

    //AUTO-MOVE SNAKE
    private void autoMove(){
        block = new Block(fieldCoordinates, panel);
        //block.stopBlockClock();
        gameHasStarted = true;
        Runnable autoMoveSnakeRunnable = new Runnable() {
            @Override
            public void run() {
                if(movingInDirection != null && snakeMoving){
                    /*if(firstMoveInitiated == false){
                        firstMoveInitiated = true;
                        //game.addNewBlocks();
                    }*/
                    if(aiIsPlaying){
                        movingInDirection = getAiDirection();
                    }

                    moveSnake();
                }
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(autoMoveSnakeRunnable, 0, boardSettings.speed, TimeUnit.MILLISECONDS);
    }

    //AI

    private String getAiDirection(){

        String direction;

        int x = getSquareXIndex(head);
        int y = getSquareYIndex(head);

        int leftMotivation = 0;
        int rightMotivation = 0;
        int upMotivation = 0;
        int downMotivation = 0;

        int leftSafety = 0;
        int rightSafety = 0;
        int upSafety = 0;
        int downSafety = 0;

        int safetyAndMotivation = 0;
        int blockBonus = boardSettings.blockBonus;



        //SAFE

        /*
        leftSafety = leftFreeFieldCount(x, y);
        rightSafety = rightFreeFieldCount(x, y);
        upSafety = topFreeFieldCount(x, y);
        downSafety = bottomFreeFieldCount(x, y);
        */

        /*
        leftSafety = leftAndSidewaysFreeFieldCount(x, y);
        rightSafety = rightAndSidewaysFreeFieldCount(x, y);
        upSafety = topAndSidewaysFreeFieldCount(x, y);
        downSafety = bottomAndSidewaysFreeFieldCount(x, y);
        */



        leftSafety = leftIntensiveFreeFieldCount(x, y);
        rightSafety = rightIntensiveFreeFieldCount(x, y);
        upSafety = topIntensiveFreeFieldCount(x, y);
        downSafety = bottomIntensiveFreeFieldCount(x, y);


        //FOOD







        if(leftSafety > 1) {
            leftMotivation = (boardSettings.columns - leftBlockDistance(x, y)) * boardSettings.blockBonus ;

            leftMotivation += leftIntensiveBlockExistence(x, y) * boardSettings.blockBonus;

        }
        if(rightSafety > 1) {
            rightMotivation = (boardSettings.columns - rightBlockDistance(x, y)) * boardSettings.blockBonus ;

            rightMotivation += rightIntensiveBlockExistence(x, y) * boardSettings.blockBonus;
        }
        if(upSafety > 1) {
            upMotivation = (boardSettings.rows - topBlockDistance(x, y)) * boardSettings.blockBonus ;

            upMotivation += topIntensiveBlockExistence(x, y) * boardSettings.blockBonus;
        }
        if(downSafety > 1) {
            downMotivation = (boardSettings.rows - bottomBlockDistance(x, y)) * boardSettings.blockBonus ;

            downMotivation += bottomIntensiveBlockExistence(x, y) * boardSettings.blockBonus;
        }


        //JOptionPane.showMessageDialog(null, "LS: " + leftSafety + ", RS: " + rightSafety + ", US: " + upSafety + ", DS: " + downSafety);
        //JOptionPane.showMessageDialog(null, "LM: " + leftMotivation + ", RM: " + rightMotivation + ", UM: " + upMotivation + ", DM: " + downMotivation);


        //COMPARE

        Random random = new Random();

        direction = "left";

        safetyAndMotivation = leftSafety + leftMotivation;

        if(rightSafety + rightMotivation > safetyAndMotivation){
            safetyAndMotivation = rightSafety + rightMotivation;
            direction = "right";
        }
        if(rightSafety + rightMotivation == safetyAndMotivation && random.nextBoolean()){
            safetyAndMotivation = rightSafety + rightMotivation;
            direction = "right";

        }
        if(upSafety + upMotivation > safetyAndMotivation){
            safetyAndMotivation = upSafety + upMotivation;
            direction = "up";

        }
        if(upSafety + upMotivation == safetyAndMotivation && random.nextBoolean()){
            safetyAndMotivation = upSafety + upMotivation;
            direction = "up";


        }

        if(downSafety + downMotivation > safetyAndMotivation){
            safetyAndMotivation = downSafety + downMotivation;
            direction = "down";
        }
        if(downSafety + downMotivation == safetyAndMotivation && random.nextBoolean()){
            safetyAndMotivation = downSafety + downMotivation;
            direction = "down";

        }

        //JOptionPane.showMessageDialog(null, safetyAndMotivation);

        return direction;
    }

    private int leftFreeFieldCount(int x, int y){
        int i;
        for(i = 0; (isFieldFree(x - i - 1, y) && doesFieldExist(x - i - 1, y)); i++){}
        return i;
    }
    private int rightFreeFieldCount(int x, int y){
        int i;
        for(i = 0; (isFieldFree(x + i + 1, y) && doesFieldExist(x + i + 1, y)); i++){}
        return i;
    }
    private int topFreeFieldCount(int x, int y){
        int i;
        for(i = 0; (isFieldFree(x, y - i - 1) && doesFieldExist(x, y - i - 1)); i++){}
        return i;
    }
    private int bottomFreeFieldCount(int x, int y){
        int i;
        for(i = 0; (isFieldFree(x, y + i + 1) && doesFieldExist(x, y + i + 1)); i++){}
        return i;
    }

    private int leftBlockCount(int x, int y){
        int i;
        int blockCount = 0;
        for(i = 1; (doesFieldExist(x - i, y) && isFieldFree(x - i, y)); i++){
            if(isFieldBlock(x - i, y))
                blockCount++;
        }
        return blockCount;
    }
    private int rightBlockCount(int x, int y){
        int i;
        int blockCount = 0;
        for(i = 1; (doesFieldExist(x + i, y) && isFieldFree(x + i, y)); i++){
            if(isFieldBlock(x + i, y))
                blockCount++;
        }
        return blockCount;
    }
    private int topBlockCount(int x, int y){
        int i;
        int blockCount = 0;
        for(i = 1; (doesFieldExist(x, y - i) && isFieldFree(x, y - i)); i++){
            if(isFieldBlock(x, y - i))
                blockCount++;
        }
        return blockCount;
    }
    private int bottomBlockCount(int x, int y){
        int i;
        int blockCount = 0;
        for(i = 1; (doesFieldExist(x, y + i) && isFieldFree(x, y + i)); i++){
            if(isFieldBlock(x, y + i))
                blockCount++;
        }
        return blockCount;
    }

    //DISTANCE TO BLOCK
    private int leftBlockDistance(int x, int y){

        for(int i = 1; (doesFieldExist(x - i, y) && isFieldFree(x - i, y)); i++){

            if(isFieldBlock(x - i, y))
                return i;

        }
        return boardSettings.columns;
    }
    private int rightBlockDistance(int x, int y){
        for(int i = 1; (doesFieldExist(x + i, y) && isFieldFree(x + i, y)); i++){

            if(isFieldBlock(x + i, y))
                return i;
        }
        return boardSettings.columns;
    }
    private int topBlockDistance(int x, int y){
        for(int i = 1; (doesFieldExist(x, y - i) && isFieldFree(x, y - i)); i++){

            if(isFieldBlock(x, y - i))
                return i;
        }
        return boardSettings.rows;
    }
    private int bottomBlockDistance(int x, int y){
        for(int i = 1; (doesFieldExist(x, y + i) && isFieldFree(x, y + i)); i++){

            if(isFieldBlock(x, y + i)){
                return i;
            }

        }
        return boardSettings.rows;
    }


    //is there a block in row or column
    private int leftBlockExistence(int x, int y){
        int blocks = 0;
        for(int i = 1; (doesFieldExist(x - i, y) && isFieldFree(x - i, y)); i++){

            if(isFieldBlock(x - i, y))
                blocks++;

        }
        return blocks;
    }
    private int leftIntensiveBlockExistence(int x, int y){
        int blocks = 0;
        for(int i = 1; (doesFieldExist(x - i, y) && isFieldFree(x - i, y)); i++){


            blocks += topBlockExistence(x - i, y);
            blocks += bottomBlockExistence(x - i, y);


        }
        return blocks;
    }
    private int rightBlockExistence(int x, int y){
        int blocks = 0;
        for(int i = 1; (doesFieldExist(x + i, y) && isFieldFree(x + i, y)); i++){

            if(isFieldBlock(x + i, y))
                blocks++;
        }
        return blocks;
    }
    private int rightIntensiveBlockExistence(int x, int y){
        int blocks = 0;
        for(int i = 1; (doesFieldExist(x + i, y) && isFieldFree(x + i, y)); i++){

            blocks += topBlockExistence(x - i, y);
            blocks += bottomBlockExistence(x - i, y);
        }
        return blocks;
    }

    private int topBlockExistence(int x, int y){
        int blocks = 0;
        for(int i = 1; (doesFieldExist(x, y - i) && isFieldFree(x, y - i)); i++){

            if(isFieldBlock(x, y - i))
                blocks++;
        }
        return blocks;
    }

    private int topIntensiveBlockExistence(int x, int y){
        int blocks = 0;
        for(int i = 1; (doesFieldExist(x, y - i) && isFieldFree(x, y - i)); i++){


            blocks += leftBlockExistence(x, y - i);
            blocks += rightBlockExistence(x, y - i);

        }
        return blocks;
    }

    private int bottomIntensiveBlockExistence(int x, int y){
        int blocks = 0;
        for(int i = 1; (doesFieldExist(x, y + i) && isFieldFree(x, y + i)); i++){

            blocks += leftBlockExistence(x, y - i);
            blocks += rightBlockExistence(x, y - i);

        }
        return blocks;
    }


    private int bottomBlockExistence(int x, int y){
        int blocks = 0;
        for(int i = 1; (doesFieldExist(x, y + i) && isFieldFree(x, y + i)); i++){

            if(isFieldBlock(x, y + i)){
                blocks++;
            }

        }
        return blocks;
    }


    //2

    private int leftAndSidewaysFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x - i - 1, y) && doesFieldExist(x - i - 1, y)); i++){
            direction1 += topFreeFieldCount(x - i - 1, y);
            direction2 += bottomFreeFieldCount(x - i - 1, y);
        }
        if(i == 0) return 0;
        i += direction1 + direction2;
        return i;
    }
    private int rightAndSidewaysFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x + i + 1, y) && doesFieldExist(x + i + 1, y)); i++){
            direction1 += topFreeFieldCount(x + i + 1, y);
            direction2 += bottomFreeFieldCount(x + i + 1, y);
        }
        if(i == 0) return 0;
        i += direction1 + direction2;

        return i;
    }
    private int topAndSidewaysFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x, y - i - 1) && doesFieldExist(x, y - i - 1)); i++){
            direction1 += leftFreeFieldCount(x, y - i - 1);
            direction2 += rightFreeFieldCount(x, y - i - 1);
        }
        if(i == 0) return 0;
        i += direction1 + direction2;

        return i;
    }
    private int bottomAndSidewaysFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x, y + i + 1) && doesFieldExist(x, y + i + 1)); i++){
            direction1 += rightFreeFieldCount(x, y + i + 1);
            direction2 += leftFreeFieldCount(x, y + i + 1);

        }
        if(i == 0) return 0;
        i += direction1 + direction2;

        return i;
    }

    //VAA SUPPORT
    private int leftAndTopFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x - i - 1, y) && doesFieldExist(x - i - 1, y)); i++){
            direction1 += topFreeFieldCount(x - i - 1, y);
            //direction2 += bottomFreeFieldCount(x - i - 1, y);
        }
        if(i == 0) return 0;
        i += direction1 + direction2;
        return i;
    }

    private int leftAndBottomFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x - i - 1, y) && doesFieldExist(x - i - 1, y)); i++){
            //direction1 += topFreeFieldCount(x - i - 1, y);
            direction2 += bottomFreeFieldCount(x - i - 1, y);
        }
        if(i == 0) return 0;
        i += direction1 + direction2;
        return i;
    }

    private int rightAndTopFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x + i + 1, y) && doesFieldExist(x + i + 1, y)); i++){
            direction1 += topFreeFieldCount(x + i + 1, y);
            //direction2 += bottomFreeFieldCount(x + i + 1, y);
        }
        if(i == 0) return 0;
        i += direction1 + direction2;

        return i;
    }

    private int rightAndBottomFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x + i + 1, y) && doesFieldExist(x + i + 1, y)); i++){
            //direction1 += topFreeFieldCount(x + i + 1, y);
            direction2 += bottomFreeFieldCount(x + i + 1, y);
        }
        if(i == 0) return 0;
        i += direction1 + direction2;

        return i;
    }

    private int topAndLeftFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x, y - i - 1) && doesFieldExist(x, y - i - 1)); i++){
            direction1 += leftFreeFieldCount(x, y - i - 1);
            //direction2 += rightFreeFieldCount(x, y - i - 1);
        }
        if(i == 0) return 0;
        i += direction1 + direction2;

        return i;
    }

    private int topAndRightFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x, y - i - 1) && doesFieldExist(x, y - i - 1)); i++){
            //direction1 += leftFreeFieldCount(x, y - i - 1);
            direction2 += rightFreeFieldCount(x, y - i - 1);
        }
        if(i == 0) return 0;
        i += direction1 + direction2;

        return i;
    }

    private int bottomAndRightFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x, y + i + 1) && doesFieldExist(x, y + i + 1)); i++){
            direction1 += rightFreeFieldCount(x, y + i + 1);
            //direction2 += leftFreeFieldCount(x, y + i + 1);

        }
        if(i == 0) return 0;
        i += direction1 + direction2;

        return i;
    }

    private int bottomAndLeftFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x, y + i + 1) && doesFieldExist(x, y + i + 1)); i++){
            //direction1 += rightFreeFieldCount(x, y + i + 1);
            direction2 += leftFreeFieldCount(x, y + i + 1);

        }
        if(i == 0) return 0;
        i += direction1 + direction2;

        return i;
    }

    //NEW SEEKER ALGO

    private void seekFromFieldOnTheLeft(int x, int y){
        int fieldX = x - 1;
        int fieldY = y;

        int left;
        int right;
        int top;
        int bottom;


        for(int i = 0; i < boardSettings.columns * boardSettings.rows - block.getBlockIndex(); i++){
            left = leftAndSidewaysFreeFieldCount(fieldX, fieldY);
            right = rightAndSidewaysFreeFieldCount(fieldX, fieldY);
            top = topAndSidewaysFreeFieldCount(fieldX, fieldY);
            bottom = bottomAndSidewaysFreeFieldCount(fieldX, fieldY);

            fieldX -= 1;


            if(right > left){
                fieldX += 1;
            }
        }
    }





    //VERY ADVANCED ALGORITHM
    private int leftIntensiveFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x - i - 1, y) && doesFieldExist(x - i - 1, y)); i++){
            direction1 += topAndSidewaysFreeFieldCount(x - i - 1, y);
            direction2 += bottomAndSidewaysFreeFieldCount(x - i - 1, y);
        }
        if((!isFieldFree(x - 1, y) || !doesFieldExist(x - 1, y))) return 0;
        //if((!isFieldFree(x - 2, y) || !doesFieldExist(x - 2, y))) return 0;
        i += direction1 + direction2;
        return i;
    }
    private int rightIntensiveFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x + i + 1, y) && doesFieldExist(x + i + 1, y)); i++){
            direction1 += topAndSidewaysFreeFieldCount(x + i + 1, y);
            direction2 += bottomAndSidewaysFreeFieldCount(x + i + 1, y);
        }
        if((!isFieldFree(x + 1, y) || !doesFieldExist(x + 1, y))) return 0;
        //if((!isFieldFree(x + 2, y) || !doesFieldExist(x + 2, y))) return 0;
        i += direction1 + direction2;

        return i;
    }
    private int topIntensiveFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x, y - i - 1) && doesFieldExist(x, y - i - 1)); i++){
            direction1 += leftAndSidewaysFreeFieldCount(x, y - i - 1);
            direction2 += rightAndSidewaysFreeFieldCount(x, y - i - 1);
        }
        if((!isFieldFree(x, y - 1) || !doesFieldExist(x, y - 1))) return 0;
        //if((!isFieldFree(x, y - 2) || !doesFieldExist(x, y - 2))) return 0;
        i += direction1 + direction2;

        return i;
    }
    private int bottomIntensiveFreeFieldCount(int x, int y){
        int i;
        int direction1 = 0;
        int direction2 = 0;
        for(i = 0; (isFieldFree(x, y + i + 1) && doesFieldExist(x, y + i + 1)); i++){
            direction1 += rightAndSidewaysFreeFieldCount(x, y + i + 1);
            direction2 += leftAndSidewaysFreeFieldCount(x, y + i + 1);

        }
        if((!isFieldFree(x, y + 1) || !doesFieldExist(x, y + 1))) return 0;
        //if((!isFieldFree(x, y + 2) || !doesFieldExist(x, y + 2))) return 0;

        i += direction1 + direction2;

        return i;
    }
    //END OF VERAY ADVANCED ALGO


    private boolean isFieldFree(int x, int y){
        if(doesFieldExist(x, y)){
            for (int i = 0; i < tail.length; i++) {
                if (tail[i] != null) {
                    if ((tail[i].getPosition().getX() == fieldCoordinates[x][y].getX())
                            && (tail[i].getPosition().getY() == fieldCoordinates[x][y].getY())) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }

    //MOVE SNAKE
    public void moveSnake(){
        //Point currentLocationOfHead = head.getPosition();
        Point newLocationOfHead = null;

        if(movingInDirection == "right") newLocationOfHead = getRightFieldCoords(head);
        if(movingInDirection == "left") newLocationOfHead = getLeftFieldCoords(head);
        if(movingInDirection == "down") newLocationOfHead = getBottomFieldCoords(head);
        if(movingInDirection == "up") newLocationOfHead = getTopFieldCoords(head);

        if(newLocationOfHead != null){
            head.setPosition(newLocationOfHead);

            if(doesHeadTouchTale()){
                snakeMoving = false;
                JOptionPane.showMessageDialog(null, blocksEaten * 100 + " POINTS");
                block.stopBlockClock();
                //game.terminate();
            }


            eatBlocks();
            moveTailWithHead(newLocationOfHead);

        }
        if(newLocationOfHead == null && snakeMoving){
            snakeMoving = false;
            JOptionPane.showMessageDialog(null, blocksEaten * 100 + " POINTS");
            block.stopBlockClock();
            //game.terminate();
        }
    }

    //BLOCK EATER

    private void eatBlocks(){
        removedHoveredBlock = false;
        for(int i = 0; i < block.getBlockIndex(); i++){
            if(block.getBlocks()[i] != null) {
                if((block.getBlocks()[i].getPosition().getX() == head.getPosition().getX())
                        && (block.getBlocks()[i].getPosition().getY() == head.getPosition().getY())) {
                    block.deleteBlock(i);
                    removedHoveredBlock = true;
                    panel.repaint();
                    addNewTailSquare();
                    blocksEaten++;
                }
            }
        }
    }

    //BLOCK CHECKER

    private boolean isBlock(Point point){

        for(int i = 0; i < block.getBlockIndex(); i++){
            if(block.getBlocks()[i] != null) {
                if((block.getBlocks()[i].getPosition().getX() == point.getX())
                        && (block.getBlocks()[i].getPosition().getY() == point.getY())) {
                    return true;

                }
            }
        }

        return false;
    }

    private boolean isFieldBlock(int x, int y){

        Point point = fieldCoordinates[x][y];

        for(int i = 0; i < block.getBlockIndex(); i++){
            if(block.getBlocks()[i] != null) {
                if((block.getBlocks()[i].getPosition().getX() == point.getX())
                        && (block.getBlocks()[i].getPosition().getY() == point.getY())) {
                    return true;

                }
            }
        }

        return false;
    }

    //SCANNER

    private boolean doesFieldExist(int x, int y){
        if(x >= 0 && y >= 0 && x < boardSettings.columns && y < boardSettings.rows){
            return true;
        }
        return false;
    }

    private int getSquareXIndex(Square s){
        Point point = s.getPosition();
        return getFieldIndexX(point);
    }

    private int getSquareYIndex(Square s){
        Point point = s.getPosition();
        return getFieldIndexY(point);
    }

    public Point getTopFieldCoords(Square s){
        int fieldIndexX = getSquareXIndex(s);
        int fieldIndexY = getSquareYIndex(s);
        if(doesFieldExist(fieldIndexX, fieldIndexY - 1)){
            return fieldCoordinates[fieldIndexX][fieldIndexY - 1];
        }
        return null;
    }

    public Point getBottomFieldCoords(Square s){

        int fieldIndexX = getSquareXIndex(s);
        int fieldIndexY = getSquareYIndex(s);

        if(doesFieldExist(fieldIndexX, fieldIndexY + 1)){

            return fieldCoordinates[fieldIndexX][fieldIndexY + 1];
        }
        return null;
    }

    public Point getLeftFieldCoords(Square s){
        int fieldIndexX = getSquareXIndex(s);
        int fieldIndexY = getSquareYIndex(s);

        if(doesFieldExist(fieldIndexX - 1, fieldIndexY)){
            return fieldCoordinates[fieldIndexX - 1][fieldIndexY];
        }
        return null;
    }

    public Point getRightFieldCoords(Square s){
        int fieldIndexX = getSquareXIndex(s);
        int fieldIndexY = getSquareYIndex(s);

        if(doesFieldExist(fieldIndexX + 1, fieldIndexY)){
            return fieldCoordinates[fieldIndexX + 1][fieldIndexY];
        }
        return null;
    }

    //CHECKER
    private boolean doesHeadTouchTale(){
        for(int i = 0; i < tail.length; i++){
            if(tail[i] != null){
                if((tail[i].getPosition().getX() == head.getPosition().getX())
                        && (tail[i].getPosition().getY() == head.getPosition().getY())){

                    //game.terminate();
                    return true;

                }
            }
        }
        return false;
    }

    private int getFieldIndexX(Point point){
        for(int x = 0; x < BoardSettings.columns; x++){
            int y = 0;
            if(point.getX() == fieldCoordinates[x][y].getX()){
                return x;
            }
        }
        return -1;
    }

    private int getFieldIndexY(Point point){
        for(int y = 0; y < BoardSettings.rows; y++){
            int x = 0;
            if(point.getY() == fieldCoordinates[x][y].getY()){
                return y;
            }
        }
        return -1;
    }

    public boolean isNewFieldInsideGrid(Point newLocationOfHead){
        int indexX = getFieldIndexX(newLocationOfHead);
        int indexY = getFieldIndexY(newLocationOfHead);

        if(indexX == -1 || indexY == -1){
            return false;
        }

        return true;
    }

    //LISTEN

    public void listen(){

        panel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {

            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if(!aiIsPlaying) {
                    switch (keyEvent.getKeyCode()) {
                        case KeyEvent.VK_RIGHT:
                            movingInDirection = "right";

                            break;
                        case KeyEvent.VK_LEFT:
                            movingInDirection = "left";

                            break;
                        case KeyEvent.VK_DOWN:
                            movingInDirection = "down";

                            break;
                        case KeyEvent.VK_UP:
                            movingInDirection = "up";

                            break;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });
        panel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {

            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.VK_1){

                    if(!gameHasStarted){
                        aiIsPlaying = true;
                        autoMove();
                    }

                }
                if(keyEvent.getKeyCode() == KeyEvent.VK_2){

                    if(!gameHasStarted){
                        aiIsPlaying = false;
                        autoMove();
                    }

                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });

    }


    //TAIL
    private void loadTail() {
        initializeTailAndLocations();
    }

    private int getMaxTailSquares(){
        return (BoardSettings.columns * BoardSettings.rows) - 1;
    }

    private void initializeTailAndLocations(){
        int maxTails = getMaxTailSquares();
        tail = new Square[maxTails];
        tailSquaresLocations = new Point[maxTails];
    }

    public void addNewTailSquare(){
        numberOfTailSquares++;
    }

    public void moveTailWithHead(Point newTailSquareLocation){


        for(int i = 0; i < numberOfTailSquares-1; i++){



            if(tail[i] == null){
                tail[i] = new Square(newTailSquareLocation, new Dimension(BoardSettings.fieldWidth, BoardSettings.fieldHeight), BoardSettings.color3);


                panel.add(tail[i]);
            }
            if(tail[i+1] == null){
                tail[i+1] = new Square(newTailSquareLocation, new Dimension(BoardSettings.fieldWidth, BoardSettings.fieldHeight), BoardSettings.color3);
                panel.add(tail[i+1]);
            }

            tail[i].setPosition(tail[i + 1].getPosition());



        }
        if(tail[numberOfTailSquares-1] == null){

            tail[numberOfTailSquares-1] = new Square(newTailSquareLocation, new Dimension(BoardSettings.fieldWidth, BoardSettings.fieldHeight), BoardSettings.color3);

            panel.add(tail[numberOfTailSquares-1]);

        } else {
            tail[numberOfTailSquares-1].setPosition(newTailSquareLocation);

        }

    }


}
