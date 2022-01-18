package com.example.ebsa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import android.os.Vibrator;

public class MainActivity extends AppCompatActivity {
    private TextView textview=null;
    private TextView textview2=null;
    private int var=0;
    private MediaPlayer mp = null;
    private Button button=null;
    private String command_text;
    BluetoothAdapter bluetoothAdapter=null;
    TextToSpeech textToSpeech=null;
    BluetoothDevice bluetoothDevice=null;
    BluetoothSocket bluetoothSocket=null;
    int bluetoothSocket_counter=0;
    static TextView textview1;
    BluetoothSocket bluetoothSocket_2_after_conneciton_is_established=null;
    boolean con_status=false;
    Vibrator vibrator=null;
    private final int DATATYPE_VIBRATION=2;
    private int data_visual_type=2;
    private int data_max_value=50;
    private Thread_check_socket_status thread = new Thread_check_socket_status();


    // la methode pour demmarer l'activite et au meme temps
    // super.onCreate(savedInstanceState); est pour sevaugarder les changements
    // Build.VERSION_CODES.M = Android M
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textview = (TextView) findViewById(R.id.text_change);
        textview2 = (TextView) findViewById(R.id.textview2);
        textview1=textview;
        button= (Button) findViewById(R.id.button);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    int lang = textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            text_to_Speech("Your device don't support Bluetooth");
        }
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }
    // cette ce le methode qui va executer quand on click sur Button
    public void Changetext(View view) throws InterruptedException {
        var++;
        textview.setText("clicked for "+String.valueOf(var));
        mp = MediaPlayer.create(this, R.raw.click_screen);
        mp.start();
        GetSpeech(view);


    }
    // cette method pour verifier si Android Speech est disponible sur le telephone et passer le resulat a la mathode onActivityResult
    //
    public void GetSpeech(View view){
        Intent intent_speech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent_speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent_speech.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, Locale.getDefault());
       if(intent_speech.resolveActivity(getPackageManager())!=null){
           startActivityForResult(intent_speech,10);
       }else{
           text_to_Speech("Your device don't support Speech input");
       }
    }
    // cette methode est pour determiner le Intent est le resulatat en executant des procedures
    // exemple Intent de textToSpeech si est succee alors on pass le code 10
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    text_to_Speech("Bluetooth is ON");
                }else if(resultCode==RESULT_CANCELED){
                    text_to_Speech("Turning Bluetooth ON Operation is Cancelled");
                }

            break;
            case 10:
                if(resultCode == RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Set_textview(result.get(0).toString());
                    try {
                        Define_commande(result.get(0));
                        command_text=result.get(0);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            break;
        }
    }
    // chaque message recu par cette method android speech il le dire en anglais
    public void text_to_Speech(String speech){
        int s = textToSpeech.speak(speech,TextToSpeech.QUEUE_FLUSH,null);
    }
    // le methode pour changer le contenue de textview
    public void Set_textview(String speech){
        textview.setText(speech);
    }
    // cette method est pour definer la commande et lancer des procedures
    public void Define_commande(String command) throws InterruptedException {
        if(command.toLowerCase(Locale.ROOT).equals("enable bluetooth")){
            if(!bluetoothAdapter.isEnabled()){
                text_to_Speech("Turning ON Bluetooth");
                Thread.sleep(2000);
                bluetoothAdapter.enable();
                text_to_Speech("Bluetooth is Enabled");
            }else{
                text_to_Speech("Bluetooth is Already ON");
            }
        }else if(command.toLowerCase(Locale.ROOT).equals("disable bluetooth")){
            if(!bluetoothAdapter.isEnabled()){
                text_to_Speech("Bluetooth is Already Disabled");
            }else{
                text_to_Speech("Turning Off Bluetooth");
                Thread.sleep(2000);
                bluetoothAdapter.disable();
                text_to_Speech("Bluetooth is Disabled");
            }
        }else if(command.toLowerCase(Locale.ROOT).equals("connect to system")){
            if(!bluetoothAdapter.isEnabled()){
                text_to_Speech("Please Enable Bluetooth");
            }else{
                bluetoothDevice=bluetoothAdapter.getRemoteDevice("00:21:13:05:B5:E2");
                bluetoothSocket_counter=0;
                text_to_Speech("trying to connect to the system");
                Thread.sleep(2000);
                do {
                    try{
                        bluetoothSocket= bluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                        text_to_Speech("Connecting to the system");
                        Thread.sleep(1000);
                        bluetoothSocket.connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bluetoothSocket_counter++;
                }while(!bluetoothSocket.isConnected() && bluetoothSocket_counter<3);


                try{
                    if(bluetoothSocket.isConnected()){
                        con_status=true;
                        text_to_Speech("Successfully Connected");
                        Thread.sleep(1000);
                    }else{
                        con_status=false;
                        text_to_Speech("could not connect to the system, please turn off, then turn on the system and try again");
                        bluetoothSocket.close();// close socket ila masd9atch le connection
                    }

                }catch(IOException e){
                    e.printStackTrace();
                }
            }

        }else if(command.toLowerCase(Locale.ROOT).equals("start system")){
            if(con_status){
                thread = new Thread_check_socket_status(bluetoothSocket,handler_change_text,vibrator);
                thread.start();
            }else{
                text_to_Speech("Please Connect to the system first");
            }
        }else if(command.toLowerCase(Locale.ROOT).toLowerCase(Locale.ROOT).contains("set max")){
            boolean check_max_value=true;
            if(command.substring(8).matches("\\d+(?:\\.\\d+)?")){
                try{
                    data_max_value=Integer.valueOf(command.substring(8));
                }catch (Exception e){
                    // if we didn't captured the exact value for example he said "change max bla" bla is not a number
                    text_to_Speech("Error Try again");
                    check_max_value=false;
                }
                if(check_max_value){
                    text_to_Speech("Max value Seuccefully Changed");
                }
            }

        }else if(command.toLowerCase(Locale.ROOT).equals("change to vibration")){
            data_visual_type=2;
            text_to_Speech("Seuccefully Changed to vibration mode");

        }else if(command.toLowerCase(Locale.ROOT).equals("change to speak")){
            data_visual_type=1;
            text_to_Speech("Seuccefully Changed to speaking mode");

        }else {
            text_to_Speech("i can't define your command");
        }
    }
    //cette method est pour activer Bluetooth sans autorisation de l'utilisateur
    public void activate_bluetooth(){
        bluetoothAdapter.enable();
        text_to_Speech("Bluetooth is Enabled");

    }
    // ce Handler est utilise pour faire des modification a le textView dans une Thread
    Handler handler_text_view = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message message) {
            textview.setText("the value is "+message);
            return false;
        }
    });
    Handler handler_change_text = new Handler();
    Handler handler_text_to_Spech = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            textview.setText(message.arg1);
            return false;
        }
    });
    // cette Class est la classe principal pour tous qui est concerne Bluetooth
    private class Thread_check_socket_status extends Thread{
        private BluetoothSocket socket;
        private Handler handler;
        private OutputStream outputStream;
        private InputStream inputStream;
        private Vibrator vibrator;
        public Thread_check_socket_status(){

        }
        public Thread_check_socket_status(BluetoothSocket socket,Handler handler,Vibrator vibrator){
            this.socket=socket;
            this.handler=handler;
            this.vibrator=vibrator;
        }


        // method pour la vibration avec la detection de SDK_INT
        // Build.VERSION_CODES.O c'est a dire SDK 26 ou on peut dire Android 8
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
        // le vibration si SDK est inferieur a 26
        void vibrate_function_less_than_26(int input){
            handler.post(new Runnable(){
                public void run() {
                    vibrator.vibrate(input);

                }
            });
        }
        // le vibration si SDK est superieur a 26
        void vibrate_function_greater_than_26(int input){
            handler.post(new Runnable(){
                @RequiresApi(api = Build.VERSION_CODES.O)
                public void run() {
                    vibrator.vibrate(VibrationEffect.createOneShot(input,VibrationEffect.DEFAULT_AMPLITUDE));

                }
            });
        }

        // la methode run qui va executer directement apres qu'on appelle la methode start()
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

                            int[] int_array=new int[3];
                            if(available<2){
                                int_array[0]=Character.getNumericValue((char)bytes[0]);

                                handler.post(new Runnable(){
                                    public void run() {
                                        textview.setText(String.valueOf(String.valueOf(int_array[0])));
                                    }
                                });
                                if(command_text.toLowerCase(Locale.ROOT).equals("change to vibration")){
                                    show_data_vibration(int_array[0]);
                                }else if(command_text.toLowerCase(Locale.ROOT).equals("change to speak")){
                                    text_to_Speech(String.valueOf(int_array[0]));
                                }


                            }else if(available<3){
                                int_array[0]=Character.getNumericValue((char)bytes[0]);
                                int_array[1]=Character.getNumericValue((char)bytes[1]);
                                int_array[0]=int_array[0]*10+int_array[1];

                                handler.post(new Runnable(){
                                    public void run() {
                                        textview.setText(String.valueOf(String.valueOf(int_array[0])));
                                    }
                                });
                                if(command_text.toLowerCase(Locale.ROOT).equals("change to vibration")){
                                    show_data_vibration(int_array[0]);
                                }else if(command_text.toLowerCase(Locale.ROOT).equals("change to speak")){
                                    text_to_Speech(String.valueOf(int_array[0]));
                                }
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
                                if(command_text.toLowerCase(Locale.ROOT).equals("change to vibration")){
                                    show_data_vibration(int_array[0]);
                                }else if(command_text.toLowerCase(Locale.ROOT).equals("change to speak")){
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


}