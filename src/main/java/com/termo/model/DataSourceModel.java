package com.termo.model;

import com.termo.gui.GameWindow;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.Scanner;

public class DataSourceModel {
    private String filename = "datasource.txt";
    private String word;

    public DataSourceModel(){
        setWord(processingData());
    }


    public String processingData(){
        try {
            File myFile = new File(DataSourceModel.class.getResource("/"+filename).toURI());
            RandomAccessFile data = new RandomAccessFile(myFile, "r");
            String text = data.readLine();
            data.close();
            return text;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean searchWord(String word){
        try {
            File myFile = new File(DataSourceModel.class.getResource("/"+filename).toURI());
            RandomAccessFile data = new RandomAccessFile(myFile, "r");
            String line;
            while ((line = data.readLine()) != null) {
                if(line.toUpperCase().equals(word)){
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
