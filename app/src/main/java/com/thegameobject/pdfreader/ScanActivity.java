package com.thegameobject.pdfreader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.thegameobject.pdfreader.MainActivity.fileList;

public class ScanActivity extends AppCompatActivity {

    Calendar c;
    SimpleDateFormat df;
    String formattedDate;

    public static final int IMAGE_GALLERY_REQUEST = 20;
    public static int index= 0;
    public final String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Pictures";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        ActivityCompat.requestPermissions(this,new String[] {CAMERA, WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        c = Calendar.getInstance();
        df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        formattedDate = df.format(c.getTime());

//        runCamera();

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    100);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent,100);

//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);

    }

    private void runCamera() {

        // formattedDate have current date/time
//        Toast.makeText(this, formattedDate, Toast.LENGTH_SHORT).show();
        Log.e("message","Current Date and Time : "+formattedDate);

        index++;
        String file = directory + formattedDate + ".jpg";
        File newFile = new File(file);

        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri outputFileUri = Uri.fromFile(newFile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivity(cameraIntent);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        if(requestCode == 100 && resultCode == RESULT_OK){
            final ImageView imageView = findViewById(R.id.imageView);
            final List<Bitmap> bitmaps = new ArrayList<>();
            final Button converterBtn = findViewById(R.id.converterBtn);

            ClipData clipData = data.getClipData();

            if(clipData != null){
                for(int i=0 ; i< clipData.getItemCount() ; i++){
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    try {
                        InputStream is = getContentResolver().openInputStream(imageUri);

                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        bitmaps.add(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                Uri imageUri = data.getData();

                try {
                    InputStream is = getContentResolver().openInputStream(imageUri);

                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    bitmaps.add(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            // to show our selected images as a slide show
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(final Bitmap b : bitmaps){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(b);
                                // Experiment
                                converterBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String file = data.getDataString();
                                        Bitmap bitmap = BitmapFactory.decodeFile(file);

                                        PdfDocument pdfDocument = new PdfDocument();
                                        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(2560,1920,1).create();
                                        PdfDocument.Page page = pdfDocument.startPage(myPageInfo);

                                        page.getCanvas().drawBitmap(bitmap,0,0,null);
                                        pdfDocument.finishPage(page);

                                        String pdfFile = directory + "/myPDFFILE.pdf";
                                        File myPDFFILE = new File(pdfFile);
                                        try {
                                            pdfDocument.writeTo(new FileOutputStream(myPDFFILE));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        pdfDocument.close();
                                    }
                                });

                            }
                        });
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }
}