package nl.openweb.tools;

public class Tools {
    public static String getEnvironmentInfo() {
        final StringBuilder output = new StringBuilder();
        System.getenv().forEach((key, value) -> output.append(
                key)
                .append(": ")
                .append(value)
                .append("\n"));
        return output.toString();
    }
}
