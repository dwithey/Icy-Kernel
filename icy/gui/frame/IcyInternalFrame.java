/**
 * 
 */
package icy.gui.frame;

import icy.common.IcyAbstractAction;
import icy.common.MenuCallback;
import icy.gui.util.LookAndFeelUtil;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.pushingpixels.substance.internal.utils.SubstanceInternalFrameTitlePane;

/**
 * @author Stephane
 */
public class IcyInternalFrame extends JInternalFrame
{
    /**
     * 
     */
    private static final long serialVersionUID = -5445569637723054083L;

    private class CloseAction extends IcyAbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4933605299188863452L;

        public CloseAction()
        {
            super("Close", new IcyIcon(ResourceUtil.ICON_CLOSE, 20), "Close window", KeyEvent.VK_F4, SystemUtil
                    .getCtrlMask());
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            close(false);
        }
    }

    /**
     * internals
     */
    SubstanceInternalFrameTitlePane titlePane = null;
    JMenuBar systemMenuBar;
    private MenuCallback systemMenuCallback;
    private boolean titleBarVisible;
    private boolean closeItemVisible;
    private boolean initialized = false;

    /**
     * @param title
     * @param resizable
     * @param closable
     * @param maximizable
     * @param iconifiable
     */
    public IcyInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable)
    {
        super(title, resizable, closable, maximizable, iconifiable);

        addPropertyChangeListener("titlePane", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                // invoke later so the titlePane variable is up to date
                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateTitlePane();
                    }
                });
            }
        });

        setFrameIcon(ResourceUtil.ICON_ICY_16);
        setVisible(false);

        systemMenuCallback = null;
        closeItemVisible = true;
        updateTitlePane(LookAndFeelUtil.getTitlePane(this));

        titleBarVisible = true;
        initialized = true;
    }

    /**
     * update internals informations linked to title pane with specified pane
     */
    protected void updateTitlePane(final SubstanceInternalFrameTitlePane pane)
    {
        // update pane save
        if (pane != null)
            titlePane = pane;
        // update menu
        if (titlePane != null)
            systemMenuBar = titlePane.getMenuBar();
        // refresh system menu whatever
        updateSystemMenu();
    }

    /**
     * update internals informations linked to title pane and title pane state
     */
    protected void updateTitlePane()
    {
        if (initialized)
        {
            // title pane can have changed
            updateTitlePane(LookAndFeelUtil.getTitlePane(this));

            if (!titleBarVisible)
                setTitleBarVisible(false);
        }
    }

    /**
     * Return true if specified point is located on title bar icon (system icon)
     */
    protected boolean isOnSystemIcon(Point p)
    {
        if (titlePane == null)
            return false;

        final int w = titlePane.getWidth();
        final int h = titlePane.getHeight();
        final Dimension iconSize = getSystemIconSize();

        final Rectangle rect;

        if (getComponentOrientation().isLeftToRight())
            rect = new Rectangle(5, (h / 2) - (iconSize.height / 2), iconSize.width, iconSize.height);
        else
            rect = new Rectangle(w - (5 + iconSize.width), (h / 2) - (iconSize.height / 2), iconSize.width,
                    iconSize.height);

        return rect.contains(p);
    }

    protected Dimension getSystemIconSize()
    {
        final Icon icon = getFrameIcon();
        return new Dimension(icon.getIconWidth(), icon.getIconHeight());
    }

    /**
     * Refresh system menu
     */
    public void updateSystemMenu()
    {
        if ((systemMenuBar != null) && (systemMenuBar.getMenuCount() > 0))
        {
            final JMenu menu;

            if (systemMenuCallback != null)
                menu = systemMenuCallback.getMenu();
            else
                menu = getDefaultSystemMenu();

            // ensure compatibility with heavyweight component
            menu.getPopupMenu().setLightWeightPopupEnabled(false);

            // rebuild menu
            systemMenuBar.removeAll();
            systemMenuBar.add(menu);
            systemMenuBar.validate();
        }
    }

    /**
     * Close the frame
     */
    public void close(boolean force)
    {
        if (force || isClosable())
            doDefaultCloseAction();
    }

    /**
     * Implement isMinimized method
     */
    public boolean isMinimized()
    {
        return isIcon();
    }

    /**
     * Implement isMaximized method
     */
    public boolean isMaximized()
    {
        return isMaximum();
    }

    /**
     * Implement setMinimized method
     */
    public void setMinimized(final boolean value)
    {
        // only relevant if state changed
        if (isMinimized() ^ value)
        {
            try
            {
                setIcon(value);
            }
            catch (PropertyVetoException e)
            {
                IcyExceptionHandler.showErrorMessage(e, true);
            }
        }
    }

    /**
     * Implement setMaximized method
     */
    public void setMaximized(final boolean value)
    {
        // have to check that else we obtain a null pointer exception
        if (getParent() == null)
            return;

        // only relevant if state changed
        if (isMaximized() ^ value)
        {
            try
            {
                // have to check for parent non null
                setMaximum(value);
            }
            catch (PropertyVetoException e)
            {
                IcyExceptionHandler.showErrorMessage(e, true);
            }
        }
    }

    /**
     * @return the titleBarVisible
     */
    public boolean isTitleBarVisible()
    {
        return titleBarVisible;
    }

    /**
     * @return the closeItemVisible
     */
    public boolean isCloseItemVisible()
    {
        return closeItemVisible;
    }

    /**
     * @param value
     *        the closeItemVisible to set
     */
    public void setCloseItemVisible(boolean value)
    {
        if (closeItemVisible != value)
        {
            closeItemVisible = value;
            updateSystemMenu();
        }
    }

    /**
     * @param value
     *        the titleBarVisible to set
     */
    public void setTitleBarVisible(boolean value)
    {
        if (value)
            LookAndFeelUtil.setTitlePane(this, titlePane);
        else
            LookAndFeelUtil.setTitlePane(this, null);
        revalidate();

        titleBarVisible = value;
    }

    /**
     * Return the default system menu
     */
    public JMenu getDefaultSystemMenu()
    {
        final JMenu result = new JMenu();

        if (closeItemVisible)
            result.add(new CloseAction());

        return result;
    }

    /**
     * @return the systemMenuCallback
     */
    public MenuCallback getSystemMenuCallback()
    {
        return systemMenuCallback;
    }

    /**
     * @param value
     *        the systemMenuCallback to set
     */
    public void setSystemMenuCallback(MenuCallback value)
    {
        if (systemMenuCallback != value)
        {
            systemMenuCallback = value;
            updateSystemMenu();
        }
    }
}
