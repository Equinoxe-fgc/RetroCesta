package com.equinoxe.retrocesta;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    final static int NUM_BOLETOS = 2000;

    private int iBoletosLibres;
    private TextView tvBoletosDisponibles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvBoletosDisponibles = (TextView)findViewById(R.id.tvBoletosDisponibles);

        if (Build.VERSION.SDK_INT >= 23) {
            Context context = getApplicationContext();
            int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        SharedPreferences prefs = getSharedPreferences("Ajustes", Context.MODE_PRIVATE);
        String sEMail = prefs.getString("eMail","");
        if (sEMail.length() == 0) {
            Intent intent = new Intent(this, Ajustes.class);
            startActivity(intent);
        }

        actualizarBoletosLibres();
    }

    protected void onRestart() {
        super.onRestart();
        actualizarBoletosLibres();
    }


    public void comprar (View v) {
        Intent intentComprar = new Intent(this,Comprar.class);
        startActivity(intentComprar);
    }

    public void devolver (View v) {
        Intent intentDevolver = new Intent(this, Devolver.class);
        startActivity(intentDevolver);
    }

    public void listados (View v) {
        Intent intentListados = new Intent(this, Listados.class);
        startActivity(intentListados);
    }

    private void actualizarBoletosLibres() {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,"RetroCesta",null,1);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor filas = db.rawQuery("select numero from numeros",null);
        // Si no hay registros se crean todos
        if (filas.getCount() == 0) {
            ContentValues registro = new ContentValues();
            for (int i=0;i<NUM_BOLETOS;i++) {
                registro.put("numero",i);
                registro.put("libre",1);
                registro.put("nick","");
                db.insert("numeros",null,registro);
            }
            iBoletosLibres = NUM_BOLETOS;
        } else {
            String params[] = new String[1];
            params[0] = Integer.toString(1);
            filas = db.rawQuery("select numero from numeros where libre = ?",params);
            iBoletosLibres = filas.getCount();
        }

        filas.close();
        db.close();

        tvBoletosDisponibles.setText(Integer.toString(iBoletosLibres));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Ajustes.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
