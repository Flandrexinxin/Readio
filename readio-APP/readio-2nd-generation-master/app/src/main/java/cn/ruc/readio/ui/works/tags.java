package cn.ruc.readio.ui.works;

public class tags {
    private int tagId;
    private String content;
    private int linkedTimes;

    public tags() {
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public int getLinkedTimes() {
        return linkedTimes;
    }

    public void setLinkedTimes(int linkedTimes) {
        this.linkedTimes = linkedTimes;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
