package iitp.naman.mksdrive;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Created by naman on 30-Sep-16.
 * Get email id for forgot password
 */

public class PasswordReset extends AppCompatActivity {

    private EditText inputEmail;
    private String inputEmail1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_passwordreset);
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle(" ");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        Button btnReset;
        inputEmail = findViewById(R.id.email);
        btnReset = findViewById(R.id.reset);

        btnReset.setOnClickListener(view -> {
            if(inputEmail.getText().toString().equals("")){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.java_login_2), Toast.LENGTH_SHORT).show();
            }
            else {
                inputEmail1 = inputEmail.getText().toString();
                Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
                intent.putExtra("username",inputEmail1);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_blank, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}