package it.dualcore.yahtzy.views;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import java.util.Locale;

import it.dualcore.yahtzy.R;

public class ScoreButton extends AppCompatButton {
    private boolean selected = false;
    private boolean signed = false;

    public ScoreButton(Context context) {
        super(context);
        this.setBackgroundResource(R.drawable.score_background_unselected);
    }

    public ScoreButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setBackgroundResource(R.drawable.score_background_unselected);
    }

    public ScoreButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setBackgroundResource(R.drawable.score_background_unselected);
    }


    public void sign(){
        if (!this.signed && this.selected) {
            this.signed = true;
            this.setBackgroundResource(R.drawable.score_background_unselected);
            this.setTextColor(getResources().getColor(R.color.colorPrimary, null));
        }
    }

    public void showScore(int score){
        if (!this.signed)
            this.setText(String.format(Locale.getDefault(),"%d", score));
    }

    public boolean isSigned(){
        return this.signed;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void select() {
        this.selected = true;
    }

    public void unselect(){
        this.selected = false;
    }

    @Override
    public boolean isInEditMode() {
        return super.isInEditMode();
    }

    public void updateBackground(){
        if (this.selected) {
            this.setBackgroundResource(R.drawable.score_background_selected);
        }
        else{
            this.setBackgroundResource(R.drawable.score_background_unselected);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }

    public void reset() {
        this.setText("");
        this.setTextColor(0xFFFFFFFF);
        this.selected = false;
        this.signed = false;
    }
}
