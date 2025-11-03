package com.studenthub;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.DefaultListModel;

public class Utils {
    public static final String DATA_FOLDER = "data";
    public static final String UPLOADS = DATA_FOLDER + File.separator + "uploads";

    public static void ensureAppFolders(){
        try{
            Files.createDirectories(Paths.get(DATA_FOLDER));
            Files.createDirectories(Paths.get(UPLOADS));
            // seed files
            File users = new File(DATA_FOLDER, "users.txt"); if(!users.exists()) users.createNewFile();
            File assignments = new File(DATA_FOLDER, "assignments.txt"); if(!assignments.exists()) assignments.createNewFile();
            File results = new File(DATA_FOLDER, "results.csv"); if(!results.exists()) results.createNewFile();
            File expenses = new File(DATA_FOLDER, "expenses.csv"); if(!expenses.exists()) expenses.createNewFile();
            File todo = new File(DATA_FOLDER, "todo.txt"); if(!todo.exists()) todo.createNewFile();
        }catch(Exception ex){ ex.printStackTrace(); }
    }

    public static File getUploadsFolder(){ return new File(UPLOADS); }

    public static boolean createUser(String username, String password){
        try{
            File f = new File(DATA_FOLDER, "users.txt");
            List<String> ls = readAllLines(f);
            for(String l: ls){ if(l.split(":")[0].equals(username)) return false; }
            appendLine(f, username+":"+password);
            return true;
        }catch(Exception ex){ ex.printStackTrace(); return false; }
    }

    public static boolean checkCredentials(String username, String password){
        try{
            File f = new File(DATA_FOLDER, "users.txt");
            List<String> lines = readAllLines(f);
            for(String l: lines){ if(l.trim().isEmpty()) continue; String[] p = l.split(":"); if(p.length>=2 && p[0].equals(username) && p[1].equals(password)) return true; }
            return false;
        }catch(Exception ex){ ex.printStackTrace(); return false; }
    }

    public static List<String> readAllLines(File f){
        try{
            if(!f.exists()) return new ArrayList<>();
            return Files.readAllLines(f.toPath());
        }catch(Exception ex){ ex.printStackTrace(); return new ArrayList<>(); }
    }

    public static void appendLine(File f, String line){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))){ bw.write(line); bw.newLine(); }
        catch(Exception ex){ ex.printStackTrace(); }
    }

    public static void writeStringToFile(File f, String s){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))){ bw.write(s); }
        catch(Exception ex){ ex.printStackTrace(); }
    }

    public static void saveCollectionToFile(DefaultListModel<String> m, File f){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))){
            for(int i=0;i<m.size();i++){ bw.write(m.get(i)); bw.newLine(); }
        }catch(Exception ex){ ex.printStackTrace(); }
    }
}
