/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.painter;

import icy.canvas.IcyCanvas;
import icy.file.xml.XMLPersistent;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.util.EventUtil;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class Anchor2D extends AbstractPainter implements XMLPersistent
{
    public interface Anchor2DListener extends PainterListener
    {
        public void positionChanged(Anchor2D source);
    }

    private static final String ID_COLOR = "color";
    private static final String ID_SELECTEDCOLOR = "selected_color";
    // private static final String ID_SELECTED = "selected";
    private static final String ID_POS_X = "pos_x";
    private static final String ID_POS_Y = "pos_y";
    private static final String ID_RAY = "ray";
    private static final String ID_VISIBLE = "visible";

    protected static final int DEFAULT_RAY = 6;
    protected static final Color DEFAULT_NORMAL_COLOR = Color.YELLOW;
    protected static final Color DEFAULT_SELECTED_COLOR = Color.WHITE;

    /**
     * position (canvas)
     */
    protected final Point2D.Double position;
    /**
     * radius
     */
    protected int ray;

    /**
     * color
     */
    protected Color color;
    /**
     * selection color
     */
    protected Color selectedColor;
    /**
     * selection flag
     */
    protected boolean selected;
    // /**
    // * flag that indicate if anchor can be removed / deleted
    // */
    // private boolean canRemove;
    /**
     * flag that indicate if anchor is visible
     */
    protected boolean visible;

    /**
     * internals
     */
    private final Ellipse2D ellipse;

    /**
     * @param sequence
     * @param x
     * @param y
     * @param ray
     * @param color
     */
    public Anchor2D(Sequence sequence, double x, double y, int ray, Color color, Color selectedColor)
    {
        super(sequence);

        position = new Point2D.Double(x, y);
        this.ray = ray;
        this.color = color;
        this.selectedColor = selectedColor;
        selected = false;
        // canRemove = true;
        visible = true;

        ellipse = new Ellipse2D.Double();
    }

    /**
     * @param sequence
     * @param position
     * @param ray
     * @param color
     */
    public Anchor2D(Sequence sequence, Point2D position, int ray, Color color, Color selectedColor)
    {
        this(sequence, position.getX(), position.getY(), ray, color, selectedColor);
    }

    /**
     * @param position
     * @param ray
     * @param color
     */
    public Anchor2D(Point2D position, int ray, Color color, Color selectedColor)
    {
        this(null, position.getX(), position.getY(), ray, color, selectedColor);
    }

    /**
     * @param sequence
     * @param position
     * @param ray
     * @param color
     */
    public Anchor2D(Sequence sequence, Point2D position, int ray, Color color)
    {
        this(sequence, position.getX(), position.getY(), ray, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param position
     * @param ray
     * @param color
     */
    public Anchor2D(Point2D position, int ray, Color color)
    {
        this(null, position.getX(), position.getY(), ray, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param sequence
     * @param x
     * @param y
     * @param ray
     */
    public Anchor2D(Sequence sequence, double x, double y, int ray)
    {
        this(sequence, x, y, ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param x
     * @param y
     * @param ray
     */
    public Anchor2D(double x, double y, int ray)
    {
        this(null, x, y, ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param sequence
     * @param position
     * @param ray
     */
    public Anchor2D(Sequence sequence, Point2D position, int ray)
    {
        this(sequence, position.getX(), position.getY(), ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param position
     * @param ray
     */
    public Anchor2D(Point2D position, int ray)
    {
        this(null, position.getX(), position.getY(), ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param sequence
     * @param x
     * @param y
     * @param color
     */
    public Anchor2D(Sequence sequence, double x, double y, Color color, Color selectedColor)
    {
        this(sequence, x, y, DEFAULT_RAY, color, selectedColor);
    }

    /**
     * @param x
     * @param y
     * @param color
     */
    public Anchor2D(double x, double y, Color color, Color selectedColor)
    {
        this(null, x, y, DEFAULT_RAY, color, selectedColor);
    }

    /**
     * @param sequence
     * @param position
     * @param color
     */
    public Anchor2D(Sequence sequence, Point2D position, Color color, Color selectedColor)
    {
        this(sequence, position.getX(), position.getY(), DEFAULT_RAY, color, selectedColor);
    }

    /**
     * @param position
     * @param color
     */
    public Anchor2D(Point2D position, Color color, Color selectedColor)
    {
        this(null, position.getX(), position.getY(), DEFAULT_RAY, color, selectedColor);
    }

    /**
     * @param sequence
     * @param x
     * @param y
     * @param color
     */
    public Anchor2D(Sequence sequence, double x, double y, Color color)
    {
        this(sequence, x, y, DEFAULT_RAY, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param x
     * @param y
     * @param color
     */
    public Anchor2D(double x, double y, Color color)
    {
        this(null, x, y, DEFAULT_RAY, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param sequence
     * @param position
     * @param color
     */
    public Anchor2D(Sequence sequence, Point2D position, Color color)
    {
        this(sequence, position.getX(), position.getY(), DEFAULT_RAY, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param position
     * @param color
     */
    public Anchor2D(Point2D position, Color color)
    {
        this(null, position.getX(), position.getY(), DEFAULT_RAY, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param sequence
     * @param x
     * @param y
     */
    public Anchor2D(Sequence sequence, double x, double y)
    {
        this(sequence, x, y, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param x
     * @param y
     */
    public Anchor2D(double x, double y)
    {
        this(null, x, y, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param sequence
     * @param position
     */
    public Anchor2D(Sequence sequence, Point2D position)
    {
        this(sequence, position.getX(), position.getY(), DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param position
     */
    public Anchor2D(Point2D position)
    {
        this(null, position.getX(), position.getY(), DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param sequence
     */
    public Anchor2D(Sequence sequence)
    {
        this(sequence, 0d, 0d, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     */
    public Anchor2D()
    {
        this(null, 0d, 0d, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @return the x
     */
    public double getX()
    {
        return position.x;
    }

    /**
     * @param x
     *        the x to set
     */
    public void setX(double x)
    {
        setPosition(x, position.y);
    }

    /**
     * @return the y
     */
    public double getY()
    {
        return position.y;
    }

    /**
     * @param y
     *        the y to set
     */
    public void setY(double y)
    {
        setPosition(position.x, y);
    }

    public Point2D getPosition()
    {
        return new Point2D.Double(position.x, position.y);
    }

    public void moveTo(Point2D p)
    {
        setPosition(p.getX(), p.getY());
    }

    public void moveTo(double x, double y)
    {
        setPosition(x, y);
    }

    public void setPosition(Point2D p)
    {
        setPosition(p.getX(), p.getY());
    }

    public void setPosition(double x, double y)
    {
        if ((position.x != x) || (position.y != y))
        {
            position.x = x;
            position.y = y;

            positionChanged();
            changed();
        }
    }

    public void translate(double dx, double dy)
    {
        setPosition(position.x + dx, position.y + dy);
    }

    /**
     * @return the ray
     */
    public int getRay()
    {
        return ray;
    }

    /**
     * @param value
     *        the ray to set
     */
    public void setRay(int value)
    {
        if (ray != value)
        {
            ray = value;
            changed();
        }
    }

    /**
     * @return the color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * @param value
     *        the color to set
     */
    public void setColor(Color value)
    {
        if (color != value)
        {
            color = value;
            changed();
        }
    }

    /**
     * @return the selectedColor
     */
    public Color getSelectedColor()
    {
        return selectedColor;
    }

    /**
     * @param value
     *        the selectedColor to set
     */
    public void setSelectedColor(Color value)
    {
        if (selectedColor != value)
        {
            selectedColor = value;
            changed();
        }
    }

    /**
     * @return the selected
     */
    public boolean isSelected()
    {
        return selected;
    }

    /**
     * @param value
     *        the selected to set
     */
    public void setSelected(boolean value)
    {
        if (selected != value)
        {
            selected = value;
            changed();
        }
    }

    // /**
    // * @return the canRemove
    // */
    // public boolean isCanRemove()
    // {
    // return canRemove;
    // }
    //
    // /**
    // * @param value
    // * the canRemove to set
    // */
    // public void setCanRemove(boolean value)
    // {
    // canRemove = value;
    // }

    /**
     * @return the visible
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * @param value
     *        the visible to set
     */
    public void setVisible(boolean value)
    {
        if (visible != value)
        {
            visible = value;
            changed();
        }
    }

    public boolean isOver(IcyCanvas canvas, Point2D p)
    {
        return isOver(canvas, p.getX(), p.getY());
    }

    public boolean isOver(IcyCanvas canvas, double x, double y)
    {
        updateEllipse(canvas);
        return ellipse.contains(x, y);
    }

    private void updateEllipse(IcyCanvas canvas)
    {
        final double adjRayX = ROI.canvasToImageLogDeltaX(canvas, ray);
        final double adjRayY = ROI.canvasToImageLogDeltaY(canvas, ray);
        ellipse.setFrame(position.x - adjRayX, position.y - adjRayY, adjRayX * 2, adjRayY * 2);
    }

    /**
     * called when anchor position has changed
     */
    protected void positionChanged()
    {
        firePositionChangedEvent();
    }

    private void firePositionChangedEvent()
    {
        for (Anchor2DListener listener : listeners.getListeners(Anchor2DListener.class))
            listener.positionChanged(this);
    }

    @Override
    protected void fireChangedEvent(PainterEvent event)
    {
        for (Anchor2DListener listener : listeners.getListeners(Anchor2DListener.class))
            listener.painterChanged(event);

        super.fireChangedEvent(event);
    }

    public void addListener(Anchor2DListener listener)
    {
        listeners.add(Anchor2DListener.class, listener);
    }

    public void removeListener(Anchor2DListener listener)
    {
        listeners.remove(Anchor2DListener.class, listener);
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        if (!visible)
            return;

        updateEllipse(canvas);

        if (selected)
            g.setColor(selectedColor);
        else
            g.setColor(color);

        g.fill(ellipse);
    }

    @Override
    public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        if (!visible)
            return;

        if (e.isConsumed())
            return;

        if (EventUtil.isLeftMouseButton(e))
        {
            // shift action
            if (EventUtil.isShiftDown(e))
            {

            }
            // normal action
            else
            {
                // consume event if we're clicking on selected point
                if (isSelected())
                    e.consume();
            }
        }
    }

    @Override
    public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        if (!visible)
            return;

        if (e.isConsumed())
            return;

        if (EventUtil.isLeftMouseButton(e))
        {
            // shift action
            if (EventUtil.isShiftDown(e))
            {

            }
            // normal action
            else
            {
                // if selected then move according to mouse position
                if (isSelected())
                {
                    final double dx = imagePoint.getX() - position.x;
                    final double dy = imagePoint.getY() - position.y;

                    translate(dx, dy);
                    // setPosition(imagePoint);
                    e.consume();
                }
            }
        }
    }

    @Override
    public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        if (!visible)
            return;

        // already consumed, no selection possible
        if (e.isConsumed())
            setSelected(false);
        else
        {
            final boolean overlapped = isOver(canvas, imagePoint);
            setSelected(overlapped);
            // so we can only have one selected at once
            if (overlapped)
                e.consume();
        }
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        beginUpdate();
        try
        {
            setColor(new Color(XMLUtil.getElementIntValue(node, ID_COLOR, DEFAULT_NORMAL_COLOR.getRGB())));
            setSelectedColor(new Color(XMLUtil.getElementIntValue(node, ID_SELECTEDCOLOR,
                    DEFAULT_SELECTED_COLOR.getRGB())));
            setX(XMLUtil.getElementDoubleValue(node, ID_POS_X, 0d));
            setY(XMLUtil.getElementDoubleValue(node, ID_POS_Y, 0d));
            setRay(XMLUtil.getElementIntValue(node, ID_RAY, DEFAULT_RAY));
            setVisible(XMLUtil.getElementBooleanValue(node, ID_VISIBLE, true));
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (node == null)
            return false;

        XMLUtil.setElementIntValue(node, ID_COLOR, getColor().getRGB());
        XMLUtil.setElementIntValue(node, ID_SELECTEDCOLOR, getSelectedColor().getRGB());
        XMLUtil.setElementDoubleValue(node, ID_POS_X, getX());
        XMLUtil.setElementDoubleValue(node, ID_POS_Y, getY());
        XMLUtil.setElementIntValue(node, ID_RAY, getRay());
        XMLUtil.setElementBooleanValue(node, ID_VISIBLE, isVisible());

        return true;
    }
}
