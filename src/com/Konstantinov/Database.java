package com.Konstantinov;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class Database {
    public String host,user,password;
    public Database(String host, String user, String password)
    {
        this.host = host;
        this.user = user;
        this.password = password;
    }
    public void Database(String name,int score)
    {
        try {
            String sql = String.format("INSERT INTO player(Name,Score) VALUES('%s,%d')", name, score);
            Statement st = connection.createStatement();
            st.executeUpdate(sql);
            st.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public ArrayList<String> getRecords(){
        ArrayList<String> result = new ArrayList<String>();
        try{
            Statement st = connection.createStatement();
            ResultSet res = st.executeQuery("SELECT * FROM player");
            while(res.next()){
                int score = res.getInt(3);
                String name = res.getString(2);
                String date = res.getString(4);

                result.add(name + "at" +date);
            }
        }
        catch (Exception e){}
    }
    public void init() {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            Connection  = DriverManager.getConnection(host, user, password);
            System.out.println("Database has been created!");

        }
        catch(Exception ex){
            System.out.println("Connection failed...");

            System.out.println(ex);
        }
    }
}
