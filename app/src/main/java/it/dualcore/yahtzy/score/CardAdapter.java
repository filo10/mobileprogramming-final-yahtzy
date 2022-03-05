package it.dualcore.yahtzy.score;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import it.dualcore.yahtzy.R;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ScoreInfoViewHolder> {

    List<ScoreInfo> scoreInfoList;
    public CardAdapter(List<ScoreInfo> scoreInfoList){
        this.scoreInfoList = scoreInfoList;
    }

    @NonNull
    @Override
    public ScoreInfoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_score, viewGroup, false);
        return new ScoreInfoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreInfoViewHolder scoreInfoViewHolder, int i){
        scoreInfoViewHolder.card_points.setText(String.format("%d ", scoreInfoList.get(i).getPoints()));
        scoreInfoViewHolder.card_date.setText(scoreInfoList.get(i).getDate() + " ");
        scoreInfoViewHolder.card_position.setText(String.format("%d", i + 1));
        if (i == 0) { // first place, make text gold
            scoreInfoViewHolder.card_position.setTextColor(0xFFD19200);
            scoreInfoViewHolder.card_position.setBackgroundResource(R.drawable.circle_gold);
        }
        if (i == 1) { // second place, make text silver
            scoreInfoViewHolder.card_position.setTextColor(0xFF949494);
            scoreInfoViewHolder.card_position.setBackgroundResource(R.drawable.circle_silver);
        }
        if (i == 2) { // third place, make text bronze
            scoreInfoViewHolder.card_position.setTextColor(0xFFFDC06F);
            scoreInfoViewHolder.card_position.setBackgroundResource(R.drawable.circle_bronze);
        }
    }

    @Override
    public int getItemCount() {
        return scoreInfoList.size();
    }

    public static class ScoreInfoViewHolder extends RecyclerView.ViewHolder{

        TextView card_points, card_date, card_position;

        ScoreInfoViewHolder(View itemView){
            super(itemView);

            card_points = itemView.findViewById(R.id.card_points);
            card_date = itemView.findViewById(R.id.card_date);
            card_position = itemView.findViewById(R.id.card_position);
        }
    }
}
