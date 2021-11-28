package org.rowland.mountainClimber;

import android.content.Context;

import com.google.android.gms.tasks.RuntimeExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

class Utils {
    private static final String MOUNTAINS_FILENAME = "ADK46";
    static void saveMountains(Context applicationContext, Map<Integer,Mountain> mountainList) {
        try {
            File mountainfile = new File(applicationContext.getFilesDir(), MOUNTAINS_FILENAME);
            if (!mountainfile.exists()) {
                mountainfile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(mountainfile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mountainList);
            oos.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeExecutionException(e);
        }
    }

    static Map<Integer,Mountain> loadMountains (Context applicationContext) {
        try {
            File mountainfile = new File(applicationContext.getFilesDir(), MOUNTAINS_FILENAME);
            if (mountainfile.exists()) {
                FileInputStream fis = new FileInputStream(mountainfile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Map<Integer,Mountain> mountainList = (Map) ois.readObject();
                ois.close();
                fis.close();
                return mountainList;
            }
            Map<Integer,Mountain> mountainList = new HashMap<>();
            String[] stringArray = applicationContext.getResources().getStringArray(R.array.ADK_46);
            for (String s: stringArray) {
                Mountain m = new Mountain(s);
                mountainList.put(m.id, m);
            }
            return mountainList;
        }
        catch (IOException e) {
            throw new RuntimeExecutionException(e);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeExecutionException(e);
        }
    }

    static void deleteMountainFile(Context applicationContext) {
        try {
            Files.deleteIfExists((new File(applicationContext.getFilesDir(), MOUNTAINS_FILENAME)).toPath());
        } catch (IOException e) {
            throw new RuntimeExecutionException(e);
        }
    }

    static String getRankString(int rank) {
        int modRank = rank % 10;
        if ((modRank) == 2) {
            if ( rank == 12) {
                return rank + "th highest mountain";
            }
            return (rank) + "nd highest mountain";
        }
        else if (modRank == 3) {
            if ( rank == 13) {
                return rank + "th highest mountain";
            }
            return rank + "rd highest mountain";
        }
        else if (modRank == 1) {
            if (rank ==1) {
                return "Tallest Mountain";
            }
            else if (rank == 11) {
                return "11th highest mountain";
            }
            else {
                return (rank + "st highest mountain");
            }
        }
        else {
            return(rank + "th highest mountain");
        }
    }

}
