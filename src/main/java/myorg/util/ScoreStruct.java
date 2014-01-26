package myorg.util;

public class ScoreStruct implements Comparable<ScoreStruct> {
    public float predict = 0.0f;
    public float positive = 0;
    public float negative = 0;

    public int compareTo(ScoreStruct o) {
        float diff = this.predict - o.predict;
        return (diff == 0.0f ? 0 : (diff > 0.0f ? -1 : 1));
    }
}
