package model;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by pawel on 07.06.16.
 */
public class WordsToFile implements ExportWords{

    String extension, from, to, fileName, path, dirPath;
    boolean whatToDo=false;
    File file;
    HashMap<String, String> mapa;
    public WordsToFile(String from, String to, String extension){
        this.extension = extension;
        this.from = from;
        this.to = to;
        fileName = from+'_'+to+'.'+extension;
        String userDirectory = Paths.get(".").toAbsolutePath().normalize().toString();

        dirPath = userDirectory+"/SavedWords/";
        path = dirPath+fileName;
      //  System.out.println(path + " "+whatToDo);
        mapa = new HashMap<>();
        createFile();
//        System.out.println(path + " "+whatToDo);
        if(!whatToDo){
            readFromFile();
        }
    }
    @Override
    public void addWord(String word, String translation){
        mapa.put(word, translation);
    }
    @Override
    public void export(){
        FileWriter fw = null;
        boolean czy = false;
        try {
            fw = new FileWriter(file);
            czy=true;
        }
        catch(Exception e){
            czy = false;
            e.printStackTrace();
        }
//        System.out.println(czy+" "+mapa.size());
        if(czy) {
            Iterator it = mapa.entrySet().iterator();
            StringBuilder nowy = new StringBuilder();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String para = pair.getKey() + "; " + pair.getValue() + "\n";
            //    System.out.println(para);
                nowy.append(para);
                it.remove();
            }
            try {
                fw.write(nowy.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                fw.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
//        System.out.println("zupa mleczna");
    }

    public void createFile(){
        new File(dirPath).mkdirs();
        file = new File(path);
        try{
            whatToDo = file.createNewFile();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void readFromFile(){
//        System.out.println("cos");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            try{
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while(line != null){
                    int pos = line.indexOf(';');
                    mapa.put(line.substring(0, pos), line.substring(pos+2));
                    line = br.readLine();
                }

            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally {
                br.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
