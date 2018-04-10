package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class TimetableURL {
    private final URL website;
    private List<String> lines = null;

    public TimetableURL(String url) throws MalformedURLException {
        website = new URL(url);
    }

    private void readFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(website.openStream()))) {
            lines = reader.lines()
                    .filter(string -> !string.isEmpty())
                    .collect(Collectors.toList());
        }
    }

    public List<String> getLines() throws IOException {
        if (lines == null) {
            readFromFile();
        }
        return lines;
    }

    public int getSemester() {
//        return 1;
        String[] parts = website.getPath().split("-");
        return parts[1].charAt(0) - '0';
    }
}
