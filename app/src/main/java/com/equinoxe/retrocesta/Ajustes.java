package com.equinoxe.retrocesta;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Ajustes extends AppCompatActivity {
    EditText etMail;
    //EditText etPapeletasFolder;
    String sEMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        etMail = (EditText)findViewById(R.id.etMail);
        //etPapeletasFolder = (EditText)findViewById()

        cargarPreferencias();

        etMail.setText(sEMail);
    }

    protected void onRestart() {
        super.onRestart();
        cargarPreferencias();

        etMail.setText(sEMail);
    }

    private void cargarPreferencias() {
        SharedPreferences prefs = getSharedPreferences("Ajustes", Context.MODE_PRIVATE);
        sEMail = prefs.getString("eMail","");
    }

    public void guardarAjustes(View v) {
        SharedPreferences prefs = getSharedPreferences("Ajustes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("eMail",etMail.getText().toString());
        editor.commit();
        Toast.makeText(this, "Ajustes guardados", Toast.LENGTH_SHORT).show();
        finish();
    }
}
