package edu.illinois.cs.cogcomp.core.utilities;

/**
 * This is a utility for tracking execution time.
 *
 * @author vivek
 */
public class ExecutionTimer {
    protected long _start;

    protected long _end;

    protected boolean started;

    public ExecutionTimer() {
        reset();
    }

    public void start() {
        _start = System.currentTimeMillis();
        started = true;
    }

    public void end() {
        _end = System.currentTimeMillis();
        started = false;
    }

    public long getTimeMillis() {
        _end = System.currentTimeMillis();
        return (_end - _start);
    }

    public long getTimeSeconds() {
        return getTimeMillis() / 1000;
    }

    public void reset() {
        _start = 0;
        _end = 0;
        started = false;

    }
}
