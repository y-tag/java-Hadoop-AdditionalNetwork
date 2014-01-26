package myorg.util;

public class ScoreStruct implements Comparable<ScoreStruct> {
    public float value = 0.0f;
    public float positive = 0.0f;
    public float negative = 0.0f;

    public int compareTo(ScoreStruct o) {
        float diff = this.value - o.value;
        return (diff == 0.0f ? 0 : (diff > 0.0f ? -1 : 1));
    }
}
