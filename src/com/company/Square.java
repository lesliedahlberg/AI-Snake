package com.company;

import javax.swing.*;
import java.awt.*;

/**
 * Created by lesliedahlberg on 22/07/14.
 */
public class Square extends JPanel {
    Point position;



    public Square(Point point, Dimension dimension, Color color){
        Dimension d = new Dimension((int) dimension.getWidth() - 1, (int) dimension.getHeight() - 1);
        setPosition(point);
        setSize(d);
        setBackground(color);
        setLayout(null);

    }

    public void setPosition(Point point){
        position = point;
        setLocation(position);
        repaint();
    }

    public Point getPosition(){
        return position;
    }



}
