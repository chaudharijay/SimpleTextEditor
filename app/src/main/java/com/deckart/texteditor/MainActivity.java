package com.deckart.texteditor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    Uri mFileUri = null;
    Intent intent;
    EditText edtTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtTxt = findViewById(R.id.textView);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int create_new = R.id.create_new;
        final int open = R.id.open;
        final int save = R.id.save;
        // Handle item selection
        switch (item.getItemId()) {
            case create_new:
                edtTxt.getText().clear();
                return true;
            case open:
                filePicker(true);
                return true;
            case save:
                filePicker(false);
                return true;
        }
        return true;
    }

    ActivityResultLauncher<Intent> mLaunchFilePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK && data != null) {
                    handleFileResult(data,true);
                }
            });

    ActivityResultLauncher<Intent> mLaunchFileSaver = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK && data != null) {
                    handleFileResult(data,false);
                }
            });

    void filePicker(boolean readOrWrite) {
        if (readOrWrite) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            mLaunchFilePicker.launch(intent);
        } else {
            intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("text/*");
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            mLaunchFileSaver.launch(intent);
        }
    }

    private void handleFileResult(Intent data, boolean readOrWrite) {
        mFileUri = data.getData();

        if (mFileUri != null) {
            try {
                getContentResolver().takePersistableUriPermission(mFileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            if (readOrWrite) {
                read();
            } else {
                write();
            }
        }
    }

    void read() {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(getContentResolver().openInputStream(mFileUri));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder builder = new StringBuilder();

            try {
                String fileContentLine;
                while ((fileContentLine = reader.readLine()) != null) {
                    fileContentLine += "\n";
                    builder.append(fileContentLine);
                }
                String fileContent = builder.toString();
                edtTxt.setText(fileContent);
            } finally {
                reader.close();
                inputStreamReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void write() {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContentResolver().openOutputStream(mFileUri));
            BufferedWriter writer = new BufferedWriter(outputStreamWriter);
            String textContent = edtTxt.getText().toString();

            try {
                writer.write(textContent);
            } finally {
                writer.close();
                outputStreamWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
