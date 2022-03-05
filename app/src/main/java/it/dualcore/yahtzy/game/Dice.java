package it.dualcore.yahtzy.game;

import java.util.Random;


/***************************************************************************************************
 *
 *  a simple object representing a dice
 *
 **************************************************************************************************/

public class Dice {

    private int upperFace;
    private boolean kept; // when true Dice is selected and will not be rolled

    public Dice() {
        this.upperFace = 0;
        this.kept = false;
    }

    public int getUpperFace() {
        return upperFace;
    }

    public boolean isKept() {
        return kept;
    }

    public void switchKept() {
        kept = !kept;
    }

    public void roll() {
        upperFace = new Random().nextInt(6) + 1;
    }

    public void reset() {
        this.kept = false;
    }
}
