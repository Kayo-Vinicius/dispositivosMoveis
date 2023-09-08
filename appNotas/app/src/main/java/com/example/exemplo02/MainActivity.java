package com.example.exemplo02;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import android.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        setContentView(R.layout.activity_main);
        sharedPrefs = getSharedPreferences("ALUNOS", Context.MODE_PRIVATE);

        Button saveButton = findViewById(R.id.buttonSave);
        Button deleteButton = findViewById(R.id.buttonDelete);
        Button gradeButton = findViewById(R.id.buttonGrade);

        gradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verNotas(view);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvar(view);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remover(view);
            }
        });
    }

    public void salvar(View view){
        EditText nome = findViewById(R.id.nome);
        EditText nota1 = findViewById(R.id.nota1);
        EditText nota2 = findViewById(R.id.nota2);
        EditText nota3 = findViewById(R.id.nota3);
        EditText resultado = findViewById(R.id.resultado);

        String name = nome.getText().toString();
        String grade1 = nota1.getText().toString();
        String grade2 = nota2.getText().toString();
        String grade3 = nota3.getText().toString();

        if (name.equals("")) {
            Toast.makeText(context, "Digite um nome", Toast.LENGTH_SHORT).show();
            return;
        }

        if (grade1.equals("") || grade2.equals("") || grade3.equals("")) {
            Toast.makeText(context, "Digite todas as notas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Salva os dados em SharedPreferences
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(name + "_nota1", grade1);
        editor.putString(name + "_nota2", grade2);
        editor.putString(name + "_nota3", grade3);
        editor.apply();

        resultado.setText("Notas salvas para: " + name + "Nota 1 =" + grade1 + "Nota 2 =" + grade2 + "Nota 3 =" + grade3);
    }

    public void remover(View view) {
        // Inflar o layout do diálogo personalizado
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.delete_aluno, null);

        // Pega o texto digitado na barra de texto para buscar o aluno
        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        EditText resultado = findViewById(R.id.resultado);

        // Cria a tela de notificação para digitar o nome do aluno
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Deletar Aluno")
                .setPositiveButton("Deletar", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String alunoToDelete = editTextName.getText().toString();

                        //Verificaando se o aluno existe e se existe remover suas notas
                        if (sharedPrefs.contains(alunoToDelete + "_nota1")) {
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.remove(alunoToDelete + "_nota1");
                            editor.remove(alunoToDelete + "_nota2");
                            editor.remove(alunoToDelete + "_nota3");
                            editor.apply();
                            resultado.setText("Aluno Removido: " + alunoToDelete);
                        } else {
                            resultado.setText("Aluno encontrado: " + alunoToDelete);
                        }
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Apenas fecha o popUp
                    }
                });

        // Mostrar o AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void verNotas(View view) {
        Intent intent = new Intent(this, NotasSalvas.class);
        startActivity(intent);
    }

    private void createFile(String filename, String content) {

        File file = new File(context.getFilesDir(), filename);

        Log.e("TESTE", "Salvando arquivo...");
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
        } catch (Exception e) {
            Log.e("TESTE", "Erro fos");
        }

    }

    private String listFiles() {
        String[] files = this.context.fileList();

        for (String f : files) {
            Log.e("TESTE", f);
        }
        return String.join("\n", files);
    }

    private String readFile(String filename) {
        FileInputStream fis = null;
        String content = null;
        try {
            fis = context.openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            return null;
        } finally {
            content = stringBuilder.toString();
        }
        return content;
    }

    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private boolean isExternalStorageReadable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    private void createSharedFile() {
        final int CREATE_FILE = 1;

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "Aula02.pdf");

        startActivityForResult(intent, CREATE_FILE);
    }

    private void openSharedFile() {
        final int PICK_PDF_FILE = 2;

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        startActivityForResult(intent, PICK_PDF_FILE);
    }

    private void writeSharedPrefs(String key, String value) {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("CONFIGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String readSharedPrefs(String key) {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("CONFIGS", Context.MODE_PRIVATE);
        String value = sharedPref.getString(key, "valor padrão");
        return value;
    }
}