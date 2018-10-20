/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
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
