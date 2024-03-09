package cn.ruc.readio.entity;

import android.graphics.Bitmap;

public class EditWorkPicture {
    private Bitmap picture = null;
    Boolean showDel = false;

    public EditWorkPicture(Bitmap picture){
        this.showDel = false;
        this.picture = picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public void setShowDel(Boolean showDel) {
        this.showDel = showDel;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public Boolean getShowDel() {
        return showDel;
    }
}
