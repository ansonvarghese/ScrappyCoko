package com.myscrap.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import com.myscrap.application.AppController;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by ms3 on 7/18/2017.
 */

public class ImageStorage {
    private static String DIRECTORY = "MyScrap";

    static String saveToSdCard(Bitmap bitmap, String filename) {

        String stored = null;
        //File sdcard = Environment.getExternalStorageDirectory() ;
        File sdcard = Environment.getExternalStorageDirectory() ;
        File folder = new File(sdcard.getAbsoluteFile()+"/"+Environment.DIRECTORY_PICTURES, DIRECTORY);//the dot makes this directory hidden to the user
        //File folder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);//the dot makes this directory hidden to the user
        if (!folder.exists()){
            boolean isDirectoryCreated= folder.mkdir();
            if (isDirectoryCreated){
                File file = new File(folder.getAbsoluteFile(), filename + ".jpg") ;
        /*if (!file.exists())
            file.mkdir();*/

                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                    stored = "success";
                    updateToGallery(file.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else {
            File file = new File(folder.getAbsoluteFile(), filename + ".jpg") ;
        /*if (!file.exists())
            file.mkdir();*/

            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                stored = "success";
                updateToGallery(file.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return stored;
    }

    private static void updateToGallery(String mCurrentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        AppController.getInstance().sendBroadcast(mediaScanIntent);
    }

    private static File getImage(String imageName) {
        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            mediaImage = new File(myDir.getPath() + "/"+DIRECTORY+"/"+imageName);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mediaImage;
    }

    static boolean checkIfImageExists(String imageName) {
        Bitmap b = null ;
        File file = ImageStorage.getImage("/"+imageName+".jpg");
        String path;
        if (file != null) {
            path = file.getAbsolutePath();
            b = BitmapFactory.decodeFile(path);
        }
        return !(b == null);
    }
}
