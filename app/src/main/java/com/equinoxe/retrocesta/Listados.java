package com.equinoxe.retrocesta;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Listados extends AppCompatActivity {
    Spinner spinUsuarios;
    EditText etListadoNumeros;
    RadioButton rbPorUsuario, rbPorNumero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listados);

        spinUsuarios = (Spinner) findViewById(R.id.spinUsuarios);
        etListadoNumeros = (EditText) findViewById(R.id.etListadoNumeros);
        rbPorUsuario = (RadioButton) findViewById(R.id.rbCompletoPorUsuario);
        rbPorNumero = (RadioButton) findViewById(R.id.rbCompletoPorNumero);

        cargarUsuarios();
    }

    public void onClickPorUsuario(View v) {
        spinUsuarios.setVisibility(View.VISIBLE);
        etListadoNumeros.setVisibility(View.VISIBLE);
    }

    public void onClickCompletoPorUsuario(View v) {
        spinUsuarios.setVisibility(View.INVISIBLE);
        etListadoNumeros.setVisibility(View.INVISIBLE);
    }

    public void onClickCompletoPorNumero(View v) {
        spinUsuarios.setVisibility(View.INVISIBLE);
        etListadoNumeros.setVisibility(View.INVISIBLE);
    }

    public void generarListado(View v) {
        if (rbPorUsuario.isChecked())
            listadoCompletoPorUsuario();
        else if (rbPorNumero.isChecked())
            listadoCompletoPorNumero();
        else {
            if (spinUsuarios.getCount() == 0)
                Toast.makeText(this, "No hay usuarios seleccionados.", Toast.LENGTH_SHORT).show();
            else
                listadoSoloUsuario();
        }
    }


    private void listadoCompletoPorUsuario() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "RetroCesta", null, 1);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor usuarios = db.rawQuery("select nick from datos order by nick", null);
        usuarios.moveToFirst();

        if (usuarios.getCount() != 0) {
            String sFicheroCSV[] = new String[usuarios.getCount()];
            for (int i = 0; i < usuarios.getCount(); i++) {
                sFicheroCSV[i] = usuarios.getString(0);
                usuarios.moveToNext();

                Cursor boletos = db.rawQuery("select numero from numeros where nick = '" + sFicheroCSV[i] + "'", null);
                boletos.moveToFirst();

                for (int iBoleto = 0; iBoleto < boletos.getCount(); iBoleto++) {
                    sFicheroCSV[i] += "," + boletos.getString(0);
                    boletos.moveToNext();
                }
            }
            db.close();

            File tarjeta = Environment.getExternalStorageDirectory();

            try {
                File file = new File(tarjeta.getAbsolutePath(), "RetroCestaUsuarios.csv");
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));

                for (int i = 0; i < sFicheroCSV.length; i++) {
                    osw.write(sFicheroCSV[i].toString());
                    osw.write("\n");
                }

                osw.flush();
                osw.close();
                Toast.makeText(this, "RetroCestaUsuarios.csv guardado correctamente", Toast.LENGTH_SHORT).show();
            } catch (IOException ioe) {
                Toast.makeText(this, "Error al grabar archivo", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No hay papeletas para listar.", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarUsuarios() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "RetroCesta", null, 1);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor filas = db.rawQuery("select nick from datos order by nick", null);
        filas.moveToFirst();

        if (filas.getCount() != 0) {
            String sUsuarios[] = new String[filas.getCount()];
            for (int i = 0; i < filas.getCount(); i++) {
                sUsuarios[i] = filas.getString(0);
                filas.moveToNext();
            }
            db.close();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sUsuarios);
            spinUsuarios.setAdapter(adapter);
        }
    }

    private void listadoSoloUsuario() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "RetroCesta", null, 1);
        SQLiteDatabase db = admin.getWritableDatabase();

        String sUsuario = spinUsuarios.getSelectedItem().toString();
        Cursor filas = db.rawQuery("select numero from numeros where nick = '" + sUsuario + "'", null);
        filas.moveToFirst();

        if (filas.getCount() != 0) {
            String sNumeros = "";
            for (int i = 0; i < filas.getCount(); i++) {
                sNumeros += filas.getString(0);
                if (i != filas.getCount() - 1)
                    sNumeros += " - ";
                filas.moveToNext();
            }
            etListadoNumeros.setText(sNumeros);
            db.close();
        }  else {
            Toast.makeText(this, "No hay papeletas para listar.", Toast.LENGTH_SHORT).show();
        }
    }

    private void listadoCompletoPorNumero() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "RetroCesta", null, 1);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor numeros = db.rawQuery("select numero, nick from numeros where libre = 0", null);
        numeros.moveToFirst();

        if (numeros.getCount() != 0) {
            String sFicheroCSV[] = new String[numeros.getCount()];
            for (int i = 0; i < numeros.getCount(); i++) {
                sFicheroCSV[i] = numeros.getString(0);
                sFicheroCSV[i] += "," + numeros.getString(1);
                numeros.moveToNext();
            }
            db.close();

            File tarjeta = Environment.getExternalStorageDirectory();

            try {
                File file = new File(tarjeta.getAbsolutePath(), "RetroCestaNumeros.csv");
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));

                for (int i = 0; i < sFicheroCSV.length; i++) {
                    osw.write(sFicheroCSV[i].toString());
                    osw.write("\n");
                }

                osw.flush();
                osw.close();
                Toast.makeText(this, "RetroCestaNumeros.csv guardado correctamente", Toast.LENGTH_SHORT).show();
            } catch (IOException ioe) {
                Toast.makeText(this, "Error al grabar archivo", Toast.LENGTH_SHORT).show();
            }
        }   else {
            Toast.makeText(this, "No hay papeletas para listar.", Toast.LENGTH_SHORT).show();
        }
    }
}