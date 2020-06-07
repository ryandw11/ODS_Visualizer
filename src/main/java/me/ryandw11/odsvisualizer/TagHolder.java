package me.ryandw11.odsvisualizer;

import me.ryandw11.ods.Tag;

public class TagHolder {
    private Tag<?> tag;
    String info;

    public TagHolder(String info, Tag<?> tag){
        this.tag = tag;
        this.info = info;
    }

    public Tag<?> getTag(){
        return tag;
    }

    public String getInfo(){
        return this.info;
    }

    @Override
    public String toString(){
        return info;
    }
}
