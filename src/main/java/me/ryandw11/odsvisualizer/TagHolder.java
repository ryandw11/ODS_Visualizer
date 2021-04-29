package me.ryandw11.odsvisualizer;

import me.ryandw11.ods.Tag;

/**
 * Hold data for a tag.
 */
public class TagHolder {
    private final Tag<?> tag;
    private final String info;

    /**
     * Create a TagHolder.
     *
     * @param info The information about the tag.
     * @param tag  The tag.
     */
    public TagHolder(String info, Tag<?> tag) {
        this.tag = tag;
        this.info = info;
    }

    /**
     * Get the tag.
     *
     * @return The tag.
     */
    public Tag<?> getTag() {
        return tag;
    }

    /**
     * Get the information.
     *
     * @return The information.
     */
    public String getInfo() {
        return this.info;
    }

    @Override
    public String toString() {
        return info;
    }
}
