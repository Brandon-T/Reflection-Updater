package acid.other;

/**
 * Created by Kira on 2014-12-06.
 */
public class Utilities {

    private static Utilities instance = new Utilities();

    private Utilities() {
    }

    public Utilities getInstance() {
        return instance;
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }

    public static String padRight(String s, int n, char padChar) {
        StringBuilder padded = new StringBuilder(s);
        while (padded.length() < n) {
            padded.append(padChar);
        }
        return padded.toString();
    }

    public static String padMiddle(String s, String t, int n) {
        StringBuilder padded = new StringBuilder(s);
        while (padded.length() + t.length() < n) {
            padded.append(' ');
        }
        return padded.append(t).toString();
    }

    public static String padMiddle(String s, String t, int n, char padChar) {
        StringBuilder padded = new StringBuilder(s);
        while (padded.length() + t.length() < n) {
            padded.append(padChar);
        }
        return padded.append(t).toString();
    }

    public static String padLeft(String s, int n, char padChar) {
        StringBuilder padded = new StringBuilder(s);
        while (padded.length() < n) {
            padded.insert(0, padChar);
        }
        return padded.toString();
    }
}
