package com.equinoxe.retrocesta;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

public class Devolver extends AppCompatActivity {
    Spinner spinListaUsuarios;
    RadioButton rbTodos, rbPorUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devolver);

        spinListaUsuarios = (Spinner)findViewById(R.id.spinListaUsuarios);
        rbTodos = (RadioButton)findViewById(R.id.rbTodos);
        rbPorUsuario = (RadioButton)findViewById(R.id.rbUsuario);

        cargarUsuarios();
    }

    protected void onRestart() {
        super.onRestart();
        cargarUsuarios();
    }

    public void devolverTodos(View v) {
        spinListaUsuarios.setVisibility(View.INVISIBLE);
    }

    public void devolverPorUsuario(View v) {
        spinListaUsuarios.setVisibility(View.VISIBLE);
    }

    public void devolverBoletos(View v) {
        if (rbTodos.isChecked()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Seguro de que desea devolver todos los boletos?").setTitle("Devolver boletos");
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    devolverTodosLosBoletos();
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Seguro de que desea devolver los boletos de " + spinListaUsuarios.getSelectedItem().toString() + "?").setTitle("Devolver boletos");
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    devolverBoletosPersonales();
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void cargarUsuarios() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,"RetroCesta",null,1);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor filas = db.rawQuery("select nick from datos order by nick",null);
        filas.moveToFirst();

        if (filas.getCount() != 0) {
            String sUsuarios[] = new String[filas.getCount()];
            for (int i = 0; i < filas.getCount(); i++) {
                sUsuarios[i] = filas.getString(0);
                filas.moveToNext();
            }
            db.close();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,sUsuarios);
            spinListaUsuarios.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No hay boletos para devolver", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void devolverTodosLosBoletos() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,"RetroCesta",null,1);
        SQLiteDatabase db = admin.getWritableDatabase();

        db.delete("datos","",null);

        ContentValues registro = new ContentValues();
        registro.put("libre",1);
        registro.put("nick","");
        db.update("numeros",registro,"",null);

        db.close();

        finish();
    }

    private void devolverBoletosPersonales() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,"RetroCesta",null,1);
        SQLiteDatabase db = admin.getWritableDatabase();

        String sUsuario = spinListaUsuarios.getSelectedItem().toString();
        db.delete("datos","nick = '" + sUsuario + "'",null);

        ContentValues registro = new ContentValues();
        registro.put("libre",1);
        registro.put("nick","");
        db.update("numeros",registro,"nick = '" + sUsuario + "'",null);

        db.close();

        Toast.makeText(this, "Boletos de " + sUsuario + " devueltos", Toast.LENGTH_LONG).show();

        finish();
    }
}
