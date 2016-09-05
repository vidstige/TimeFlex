package se.vidstige.timeflex;

import java.util.Date;

public class Shift {

    private final Date start;
    private Date end;

    public Shift(Date start, Date end) {
        this.start = start;
    }

    public Shift(Date start) {
        this(start, null);
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}
