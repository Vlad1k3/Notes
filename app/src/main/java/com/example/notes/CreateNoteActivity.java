package com.example.notes;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateNoteActivity extends AppCompatActivity {

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_note);

        // Получить переданные данные
        String noteTitle = getIntent().getStringExtra("NOTE_TITLE");
        String noteContent = getIntent().getStringExtra("NOTE_CONTENT");

        // Инициализация Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference("user_notes");

        // Найти и заполнить EditText
        EditText noteTitleEditText = findViewById(R.id.noteTitleEditText);
        EditText noteContentEditText = findViewById(R.id.noteContentEditText);
        noteTitleEditText.setText(noteTitle);
        noteContentEditText.setText(noteContent);

        ImageButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = noteTitleEditText.getText().toString();
                String content = noteContentEditText.getText().toString();

                // Проверка на пустые поля
                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(CreateNoteActivity.this, "Поля не могут быть пустыми", Toast.LENGTH_SHORT).show();
                    return; // Завершаем метод, чтобы заметка не была сохранена
                }

                // Сохранение заметки в Firebase
                databaseRef.child(title).setValue(content)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(CreateNoteActivity.this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(CreateNoteActivity.this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                        });
            }
        });


        ImageButton createNoteButton = findViewById(R.id.exitButton);
        createNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
