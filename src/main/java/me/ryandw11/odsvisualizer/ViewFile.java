package me.ryandw11.odsvisualizer;

import me.ryandw11.ods.Compression;
import me.ryandw11.ods.ObjectDataStructure;
import me.ryandw11.ods.Tag;
import me.ryandw11.ods.tags.ListTag;
import me.ryandw11.ods.tags.ObjectTag;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class ViewFile extends JFrame {
    private JPanel tagPnl;
    public ViewFile(File file){
        super("Viewing ODS File: " + file.getName());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 500);
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        System.out.println(getCompressionType(file));
        ObjectDataStructure ods = new ObjectDataStructure(file, getCompressionType(file));

        DefaultMutableTreeNode top = new DefaultMutableTreeNode(file.getName());
        try {
            createNodes(top, ods.getAll(), false);
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null, "This file cannot be read. Are you sure this is an ODS file?", "Cannot Read File", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        JTree tree = new JTree(top);

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if(node == null) return;
            if(!(node.getUserObject() instanceof TagHolder)) return;
            TagHolder tagHolder = (TagHolder) node.getUserObject();
            setTagInfo(tagHolder.getTag());
        });

        JScrollPane treeView = new JScrollPane(tree);
        this.add(treeView, BorderLayout.CENTER);
        JPanel sidePnl = new JPanel();
        sidePnl.add(getFileInfo(file, ods));
        this.tagPnl = setupTagInfo();
        sidePnl.add(tagPnl);
        sidePnl.setLayout(new BoxLayout(sidePnl, BoxLayout.Y_AXIS));
        this.add(sidePnl, BorderLayout.EAST);


    }

    private JPanel getFileInfo(File f, ObjectDataStructure ods){
        JPanel panel = new JPanel();
        Border bb = BorderFactory.createLineBorder(Color.black);
        TitledBorder tt = BorderFactory.createTitledBorder(bb, "File Information");
        panel.setBorder(tt);
        panel.add(new JLabel("File Name: " + f.getName()));
        panel.add(new JLabel("File Size: " + f.length() + " bytes"));
        panel.add(new JLabel("File Compression: " + getCompressionType(f)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JPanel setupTagInfo(){
        JPanel panel = new JPanel();
        Border bb = BorderFactory.createLineBorder(Color.black);
        TitledBorder tt = BorderFactory.createTitledBorder(bb, "Tag Information");
        panel.setBorder(tt);
        panel.add(new JLabel("Select a tag to view it's information!"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private void setTagInfo(Tag<?> tag){
        tagPnl.removeAll();
        if(tag instanceof ObjectTag){
            tagPnl.add(new JLabel("Tag Name: " + tag.getName()));
            tagPnl.add(new JLabel("Tag Type: " + tag.getClass().getSimpleName()));
            tagPnl.add(new JLabel("# of SubTags: " + ((ObjectTag) tag).getValue().size()));
        }else if(tag instanceof ListTag<?>){
            tagPnl.add(new JLabel("Tag Name: " + tag.getName()));
            tagPnl.add(new JLabel("Tag Type: " + tag.getClass().getSimpleName()));
            tagPnl.add(new JLabel("# of Elements: " + ((ListTag<?>) tag).getValue().size()));
        }else{
            tagPnl.add(new JLabel("Tag Name: " + tag.getName()));
            tagPnl.add(new JLabel("Tag Value: " + tag.getValue()));
            tagPnl.add(new JLabel("Tag Type: " + tag.getClass().getSimpleName()));
        }
        tagPnl.updateUI();
    }

    private void createNodes(DefaultMutableTreeNode top, List<Tag<?>> tags, boolean list){
        String prefix = "";
        int i = 0;
        for(Tag<?> tag : tags){
            if(list)
                prefix = "{" + i + "} :: ";
            if(tag instanceof ObjectTag){
                ObjectTag objTag = (ObjectTag) tag;
                DefaultMutableTreeNode cat = new DefaultMutableTreeNode(new TagHolder(prefix + tag.getName(), tag));
                createNodes(cat, objTag.getValue(), false);
                top.add(cat);
            }else if(tag instanceof ListTag){
                ListTag<Tag<?>> listTag = (ListTag<Tag<?>>) tag;
                DefaultMutableTreeNode cat = new DefaultMutableTreeNode(new TagHolder(prefix + tag.getName() + " :: List[" + listTag.getValue().size() + "]", tag));
                createNodes(cat, listTag.getValue(), true);
                top.add(cat);
            }
            else{
                top.add(new DefaultMutableTreeNode(new TagHolder(prefix + tag.getName() + " :: " + tag.getValue(), tag)));
            }

            i++;
        }
    }

    private Compression getCompressionType(File f){
        boolean error = false;
        try{
            new GZIPInputStream(new FileInputStream(f));
        }catch(IOException ex){
            error = true;
        }
        if(!error)
            return Compression.GZIP;
        error = false;
        try{
            new InflaterInputStream(new FileInputStream(f)).read();
        }catch (IOException ex){
            error = true;
        }
        if(!error)
            return Compression.ZLIB;
        return Compression.NONE;
    }
}