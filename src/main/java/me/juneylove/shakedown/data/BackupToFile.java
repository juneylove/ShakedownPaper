package me.juneylove.shakedown.data;

import java.io.*;

public class BackupToFile implements Serializable {

    public static boolean SaveObject(Object obj, String filename) {

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            ObjectOutputStream objectOutputStream;
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    public static Object LoadObject(String filename) {

        try {
            FileInputStream fileInputStream = new FileInputStream(filename);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object result = objectInputStream.readObject();
            objectInputStream.close();
            return result;
        } catch (Exception e) {
            return null;
        }

    }

}
