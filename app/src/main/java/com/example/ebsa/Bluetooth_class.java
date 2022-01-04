package com.example.ebsa;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Vibrator;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Bluetooth_class extends Thread{
    private BluetoothSocket socket;
    private Handler handler;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Vibrator vibrator;
    private TextView textview;
    private boolean check_con_status;
    private int[] int_array=new int[3];
    private MainActivity main = new MainActivity();
    private Handler handler_change_text = new Handler();
    private int available;
    byte[] bytes;
    public Bluetooth_class(){

    }
    public Bluetooth_class(BluetoothSocket socket, TextView textview, Vibrator vibrator){
        this.socket=socket;
        this.textview=textview;
        this.vibrator=vibrator;

    }

    public void run(){
        check_con_status=true;
        while(check_con_status){
            try{
                outputStream = socket.getOutputStream();
                outputStream.write(48);
            }catch (IOException exception) {
                check_con_status=false;
            }
            if(check_con_status){
                try{
                    inputStream=socket.getInputStream();
                    while(inputStream.available() == 0) {
                        inputStream = socket.getInputStream();
                    }
                    available = inputStream.available();
                    bytes = new byte[available];
                    inputStream.read(bytes, 0, available);
                    get_data_int_and_display();
                }catch (IOException e){
                    handler_change_text.post(new Runnable(){
                        public void run() {
                            textview.setText("Connection Lost");

                        }
                    });
                }


            }else{
                main.text_to_Speech("Connection has been Lost");
                try {

                    inputStream.close();
                    outputStream.close();
                    socket.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

        }

    }


    public void get_data_int_and_display(){
        if(available<2){
            int_array[0]=Character.getNumericValue((char)bytes[0]);
            handler.post(new Runnable(){
                public void run() {
                    textview.setText(String.valueOf(String.valueOf(int_array[0])));
                }
            });

        }else if(available<3){
            int_array[0]=Character.getNumericValue((char)bytes[0]);
            int_array[1]=Character.getNumericValue((char)bytes[1]);
            int_array[0]=int_array[0]*10+int_array[1];

            handler.post(new Runnable(){
                public void run() {
                    textview.setText(String.valueOf(String.valueOf(int_array[0])));
                }
            });
        }else{
            int_array[0]=Character.getNumericValue((char)bytes[0]);
            int_array[1]=Character.getNumericValue((char)bytes[1]);
            int_array[2]=Character.getNumericValue((char)bytes[2]);
            int_array[0]=int_array[0]*100+int_array[1]*10+int_array[2];
            handler.post(new Runnable(){
                public void run() {
                    textview.setText(String.valueOf(String.valueOf(int_array[0])));
                }
            });
        }
    }



}
