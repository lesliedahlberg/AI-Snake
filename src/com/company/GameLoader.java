package com.company;

/**
 * Created by lesliedahlberg on 22/07/14.
 */
public class GameLoader {

    BoardSettings boardSettings = new BoardSettings();

    Grid grid;

    GameLoader(){
        grid = new Grid(boardSettings);
    }



}
