package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 10.09.2016.
 */
public class ParseURL {
    /**
     * Holds the url to the timetable.
     */
    private final URL website;

    /**
     * Contains the HTML lines of website.
     */
    private List<String> lines = null;

    /**
     * Constructor for ParseURL
     *
     * @param url - the url to the timetable
     * @throws MalformedURLException
     */
    public ParseURL(String url) throws MalformedURLException {
        website = new URL(url);
    }

    /**
     * Reads and stores the source code of the website
     *
     * @throws IOException
     */
    private void readFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(website.openStream()))) {
            lines = reader.lines()
                    .filter(string -> !string.isEmpty())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Getter for the source code of the website
     *
     * @return a list of all the lines
     * @throws IOException
     */
    public List<String> getLines() throws IOException {
        if (lines == null) {
            readFromFile();
        }
        return lines;
    }

    /**
     * Determines the semester of the timetable from the url
     *
     * @return 1 if the timetable is for the first semester and 2 otherwise
     */
    public int getSemester() {
        String[] parts = website.getPath().split("-");
        return parts[1].charAt(0) - '0';
    }

}