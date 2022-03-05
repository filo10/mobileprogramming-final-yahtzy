package it.dualcore.yahtzy.score;

public class ScoreInfo implements Comparable<ScoreInfo> {

    private int points;
    private String date;

    public ScoreInfo(int points, String date) {
        this.points = points;
        this.date = date;
    }

    public int getPoints() {
        return points;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int compareTo(ScoreInfo o) {
        // rules to compare two ScoreInfo
        // we need in ShowScoreActivity to sort a List<ScoreInfo> by highest number of points
        // so we need to return a positive number when ScoreInfo "o" has less points than this object
        // basically a reversed regular int sort.
        //
        // which shortly means:
        //              - (more points) > (less points)
        //              - (less points) < (more points)
        //              - (x points) == (x points)

        return o.getPoints() - getPoints();
    }
}
