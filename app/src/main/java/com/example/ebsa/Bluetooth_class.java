package com.example.ebsa;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class Bluetooth_class extends Thread{
    private BluetoothSocket socket;
    private Handler handler;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Vibrator vibrator;
    private String message_vocal



    public Thread_check_socket_status(){

    }
    public Thread_check_socket_status(BluetoothSocket socket,Handler handler,Vibrator vibrator){
        this.socket=socket;
        this.handler=handler;
        this.vibrator=vibrator;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    void define_and_show_data(int data_type, int input) throws InterruptedException {
        if(data_type == 1){
            text_to_Speech(String.valueOf(input));
            if(input<10){
                this.sleep(2000);
            }else if(input<99){
                this.sleep(3000);
            }else{
                this.sleep(3500);
            }

        }else if(data_type == 2){
            show_data_vibration(input);
            Thread.sleep(500);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    void show_data_vibration(int input_data) throws InterruptedException {
        boolean vibrate_version=true;
        if(Build.VERSION.SDK_INT>=26){
            vibrate_version=true;
        }else{
            vibrate_version=false;
        }
        if(input_data<=data_max_value){
            int single_layer = data_max_value/5;
            if(input_data<single_layer){
                if(vibrate_version){
                    vibrate_function_greater_than_26(1000);
                }else {
                    vibrate_function_less_than_26(1000);
                }

            }else if(input_data<single_layer*2){
                if(vibrate_version){
                    vibrate_function_greater_than_26(600);
                }else {
                    vibrate_function_less_than_26(600);
                }
                Thread.sleep(500);
            }else if(input_data<single_layer*3){
                if(vibrate_version){
                    vibrate_function_greater_than_26(500);
                }else {
                    vibrate_function_less_than_26(500);
                }
                Thread.sleep(1000);
            }else if(input_data<single_layer*4){
                if(vibrate_version){
                    vibrate_function_greater_than_26(300);
                }else {
                    vibrate_function_less_than_26(300);
                }
                Thread.sleep(1500);
            }else if(input_data<single_layer*5){
                if(vibrate_version){
                    vibrate_function_greater_than_26(100);
                }else {
                    vibrate_function_less_than_26(100);
                }
                Thread.sleep(2000);
            }
        }
    }
    void vibrate_function_less_than_26(int input){
        handler.post(new Runnable(){
            public void run() {
                vibrator.vibrate(input);

            }
        });
    }
    void vibrate_function_greater_than_26(int input){
        handler.post(new Runnable(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {
                vibrator.vibrate(VibrationEffect.createOneShot(input,VibrationEffect.DEFAULT_AMPLITUDE));

            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void run(){
        boolean check_status=true;
        String string2 = "";
        while(check_status){

            try {
                outputStream = socket.getOutputStream();
                outputStream.write(48);
            } catch (IOException exception) {
                check_status=false;

            }
            if(check_status){
                try {
                    inputStream = socket.getInputStream();
                    while(inputStream.available() == 0) {
                        inputStream = socket.getInputStream();
                    }
                    int available = inputStream.available();
                    byte[] bytes = new byte[available];
                    inputStream.read(bytes, 0, available);
                    string2 = new String(bytes);
//                    show_data(Integer.parseInt(string2));
//                    int received_data=Integer.parseInt(string2);
                    int[] int_array=new int[3];
                    if(available<2){
                        int_array[0]=Character.getNumericValue((char)bytes[0]);
//                        define_and_show_data(data_visual_type,int_array[0]);
                        handler.post(new Runnable(){
                            public void run() {
                                textview.setText(String.valueOf(String.valueOf(int_array[0])));
                            }
                        });
                        if(latifa_command.toLowerCase(Locale.ROOT).equals("change to vibration")){
                            show_data_vibration(int_array[0]);
                        }else if(latifa_command.toLowerCase(Locale.ROOT).equals("change to speak")){
                            text_to_Speech(String.valueOf(int_array[0]));
                        }



                    }else if(available<3){
                        int_array[0]=Character.getNumericValue((char)bytes[0]);
                        int_array[1]=Character.getNumericValue((char)bytes[1]);
                        int_array[0]=int_array[0]*10+int_array[1];
//                        define_and_show_data(data_visual_type,int_array[0]);
                        handler.post(new Runnable(){
                            public void run() {
                                textview.setText(String.valueOf(String.valueOf(int_array[0])));
                            }
                        });
                        if(latifa_command.toLowerCase(Locale.ROOT).equals("change to vibration")){
                            show_data_vibration(int_array[0]);
                        }else if(latifa_command.toLowerCase(Locale.ROOT).equals("change to speak")){
                            text_to_Speech(String.valueOf(int_array[0]));
                        }
                    }else{
                        int_array[0]=Character.getNumericValue((char)bytes[0]);
                        int_array[1]=Character.getNumericValue((char)bytes[1]);
                        int_array[2]=Character.getNumericValue((char)bytes[2]);
                        int_array[0]=int_array[0]*100+int_array[1]*10+int_array[2];
//                        define_and_show_data(data_visual_type,int_array[0]);
                        handler.post(new Runnable(){
                            public void run() {
                                textview.setText(String.valueOf(String.valueOf(int_array[0])));
                            }
                        });
                        if(latifa_command.toLowerCase(Locale.ROOT).equals("change to vibration")){
                            show_data_vibration(int_array[0]);
                        }else if(latifa_command.toLowerCase(Locale.ROOT).equals("change to speak")){
                            text_to_Speech(String.valueOf(int_array[0]));
                        }
                    }
                    inputStream.read();
                } catch (IOException | InterruptedException exception) {
                    check_status=false;
                    handler.post(new Runnable(){
                        public void run() {
                            textview.setText("Connection Lost");

                        }
                    });
                    text_to_Speech("Warning, Connection to the system has been lost.");
                }

            }else{
                text_to_Speech("Warning, Connection to the system has been lost.");
            }



        }
        try {
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException exception) {
            handler.post(new Runnable(){
                public void run() {
                    textview.setText(String.valueOf("socket and IO close Exception"));
                }
            });
        }

    }

}
