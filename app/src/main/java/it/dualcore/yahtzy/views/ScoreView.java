package it.dualcore.yahtzy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.dualcore.yahtzy.R;

public class ScoreView extends LinearLayout {
    private ScoreButton scoreBtn;

    public ScoreView(Context context){
        super(context);
        initializeViews(context);
    }

    public ScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ScoreView, 0, 0);
        String scoreText = a.getString(R.styleable.ScoreView_scoreText);
        Drawable drawable = a.getDrawable((R.styleable.ScoreView_scoreImage));
        a.recycle();
        initializeViews(context, scoreText, drawable);
    }

    public ScoreButton getScoreButton(){
        return scoreBtn;
    }

    public void initializeViews(Context context, String text, Drawable drawable){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.score_view, this);
        TextView tv = (TextView) getChildAt(0);
        ImageView iv = (ImageView) getChildAt(1);
        scoreBtn = (ScoreButton) getChildAt(2);
        tv.setText(text);
        iv.setImageDrawable(drawable);
    }

    public void initializeViews(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.score_view, this);
    }

    public void signScore(){
        scoreBtn = (ScoreButton) getChildAt(2);
        scoreBtn.sign();
    }

    public void showScore(int score){
        scoreBtn = (ScoreButton) getChildAt(2);
        scoreBtn.showScore(score);
    }

    public void reset(){
        this.scoreBtn.reset();
    }

}
