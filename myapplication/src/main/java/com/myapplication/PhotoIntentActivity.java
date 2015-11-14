package com.myapplication;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;


public class PhotoIntentActivity extends Activity {

    private static final int ACTION_TAKE_PHOTO_B = 1;
    private static final int ACTION_TAKE_PHOTO_S = 2;
    private static final int ACTION_TAKE_VIDEO = 3;

    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    private ImageView[] mImageView = new ImageView[4];
    private Bitmap mImageBitmap;

    private static final String VIDEO_STORAGE_KEY = "viewvideo";
    private static final String VIDEOVIEW_VISIBILITY_STORAGE_KEY = "videoviewvisibility";
    private VideoView mVideoView;
    private Uri mVideoUri;

    private String[] mCurrentPhotoPath = new String[4];
    int counter = 0;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    SharedPreferences sharedPref;

    /* Photo album for this application */
    private String getAlbumName() {
        return getString(R.string.album_name);
    }


    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath[counter] = f.getAbsolutePath();

        return f;
    }

    private void setPic(int i) {

        Log.e("InClass", "In Setpic - " + i);
		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = mImageView[i].getWidth();
        int targetH = mImageView[i].getHeight();
    Log.e("InClass", targetH+"");
		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath[i], bmOptions);


        SharedPreferences.Editor editor = sharedPref.edit();
        // if(mCurrentPhotoPath!=null || mCurrentPhotoPath!="")
        editor.putString("mCurrentPhotoPath"+i, mCurrentPhotoPath[i]);
        editor.commit();




        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        Log.e("InClass", photoH+"");
		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
           scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath[i], bmOptions);
		
		/* Associate the Bitmap to the ImageView */
        mImageView[i].setImageBitmap(bitmap);
        mVideoUri = null;
        mImageView[i].setVisibility(View.VISIBLE);
        //mVideoView.setVisibility(View.INVISIBLE);
       // counter ++;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath[counter]);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        counter ++;
        Log.e("InClass", counter+"");
    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch(actionCode) {
            case ACTION_TAKE_PHOTO_B:
                File f = null;

                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath[counter] = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        } // switch

        startActivityForResult(takePictureIntent, actionCode);
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
    }

    private void handleSmallCameraPhoto(Intent intent) {
        Bundle extras = intent.getExtras();
        mImageBitmap = (Bitmap) extras.get("data");
        mImageView[counter].setImageBitmap(mImageBitmap);
        mVideoUri = null;
        mImageView[counter].setVisibility(View.VISIBLE);
        mVideoView.setVisibility(View.INVISIBLE);
    }

    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath[counter] != null) {
            setPic(counter);
            galleryAddPic();
        //
        //    mCurrentPhotoPath = null;
        }

    }

    private void handleCameraVideo(Intent intent) {
        mVideoUri = intent.getData();
        mVideoView.setVideoURI(mVideoUri);
        mImageBitmap = null;
        mVideoView.setVisibility(View.VISIBLE);
        mImageView[counter].setVisibility(View.INVISIBLE);
    }

    Button.OnClickListener mTakePicOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                }
            };

    Button.OnClickListener mTakePicSOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
                }
            };

    Button.OnClickListener mTakeVidOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakeVideoIntent();
                }
            };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        counter = sharedPref.getInt("counter", 0);

        Log.e("InClass", counter+"");

        mImageView[0] = (ImageView) findViewById(R.id.imageView1);
        mImageView[1] = (ImageView) findViewById(R.id.imageView2);
        mImageView[2] = (ImageView) findViewById(R.id.imageView3);
        mImageView[3] = (ImageView) findViewById(R.id.imageView4);

        mVideoView = (VideoView) findViewById(R.id.videoView1);
        mImageBitmap = null;
        mVideoUri = null;

        Button picBtn = (Button) findViewById(R.id.btnIntend);
        setBtnListenerOrDisable(
                picBtn,
                mTakePicOnClickListener,
                MediaStore.ACTION_IMAGE_CAPTURE
        );

        Button picSBtn = (Button) findViewById(R.id.btnIntendS);
        setBtnListenerOrDisable(
                picSBtn,
                mTakePicSOnClickListener,
                MediaStore.ACTION_IMAGE_CAPTURE
        );

        Button vidBtn = (Button) findViewById(R.id.btnIntendV);
        setBtnListenerOrDisable(
                vidBtn,
                mTakeVidOnClickListener,
                MediaStore.ACTION_VIDEO_CAPTURE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

        //if(mCurrentPhotoPath != null)
          //  setPic();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO_B: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            } // ACTION_TAKE_PHOTO_B

            case ACTION_TAKE_PHOTO_S: {
                if (resultCode == RESULT_OK) {
                    handleSmallCameraPhoto(data);
                }
                break;
            } // ACTION_TAKE_PHOTO_S

            case ACTION_TAKE_VIDEO: {
                if (resultCode == RESULT_OK) {
                    handleCameraVideo(data);
                }
                break;
            } // ACTION_TAKE_VIDEO
        } // switch
    }

    // Some lifecycle callbacks so that the image can survive orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
        //outState.putParcelable(VIDEO_STORAGE_KEY, mVideoUri);
        //outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null));
        //outState.putBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY, (mVideoUri != null));
        //outState.putString("mCurrentPhotoPath", mCurrentPhotoPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
        //mVideoUri = savedInstanceState.getParcelable(VIDEO_STORAGE_KEY);
    //    mImageView.setImageBitmap(mImageBitmap);
      //  mImageView.setVisibility(
                //          savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ?
                //                ImageView.VISIBLE : ImageView.INVISIBLE
        //        ImageView.VISIBLE
       // );
        //mVideoView.setVideoURI(mVideoUri);
        //mVideoView.setVisibility(
         //       savedInstanceState.getBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY) ?
           //             ImageView.VISIBLE : ImageView.INVISIBLE
        //);

    //    mCurrentPhotoPath = savedInstanceState.getString("mCurrentPhotoPath");

    //    Log.e("InClass", mCurrentPhotoPath);
    }

    /**
     * Indicates whether the specified action can be used as an intent. This
     * method queries the package manager for installed packages that can
     * respond to an intent with the specified action. If no suitable package is
     * found, this method returns false.
     * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
     *
     * @param context The application's environment.
     * @param action The Intent action to check for availability.
     *
     * @return True if an Intent with the specified action can be sent and
     *         responded to, false otherwise.
     */
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void setBtnListenerOrDisable(
            Button btn,
            Button.OnClickListener onClickListener,
            String intentName
    ) {
        if (isIntentAvailable(this, intentName)) {
            btn.setOnClickListener(onClickListener);
        } else {
            btn.setText(
                    getText(R.string.cannot).toString() + " " + btn.getText());
            btn.setClickable(false);
        }
    }

    public void onPause() {
        super.onPause();
        Log.e("InClass", "onPause");
      //  SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
       // if(mCurrentPhotoPath!=null || mCurrentPhotoPath!="")

      //  editor.putInt("counter", counter);
        for(int j=0;j<counter; j++) {
            editor.putString("mCurrentPhotoPath" + j, mCurrentPhotoPath[j]);
            editor.commit();
        }

    }

    public void onResume() {
        super.onResume();
        Log.e("InClass", "onResume");
      //  counter = sharedPref.getInt("counter", 0);
        for(int j=0;j<counter;j++) {
            mCurrentPhotoPath[j] = sharedPref.getString("mCurrentPhotoPath" + j, "");
            Log.e("InClass", mCurrentPhotoPath[j]);
            //if (mCurrentPhotoPath[j] != "")
                setPic(j);
        }
    }

    public void onStart() {
        super.onStart();
        Log.e("InClass", "onStart");
    }

    public void onStop() {
        super.onStop();
        Log.e("InClass", "OnStop");
    }

    public void onDestroy() {
        super.onDestroy();
        //sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("counter", counter);
        editor.commit();
        Log.e("InClass", "onDestroy");
    }

}