package com.eukaris.streamingfirebase;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class CloudStorageActivity extends AppCompatActivity implements View.OnClickListener{

    private Uri videoUri;
    private static final int REQUEST_CODE = 101;
    private StorageReference videoRef;



    private File mediaFile;
    private Button grabar;
    private Button actualizar;
    private Button descargar;
    private String mCurrentVideoPath;
    private VideoView videoView;
    private int position=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_storage);

        grabar= (Button)findViewById(R.id.grabar);
        grabar.setOnClickListener(this);

        actualizar= (Button)findViewById(R.id.actualizar);
        actualizar.setOnClickListener(this);

        descargar= (Button)findViewById(R.id.descargar);
        descargar.setOnClickListener(this);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        videoRef = storageRef.child("/videos/" + uid + "/userIntro.mp4");


        int permissionCheck = ContextCompat.checkSelfPermission(CloudStorageActivity.this,
                Manifest.permission.CAMERA);


    }



    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void startRecord(){
        Intent intennt = new Intent (MediaStore.ACTION_VIDEO_CAPTURE);

        if(intennt.resolveActivity(getPackageManager()) != null){

            mediaFile = null;

            try{
                createMediaFile();
            }catch (Exception e){
                Log.e("debug", "error al crear archivo temporal "+e.getLocalizedMessage());
                return;
            }

            if (mediaFile != null) {

                /** TODO STUB: */
                videoUri =
                FileProvider.getUriForFile(CloudStorageActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        createMediaFile());

                Log.e(CloudStorageActivity.class.getName(), "video uri: "+videoUri);

                intennt.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
                startActivityForResult(intennt, REQUEST_CODE);
            }
        }
    }


    public File createMediaFile()  {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "MP4_" + timeStamp + "_";


        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");

        try {
            mediaFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".mp4",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentVideoPath = "file:" + mediaFile.getAbsolutePath();
        return mediaFile;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Show the thumbnail on ImageView
            Log.e(CloudStorageActivity.class.getName(), "video uri: "+videoUri.getPath());
            //Uri imageUri = Uri.parse(vi);
            File file = new File(videoUri.getPath());
            try {
                InputStream stream = new FileInputStream(file);
                upload();

/*
                UploadTask uploadTask = videoRef.putStream(stream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(getApplicationContext(), "onfailed ", Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Toast.makeText(getApplicationContext(), "Video saved to:\n" +
                                downloadUrl, Toast.LENGTH_LONG).show();
                        Log.e(CloudStorageActivity.class.getName(), "uri para descarga: "+downloadUrl);

                    }
                });

*/

            } catch (FileNotFoundException e) {
                return;
            }

        }

        /*videoUri = data.getData();


        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video saved to:\n" +
                        videoUri, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_LONG).show();
            }
        }
        */


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CloudStorageActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public void upload() {
        if (videoUri != null) {
            UploadTask uploadTask = videoRef.putFile(videoUri);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CloudStorageActivity.this,
                            "Upload failed: " + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();

                }
            }).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CloudStorageActivity.this, "Upload complete",
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnProgressListener(
                    new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
        } else {
            Toast.makeText(CloudStorageActivity.this, "Nothing to upload",
                    Toast.LENGTH_LONG).show();
        }
    }


    public void download() {

        try {
            final File localFile = File.createTempFile("userIntro", "mp4");

            videoRef.getFile(localFile).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(
                                FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(CloudStorageActivity.this, "Download complete",
                                    Toast.LENGTH_LONG).show();


                            final VideoView videoView =
                                    (VideoView) findViewById(R.id.videoview);
                            videoView.setVideoURI(Uri.fromFile(localFile));
                            videoView.start();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CloudStorageActivity.this,
                            "Download failed: " + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(CloudStorageActivity.this,
                    "Failed to create temp file: " + e.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }




    @Override
    public void onClick(View view) {
        if (view == grabar) {
            CloudStorageActivityPermissionsDispatcher.startRecordWithPermissionCheck(this);
        }else
        if(view == actualizar){
            upload();
        }else
        if(view== descargar){
            download();

        }

    }
}
