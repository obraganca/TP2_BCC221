package com.termo.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.text.Normalizer;

public class DataSourceModel {
    private String filename;
    private String word;
    Vector<String> palavras;
    private Map<String, String> normalizedToOriginal; // normalizada -> original

    public DataSourceModel(String path){
        filename = path;
        palavras = new Vector<>();
        normalizedToOriginal = new HashMap<>();
        setWord(processingData());
    }

    public String processingData(){
        try (InputStream in = Files.newInputStream(Paths.get(filename));
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                palavras.add(trimmed);
                String key = normalize(trimmed).toLowerCase();
                // só mantém a primeira ocorrência do normalizado -> original
                normalizedToOriginal.putIfAbsent(key, trimmed);
            }

            if (palavras.isEmpty()) {
                throw new RuntimeException("Nenhuma palavra encontrada em " + filename);
            }

            Random random = new Random();
            return palavras.get(random.nextInt(palavras.size()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifica se a palavra existe no dicionário considerando normalização (acentos/ç ignorados).
     */
    public Boolean searchWord(String word){
        if (word == null) return false;
        String norm = normalize(word).toLowerCase();
        return normalizedToOriginal.containsKey(norm);
    }

    /**
     * Retorna a forma original (do arquivo) que corresponde à palavra dada (considerando normalização).
     * Ex.: "cafe" -> pode retornar "café" se estiver no datasource.
     * Retorna null se não encontrar.
     */
    public String getCanonicalWord(String word) {
        if (word == null) return null;
        String norm = normalize(word).toLowerCase();
        return normalizedToOriginal.get(norm);
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    // Normaliza removendo marcas diacríticas (acentos) e cedilha -> transforma ç em c, á em a, etc.
    private String normalize(String s) {
        if (s == null) return null;
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{M}", "");
        return n;
    }
}
