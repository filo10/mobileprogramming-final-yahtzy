package it.dualcore.yahtzy.game;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import it.dualcore.yahtzy.R;
import it.dualcore.yahtzy.views.DiceButton;
import it.dualcore.yahtzy.views.ScoreButton;
import it.dualcore.yahtzy.views.ScoreView;


public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private Button rollButton, signButton, giveUpButton, bonusView;
    private TextView tv_score, tv_yahtzyAnim, tv_yahtzyBonusCounter;
    private DiceButton[] diceButtons = new DiceButton[5];
    private ScoreButton lastScoreBtn = null;
    private ScoreView[] scoreViews = new ScoreView[13];

    private Combination combination;
    private int rollCounter, turnCounter, score, bonusScore, yahtzyBonusCounter;
    private final int rollAnimations = 20;
    private int[] animationFaceDice = new int[5];
    private int[] diceImages = new int[] {R.drawable.dice_face1, R.drawable.dice_face2, R.drawable.dice_face3, R.drawable.dice_face4, R.drawable.dice_face5, R.drawable.dice_face6};
    private Handler animationHandler, animationEndedHandler;
    private boolean bonusWasAssigned, animIsRunning, yahtzyBoxSigned;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // handle Android UI
        hideUI();
        autoHideUI();

        // setting the widgets
        rollButton = findViewById(R.id.rollButton);
        rollButton.setOnClickListener(this);

        signButton = findViewById(R.id.signButton);
        signButton.setOnClickListener(this);

        giveUpButton = findViewById(R.id.giveUpButton);
        giveUpButton.setOnClickListener(this);

        Button instructionButton = findViewById(R.id.instructionButtonGame);
        instructionButton.setOnClickListener(this);

        tv_score = findViewById(R.id.tv_score);

        tv_yahtzyAnim = findViewById(R.id.tv_animation);
        tv_yahtzyAnim.setVisibility(View.GONE);

        tv_yahtzyBonusCounter = findViewById(R.id.tv_yahtzyBonusCounter);
        tv_yahtzyBonusCounter.setVisibility(View.GONE);

        bonusView = findViewById(R.id.bonus);

        initTable();
        initDice();
        hideDice();


        // init the game
        this.start();


        // handling concurrency between animations and UI:
        // while dice are rolling, update their faces with the info from the rollAnimation() thread
        animationHandler = new Handler() {
            public void handleMessage(Message msg) {
                for (int i = 0; i < 5; i++) {
                    if (!combination.getDice()[i].isKept())
                        diceButtons[i].setImageResource(diceImages[animationFaceDice[i]]);
                }
            }
        };

        // when rolling animation is ended
        animationEndedHandler = new Handler() {
            public void handleMessage(Message msg) {

                // do the actual roll and show it
                combination.roll();
                updateEveryDiceForeground();

                // re-enable the diceButtons
                for(int i = 0; i < 5; i++)  diceButtons[i].setEnabled(true);

                // check how many times player has rolled for this turn
                if (rollCounter < 3) {
                    rollCounter++;
                    if (rollCounter == 3) {
                        disableGameButton(rollButton);
                        rollButton.setText(getString(R.string.roll_0_left));
                    }
                    else{
                        enableGameButton(rollButton);
                        rollButton.setText(String.format(Locale.getDefault(), "%s (%d)", getString(R.string.roll), 3 - rollCounter));
                    }
                }

                // if a yahtzy rolled out
                if (combination.isYahtzee()){
                    // play a sound, show a text
                    playSoundFX(R.raw.ta_da);
                    // check if Yahtzy Bonus can be given
                    if (yahtzyBoxSigned) {
                        textAnimation("Yahtzy\nBonus!\n+100");
                        score += 100;
                        yahtzyBonusCounter++;
                        tv_yahtzyBonusCounter.setText(String.format(Locale.getDefault(), "Yahtzy\nBonus x%d", yahtzyBonusCounter));
                        tv_yahtzyBonusCounter.setVisibility(View.VISIBLE);
                    }
                    else
                        textAnimation(String.format("%s!", getString(R.string.app_name)));
                }

                // show the player what score can he achieve with the actual roll for each combination
                updateTable();
            }
        };
    }

    public void start() {
        // init a new game

        // create the 5 dice
        combination = new Combination();

        // link diceButtons to Dice objects
        combination.link(diceButtons);

        // (re)set widgets
        for(int i = 0; i<13; i++)
            scoreViews[i].reset();
        tv_score.setText(getString(R.string.str_tv_score));
        giveUpButton.setText(getString(R.string.give_up));
        bonusView.setText("0/63");
        bonusView.setTextColor(0xFFFFFFFF);
        lastScoreBtn = null;
        tv_yahtzyBonusCounter.setVisibility(View.GONE);
        disableGameButton(signButton);
        enableGameButton(rollButton);

        // (re)set variables
        rollCounter = 0;
        turnCounter = 0;
        score = 0;
        bonusScore = 0;
        yahtzyBonusCounter = 0;
        bonusWasAssigned = false;
        yahtzyBoxSigned = false;
    }

    public void end() {
        // what to do when a game is ended

        // check if the actual score is a new highscore
        boolean isHighscore = isScoreInTopTen(score);

        // if a new highscore is set
        if (isHighscore) {
            // wait 3 seconds for the animation to end and...
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // ask the player what to do
                    gameEndedDialog();
                }
            }, 3000);
        }
        else    // do not wait 3 seconds
            gameEndedDialog();  // ask the player what to do
    }

    public boolean isScoreInTopTen(int score) {
        // check if the score is in the top 10
        // if so save the actual score (and date) in sharedPreferences overriding the worst

        // get shared pref
        SharedPreferences sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE);

        // keys for the top 10 scores
        String [] score_strings = new String[] {"bestScore0", "bestScore1", "bestScore2", "bestScore3", "bestScore4", "bestScore5", "bestScore6", "bestScore7", "bestScore8", "bestScore9"};

        int minIndex = 10;
        int min = 9999;

        // searching for the minimum best score
        for (int i = 0; i < 10; i++) {
            int current = sharedPref.getInt(score_strings[i], 0);
            if (current < min) {
                min = current;
                minIndex = i;
            }
        }

        // check if the actual score is in top 10; if minIndex >= 10 something went wrong
        if (score > sharedPref.getInt(score_strings[minIndex], 0) && (minIndex < 10)) {
            SharedPreferences.Editor editor = sharedPref.edit();

            // save the number of points
            editor.putInt(score_strings[minIndex], score);

            // get the date
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);

            // save the date
            editor.putString("bestScore"+Integer.toString(minIndex)+"_date", String.format(Locale.getDefault(),"%d/%d/%d", day, month, year));

            editor.apply();

            // celebrate the player
            playSoundFX(R.raw.jackpot);
            textAnimation(getString(R.string.new_highscore));

            return true;
        }

        return false;
    }

    private void disableGameButton(Button button) {
        button.setEnabled(false);
        button.setTextColor(0xFFAAAAAA);
    }

    public void enableGameButton(Button button){
        button.setEnabled(true);
        button.setTextColor(0xFFFFFFFF);
    }

    public void initDice() {
        // setting the widgets

        diceButtons[0] = findViewById(R.id.dice0);
        diceButtons[1] = findViewById(R.id.dice1);
        diceButtons[2] = findViewById(R.id.dice2);
        diceButtons[3] = findViewById(R.id.dice3);
        diceButtons[4] = findViewById(R.id.dice4);

        for(int i = 0; i < 5; i++)
            diceButtons[i].setOnClickListener(this);
    }

    public void hideDice() {
        // make the dice not visible
        for(int i = 0; i < 5; i++)
            diceButtons[i].setVisibility(View.INVISIBLE);
    }

    public void showDice() {
        // make dice visible
        for(int i = 0; i < 5; i++)
            diceButtons[i].setVisibility(View.VISIBLE);
    }

    public void updateEveryDiceForeground() {
        // update the face of the dice
        for(int i = 0; i < 5; i++)
            diceButtons[i].updateForeground();
    }

    public void updateEveryDiceBackground() {
        // show, changing the diceButton background, if a dice is selected
        for(int i = 0; i < 5; i++)
            diceButtons[i].updateBackground();
    }

    public void initTable() {
        // setting the widgets

        scoreViews[0] = findViewById(R.id.oneScore);
        scoreViews[1] = findViewById(R.id.twoScore);
        scoreViews[2] = findViewById(R.id.threeScore);
        scoreViews[3] = findViewById(R.id.fourScore);
        scoreViews[4] = findViewById(R.id.fiveScore);
        scoreViews[5] = findViewById(R.id.sixScore);
        scoreViews[6] = findViewById(R.id.threeOfAKindScore);
        scoreViews[7] = findViewById(R.id.fourOfAKindScore);
        scoreViews[8] = findViewById(R.id.smallStraightScore);
        scoreViews[9] = findViewById(R.id.largeStraightScore);
        scoreViews[10] = findViewById(R.id.fullHouseScore);
        scoreViews[11] = findViewById(R.id.yatzheeScore);
        scoreViews[12] = findViewById(R.id.chanceScore);

        for(int i = 0; i<13; i++){
            scoreViews[i].getScoreButton().setOnClickListener(this);
        }
    }

    public void updateTable() {
        // calculate the score of the actual roll for each combination

        scoreViews[0].showScore(combination.oneScore());
        scoreViews[1].showScore(combination.twoScore());
        scoreViews[2].showScore(combination.threeScore());
        scoreViews[3].showScore(combination.fourScore());
        scoreViews[4].showScore(combination.fiveScore());
        scoreViews[5].showScore(combination.sixScore());
        scoreViews[6].showScore(combination.threeOfAKindScore());
        scoreViews[7].showScore(combination.fourOfAKindScore());
        scoreViews[8].showScore(combination.smallStraightScore());
        scoreViews[9].showScore(combination.largeStraightScore());
        scoreViews[10].showScore(combination.fullHouseScore());
        scoreViews[11].showScore(combination.yatzheeScore());
        scoreViews[12].showScore(combination.getChanceScore());

        // below this rules to apply the Joker Rule (free choice version)

        // if a yahtzy was just rolled and yahtzy score box was checked in this game before this turn
        if (yahtzyBoxSigned && combination.isYahtzee()) {
            ScoreButton yahtzyFaceRolled = scoreViews[combination.getDice()[0].getUpperFace() - 1].getScoreButton();
            // check if the upper section box corresponding to the face of the yahtzy just rolled was not used
            if (yahtzyFaceRolled.isSigned()) {
                scoreViews[8].showScore(30);    // joker small straight
                scoreViews[9].showScore(40);    // joker large straight
                scoreViews[10].showScore(25);   // joker full house
            }
        }
    }

    @Override
    public void onClick(View v) {

        hideUI();

        // make possible for the player to select a combination only if dice are not rolling
        if (!animIsRunning) {
            for (ScoreView scoreView : scoreViews) {
                ScoreButton scoreBtn = scoreView.getScoreButton();
                // if the player tap on a ScoreButton not selected and rolled at least once
                if (v == scoreBtn && !scoreBtn.isSigned() && rollCounter != 0) {
                    // if he tap the ScoreButton selected, unselect it, make it impossible to sign
                    if (lastScoreBtn == scoreBtn) {
                        scoreBtn.unselect();
                        scoreBtn.updateBackground();
                        lastScoreBtn = null;
                        disableGameButton(signButton);
                        break;
                    }
                    // if another ScoreButton is selected, unselect it
                    if (lastScoreBtn != null) {
                        lastScoreBtn.unselect();
                        lastScoreBtn.updateBackground();
                    }
                    // and select the one pressed, make possible to sign
                    scoreBtn.select();
                    scoreBtn.updateBackground();
                    lastScoreBtn = scoreBtn;
                    enableGameButton(signButton);
                    break;
                }
            }
        }

        if (v.getId() == R.id.rollButton) {

            showDice();

            // if a combination is selected, unselect it
            if(lastScoreBtn != null) {
                lastScoreBtn.unselect();
                lastScoreBtn.updateBackground();
                lastScoreBtn = null;
            }
            // if all 5 dice are selected
            if (combination.getDice()[0].isKept() && combination.getDice()[1].isKept() &&
                    combination.getDice()[2].isKept() && combination.getDice()[3].isKept() &&
                    combination.getDice()[4].isKept() ) {
                // "waste" a roll
                if (rollCounter < 3) {
                    rollCounter++;
                    if (rollCounter == 3) {
                        disableGameButton(rollButton);
                        rollButton.setText(getString(R.string.roll_0_left));
                    }
                    else{
                        enableGameButton(rollButton);
                        rollButton.setText(String.format(Locale.getDefault(), "%s (%d)", getString(R.string.roll), 3 - rollCounter));
                    }
                }
                // and don't play the animation
                return;
            }
            rollAnimation();
        }

        if (v.getId() == R.id.signButton) {

            for(int i=0; i <13; i++){
                boolean oldSign = scoreViews[i].getScoreButton().isSigned();
                scoreViews[i].signScore();
                if( (i < 6) && (scoreViews[i].getScoreButton().isSigned() != oldSign) && (bonusScore < 63)){
                    bonusScore += Integer.parseInt(scoreViews[i].getScoreButton().getText().toString());
                }
                if(!scoreViews[i].getScoreButton().isSigned())
                    scoreViews[i].getScoreButton().setText("");
            }
            if (bonusScore >= 63){
                if (!bonusWasAssigned) {
                    bonusView.setText("+ 35");
                    bonusView.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                    bonusView.setTextSize(18);
                    score += 35;
                    bonusWasAssigned = true;
                }
            }
            else
                bonusView.setText(String.format(Locale.getDefault(), "%d/63", bonusScore));
            score += Integer.parseInt(lastScoreBtn.getText().toString());

            // if the player is signing the Yahtzy ScoreButton having rolled a Yahtzy
            if ((lastScoreBtn == scoreViews[11].getScoreButton()) && (Integer.parseInt(lastScoreBtn.getText().toString()) == 50))
                yahtzyBoxSigned = true; // make it eligible for the Yahtzy Bonus in case he'll roll another Yahtzy

            tv_score.setText(String.format(Locale.getDefault(), "%s %d", getString(R.string.str_tv_score), score));
            lastScoreBtn = null;

            hideDice();
            combination.reset();
            updateEveryDiceBackground();
            disableGameButton(signButton);
            enableGameButton(rollButton);
            rollButton.setText(getString(R.string.roll));
            rollCounter = 0;
            turnCounter++;
            if(turnCounter == 13) {
                disableGameButton(rollButton);
                giveUpButton.setText(getString(R.string.exit));
                end();
            }
        }

        if (v.getId() == R.id.giveUpButton) {
            if (turnCounter == 13)
                gameEndedDialog();
            else
                exitDialog();
        }

        if (v.getId() == R.id.instructionButtonGame)
            instructionDialog();

        // select and unselect a dice to keep it and not roll it

        if (v.getId() == R.id.dice0) {
            combination.getDice()[0].switchKept();
            diceButtons[0].updateBackground();
        }

        if (v.getId() == R.id.dice1) {
            combination.getDice()[1].switchKept();
            diceButtons[1].updateBackground();
        }

        if (v.getId() == R.id.dice2) {
            combination.getDice()[2].switchKept();
            diceButtons[2].updateBackground();
        }

        if (v.getId() == R.id.dice3) {
            combination.getDice()[3].switchKept();
            diceButtons[3].updateBackground();
        }

        if (v.getId() == R.id.dice4) {
            combination.getDice()[4].switchKept();
            diceButtons[4].updateBackground();
        }

    }

    public void hideUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public void autoHideUI() {
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 2600ms
                                    hideUI();
                                }
                            }, 2600);
                        }
                    }
                });
    }

    public void exitDialog() {
        // ask the player what to do if he press back while the game is not ended yet

        AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DialogTheme));
        dialog.setTitle(getString(R.string.give_up));
        dialog.setMessage(getString(R.string.give_up_message));
        dialog.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener(){
            @Override public void onClick(DialogInterface dialog, int which){ GameActivity.super.onBackPressed();} });
        dialog.setNegativeButton(getString(R.string.stay), new DialogInterface.OnClickListener(){
            @Override public void onClick(DialogInterface dialog, int which){ } });
        dialog.create().show();
    }

    public void gameEndedDialog() {
        // ask the player what to do once the game is ended

        AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DialogTheme));
        dialog.setTitle(getString(R.string.game_ended));
        dialog.setMessage(String.format(Locale.getDefault(), getString(R.string.game_ended_message), score));
        dialog.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener(){
            @Override public void onClick(DialogInterface dialog, int which){ GameActivity.super.onBackPressed();} });
        dialog.setNegativeButton(getString(R.string.play_again), new DialogInterface.OnClickListener(){
            @Override public void onClick(DialogInterface dialog, int which){ start(); } });
        dialog.create().show();
    }

    public void instructionDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DialogTheme));
        dialog.setTitle(getString(R.string.instruction_title));
        dialog.setMessage(getString(R.string.instruction_message));
        dialog.setNegativeButton(getString(R.string.got_it), new DialogInterface.OnClickListener(){
            @Override public void onClick(DialogInterface dialog, int which){ }});
        dialog.create().show();
    }

    @Override
    public void onBackPressed() {
        // when Android back button is pressed, don't go straight back
        if (turnCounter == 13)
            gameEndedDialog();
        else
            exitDialog();
    }

    public void rollAnimation() {
        disableGameButton(rollButton);
        disableGameButton(signButton);
        for(int i = 0; i < 5; i++)
            diceButtons[i].setEnabled(false);

        // *they see me rolling*
        animIsRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < rollAnimations; i++) {
                    threadRollAnimation();
                }
                animationEndedHandler.sendEmptyMessage(0);
                animIsRunning = false;
            }
        }).start();

        // play a sound
        MediaPlayer mp = MediaPlayer.create(this, R.raw.rolling_sound);
        try {
            mp.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.start();
    }

    public void threadRollAnimation() {
        for (int i = 0; i < 5; i++)
            animationFaceDice[i] = new Random().nextInt(6);
        synchronized (getLayoutInflater()) {
            animationHandler.sendEmptyMessage(0);
        }
        try { // delay to alloy for smooth animation
            int delayTime = 60;
            Thread.sleep(delayTime);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void textAnimation(String text) {
        // show some text for a moment on top of the screen
        Animation animationIn = AnimationUtils.loadAnimation(this, R.anim.yathzi_anim_in);
        Animation animationOut = AnimationUtils.loadAnimation(this, R.anim.yathzi_anim_out);
        tv_yahtzyAnim.setText(text);
        tv_yahtzyAnim.setVisibility(View.VISIBLE);
        tv_yahtzyAnim.startAnimation(animationIn);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 2600ms
                tv_yahtzyAnim.startAnimation(animationOut);
                tv_yahtzyAnim.setVisibility(View.GONE);
            }
        }, 2600);
    }

    public void playSoundFX(int res_sound) {
        // play!
        MediaPlayer mp = MediaPlayer.create(this, res_sound);
        try {
            mp.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.start();
    }

}
