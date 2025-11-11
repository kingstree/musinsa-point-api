package musinsa.points.common.log.util;

public class StackTraceUtil {
    private static final int MAX_CHARS = 8000;
    public static String toTrimmedString(Throwable t) {
        if (t == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(t.toString()).append(" ");
        for (StackTraceElement e : t.getStackTrace()) {
            sb.append(" at ").append(e).append(" ");
            if (sb.length() > MAX_CHARS) break;
        }
        return sb.toString();
    }
}
