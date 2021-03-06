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
package icy.plugin.abstract_;

import icy.gui.frame.IcyFrame;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.ImageUtil;
import icy.main.Icy;
import icy.network.IcyNetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.preferences.PluginsPreferences;
import icy.preferences.XMLPreferences;
import icy.resource.ResourceUtil;
import icy.sequence.Sequence;

import java.awt.Image;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * Base class for Plugin.<br>
 * <br>
 * Provide some helper methods.
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public abstract class Plugin
{
    private static final String ID_PLUGINCLASSNAME = "pluginClassName";
    private static final String ID_ERRORLOG = "errorLog";

    public static void report(PluginDescriptor plugin, String errorLog)
    {
        final HashMap<String, String> values = new HashMap<String, String>();

        if (plugin != null)
            values.put(ID_PLUGINCLASSNAME, plugin.getClassName());
        else
            values.put(ID_PLUGINCLASSNAME, "");

        final String icyId;
        final String pluginId;

        icyId = "ICY Version " + Icy.version + "\n";
        if (plugin != null)
            pluginId = "Plugin " + plugin.toString() + "\n";
        else
            pluginId = "";

        values.put(ID_ERRORLOG, icyId + pluginId + "\n" + errorLog);

        // send report
        IcyNetworkUtil.report(values);
    }

    private final PluginDescriptor descriptor;

    public Plugin()
    {
        // get descriptor from loader
        descriptor = PluginLoader.getPlugin(getClass().getName());

        if (descriptor == null)
        {
            System.err.println("Plugin '" + getClass().getName() + "' started but not found in PluginLoader !");
            System.err.println("Local XML plugin description file is probably incorrect.");
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        // unregister plugin (weak reference so we can do it here)
        Icy.getMainInterface().unRegisterPlugin(this);

        super.finalize();
    }

    /**
     * @return the descriptor
     */
    public PluginDescriptor getDescriptor()
    {
        return descriptor;
    }

    public Viewer getFocusedViewer()
    {
        return Icy.getMainInterface().getFocusedViewer();
    }

    public Sequence getFocusedSequence()
    {
        return Icy.getMainInterface().getFocusedSequence();
    }

    public IcyBufferedImage getFocusedImage()
    {
        return Icy.getMainInterface().getFocusedImage();
    }

    public void addIcyFrame(final IcyFrame frame)
    {
        frame.addToMainDesktopPane();
    }

    public void addSequence(final Sequence sequence)
    {
        Icy.addSequence(sequence);
    }

    public void removeSequence(final Sequence sequence)
    {
        sequence.close();
    }

    public ArrayList<Sequence> getSequences()
    {
        return Icy.getMainInterface().getSequences();
    }

    /**
     * Return the resource as data stream from given resource name
     * 
     * @param name
     *        resource name
     */
    public InputStream getResourceAsStream(String name)
    {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    /**
     * Return the image resource from given resource name
     * 
     * @param resourceName
     *        resource name
     */
    public Image getImageResource(String resourceName)
    {
        return ImageUtil.loadImage(getResourceAsStream(resourceName));
    }

    /**
     * Return the icon resource from given resource name
     * 
     * @param resourceName
     *        resource name
     */
    public ImageIcon getIconResource(String resourceName)
    {
        return ResourceUtil.getImageIcon(getImageResource(resourceName));
    }

    /**
     * Retrieve the preferences root for this plugin.<br>
     */
    public XMLPreferences getPreferencesRoot()
    {
        return PluginsPreferences.root(this);
    }

    /**
     * Retrieve the plugin preferences node for specified name.<br>
     * i.e : getPreferences("window") will return node
     * "plugins.[authorPackage].[pluginClass].window"
     */
    public XMLPreferences getPreferences(String name)
    {
        return getPreferencesRoot().node(name);
    }
}
