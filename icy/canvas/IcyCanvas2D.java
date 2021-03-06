/**
 * 
 */
package icy.canvas;

import icy.gui.main.MainFrame;
import icy.gui.viewer.Viewer;
import icy.main.Icy;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Stephane
 */
public abstract class IcyCanvas2D extends IcyCanvas
{
    /**
     * 
     */
    private static final long serialVersionUID = 743937493919099495L;

    public IcyCanvas2D(Viewer viewer)
    {
        super(viewer);

        // default for 2D canvas
        posX = -1;
        posY = -1;
    }

    /**
     * Return mouse image position
     */
    public Point2D.Double getMouseImagePos()
    {
        return new Point2D.Double(getMouseImagePosX(), getMouseImagePosY());
    }

    /**
     * Set mouse image position
     */
    public void setMouseImagePos(double x, double y)
    {
        setMouseImagePosX(x);
        setMouseImagePosY(y);
    }

    /**
     * Set mouse image position
     */
    public void setMouseImagePos(Point2D.Double point)
    {
        setMouseImagePos(point.x, point.y);
    }

    /**
     * Convert specified canvas delta to image delta.<br>
     */
    protected Point2D.Double canvasToImageDelta(int x, int y, double scaleX, double scaleY, double rot)
    {
        // get cos and sin
        final double cos = Math.cos(-rot);
        final double sin = Math.sin(-rot);

        // apply rotation
        final double resX = (x * cos) - (y * sin);
        final double resY = (x * sin) + (y * cos);

        // and scale
        return new Point2D.Double(resX / scaleX, resY / scaleY);
    }

    /**
     * Convert specified canvas delta point to image delta point
     */
    public Point2D.Double canvasToImageDelta(int x, int y)
    {
        return canvasToImageDelta(x, y, getScaleX(), getScaleX(), getRotationZ());
    }

    /**
     * Convert specified canvas delta point to image delta point
     */
    public Point2D.Double canvasToImageDelta(Point point)
    {
        return canvasToImageDelta(point.x, point.y);
    }

    /**
     * Convert specified canvas point to image point.<br>
     * By default we consider the rotation applied relatively to image center.<br>
     * Override this method if you want different transformation type.
     */
    protected Point2D.Double canvasToImage(int x, int y, int offsetX, int offsetY, double scaleX, double scaleY,
            double rot)
    {
        // get canvas center
        final double canvasCenterX = getCanvasSizeX() / 2;
        final double canvasCenterY = getCanvasSizeY() / 2;

        // center to canvas for rotation
        final double dx = x - canvasCenterX;
        final double dy = y - canvasCenterY;

        // get cos and sin
        final double cos = Math.cos(-rot);
        final double sin = Math.sin(-rot);

        // apply rotation
        double resX = (dx * cos) - (dy * sin);
        double resY = (dx * sin) + (dy * cos);

        // translate back to position
        resX += canvasCenterX;
        resY += canvasCenterY;

        // basic transform to image coordinates
        resX = ((resX - offsetX) / scaleX);
        resY = ((resY - offsetY) / scaleY);

        return new Point2D.Double(resX, resY);
    }

    /**
     * Convert specified canvas point to image point
     */
    public Point2D.Double canvasToImage(int x, int y)
    {
        return canvasToImage(x, y, getOffsetX(), getOffsetY(), getScaleX(), getScaleY(), getRotationZ());
    }

    /**
     * Convert specified canvas point to image point
     */
    public Point2D.Double canvasToImage(Point point)
    {
        return canvasToImage(point.x, point.y);
    }

    /**
     * Convert specified canvas rectangle to image rectangle
     */
    public Rectangle2D.Double canvasToImage(int x, int y, int w, int h)
    {
        // convert each rectangle point
        final Point2D.Double pt1 = canvasToImage(x, y);
        final Point2D.Double pt2 = canvasToImage(x + w, y);
        final Point2D.Double pt3 = canvasToImage(x + w, y + h);
        final Point2D.Double pt4 = canvasToImage(x, y + h);

        // get minimum and maximum X / Y
        final double minX = Math.min(pt1.x, Math.min(pt2.x, Math.min(pt3.x, pt4.x)));
        final double maxX = Math.max(pt1.x, Math.max(pt2.x, Math.max(pt3.x, pt4.x)));
        final double minY = Math.min(pt1.y, Math.min(pt2.y, Math.min(pt3.y, pt4.y)));
        final double maxY = Math.max(pt1.y, Math.max(pt2.y, Math.max(pt3.y, pt4.y)));

        // return transformed rectangle
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Convert specified canvas rectangle to image rectangle
     */
    public Rectangle2D.Double canvasToImage(Rectangle rect)
    {
        return canvasToImage(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Convert specified image delta to canvas delta.<br>
     */
    protected Point imageToCanvasDelta(double x, double y, double scaleX, double scaleY, double rot)
    {
        // apply scale
        final double dx = x * scaleX;
        final double dy = y * scaleY;

        // get cos and sin
        final double cos = Math.cos(rot);
        final double sin = Math.sin(rot);

        // apply rotation
        final double resX = (dx * cos) - (dy * sin);
        final double resY = (dx * sin) + (dy * cos);

        return new Point((int) Math.round(resX), (int) Math.round(resY));
    }

    /**
     * Convert specified image delta point to canvas delta point
     */
    public Point imageToCanvasDelta(double x, double y)
    {
        return imageToCanvasDelta(x, y, getScaleX(), getScaleY(), getRotationZ());
    }

    /**
     * Convert specified image delta point to canvas delta point
     */
    public Point imageToCanvasDelta(Point2D.Double point)
    {
        return imageToCanvasDelta(point.x, point.y);
    }

    /**
     * Convert specified image point to canvas point.<br>
     * By default we consider the rotation applied relatively to image center.<br>
     * Override this method if you want different transformation type.
     */
    protected Point imageToCanvas(double x, double y, int offsetX, int offsetY, double scaleX, double scaleY, double rot)
    {
        // get canvas center
        final double canvasCenterX = getCanvasSizeX() / 2;
        final double canvasCenterY = getCanvasSizeY() / 2;

        // basic transform to canvas coordinates and canvas centering
        final double dx = ((x * scaleX) + offsetX) - canvasCenterX;
        final double dy = ((y * scaleY) + offsetY) - canvasCenterY;

        // get cos and sin
        final double cos = Math.cos(rot);
        final double sin = Math.sin(rot);

        // apply rotation
        double resX = (dx * cos) - (dy * sin);
        double resY = (dx * sin) + (dy * cos);

        // translate back to position
        resX += canvasCenterX;
        resY += canvasCenterY;

        return new Point((int) Math.round(resX), (int) Math.round(resY));
    }

    /**
     * Convert specified image point to canvas point
     */
    public Point imageToCanvas(double x, double y)
    {
        return imageToCanvas(x, y, getOffsetX(), getOffsetY(), getScaleX(), getScaleY(), getRotationZ());
    }

    /**
     * Convert specified image point to canvas point
     */
    public Point imageToCanvas(Point2D.Double point)
    {
        return imageToCanvas(point.x, point.y);
    }

    /**
     * Convert specified image rectangle to canvas rectangle
     */
    public Rectangle imageToCanvas(double x, double y, double w, double h)
    {
        // convert each rectangle point
        final Point pt1 = imageToCanvas(x, y);
        final Point pt2 = imageToCanvas(x + w, y);
        final Point pt3 = imageToCanvas(x + w, y + h);
        final Point pt4 = imageToCanvas(x, y + h);

        // get minimum and maximum X / Y
        final int minX = Math.min(pt1.x, Math.min(pt2.x, Math.min(pt3.x, pt4.x)));
        final int maxX = Math.max(pt1.x, Math.max(pt2.x, Math.max(pt3.x, pt4.x)));
        final int minY = Math.min(pt1.y, Math.min(pt2.y, Math.min(pt3.y, pt4.y)));
        final int maxY = Math.max(pt1.y, Math.max(pt2.y, Math.max(pt3.y, pt4.y)));

        // return transformed rectangle
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Convert specified image rectangle to canvas rectangle
     */
    public Rectangle imageToCanvas(Rectangle2D.Double rect)
    {
        return imageToCanvas(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Get 2D view size in canvas pixel coordinate
     * 
     * @return a Dimension which represents the visible size.
     */
    public Dimension getCanvasSize()
    {
        return new Dimension(getCanvasSizeX(), getCanvasSizeY());
    }

    /**
     * Get 2D image size
     */
    public Dimension getImageSize()
    {
        return new Dimension(getImageSizeX(), getImageSizeY());
    }

    /**
     * Get 2D image size in canvas pixel coordinate
     */
    public Dimension getImageCanvasSize()
    {
        final double imageSizeX = getImageSizeX();
        final double imageSizeY = getImageSizeY();
        final double scaleX = getScaleX();
        final double scaleY = getScaleY();
        final double rot = getRotationZ();

        // convert image rectangle
        final Point pt1 = imageToCanvas(0d, 0d, 0, 0, scaleX, scaleY, rot);
        final Point pt2 = imageToCanvas(imageSizeX, 0d, 0, 0, scaleX, scaleY, rot);
        final Point pt3 = imageToCanvas(0d, imageSizeY, 0, 0, scaleX, scaleY, rot);
        final Point pt4 = imageToCanvas(imageSizeX, imageSizeY, 0, 0, scaleX, scaleY, rot);

        final int minX = Math.min(pt1.x, Math.min(pt2.x, Math.min(pt3.x, pt4.x)));
        final int maxX = Math.max(pt1.x, Math.max(pt2.x, Math.max(pt3.x, pt4.x)));
        final int minY = Math.min(pt1.y, Math.min(pt2.y, Math.min(pt3.y, pt4.y)));
        final int maxY = Math.max(pt1.y, Math.max(pt2.y, Math.max(pt3.y, pt4.y)));

        return new Dimension(maxX - minX, maxY - minY);
    }

    /**
     * Get 2D canvas visible rectangle (canvas coordinate)
     */
    public Rectangle getCanvasVisibleRect()
    {
        return getVisibleRect();
    }

    /**
     * Get 2D image visible rectangle (image coordinate)
     */
    public Rectangle2D getImageVisibleRect()
    {
        return canvasToImage(getCanvasVisibleRect());
    }

    /**
     * Center image on specified image position in canvas
     */
    public void centerOnImage(double x, double y)
    {
        // get point on canvas
        final Point pt = imageToCanvas(x, y);
        final int canvasCenterX = getCanvasSizeX() / 2;
        final int canvasCenterY = getCanvasSizeY() / 2;

        final Point2D.Double newTrans = canvasToImageDelta(canvasCenterX - pt.x, canvasCenterY - pt.y, 1d, 1d,
                getRotationZ());

        setOffsetX(getOffsetX() + (int) Math.round(newTrans.x));
        setOffsetY(getOffsetY() + (int) Math.round(newTrans.y));
    }

    /**
     * Center image on specified image position in canvas
     */
    public void centerOnImage(Point2D.Double pt)
    {
        centerOnImage(pt.x, pt.y);
    }

    /**
     * Center image in canvas
     */
    public void centerImage()
    {
        centerOnImage(getImageSizeX() / 2, getImageSizeY() / 2);
    }

    /**
     * get scale X and scale Y so image fit in canvas view dimension
     */
    protected Point2D.Double getFitImageToCanvasScale()
    {
        final double imageSizeX = getImageSizeX();
        final double imageSizeY = getImageSizeY();

        if ((imageSizeX > 0d) && (imageSizeY > 0d))
        {
            final double rot = getRotationZ();

            // convert image rectangle
            final Point pt1 = imageToCanvas(0d, 0d, 0, 0, 1d, 1d, rot);
            final Point pt2 = imageToCanvas(imageSizeX, 0d, 0, 0, 1d, 1d, rot);
            final Point pt3 = imageToCanvas(0d, imageSizeY, 0, 0, 1d, 1d, rot);
            final Point pt4 = imageToCanvas(imageSizeX, imageSizeY, 0, 0, 1d, 1d, rot);

            final int minX = Math.min(pt1.x, Math.min(pt2.x, Math.min(pt3.x, pt4.x)));
            final int maxX = Math.max(pt1.x, Math.max(pt2.x, Math.max(pt3.x, pt4.x)));
            final int minY = Math.min(pt1.y, Math.min(pt2.y, Math.min(pt3.y, pt4.y)));
            final int maxY = Math.max(pt1.y, Math.max(pt2.y, Math.max(pt3.y, pt4.y)));

            // get image dimension transformed by rotation
            final double sx = (double) getCanvasSizeX() / (double) (maxX - minX);
            final double sy = (double) getCanvasSizeY() / (double) (maxY - minY);

            return new Point2D.Double(sx, sy);
        }

        return null;
    }

    /**
     * Change scale so image fit in canvas view dimension
     */
    public void fitImageToCanvas()
    {
        final Point2D.Double s = getFitImageToCanvasScale();

        if (s != null)
        {
            final double scale = Math.min(s.x, s.y);

            setScaleX(scale);
            setScaleY(scale);
        }
    }

    /**
     * Change canvas size (so viewer size) to get it fit with image dimension if possible
     */
    public void fitCanvasToImage()
    {
        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
        final Dimension imageCanvasSize = getImageCanvasSize();

        if ((imageCanvasSize.width > 0) && (imageCanvasSize.height > 0) && (mainFrame != null))
        {
            final Dimension maxDim = mainFrame.getDesktopSize();
            final Dimension adjImgCnvSize = canvasToViewer(imageCanvasSize);

            // fit in available space --> resize viewer
            viewer.setSize(Math.min(adjImgCnvSize.width, maxDim.width), Math.min(adjImgCnvSize.height, maxDim.height));
        }
    }

    /**
     * Convert canvas dimension to viewer dimension
     */
    public Dimension canvasToViewer(Dimension dim)
    {
        final Dimension canvasViewSize = getCanvasSize();
        final Dimension viewerSize = viewer.getSize();
        final Dimension result = new Dimension(dim);

        result.width -= canvasViewSize.width;
        result.width += viewerSize.width;
        result.height -= canvasViewSize.height;
        result.height += viewerSize.height;

        return result;
    }

    /**
     * Convert viewer dimension to canvas dimension
     */
    public Dimension viewerToCanvas(Dimension dim)
    {
        final Dimension canvasViewSize = getCanvasSize();
        final Dimension viewerSize = viewer.getSize();
        final Dimension result = new Dimension(dim);

        result.width -= viewerSize.width;
        result.width += canvasViewSize.width;
        result.height -= viewerSize.height;
        result.height += canvasViewSize.height;

        return result;
    }
}
