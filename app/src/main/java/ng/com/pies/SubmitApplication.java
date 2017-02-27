package ng.com.pies;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pies.platform.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;


public class SubmitApplication extends AppCompatActivity {

    private String docFilePath = "";
    private Button upload, apply_button;
    private DatabaseReference mDatabase1;
    private EditText appl_name, appl_email,appl_phone;
    private static final int PICK_FILE_REQUEST = 1;
    private FirebaseStorage storageReference1;
    private ProgressDialog progressDialog;
    private UploadTask taskUpload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_application);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(SubmitApplication.this);
        progressDialog.setMessage("Uploading.. please wait!");
        progressDialog.setCancelable(false);

        // dtatabse reference


        //reference to the input field
        appl_name = (EditText) findViewById(R.id.apply_name);
        appl_email = (EditText) findViewById(R.id.apply_email);
        appl_phone = (EditText) findViewById(R.id.apply_phone);
        apply_button = (Button) findViewById(R.id.apply_button);

        apply_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = appl_name.getText().toString();
                String email = appl_email.getText().toString();
                String phone = appl_phone.getText().toString();

                if(name.isEmpty()){
                    appl_name.setError("enter name");
                }
                else  if(email.isEmpty()){
                    appl_email.setError("enter email");
                }
                else if(phone.isEmpty()){
                    appl_phone.setError("enter phone number");
                }

                else{

//                    progressDialog.show();
                    Uploadfile task = new Uploadfile(taskUpload,name, email,phone,docFilePath, mDatabase1,storageReference1);
                    task.execute();


                }
            }
        });

        upload = (Button) findViewById(R.id.apply_upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDocument();
            }
        });
    }
    private void getDocument()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        //starts new activity to select file and return data
        startActivityForResult(Intent.createChooser(intent,"Choose File to Upload.."),PICK_FILE_REQUEST);
        // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
      //  startActivityForResult(intent, REQUEST_CODE_DOC);
    }

   // and onactivityresult:

    @Override
    protected void onActivityResult(int req, int result, Intent data)
    {
        // TODO Auto-generated method stub
        super.onActivityResult(req, result, data);
        if (result == Activity.RESULT_OK)

        {

            if(result == PICK_FILE_REQUEST){
                if(data == null){
                    return;
                }
            }

            Uri fileuri = data.getData();

            docFilePath = getFileNameByUri(this,fileuri);
            if(docFilePath == null){
                Toast.makeText(SubmitApplication.this, "no file founded", Toast.LENGTH_SHORT).show();
            }else{
                apply_button.setEnabled(true);
                Toast.makeText(SubmitApplication.this, docFilePath, Toast.LENGTH_SHORT).show();
            }
            //Toast.makeText(SubmitApplication.this, docFilePath, Toast.LENGTH_SHORT).show();

        }
    }

// get file path

    private String getFileNameByUri(Context context, Uri uri)
    {
        String filepath = "";//default fileName
        //Uri filePathUri = uri;
        File file;
        if (uri.getScheme().toString().compareTo("content") == 0)
        {
            Cursor cursor = context.getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.ORIENTATION }, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();

            String mImagePath = cursor.getString(column_index);
            cursor.close();
            filepath = mImagePath;

        }
        else
        if (uri.getScheme().compareTo("file") == 0)
        {
            try
            {
                file = new File(new URI(uri.toString()));
                if (file.exists())
                    filepath = file.getAbsolutePath();

            }
            catch (URISyntaxException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            filepath = uri.getPath();
        }
        return filepath;
    }

    public void onPostUploaded(final String error) {
        SubmitApplication.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
             progressDialog.hide();
                if (error == null) {
                    Toast.makeText(SubmitApplication.this, "File uploaded!", Toast.LENGTH_SHORT).show();
                    finish();
    } else {
        Toast.makeText(SubmitApplication.this, error, Toast.LENGTH_SHORT).show();
    }
}
});
        }
    private class Uploadfile extends AsyncTask<Void,Void,Void>{
            String name,email,phone,fileUrl;
            DatabaseReference mDatabase;
        FirebaseStorage storageReference;
            private UploadTask uploadTask;

        public Uploadfile(UploadTask task,String name,String email, String phone, String fileUrl, DatabaseReference mDatabase, FirebaseStorage storageReference){
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.fileUrl = fileUrl;
            this.mDatabase = mDatabase;
            this.storageReference = storageReference;
            this.uploadTask = task;

            }

        @Override
        protected Void doInBackground(Void... uris) {
            mDatabase = FirebaseDatabase.getInstance().getReference();


            storageReference = FirebaseStorage.getInstance();


        // Create file metadata including the content type
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("file/pdf")
                    .build();
            InputStream file = null;
            try {
                file = new FileInputStream(new File(fileUrl));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            StorageReference storage = storageReference.getReferenceFromUrl("gs://platform-237cf.appspot.com");
            Long timestamp = System.currentTimeMillis();
            StorageReference store = storage.child("SubmittedCv").child(timestamp.toString());


            uploadTask = store.putStream(file);

        // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    onPostUploaded(exception.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    cv_item item = new cv_item(name, email,phone,String.valueOf(downloadUrl));
                    mDatabase.child("Submitted-Applications").push().setValue(item);
                   onPostUploaded(null);

                }
            });
            return null;
        }
    }
}
