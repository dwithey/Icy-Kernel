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
package icy.gui.dialog;

import icy.file.FileFormat;
import icy.file.FileUtil;
import icy.file.Saver;
import icy.gui.component.RangeComponent;
import icy.gui.util.GuiUtil;
import icy.main.Icy;
import icy.preferences.ApplicationPreferences;
import icy.preferences.XMLPreferences;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import loci.formats.gui.ExtensionFileFilter;

/**
 * @author Stephane
 */
public class ImageSaverDialog extends JFileChooser
{
    /**
     * 
     */
    private static final long serialVersionUID = 8771369900303584478L;

    private static final String PREF_ID = "frame/imageSaver";

    private static final String ID_WIDTH = "width";
    private static final String ID_HEIGHT = "height";
    private static final String ID_PATH = "path";
    private static final String ID_MULTIPLEFILE = "multipleFile";
    private static final String ID_FILETYPE = "fileType";

    // GUI
    final JCheckBox multipleFileCheck;
    final JPanel multiplesFilePanel;

    final JSpinner fpsSpinner;
    final JPanel fpsPanel;

    final JSpinner zSpinner;
    final JPanel zPanel;
    final JSpinner tSpinner;
    final JPanel tPanel;

    final RangeComponent zRange;
    final JPanel zRangePanel;
    final RangeComponent tRange;
    final JPanel tRangePanel;

    // internal
    final boolean singleZ;
    final boolean singleT;
    final boolean singleImage;

    /**
     * 
     */
    public ImageSaverDialog(Sequence sequence, int defZ, int defT)
    {
        super();

        final XMLPreferences preferences = ApplicationPreferences.getPreferences().node(PREF_ID);

        singleZ = (sequence.getSizeZ() == 1);
        singleT = (sequence.getSizeT() == 1);
        singleImage = singleZ && singleT;

        // can't use WindowsPositionSaver as JFileChooser is a fake JComponent
        // only dimension is stored
        setCurrentDirectory(new File(preferences.get(ID_PATH, "")));
        setPreferredSize(new Dimension(preferences.getInt(ID_WIDTH, 600), preferences.getInt(ID_HEIGHT, 400)));

        setDialogTitle("ICY - Save image file");

        // remove default filter
        removeChoosableFileFilter(getAcceptAllFileFilter());
        // then add our supported save format
        addChoosableFileFilter(FileFormat.TIFF.getExtensionFileFilter());
        addChoosableFileFilter(FileFormat.PNG.getExtensionFileFilter());
        addChoosableFileFilter(FileFormat.JPG.getExtensionFileFilter());
        addChoosableFileFilter(FileFormat.AVI.getExtensionFileFilter());
        setFileFilter(getChoosableFileFilters()[preferences.getInt(ID_FILETYPE, 0)]);

        setMultiSelectionEnabled(false);
        setFileSelectionMode(JFileChooser.FILES_ONLY);

        String filename = FileUtil.getFileName(sequence.getFilename(), true);
        // empty filename --> use sequence name as default filename
        if (StringUtil.isEmpty(filename))
            filename = sequence.getName();
        if (!StringUtil.isEmpty(filename))
        {
            // test if filename has already a valid extension
            final String ext = getDialogExtension(filename);
            // remove extension from filename
            if (ext != null)
                filename = filename.substring(0, filename.indexOf(ext) - 1);
            setSelectedFile(new File(filename));
        }

        multipleFileCheck = new JCheckBox();
        multipleFileCheck.setSelected(preferences.getBoolean(ID_MULTIPLEFILE, false));
        multipleFileCheck.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent arg0)
            {
                updateSettingPanel();
            }
        });
        multiplesFilePanel = GuiUtil.createLineBoxPanel(new JLabel("Save as multiple file "),
                Box.createHorizontalGlue(), multipleFileCheck);

        fpsSpinner = new JSpinner(new SpinnerNumberModel(15, 1, 99, 1));
        fpsPanel = GuiUtil.createLineBoxPanel(GuiUtil.createFixedWidthLabel("Frame per second ", 100),
                Box.createHorizontalGlue(), fpsSpinner);

        zSpinner = new JSpinner(new SpinnerNumberModel((defZ == -1) ? 0 : defZ, 0, sequence.getSizeZ() - 1, 1));
        zPanel = GuiUtil.createLineBoxPanel(GuiUtil.createFixedWidthLabel("Z slice ", 100), Box.createHorizontalGlue(),
                zSpinner);
        tSpinner = new JSpinner(new SpinnerNumberModel((defT == -1) ? 0 : defT, 0, sequence.getSizeT() - 1, 1));
        tPanel = GuiUtil.createLineBoxPanel(GuiUtil.createFixedWidthLabel("T slice ", 100), Box.createHorizontalGlue(),
                tSpinner);

        zRange = new RangeComponent(0, sequence.getSizeZ() - 1, 1);
        zRange.setLabelVisible(false);
        zRangePanel = GuiUtil.createLineBoxPanel(new JLabel("Z range "), Box.createHorizontalGlue(), zRange);
        tRange = new RangeComponent(0, sequence.getSizeT() - 1, 1);
        tRange.setLabelVisible(false);
        tRangePanel = GuiUtil.createLineBoxPanel(new JLabel("T range "), Box.createHorizontalGlue(), tRange);

        final JPanel settingPanel = new JPanel();
        settingPanel.setBorder(BorderFactory.createTitledBorder((Border) null));
        settingPanel.setLayout(new BorderLayout());

        settingPanel.add(
                GuiUtil.createPageBoxPanel(multiplesFilePanel, fpsPanel, zPanel, tPanel, zRangePanel, tRangePanel),
                BorderLayout.NORTH);
        settingPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);

        setAccessory(settingPanel);
        updateSettingPanel();

        // listen file filter change
        addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                updateSettingPanel();
            }
        });

        // display loader
        final int value = showSaveDialog(Icy.getMainInterface().getMainFrame());

        // action confirmed ?
        if (value == JFileChooser.APPROVE_OPTION)
        {
            // Choose writer should be compatible
            if (Saver.isCompatible(Saver.getWriter(getSelectedFileFormat()), sequence.getColorModel()))
            {
                // test and add extension if needed
                final ExtensionFileFilter extensionFilter = (ExtensionFileFilter) getFileFilter();
                File file = getSelectedFile();
                final String outfileName = file.getAbsolutePath();

                // add file filter extension to filename if not already present
                if (!hasExtension(outfileName.toLowerCase(), extensionFilter))
                    file = new File(outfileName + "." + extensionFilter.getExtension());

                // ask for confirmation as file already exists
                if (!file.exists() || ConfirmDialog.confirm("Overwrite existings file ?"))
                {
                    // store current path
                    preferences.put(ID_PATH, getCurrentDirectory().getAbsolutePath());

                    final Sequence s = sequence;
                    final File f = file;
                    final int zMin, zMax;
                    final int tMin, tMax;
                    final int fps;
                    final boolean multipleFile;

                    if (zPanel.isVisible())
                        zMin = zMax = ((Integer) zSpinner.getValue()).intValue();
                    else if (zRangePanel.isVisible())
                    {
                        zMin = ((Integer) zRange.getStart()).intValue();
                        zMax = ((Integer) zRange.getEnd()).intValue();
                    }
                    else
                        zMin = zMax = 0;
                    if (tPanel.isVisible())
                        tMin = tMax = ((Integer) tSpinner.getValue()).intValue();
                    else if (tRangePanel.isVisible())
                    {
                        tMin = ((Integer) tRange.getStart()).intValue();
                        tMax = ((Integer) tRange.getEnd()).intValue();
                    }
                    else
                        tMin = tMax = 0;
                    if (fpsPanel.isVisible())
                        fps = ((Integer) fpsSpinner.getValue()).intValue();
                    else
                        fps = 1;
                    if (multiplesFilePanel.isVisible())
                        multipleFile = multipleFileCheck.isSelected();
                    else
                        multipleFile = false;

                    // do save in background process
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Saver.save(s, f, zMin, zMax, tMin, tMax, fps, multipleFile);
                        }
                    });
                }
            }
            else
            {
                // incompatible saver for this sequence
                MessageDialog
                        .showDialog("The selected image save format is not compatible with your sequence format.\n"
                                + "Convert your sequence to RGB or Grayscale sequence or choose a compatible save format (as TIFF).");
            }

            // store interface option
            preferences.putInt(ID_WIDTH, getWidth());
            preferences.putInt(ID_HEIGHT, getHeight());
            preferences.putBoolean(ID_MULTIPLEFILE, multipleFileCheck.isSelected());
            preferences.putInt(ID_FILETYPE, Arrays.asList(getChoosableFileFilters()).indexOf(getFileFilter()));
        }
    }

    private boolean hasExtension(String name, ExtensionFileFilter extensionFilter)
    {
        return getExtension(name, extensionFilter) != null;
    }

    private String getExtension(String name, ExtensionFileFilter extensionFilter)
    {
        for (String ext : extensionFilter.getExtensions())
            if (name.endsWith(ext.toLowerCase()))
                return ext;

        return null;
    }

    private String getDialogExtension(String name)
    {
        for (FileFilter filter : getChoosableFileFilters())
        {
            final String ext = getExtension(name, (ExtensionFileFilter) filter);

            if (ext != null)
                return ext;
        }

        return null;
    }

    private FileFormat getSelectedFileFormat()
    {
        return FileFormat.getFileFormat(((ExtensionFileFilter) getFileFilter()).getExtension(), FileFormat.TIFF);
    }

    void updateSettingPanel()
    {
        final FileFormat fileFormat = getSelectedFileFormat();

        final boolean tif = (fileFormat == FileFormat.TIFF);
        final boolean jpg = (fileFormat == FileFormat.JPG);
        final boolean avi = (fileFormat == FileFormat.AVI);

        if (singleImage)
        {
            // single image, no need to display selection option
            multiplesFilePanel.setVisible(false);
            zPanel.setVisible(false);
            zRangePanel.setVisible(false);
            tPanel.setVisible(false);
            tRangePanel.setVisible(false);
        }
        else
        {
            multiplesFilePanel.setVisible(!avi);

            if (multiplesFilePanel.isVisible() && multipleFileCheck.isSelected())
            {
                // save as multiple file so display range option
                zPanel.setVisible(false);
                zRangePanel.setVisible(true && !singleZ);
                tPanel.setVisible(false);
                tRangePanel.setVisible(true && !singleT);
            }
            else
            {
                // save as single file so type give restriction here
                zPanel.setVisible(!tif && !singleZ);
                zRangePanel.setVisible(tif && !singleZ);
                tPanel.setVisible(jpg && !singleT);
                tRangePanel.setVisible(!jpg && !singleT);
            }
        }

        fpsPanel.setVisible(avi);

        getAccessory().setVisible(
                zPanel.isVisible() || zRangePanel.isVisible() || tPanel.isVisible() || tRangePanel.isVisible()
                        || multiplesFilePanel.isVisible() || fpsPanel.isVisible());
    }
}
