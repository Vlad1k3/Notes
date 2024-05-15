package com.example.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> fileNames;
    private TextView notesCountTextView;
    private TextView noNotesTextView;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        notesCountTextView = findViewById(R.id.notesCountTextView);
        noNotesTextView = findViewById(R.id.noNotesTextView);
        fileNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.list_item_layout, R.id.listItemTitleTextView, fileNames);
        listView.setAdapter(adapter);

        // Инициализация Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference("user_notes");

        // Получение списка заметок из Firebase
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fileNames.clear();
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    String title = noteSnapshot.getKey();
                    String content = noteSnapshot.getValue(String.class); // Получаем контент заметки
                    fileNames.add(title);
                }
                adapter.notifyDataSetChanged();
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок при загрузке данных
                Toast.makeText(MainActivity.this, "Ошибка загрузки заметок", Toast.LENGTH_SHORT).show();
            }
        });

        // Обработчик нажатия на элемент списка
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedNoteTitle = fileNames.get(position);
            openNoteForEditing(selectedNoteTitle);
        });

        // Обработчик долгого нажатия на элемент списка
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String selectedNoteTitle = fileNames.get(position);
            showDeleteNoteDialog(selectedNoteTitle);
            return true;
        });

        Button createNoteButton = findViewById(R.id.button);
        createNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
                startActivity(intent);
            }
        });
    }

    // Метод для отображения или скрытия текста "У вас нет заметок"
    private void updateUI() {
        if (fileNames.isEmpty()) {
            noNotesTextView.setVisibility(View.VISIBLE);
        } else {
            noNotesTextView.setVisibility(View.GONE);
        }


        String notesCountText;
        int notesCount = fileNames.size();
        if (notesCount == 1) {
            notesCountText = "1 заметка";
        } else {
            notesCountText = notesCount + " " + getProperNoun(notesCount);
        }
        notesCountTextView.setText(" " + notesCountText);
    }

    // Метод для определения правильного существительного в зависимости от количества
    private String getProperNoun(int count) {
        if (count % 10 == 1 && count % 100 != 11) {
            return "заметка";
        } else if ((count % 10 == 2 && count % 100 != 12) || (count % 10 == 3 && count % 100 != 13) ||
                (count % 10 == 4 && count % 100 != 14)) {
            return "заметки";
        } else {
            return "заметок";
        }
    }

    // Метод для отображения диалога удаления заметки
    private void showDeleteNoteDialog(String noteTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Удалить заметку '" + noteTitle + "'?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteNoteFile(noteTitle);
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    // Метод для удаления заметки
    private void deleteNoteFile(String noteTitle) {
        databaseRef.child(noteTitle).removeValue();
    }

    // Метод для открытия заметки для редактирования
// Метод для открытия заметки для редактирования
    public void openNoteForEditing(String noteTitle) {
        // Получаем контент заметки по заголовку
        databaseRef.child(noteTitle).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String noteContent = dataSnapshot.getValue(String.class);
                Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
                intent.putExtra("NOTE_TITLE", noteTitle);
                intent.putExtra("NOTE_CONTENT", noteContent); // Передаем контент заметки
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок при загрузке данных
                Toast.makeText(MainActivity.this, "Ошибка загрузки контента заметки", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
