package com.Konstantinov;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;
import java.util.ArrayList;


public class GameWindows extends JFrame {
    private static GameWindows game_windows;
    private static  long last_frame_time;
    private static Image background;
    private static Image gameover;
    private static Image drop;
    private static Image restart;
    private static float drop_left = 200;
    private static float drop_top = -100;
    private static float drop_v = 200;
    private static int direction = -1;
    private static int score;
    private static boolean end;
    private static float drop_width= 100;
    private static float drop_height= 152;
    private static boolean pause = false;
    private static  float drop_speed_save ;
    private static double mouserecordX = 0;
    private static double mouserecordY = 0;

    private static Entry nameEntry;
    private static Database db;

    private static boolean isRecorded = false;
    public static boolean drawRecords = false;
    private static ArrayList<String> recordsList = new ArrayList<String>();


    public static void main(String[] args) throws IOException {
        db = new Database("jdbc:mysql://localhost/gamedrop?useLegacyDatetimeCode=false&serverTimezone=Europe/Helsinki","root","");
        db.init();
        background= ImageIO.read(GameWindows.class.getResourceAsStream("background.png"));
        drop= ImageIO.read(GameWindows.class.getResourceAsStream("drop.png")).getScaledInstance((int) drop_width, (int) drop_height, Image.SCALE_DEFAULT);
        gameover= ImageIO.read(GameWindows.class.getResourceAsStream("gameover.png")).getScaledInstance(300,200, Image.SCALE_DEFAULT);
        restart= ImageIO.read(GameWindows.class.getResourceAsStream("restart.jpg")).getScaledInstance(64,64, Image.SCALE_DEFAULT);
        game_windows = new GameWindows();
        game_windows.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);/*Создание обьекта и конец программы*/
        game_windows.setLocation(200,100);
        game_windows.setSize(977,579);
        game_windows.setResizable(false);
        last_frame_time = System.nanoTime();
        GameField game_field = new GameField();
        game_field.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);

            }
        });

        game_field.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3){
                    if(pause) {
                        pause = false;
                        drop_v = drop_speed_save;
                    }
                    else {
                        drop_speed_save = drop_v;
                        drop_v=0;
                        mouserecordX = MouseInfo.getPointerInfo().getLocation().getX();
                        mouserecordY = MouseInfo.getPointerInfo().getLocation().getY();
                        pause = true;
                    }
                    try{
                        Robot r = new Robot();
                        r.mouseMove((int)mouserecordX,(int)mouserecordY);
                    }
                    catch (AWTException ee){

                    }


                }
                if(e.getButton()== MouseEvent.BUTTON1){
                    if(pause) return;

                    int x = e.getX();
                    int y = e.getY();


                    float drop_right = drop_left + drop.getWidth(null);
                    float drop_bottom = drop_top + drop.getHeight(null);
                    boolean is_drop = x >= drop_left && x <= drop_right && y >= drop_top && y <= drop_bottom;
                    if(is_drop){
                        if (drop_height > 25 && drop_width > 50){
                            drop_width = drop_width -1;
                            drop_height = drop_height -2;
                            try{
                                dropResize();
                            }
                            catch (IOException ioe){

                            }

                        }
                        drop_top = -100;
                        drop_left = (int) (Math.random() * (game_field.getWidth() - drop.getWidth(null)));
                        drop_v = drop_v + 20;
                        score ++;
                        onDirection();
                        game_windows.setTitle("Score: " + score);

                    }
                    if (end){
                        boolean isRestart = x>=250 && x <=250 + restart.getWidth(null) && y>= 200 && y<= 200 + restart.getHeight( null);
                        if(isRestart){
                            end = false;
                            score = 0;
                            game_windows.setTitle("Score" + score);
                            drop_top = -100;
                            drop_left= (int) (Math.random()*(game_field.getWidth()-drop.getWidth(null)));
                            drop_v=200;

                            drop_width= 100;
                            drop_height= 152;
                            isRecorded = false;
                            drawRecords = true;
                        }
                    }
                }
                }

        });
        nameEntry = new Entry();
        game_windows.addKeyListener(new KeyAdapter()
        {
            @Override
            public void  keyTyped(KeyEvent e){

            }
            @Override
            public void  keyPressed(KeyEvent e){
                nameEntry.keyPress(e);
                if (nameEntry.isActive && !isRecorded){
                    if(e.getKeyCode() == KeyEvent.VK_ENTER){
                        db.addRecord(nameEntry.text, score);
                        isRecorded = true;
                        recordsList = db.getRecords();
                        drawRecords = true;

                    }
                }
            }
            @Override
            public void  keyReleased(KeyEvent e){

            }
        });



        game_windows.add(game_field);
        game_windows.setVisible(true);

    }
    private static  void dropResize() throws IOException{
       drop = ImageIO.read(GameWindows.class.getResourceAsStream("drop.png")).getScaledInstance((int) drop_width, (int) drop_height, Image.SCALE_DEFAULT);

    }
    private static int onDirection(){
        int rand = (int)(Math.random()*2+1 );
        if (rand == 2) direction = 1;
        else direction = -1;
        //System.out.print(1);

        return direction;
    }
    private static void onRepaint(Graphics g){
        /*g.fillOval(10,10,200,100);
        g.setColor(Color.blue);
        g.drawLine(100,140,100,100);*/
        long current_time= System.nanoTime();
        float delta_time = (current_time - last_frame_time) * 0.000000001f;
        last_frame_time= current_time;
        drop_top = drop_top + drop_v * delta_time;
        drop_left =drop_left + (direction * drop_v)* delta_time;
        g.drawImage(background,0,0,null);
        g.drawImage(drop, (int) drop_left,(int) drop_top,null);

        if (drop_top > game_windows.getHeight())
        {
            g.drawImage(gameover, 280, 120, null);
            g.drawImage(restart, 250, 200, null);
            end = true;
        }
        if(drop_left <= 0.0 ||drop_left + drop_width > game_windows.getWidth()) {
            if(direction == -1) direction = 1;
            else direction=-1;
        }
        if (drawRecords)
        {
            for (int i = 0; i < recordsList.size(); i++)
            {
                g.drawString(recordsList.get(i), 200, 25 + 25 * i);
                g.setColor(new Color(255,255,255));
            }
        }
        nameEntry.isActive = end;
        nameEntry.update(g);
        /*
        g.drawImage(kaplya,0,0,null);
        g.drawImage(gameover,0,0,null);*/


    }
    private static class GameField extends JPanel{
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponents(g);
            onRepaint(g);
            repaint();
        }
    }
    private static void PaintPause(Graphics g){
        g.drawImage(gameover, 280, 120, null);
    }

}
