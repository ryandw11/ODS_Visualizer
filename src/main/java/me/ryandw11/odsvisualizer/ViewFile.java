package me.ryandw11.odsvisualizer;

import com.github.luben.zstd.ZstdInputStream;
import me.ryandw11.ods.ODS;
import me.ryandw11.ods.ObjectDataStructure;
import me.ryandw11.ods.Tag;
import me.ryandw11.ods.compression.Compressor;
import me.ryandw11.ods.compression.GZIPCompression;
import me.ryandw11.ods.compression.NoCompression;
import me.ryandw11.ods.compression.ZLIBCompression;
import me.ryandw11.ods.exception.ODSException;
import me.ryandw11.ods.tags.CompressedObjectTag;
import me.ryandw11.ods.tags.InvalidTag;
import me.ryandw11.ods.tags.ListTag;
import me.ryandw11.ods.tags.ObjectTag;
import me.ryandw11.odscp.zstd.ZSTDCompression;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class ViewFile extends JFrame {
    private final JPanel tagPnl;
    private final DefaultMutableTreeNode top;
    private final JTree tree;

    private ObjectDataStructure ods;

    /**
     * View a file.
     *
     * @param file The file to view.
     */
    public ViewFile(File file) {
        super("Viewing ODS File: " + file.getName());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 500);
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        System.out.println(getCompressionType(file));
        ObjectDataStructure ods = new ObjectDataStructure(file, getCompressionType(file));
        ODS.allowUndefinedTags(true);
        this.ods = ods;

        DefaultMutableTreeNode top = new DefaultMutableTreeNode(file.getName());
        this.top = top;
        try {
            createNodes(top, ods.getAll(), false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "This file cannot be read. Are you sure this is an ODS file?",
                    "Cannot Read File", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        JTree tree = new JTree(top);
        this.tree = tree;
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node == null) return;
            if (!(node.getUserObject() instanceof TagHolder)) return;
            TagHolder tagHolder = (TagHolder) node.getUserObject();
            setTagInfo(tagHolder.getTag());
        });

        JScrollPane treeView = new JScrollPane(tree);
        this.add(treeView, BorderLayout.CENTER);
        JPanel sidePnl = new JPanel();
        sidePnl.add(getFileInfo(file));
        this.tagPnl = setupTagInfo();
        sidePnl.add(tagPnl);
        sidePnl.setLayout(new BoxLayout(sidePnl, BoxLayout.Y_AXIS));
        this.add(sidePnl, BorderLayout.EAST);

        this.setJMenuBar(setupTopBar());
    }

    /**
     * Setup the top menu bar.
     *
     * @return The created top menu bar.
     */
    private JMenuBar setupTopBar() {
        JMenu file = new JMenu("File");
        JMenuItem reload = new JMenuItem("Reload");
        reload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        reload.addActionListener(e -> {
            top.removeAllChildren();
            createNodes(top, ods.getAll(), false);
            tree.updateUI();
        });

        JMenuItem open = new JMenuItem("Open ...");
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        open.addActionListener(e -> {
            FileDialog dialog = new FileDialog((Frame) null, "Select File to Open");
            dialog.setMode(FileDialog.LOAD);
            dialog.setVisible(true);
            File tempFile = new File(dialog.getDirectory() + dialog.getFile());
            if (!tempFile.exists()) {
                return;
            }
            ods = new ObjectDataStructure(tempFile, getCompressionType(tempFile));
            top.removeAllChildren();
            try {
                createNodes(top, ods.getAll(), false);
            }catch (ODSException ex) {
                JOptionPane.showMessageDialog(null, "File has an invalid format or has been corrupted. Is this an ODS file?",
                        "Cannot Read File", JOptionPane.ERROR_MESSAGE);
            }
            tree.updateUI();
        });
        file.add(reload);
        file.add(open);

        JMenuBar bar = new JMenuBar();
        bar.add(file);
        return bar;
    }

    /**
     * Get the information for a file.
     *
     * @param f The file
     * @return The JPanel with the file info.
     */
    private JPanel getFileInfo(File f) {
        JPanel panel = new JPanel();
        Border bb = BorderFactory.createLineBorder(Color.black);
        TitledBorder tt = BorderFactory.createTitledBorder(bb, "File Information");
        panel.setBorder(tt);
        panel.add(new JLabel("File Name: " + f.getName()));
        panel.add(new JLabel("File Size: " + f.length() + " bytes"));
        panel.add(new JLabel("File Compression: " + correctName(ODS.getCompressorName(getCompressionType(f)))));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    /**
     * Setup the information panel for a tag.
     *
     * @return The created JPanel.
     */
    private JPanel setupTagInfo() {
        JPanel panel = new JPanel();
        Border bb = BorderFactory.createLineBorder(Color.black);
        TitledBorder tt = BorderFactory.createTitledBorder(bb, "Tag Information");
        panel.setBorder(tt);
        panel.add(new JLabel("Select a tag to view it's information!"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    /**
     * Set the Tag Info panel for a certain tag.
     *
     * @param tag The tag to set the panel info for.
     */
    private void setTagInfo(Tag<?> tag) {
        tagPnl.removeAll();
        if (tag instanceof ObjectTag) {
            tagPnl.add(new JLabel("Tag Name: " + tag.getName()));
            tagPnl.add(new JLabel("Tag Type: " + tag.getClass().getSimpleName()));
            tagPnl.add(new JLabel("# of SubTags: " + ((ObjectTag) tag).getValue().size()));
        } else if (tag instanceof CompressedObjectTag) {
            tagPnl.add(new JLabel("Tag Name: " + tag.getName()));
            tagPnl.add(new JLabel("Tag Type: " + tag.getClass().getSimpleName()));
            tagPnl.add(new JLabel("Compression Type: " + ODS.getCompressorName(((CompressedObjectTag) tag).getCompressor())));
            tagPnl.add(new JLabel("# of SubTags: " + ((CompressedObjectTag) tag).getValue().size()));
        } else if (tag instanceof ListTag<?>) {
            tagPnl.add(new JLabel("Tag Name: " + tag.getName()));
            tagPnl.add(new JLabel("Tag Type: " + tag.getClass().getSimpleName()));
            tagPnl.add(new JLabel("# of Elements: " + ((ListTag<?>) tag).getValue().size()));
        } else if (tag instanceof InvalidTag) {
            tagPnl.add(new JLabel("Tag Name: " + tag.getName()));
            tagPnl.add(new JLabel("Tag Type: " + tag.getClass().getSimpleName()));
            tagPnl.add(new JLabel("Tag Size: " + ((InvalidTag) tag).getValue().length + " Bytes"));
        } else {
            tagPnl.add(new JLabel("Tag Name: " + tag.getName()));
            tagPnl.add(new JLabel("Tag Value: " + tag.getValue()));
            tagPnl.add(new JLabel("Tag Type: " + tag.getClass().getSimpleName()));
        }
        tagPnl.updateUI();
    }

    /**
     * Create nodes for the tree.
     *
     * @param top  The top tree node.
     * @param tags The list of tags.
     * @param list If the nodes being created are apart of a list-type tag.
     */
    private void createNodes(DefaultMutableTreeNode top, List<Tag<?>> tags, boolean list) {
        String prefix = "";
        int i = 0;
        for (Tag<?> tag : tags) {
            if (list)
                prefix = "{" + i + "} :: ";
            if (tag instanceof ObjectTag) {
                ObjectTag objTag = (ObjectTag) tag;
                DefaultMutableTreeNode cat = new DefaultMutableTreeNode(new TagHolder(prefix + tag.getName(), tag));
                createNodes(cat, objTag.getValue(), false);
                top.add(cat);
            } else if (tag instanceof CompressedObjectTag) {
                CompressedObjectTag objTag = (CompressedObjectTag) tag;
                DefaultMutableTreeNode cat = new DefaultMutableTreeNode(new TagHolder(prefix + tag.getName(), tag));
                createNodes(cat, objTag.getValue(), false);
                top.add(cat);
            } else if (tag instanceof ListTag) {
                ListTag<Tag<?>> listTag = (ListTag<Tag<?>>) tag;
                DefaultMutableTreeNode cat = new DefaultMutableTreeNode(new TagHolder(prefix + tag.getName() + " :: List[" + listTag.getValue().size() + "]", tag));
                createNodes(cat, listTag.getValue(), true);
                top.add(cat);
            } else if (tag instanceof InvalidTag) {
                top.add(new DefaultMutableTreeNode(new TagHolder(prefix + tag.getName() + " :: CustomTag [" + ((InvalidTag) tag).getValue().length + " Bytes]", tag)));
            } else {
                top.add(new DefaultMutableTreeNode(new TagHolder(prefix + tag.getName() + " :: " + tag.getValue(), tag)));
            }

            i++;
        }
    }

    /**
     * Get the compression type of a file.
     * <p>This does not work on custom compression types.</p>
     *
     * @param f The file f.
     * @return The compression type.
     */
    private Compressor getCompressionType(File f) {
        boolean error = false;
        try {
            new GZIPInputStream(new FileInputStream(f));
        } catch (IOException ex) {
            error = true;
        }
        if (!error)
            return new GZIPCompression();
        error = false;
        try {
            new InflaterInputStream(new FileInputStream(f)).read();
        } catch (IOException ex) {
            error = true;
        }
        if (!error)
            return new ZLIBCompression();
        error = false;
        try {
            new ZstdInputStream(new FileInputStream(f)).read();
        }catch (IOException e) {
            error = true;
        }
        if(!error)
            return new ZSTDCompression();
        return new NoCompression();
    }

    /**
     * Correct the name of the Compression Type.
     *
     * @param name The name input.
     * @return The corrected output.
     */
    private String correctName(String name) {
        return name == null ? "None" : name;
    }
}
