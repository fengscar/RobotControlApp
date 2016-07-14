package com.feng.Database;

import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import com.feng.Constant.I_MapData;
import com.feng.RSS.R;
import com.feng.Utils.T;

import java.util.List;

public class Route extends DatabaseManager implements I_MapData {
    private int workspaceID;  //所在的工作区编号
    private int id; //路线编号
    private int preID;  //前级路线号
    private String name;
    private boolean enabled;

    //region Constructor
    public Route() {
    }

    public Route(int routeID) {
        this.id = routeID;
    }

    public Route(int id, int preID, String name, int workspaceID) {
        super();
        this.id = id;
        this.name = name;
        this.preID = preID;
        this.workspaceID = workspaceID;
    }

    public Route(int id, int preID, String name, int workspaceID,
                 boolean enabled) {
        super();
        this.workspaceID = workspaceID;
        this.id = id;
        this.preID = preID;
        this.name = name;
        this.enabled = enabled;
    }
    //endregion

    //region Getter/Setter
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getWorkspaceID() {
        return workspaceID;
    }

    public void setWorkspaceID(int workspaceID) {
        this.workspaceID = workspaceID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPreID() {
        return preID;
    }

    public void setPreID(int preID) {
        this.preID = preID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //endregion

    //region hashCode,equals,toString...
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
        Route other = (Route) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Route [workspaceID=" + workspaceID + ", id=" + id + ", preID="
                + preID + ", name=" + name + ", enabled=" + enabled + "]";
    }
    //endregion

    public static Route loadRoute(Route route, List<View> viewList) {
        for (View currentView : viewList) {
            try {
                switch (currentView.getId()) {
                    case R.id.spPreRoute:
                        Spinner spPreRoute = (Spinner) currentView;
                        // 如果Spinner不可见,表示前级路线为0
                        if (spPreRoute.getVisibility() == View.INVISIBLE) {
                            route.setPreID(0);
                            break;
                        }
                        // 如果可见,获取前级路线ID
                        String preRouteName = spPreRoute.getSelectedItem().toString();
                        Route preRoute = MapDatabaseHelper.getInstance().getRouteByName(route.getWorkspaceID(), preRouteName);
                        route.setPreID(preRoute.getId());

                        break;
                    case R.id.etRouteName:
                        route.setName(((EditText) currentView).getText().toString());
                        break;
                    case R.id.swRouteEnable:
                        route.setEnabled(((Switch) currentView).isChecked());
                        break;
                }
            } catch (NumberFormatException e) {
                T.show("添加失败,请完整输入数据");
                return null;
            }
        }
        return route;
    }


}
