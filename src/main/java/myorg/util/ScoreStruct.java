package myorg.util;

public class ScoreStruct implements Comparable {
    public float predict = 0.0f;
    public int positive = 0;
    public int negative = 0;

    public int compareTo(Object o) {
        float diff = this.predict - ((ScoreStruct)o).predict;
        return (diff == 0.0f ? 0 : (diff > 0.0f ? -1 : 1));
    }
}
