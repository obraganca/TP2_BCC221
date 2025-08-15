package com.termo.model;

import com.termo.gui.GameWindow;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.nio.file.Files;

import static java.lang.System.in;


public class DataSourceModel {
    private String filename = "datasource.txt";
    private String word;

    public DataSourceModel(){
        setWord(processingData());
    }


    public String processingData(){
        try (InputStream in = DataSourceModel.class.getResourceAsStream("/" + filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

                Vector<String> palavras = new Vector<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    palavras.add(line);
                }

                Random random = new Random();
                return palavras.get(random.nextInt(palavras.size()));
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
