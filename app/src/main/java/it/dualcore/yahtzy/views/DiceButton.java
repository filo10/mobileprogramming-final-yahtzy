package it.dualcore.yahtzy.views;

import android.content.Context;
import android.util.AttributeSet;

import it.dualcore.yahtzy.R;
import it.dualcore.yahtzy.game.Dice;


/***************************************************************************************************
 *
 *  a widget to show the face of a Dice class object
 *
 **************************************************************************************************/

public class DiceButton extends android.support.v7.widget.AppCompatImageButton {

    private Dice dice;

    public DiceButton(Context context) {
        super(context);
        this.setBackgroundResource(R.drawable.dice_background_unselected);
    }

    public DiceButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setBackgroundResource(R.drawable.dice_background_unselected);
    }

    public DiceButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setBackgroundResource(R.drawable.dice_background_unselected);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // make it square

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }

    @Override
    public boolean isInEditMode() {
        return super.isInEditMode();
    }

    public void linkDiceObject(Dice dice) {
        this.dice = dice;
    }

    public void updateBackground() {
        if (this.dice.isKept())
            this.setBackgroundResource(R.drawable.dice_background_selected);
        else
            this.setBackgroundResource(R.drawable.dice_background_unselected);
    }

    public void updateForeground() {
        if (this.dice.getUpperFace() == 1)
            this.setImageResource(R.drawable.dice_face1);
        if (this.dice.getUpperFace() == 2)
            this.setImageResource(R.drawable.dice_face2);
        if (this.dice.getUpperFace() == 3)
            this.setImageResource(R.drawable.dice_face3);
        if (this.dice.getUpperFace() == 4)
            this.setImageResource(R.drawable.dice_face4);
        if (this.dice.getUpperFace() == 5)
            this.setImageResource(R.drawable.dice_face5);
        if (this.dice.getUpperFace() == 6)
            this.setImageResource(R.drawable.dice_face6);
    }

}
