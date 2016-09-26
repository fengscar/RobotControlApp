package com.feng.Database.Map;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.feng.Constant.I_MapData;
import com.feng.Utils.T;

import java.util.List;

public class Workspace implements I_MapData {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String FLOOR = "floor";
    public static final String BMP = "map";
    public static final String BMP_WIDTH = "bmpWidth";
    public static final String BMP_HEIGHT = "bmpHeight";

    public static final String COLUMNS = TextUtils.concat(ID, NAME, FLOOR, BMP, BMP_WIDTH, BMP_HEIGHT).toString();


    private int id;
    private String name;
    private int floor;

    private Bitmap mapPic;
    private int mWidth, mHeight;

    //region Constructor
    public Workspace() {
    }

    public Workspace(int id, int floor, String name) {

        this.id = id;
        this.name = name;
        this.floor = floor;
        this.mapPic = null;
    }

    public Workspace(int id, String name, int floor, Bitmap mapPic) {
        super();
        this.id = id;
        this.name = name;
        this.floor = floor;
        this.mapPic = mapPic;
    }

    public Workspace(int id, String name, int floor, Bitmap mapPic, int width, int height) {
        this.id = id;
        this.name = name;
        this.floor = floor;
        this.mapPic = mapPic;
        mWidth = width;
        mHeight = height;
    }
    //endregion

    //region hashCode,equals
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Workspace other = (Workspace) obj;
        return id == other.id;
    }
    //endregion

    //region Setter/Getter
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getFloor() {
        return floor;
    }

    public void setMapPic(Bitmap mapPic) {
        this.mapPic = mapPic;
    }

    public Bitmap getMapPic() {
        return mapPic;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    //endregion

    public static Workspace loadWorkspace(List<View> viewList) {
        String str[] = new String[3];
        for (int i = 0; i < 3; i++) {
            if (viewList.get(i) instanceof EditText) {
                str[i] = ((EditText) viewList.get(i)).getText().toString();
            } else if (viewList.get(i) instanceof TextView) {
                str[i] = ((TextView) viewList.get(i)).getText().toString();
            }
            if (str[i].length() <= 0) {
                T.show("请完整输入");
                return null;
            }
        }
        return new Workspace(Integer.parseInt(str[0]), Integer.parseInt(str[1]), str[2]);
    }

}
