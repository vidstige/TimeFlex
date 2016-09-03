package se.vidstige.timeflex;

import java.util.Date;

public class Punch {

    enum Direction {
        IN,
        OUT
    }

    private String user;
    private String workplace;
    private Direction direction;
    private Date timestamp;

    public static Punch in(Date timestamp)
    {
        return new Punch("vidstige", "volumental", Direction.IN, timestamp);
    }

    public static Punch out(Date timestamp) {
        return new Punch("vidstige", "volumental", Direction.OUT, timestamp);
    }

    public Punch(String user, String workplace, Direction direction, Date timestamp) {
        this.user = user;
        this.workplace = workplace;
        this.direction = direction;
        this.timestamp = timestamp;
    }

    public Direction getDirection() {
        return direction;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUser() {
        return user;
    }

    public String getWorkplace() {
        return workplace;
    }
}
