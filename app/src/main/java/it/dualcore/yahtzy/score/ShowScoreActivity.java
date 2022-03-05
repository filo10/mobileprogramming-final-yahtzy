package it.dualcore.yahtzy.score;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import it.dualcore.yahtzy.R;

public class ShowScoreActivity extends AppCompatActivity implements View.OnClickListener {

    RecyclerView recyclerView;
    Button resetHscore;

    SharedPreferences sharedPref;
    CardAdapter adapter;
    private List<ScoreInfo> scoreInfoList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        resetHscore = findViewById(R.id.resetHscore);
        resetHscore.setOnClickListener(this);

        //prendi tutti i punteggi
        sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE);
        for (int i = 0; i < 10; i++)
            scoreInfoList.add(new ScoreInfo(sharedPref.getInt(String.format(Locale.getDefault(), "bestScore%d", i), 0), sharedPref.getString(String.format(Locale.getDefault(), "bestScore%d_date", i), "")));
        //scoreInfoList.sort(Comparator.comparing(ScoreInfo::getPoints).reversed());    // OMG, requires API 24+
        Collections.sort(scoreInfoList, null);

        adapter = new CardAdapter(scoreInfoList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.resetHscore){
            // deleting every score recorded

            // make a backup in case of a mistake
            List<ScoreInfo> backup = new ArrayList<>(scoreInfoList);

            // get sharedPref
            SharedPreferences.Editor editor = sharedPref.edit();

            // save blank scores
            for (int i = 0; i < 10; i++){
                editor.putInt(String.format(Locale.getDefault(),"bestScore%d", i), 0);
                editor.putString(String.format(Locale.getDefault(),"bestScore%d_date", i), ""); //+"_date", "");
            }
            editor.apply();

            // delete every score
            scoreInfoList.clear();

            // set blank fields
            for (int i = 0; i < 10; i++){
                scoreInfoList.add(new ScoreInfo(0, ""));
            }

            // update the recyclerView
            adapter.notifyDataSetChanged();

            // make sure that reset was not pressed by error
            Snackbar.make(findViewById(R.id.layout_highscore), getString(R.string.highscore_delete), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.undo), new View.OnClickListener() {
                        // if not, undo the reset using the backup
                        @Override
                        public void onClick(View v) {
                            scoreInfoList.clear();
                            for (int i = 0; i < 10; i++){
                                editor.putInt(String.format(Locale.getDefault(), "bestScore%d", i), backup.get(i).getPoints());
                                editor.putString(String.format(Locale.getDefault(), "bestScore%d_date", i), backup.get(i).getDate());
                            }
                            scoreInfoList = new ArrayList<>(backup);
                            adapter = new CardAdapter(scoreInfoList);
                            recyclerView.setAdapter(adapter);
                            editor.apply();
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.colorPrimary, null)).show();
        }
    }
}
