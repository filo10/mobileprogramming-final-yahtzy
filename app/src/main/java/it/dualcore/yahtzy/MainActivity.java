package it.dualcore.yahtzy;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;

import it.dualcore.yahtzy.game.GameActivity;
import it.dualcore.yahtzy.score.ShowScoreActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button gameButton, scoreButton, instructionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameButton = findViewById(R.id.gameButton);
        gameButton.setOnClickListener(this);

        scoreButton = findViewById(R.id.scoreButton);
        scoreButton.setOnClickListener(this);

        instructionButton = findViewById(R.id.instructionButton);
        instructionButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.gameButton) {
            Intent i = new Intent(this, GameActivity.class);
            startActivity(i);
        }
        if(v.getId() == R.id.scoreButton) {
            Intent i = new Intent(this, ShowScoreActivity.class);
            startActivity(i);
        }
        if(v.getId() == R.id.instructionButton) {
            instructionDialog();
        }
    }

    public void instructionDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DialogTheme));
        dialog.setTitle(getString(R.string.instruction_title));
        dialog.setMessage(getString(R.string.instruction_message));
        dialog.setNegativeButton(getString(R.string.got_it), new DialogInterface.OnClickListener(){
            @Override public void onClick(DialogInterface dialog, int which){ }});
        dialog.create().show();
    }

}
