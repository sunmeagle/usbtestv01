package com.example.administrator.usbtestv01.tools;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by Administrator on 2016/6/21 0021.
 */
public class SaveException {


    public static void saveExceptionMsg(Exception e){
        PrintStream newOut = null ;
        try {
            newOut = new PrintStream(new BufferedOutputStream(new FileOutputStream("/sdcard/USBLog.txt",true)));

            e.printStackTrace(newOut) ;

            newOut.close();
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();

        }
    }
}
