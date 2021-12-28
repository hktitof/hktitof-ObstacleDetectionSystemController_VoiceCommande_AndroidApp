package com.example.ebsa;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView textview=null;
    private int var=0;
    private MediaPlayer mp = null;
    private Button button=null;
    BluetoothAdapter bluetoothAdapter=null;
    TextToSpeech textToSpeech=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textview = (TextView) findViewById(R.id.text_change);
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
    }

    public void Changetext(View view){
        var++;
        textview.setText("clicked for "+String.valueOf(var));
        mp = MediaPlayer.create(this, R.raw.click_screen);
        mp.start();
        GetSpeech(view);
    }
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
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            break;
        }
    }

    public void text_to_Speech(String speech){
        int s = textToSpeech.speak(speech,TextToSpeech.QUEUE_FLUSH,null);
    }
    public void Set_textview(String speech){
        textview.setText(speech);
    }
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
        } else{
            text_to_Speech("i can't define your command");
        }
    }
    //activate_bluetooth not used yet
    public void activate_bluetooth(){
        bluetoothAdapter.enable();
        text_to_Speech("Bluetooth is Enabled");
//        Intent intent_bluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        startActivityForResult(intent_bluetooth,1);
    }

}