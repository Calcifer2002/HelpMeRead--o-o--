package com.example.helpmeread__o_o__;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button button_capture, button_copy, button_speak;
    TextToSpeech TTS;
    TextView data,SizerNo;
    Bitmap bitmap;
    SeekBar Sizer;
    int TextSize= 20;
    private  static  final int REQUEST_CAMERA=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_capture = findViewById(R.id.capture);
        button_copy= findViewById(R.id.copy);
        button_speak = findViewById(R.id.speak);
        data = findViewById(R.id.data);
        Sizer = findViewById(R.id.Sizer);
        SizerNo = findViewById(R.id.SizerNo);
        data.setTextSize(TextSize);// default sp.
        SizerNo.setText(Sizer.getProgress() + "/" + Sizer.getMax());


        Sizer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int i2 =0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextSize= TextSize + (i- i2); // i 1 i2 0 so increase of 1. then i2 becomes the same as i in round 2 so if i becomes 2 then i2 is one so again an increase of 1. So the increase in size would be smaller and managable aka reducable otherwise it keeps on increasing even when reduced. for ex if after i becomes 2 we wanna reduce, so after second round i2 becomes 2 and if we reduce i to 1 there will be a -1 change hence reducing size.
                i2= i;

                data.setTextSize(TextSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SizerNo.setText(i2+ "/" + Sizer.getMax());// i from onPchanged (i is the progress here
            }
        });
        TTS= new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i==TextToSpeech.SUCCESS){
                    int result= TTS.setLanguage(Locale.ENGLISH);

                    if(result== TextToSpeech.LANG_MISSING_DATA || result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        button_speak.setEnabled(true);
                    }

                }
                else {
                    Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
                }

            }
        });



        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){ //Checking for permission.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            },REQUEST_CAMERA);
        }

        button_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
                Toast.makeText(MainActivity.this,"Please turn the picture to portrait mode!",Toast.LENGTH_LONG).show();
            }
        });

        button_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String final_text=data.getText().toString();
                copyText(final_text);

            }
        });

        button_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });


        button_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {  //grabs image
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK){
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),resultUri);
                    TextfromImage(bitmap);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void TextfromImage(Bitmap bitmap){
        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
        if (!recognizer.isOperational()){
            Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();

        }
        else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build(); //builder for creating a frame instance
            SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame); //sparse array means array in which many elements have value 0
            StringBuilder stringBuilder= new StringBuilder();
            for (int i=0;i< textBlockSparseArray.size();i++){
                TextBlock textBlock = textBlockSparseArray.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");



            }
            data.setText(stringBuilder.toString());
            button_capture.setText("Retake");
            button_copy.setVisibility(View.VISIBLE);
            button_speak.setVisibility(View.VISIBLE);
            Sizer.setVisibility(View.VISIBLE);
            SizerNo.setVisibility(View.VISIBLE);

        }
    }

    private void speak(){
        String text = data.getText().toString();
        TTS.speak(text,TextToSpeech.QUEUE_ADD,null);//QUEUE_ADD Speaks after last speak is over, QUEUE_FLUSH Speaks before last speak is over.
    }

    @Override
    protected void onDestroy() {
        //on closing app/backing
        super.onDestroy();
        if(TTS !=null){
            TTS.stop();
            TTS.shutdown();

        }
    }

    private void copyText (String text){
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied data", text);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(MainActivity.this,"Copied!",Toast.LENGTH_SHORT).show();
    }
}

