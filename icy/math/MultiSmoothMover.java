/**
 * 
 */
package icy.math;

import icy.math.SmoothMover.SmoothMoveType;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

/**
 * @author Stephane
 */
public class MultiSmoothMover implements ActionListener
{
    public static interface MultiSmoothMoverListener
    {
        public void moveStarted(MultiSmoothMover source, int index, double start, double end);

        public void moveModified(MultiSmoothMover source, int index, double start, double end);

        public void moveEnded(MultiSmoothMover source, int index, double value);

        public void valueChanged(MultiSmoothMover source, int index, double newValue, int pourcent);
    }

    public static class MultiSmoothMoverAdapter implements MultiSmoothMoverListener
    {
        @Override
        public void moveStarted(MultiSmoothMover source, int index, double start, double end)
        {
        }

        @Override
        public void moveModified(MultiSmoothMover source, int index, double start, double end)
        {
        }

        @Override
        public void moveEnded(MultiSmoothMover source, int index, double value)
        {
        }

        @Override
        public void valueChanged(MultiSmoothMover source, int index, double newValue, int pourcent)
        {
        }
    }

    /**
     * current value
     */
    private double[] currentValues;
    /**
     * smooth movement type
     */
    private SmoothMoveType type;
    /**
     * time to do move (in ms)
     */
    private int moveTime;

    /**
     * internals
     */
    private final Timer timer;
    private boolean[] moving;
    private double[] destValues;
    private double[][] stepValues;
    // private int stepIndex;
    private long[] startTime;
    private final ArrayList<MultiSmoothMoverListener> listeners;

    public MultiSmoothMover(int size, SmoothMoveType type)
    {
        super();

        currentValues = new double[size];
        moving = new boolean[size];
        destValues = new double[size];
        startTime = new long[size];

        this.type = type;
        // 60 updates per second by default
        timer = new Timer(1000 / 60, this);
        // no initial delay
        timer.setInitialDelay(0);
        timer.setRepeats(true);
        // timer always running here
        timer.start();
        // default : 1 second to reach destination
        moveTime = 1000;
        // default
        stepValues = new double[size][0];

        listeners = new ArrayList<MultiSmoothMoverListener>();
    }

    public MultiSmoothMover(int size)
    {
        this(size, SmoothMoveType.LINEAR);
    }

    /**
     * Move the value at specified index to 'value'
     */
    public void moveTo(int index, double value)
    {
        if (destValues[index] != value)
        {
            destValues[index] = value;
            // start movement
            start(index, System.currentTimeMillis());
        }
    }

    /**
     * Move all values
     */
    public void moveTo(double[] values)
    {
        final int maxInd = Math.min(values.length, destValues.length);

        // first we check we have at least one value which had changed
        boolean changed = false;
        for (int index = 0; index < maxInd; index++)
        {
            if (destValues[index] != values[index])
            {
                changed = true;
                break;
            }
        }

        // value changed ?
        if (changed)
        {
            // better synchronization for multiple changes
            final long time = System.currentTimeMillis();

            for (int index = 0; index < maxInd; index++)
            {
                destValues[index] = values[index];
                // start movement
                start(index, time);
            }
        }
    }

    public boolean isMoving(int index)
    {
        return moving[index];
    }

    public boolean isMoving()
    {
        for (boolean b : moving)
            if (b)
                return true;

        return false;
    }

    private void start(int index, long time)
    {
        final double current = currentValues[index];
        final double dest = destValues[index];

        // number of step to reach final value
        final int size = Math.max(moveTime / timer.getDelay(), 1);

        // calculate interpolation
        switch (type)
        {
            case NONE:
                stepValues[index] = new double[2];
                stepValues[index][0] = current;
                stepValues[index][1] = dest;
                break;

            case LINEAR:
                stepValues[index] = Interpolator.doLinearInterpolation(current, dest, size);
                break;

            case LOG:
                stepValues[index] = Interpolator.doLogInterpolation(current, dest, size);
                break;

            case EXP:
                stepValues[index] = Interpolator.doExpInterpolation(current, dest, size);
                break;
        }

        // notify and start
        if (!isMoving(index))
        {
            moveStarted(index, time);
            moving[index] = true;
        }
        else
            moveModified(index, time);
    }

    /**
     * Stop specified index
     */
    public void stop(int index)
    {
        // stop and notify
        if (isMoving(index))
        {
            moving[index] = false;
            moveEnded(index);
        }
    }

    /**
     * Stop all
     */
    public void stopAll()
    {
        // stop all
        for (int index = 0; index < moving.length; index++)
            if (moving[index])
                moveEnded(index);
    }

    /**
     * Shutdown the mover object (this actually stop internal timer and remove all listeners)
     */
    public void shutDown()
    {
        timer.stop();
        timer.removeActionListener(this);
        listeners.clear();
    }

    /**
     * @return the update delay (in ms)
     */
    public int getUpdateDelay()
    {
        return timer.getDelay();
    }

    /**
     * @param updateDelay
     *        the update delay (in ms) to set
     */
    public void setUpdateDelay(int updateDelay)
    {
        timer.setDelay(updateDelay);
    }

    /**
     * @return the smooth type
     */
    public SmoothMoveType getType()
    {
        return type;
    }

    /**
     * @param type
     *        the smooth type to set
     */
    public void setType(SmoothMoveType type)
    {
        this.type = type;
    }

    /**
     * @return the moveTime
     */
    public int getMoveTime()
    {
        return moveTime;
    }

    /**
     * @param moveTime
     *        the moveTime to set
     */
    public void setMoveTime(int moveTime)
    {
        // can't be < 1
        this.moveTime = Math.max(moveTime, 1);
    }

    /**
     * Immediately set the value
     */
    public void setValue(int index, double value)
    {
        // stop current movement
        stop(index);
        // directly set value
        destValues[index] = value;
        setCurrentValue(index, value, 100);
    }

    /**
     * Immediately set all values
     */
    public void setValues(double[] values)
    {
        final int maxInd = Math.min(values.length, destValues.length);

        for (int index = 0; index < maxInd; index++)
        {
            final double value = values[index];
            // stop current movement
            stop(index);
            // directly set value
            destValues[index] = value;
            setCurrentValue(index, value, 100);
        }
    }

    /**
     * @return the value
     */
    public double getValue(int index)
    {
        return currentValues[index];
    }

    /**
     * @return the destValue
     */
    public double getDestValue(int index)
    {
        return destValues[index];
    }

    /**
     * update current value from elapsed time
     */
    private void updateCurrentValue(int index, long time)
    {
        final int elapsedMsTime = (int) (time - startTime[index]);

        // move completed ?
        if ((type == SmoothMoveType.NONE) || (elapsedMsTime >= moveTime))
        {
            setCurrentValue(index, destValues[index], 100);
            // stop
            stop(index);
        }
        else
        {
            final int len = stepValues[index].length;
            final int ind = Math.min((elapsedMsTime * len) / moveTime, len - 2);
            // set value
            setCurrentValue(index, stepValues[index][ind + 1], (elapsedMsTime * 100) / moveTime);
        }
    }

    private void setCurrentValue(int index, double value, int pourcent)
    {
        if (currentValues[index] != value)
        {
            currentValues[index] = value;
            // notify value changed
            changed(index, value, pourcent);
        }
    }

    public void addListener(MultiSmoothMoverListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(MultiSmoothMoverListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Move started event
     */
    private void moveStarted(int index, long time)
    {
        startTime[index] = time;

        for (MultiSmoothMoverListener listener : listeners)
            listener.moveStarted(this, index, currentValues[index], destValues[index]);
    }

    /**
     * Move modified event
     */
    private void moveModified(int index, long time)
    {
        startTime[index] = time;

        for (MultiSmoothMoverListener listener : listeners)
            listener.moveModified(this, index, currentValues[index], destValues[index]);
    }

    /**
     * Move ended event
     */
    private void moveEnded(int index)
    {
        for (MultiSmoothMoverListener listener : listeners)
            listener.moveEnded(this, index, currentValues[index]);
    }

    /**
     * Value changed event
     * 
     * @param oldValue
     * @param newValue
     * @param i
     */
    private void changed(int index, double newValue, int pourcent)
    {
        for (MultiSmoothMoverListener listener : listeners)
            listener.valueChanged(this, index, newValue, pourcent);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        // better synchronization for multiple changes
        final long time = System.currentTimeMillis();

        // process only moving values
        for (int index = 0; index < moving.length; index++)
            if (moving[index])
                updateCurrentValue(index, time);
    }

}
