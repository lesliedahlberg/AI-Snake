package com.company;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lesliedahlberg on 22/07/14.
 */
public class Block {
    Square[] block;
    int blockIndex;
    JPanel panel;
    Point[][] fieldCoordinates;
    Point[] points;
    ScheduledExecutorService executor;

    public Block(Point[][] fieldCoordinates, JPanel panel) {
        this.fieldCoordinates = fieldCoordinates;
        blockIndex = 0;
        this.panel = panel;

        renderBlocks();

    }

    public void startBlockAddingClock(){
        Runnable newBlocks = new Runnable() {
            @Override
            public void run() {
                newRandomBlock();
            }
        };

        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(newBlocks, 0, 200, TimeUnit.MILLISECONDS);
    }

    public void stopBlockClock(){
        executor.shutdown();
    }

    private Point newRandomPoint(){
        int columns = BoardSettings.columns;
        int rows = BoardSettings.rows;

        Random random = new Random();
        int x = random.nextInt(columns);
        int y = random.nextInt(rows);

        Point point = fieldCoordinates[x][y];

        return point;
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public void newRandomBlock(){

        block[blockIndex] = new Square(newRandomPoint(), new Dimension(BoardSettings.fieldWidth, BoardSettings.fieldHeight), BoardSettings.color2);
        panel.add(block[blockIndex]);
        panel.repaint();
        blockIndex++;
    }


    private void renderBlocks(){
        block = new Square[BoardSettings.columns * BoardSettings.rows];
        startBlockAddingClock();
    }


    public Square[] getBlocks(){
        return block;
    }

    public void deleteBlock(int blockID){
        block[blockID].setVisible(false);
        block[blockID] = null;
    }
}
