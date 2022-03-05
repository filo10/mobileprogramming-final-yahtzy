package it.dualcore.yahtzy.game;

import it.dualcore.yahtzy.views.DiceButton;


/***************************************************************************************************
 *
 *  a set of 5 dice
 *
 **************************************************************************************************/

public class Combination {

    private Dice[] dice = new Dice[5];

    public Combination() {
        for (int i = 0; i< 5; i++) {
            this.dice[i] = new Dice();
        }
    }

    public Dice[] getDice() {
        return this.dice;
    }

    public void roll() {
        for (Dice dice : this.dice) {
            if (!dice.isKept())
                dice.roll();
        }
    }

    public void link(DiceButton[] diceButtons) {
        // link every Dice with a diceButton
        if (diceButtons.length != 5) {
            // error
            return;
        }
        for (int i = 0; i < 5; i++) {
            diceButtons[i].linkDiceObject(this.dice[i]);
        }
    }

    public void reset() {
        for (Dice dice : this.dice)
            dice.reset();
    }

    public boolean isYahtzee() { //can't use a TM...
        int face = this.getDice()[0].getUpperFace();
        for (int i = 1; i<5; i++){
            if (this.getDice()[i].getUpperFace() != face)
                return false;
        }
        return true;
    }

    public int oneScore(){
        int score = 0;
        for(Dice dice: this.getDice()){
            if (dice.getUpperFace() == 1)
            score += 1;
        }
        return score;
    }

    public int twoScore (){
        int score = 0;
        for(Dice dice: this.getDice()){
            if (dice.getUpperFace() == 2)
                score += 2;
        }
        return score;
    }

    public int threeScore (){
        int score = 0;
        for(Dice dice: this.getDice()){
            if (dice.getUpperFace() == 3)
                score += 3;
        }
        return score;
    }

    public int fourScore (){
        int score = 0;
        for(Dice dice: this.getDice()){
            if (dice.getUpperFace() == 4)
                score += 4;
        }
        return score;
    }

    public int fiveScore (){
        int score = 0;
        for(Dice dice: this.getDice()){
            if (dice.getUpperFace() == 5)
                score += 5;
        }
        return score;
    }

    public int sixScore (){
        int score = 0;
        for(Dice dice: this.getDice()){
            if (dice.getUpperFace() == 6)
                score += 6;
        }
        return score;
    }

    public int smallStraightScore() {
        int[] check = {0, 0, 0, 0, 0, 0};
        for (Dice dice : this.getDice()) {
            check[dice.getUpperFace()-1] = 1;
        }
        if (check[2] == 1 && check[3] == 1) {
            if ((check[0] == 1 && check[1] == 1) || (check[1] == 1 && check[4] == 1) || (check[4] == 1 && check[5] == 1))
                return 30;
        }
        return 0;
    }

    public int largeStraightScore(){
        int[] checkArr = {0, 0, 0, 0, 0, 0};
        for (Dice dice : this.getDice()) {
            checkArr[dice.getUpperFace()-1] = 1;
        }
        if (checkArr[1] == 1 && checkArr[2] == 1 && checkArr[3] == 1 && checkArr[4] == 1 ){
            if (checkArr[0] == 1 || checkArr[5] == 1) return 40;
        }
        return 0;
    }

    public int threeOfAKindScore(){
        int score = 0;
        int[] checkArr = {0, 0, 0, 0, 0, 0};
        for (Dice dice : this.getDice()) {
            checkArr[dice.getUpperFace()-1] += 1;
            score += dice.getUpperFace();
        }
        for (int check : checkArr){
            if (check >= 3) return score;
        }
        return 0;
    }

    public int fourOfAKindScore(){
        int score = 0;
        int[] checkArr = {0, 0, 0, 0, 0, 0};
        for (Dice dice : this.getDice()) {
            checkArr[dice.getUpperFace()-1] += 1;
            score += dice.getUpperFace();
        }
        for (int check : checkArr){
            if (check >= 4) return score;
        }
        return 0;
    }

    public int fullHouseScore(){
        int[] checkArr = {0, 0, 0, 0, 0, 0};
        for (Dice dice : this.getDice()) {
            checkArr[dice.getUpperFace()-1] += 1;
        }
        for (int check : checkArr) {
            if (check == 1 || check > 3) return 0;
        }
        return 25;
    }

    public int yatzheeScore(){
        if (isYahtzee())
            return 50;
        return 0;
    }

    public int getChanceScore(){
        int score = 0;
        for (Dice dice : this.getDice()) {
            score += dice.getUpperFace();
        }
        return score;
    }

}
