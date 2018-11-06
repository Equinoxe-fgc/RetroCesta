package com.equinoxe.retrocesta;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class Comprar extends AppCompatActivity {
    EditText etNick, etNombre, etCorreo, etCantidad, etListaBoletos;
    RadioButton rbAleatorio, rbPeticion;
    TextView tvCantidad;

    int iBoletosSeleccionados[];
    int iNumBoletosSeleccionados;

    String sEMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar);

        etNick = (EditText)findViewById(R.id.etNick);
        etNombre = (EditText)findViewById(R.id.etNombre);
        etCorreo = (EditText)findViewById(R.id.etCorreo);
        etCantidad = (EditText)findViewById(R.id.etCantidad);
        etListaBoletos = (EditText)findViewById(R.id.etListaBoletos);

        rbAleatorio = (RadioButton)findViewById(R.id.rbAleatorios);
        rbPeticion = (RadioButton)findViewById(R.id.rbPeticion);

        tvCantidad = (TextView)findViewById(R.id.tvCantidad);

        iBoletosSeleccionados = new int[100];

        cargarEMail();
    }

    protected void onRestart() {
        super.onRestart();
        cargarEMail();
    }

    private void cargarEMail() {
        SharedPreferences prefs = getSharedPreferences("Ajustes", Context.MODE_PRIVATE);
        sEMail = prefs.getString("eMail","");
    }

    public void checkAleatorioOPeticion(View v) {
        if (rbAleatorio.isChecked()) {
            etCantidad.setVisibility(View.VISIBLE);
            etListaBoletos.setVisibility(View.INVISIBLE);
            tvCantidad.setText("Cantidad");
        } else {
            etCantidad.setVisibility(View.INVISIBLE);
            etListaBoletos.setVisibility(View.VISIBLE);
            tvCantidad.setText("Lista de números");
        }
    }

    public void comprar (View v) {
        if (checkDisponibilidad()) {
            String sNick = etNick.getText().toString();
            String sNombre = etNombre.getText().toString();
            String sCorreo = etCorreo.getText().toString();

            if (sNick.length() == 0){
                Toast.makeText(this, "El nick es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,"RetroCesta",null,1);
            SQLiteDatabase db = admin.getWritableDatabase();

            for (int i = 0; i < iNumBoletosSeleccionados; i++) {
                ContentValues registro = new ContentValues();
                //registro.put("numero",i);
                registro.put("libre",0);
                registro.put("nick", sNick);
                db.update("numeros", registro, "numero = " + iBoletosSeleccionados[i],null);
            }

            String sSQL = "insert into datos values ('" + sNick + "','" + sNombre + "','" + sCorreo + "')";
            db.execSQL(sSQL);
            db.close();

            enviarCorreoElectronico(v);
            Toast.makeText(this, "Boletos comprados por " + sNick + ": " + iNumBoletosSeleccionados, Toast.LENGTH_LONG).show();
            finish();
        }
    }


    public void disponibilidad(View v) {
        if (rbAleatorio.isChecked()) {
            String sNumBoletos = etCantidad.getText().toString();
            if (sNumBoletos.length() == 0) {
                Toast.makeText(this, "Selecciona cantidad de boletos", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (rbPeticion.isChecked()) {
            String sListaBoletos = etListaBoletos.getText().toString();
            if (sListaBoletos.length() == 0) {
                Toast.makeText(this, "Lista los boletos deseados separados por espacios", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (checkDisponibilidad())
            Toast.makeText(this, "Boletos libres", Toast.LENGTH_SHORT).show();
        else if (rbAleatorio.isChecked())
            Toast.makeText(this, "No hay suficientes boletos", Toast.LENGTH_SHORT).show();
        //else
        //    Toast.makeText(this, "Algún boleto no disponible", Toast.LENGTH_SHORT).show();
    }

    public boolean checkDisponibilidad() {
        boolean bDispOK = true;
        Cursor filas;
        int iNumeroActual;
        String sNumBoletos = etCantidad.getText().toString();
        String sListaBoletos = etListaBoletos.getText().toString();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,"RetroCesta",null,1);
        SQLiteDatabase db = admin.getWritableDatabase();

        if (rbAleatorio.isChecked()) {
            filas = db.rawQuery("select numero from numeros where libre = 1",null);

            iNumBoletosSeleccionados = Integer.parseInt(sNumBoletos);

            if (filas.getCount() >= iNumBoletosSeleccionados) {
                /*filas.moveToFirst();
                for (int  i=0; i < iNumBoletosSeleccionados; i++) {
                    iBoletosSeleccionados[i] = filas.getInt(0);
                    filas.moveToNext();
                }*/
                int iElegidos[] = new int[iNumBoletosSeleccionados];
                elegirNumerosAleatorios(iElegidos, filas.getCount());
                for (int  i=0; i < iNumBoletosSeleccionados; i++) {
                    filas.moveToPosition(iElegidos[i]);
                    iBoletosSeleccionados[i] = filas.getInt(0);
                }
            }
            else {
                bDispOK = false;
            }
        } else {
            try {
                String sBoletos[];
                sBoletos = sListaBoletos.split(" ");
                iNumBoletosSeleccionados = sBoletos.length;

                if (iNumBoletosSeleccionados == 0) {
                    bDispOK = false;
                    Toast.makeText(this, "No hay ningún número de boleto", Toast.LENGTH_SHORT).show();
                }

                for (int  i=0; i < iNumBoletosSeleccionados; i++) {
                    iNumeroActual = Integer.parseInt(sBoletos[i]);

                    if (iNumeroActual < 0 || iNumeroActual > 999) {
                        Toast.makeText(this, "Los boletos deben estar entre 0 y 999: " + iNumeroActual, Toast.LENGTH_SHORT).show();
                        bDispOK = false;
                        continue;
                    }

                    filas = db.rawQuery("select numero from numeros where libre = 1 and numero = " + iNumeroActual, null);
                    if (filas.getCount() == 0) {
                        bDispOK = false;
                        Toast.makeText(this, "Número: " + iNumeroActual + ": No Disponible", Toast.LENGTH_SHORT).show();
                    } else {
                        iBoletosSeleccionados[i] = iNumeroActual;
                        //Toast.makeText(this, "Número: " + iNumeroActual + ": Libre", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error procesando lista de boletos. Deben ser números entre 0 y 999 e ir separados por espacios", Toast.LENGTH_SHORT).show();
                bDispOK = false;
            }
        }
        db.close();

        return bDispOK;
    }

    private void elegirNumerosAleatorios(int iElegidos[], int iNumMax) {
        Random randomGenerator = new Random();
        int randomInt;
        int iEncontrados = 0;
        boolean bEnLista;

        while (iEncontrados != iElegidos.length) {
            randomInt = randomGenerator.nextInt(iNumMax);

            bEnLista = false;
            for (int i = 0; !bEnLista && i < iEncontrados; i++) {
                if (randomInt == iElegidos[i])
                    bEnLista = true;
            }

            if (!bEnLista) {
                iElegidos[iEncontrados] = randomInt;
                iEncontrados++;
            }
        }
    }


    public void enviarCorreoElectronico(View v) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            emailIntent.setData(Uri.parse("mailto:"));
            String[] to = {etCorreo.getText().toString()};
            String[] cc = {sEMail};
            String sMensaje = "Gracias " + etNick.getText().toString() + " por participar en la Retro Cesta de Retro Entre Amigos.\n\n";
            sMensaje += "Tus números asignados son:\n";

            String filename;
            File filelocation;
            ArrayList<Uri> uris = new ArrayList<Uri>();
            for (int i = 0; i < iNumBoletosSeleccionados; i++) {
                sMensaje = sMensaje + " " + String.format("%04d", iBoletosSeleccionados[i]) + " " +
                                            (iBoletosSeleccionados[i] + 2000) + " " +
                                            (iBoletosSeleccionados[i] + 4000) + " " +
                                            (iBoletosSeleccionados[i] + 6000) + " " +
                                            (iBoletosSeleccionados[i] + 8000) + "\n";

                filename = "RetroCesta_Papeletas " + iBoletosSeleccionados[i] + ".pdf";
                filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/RetroCesta/", filename);
                Uri u = Uri.fromFile(filelocation);
                uris.add(u);
            }
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

            sMensaje += "\nPuedes encontrar las papeletas adjuntas a este correo.";
            sMensaje += "\nSuerte.";

            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "RetroCesta de RetroEntreAmigos.com: Números asignados");
            emailIntent.putExtra(Intent.EXTRA_TEXT,sMensaje);
            emailIntent.setType("message/rfc822");
            startActivity(Intent.createChooser(emailIntent, "Email "));
    }
}
