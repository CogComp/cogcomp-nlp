package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhilifeng on 1/4/17.
 */
public class TimexChunk {

    private HashMap<String, String> attributes;
    private int charStart; //character offset
    private int charEnd;
    private String content;
    private Interval interval;

    public TimexChunk() {
        this.attributes = new HashMap<>();
    }

    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public String beginAnnotation(int tid) {
        return String.format("<TIMEX3 %s %s %s%s>",
                "tid=\"t" + String.valueOf(tid) + "\"",
                "type=\"" + (attributes.get("type") == null ? "DATE" : attributes.get("type") ) + "\"",
                "value=\"" + attributes.get("value") + "\"",
                attributes.get("mod") == null ? "" : " mod=\"" + attributes.get("mod") + "\"");
    }

    public String endAnnotation() {
        return "</TIMEX3>";
    }

    public int getCharStart() {
        return charStart;
    }

    public int getCharEnd() {
        return charEnd;
    }

    public void setCharStart(int charStart) {
        this.charStart = charStart;
    }

    public void setCharEnd(int charEnd) {
        this.charEnd = charEnd;
    }

    public String toTIMEXString() {
        return String.format("%s <TIMEX3 %s %s%s>",
                content,
                "type=\"" + (attributes.get("type") == null ? "DATE" : attributes.get("type") ) + "\"",
                "value=\"" + attributes.get("value") + "\"",
                attributes.get("mod") == null ? "" : " mod=\"" + attributes.get("mod") + "\"");
    }
}
