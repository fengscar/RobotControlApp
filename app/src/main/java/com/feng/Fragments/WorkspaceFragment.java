package com.feng.Fragments;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.feng.Activities.EditMapActivity;
import com.feng.Activities.EditWorkspaceActivity;
import com.feng.Activities.EditWorkspaceActivity.ChangeMapCallback;
import com.feng.Activities.MainActivity;
import com.feng.Base.BaseActivity;
import com.feng.CustomView.CustomDialog;
import com.feng.CustomView.CustomDialogCallback;
import com.feng.CustomView.PopupWindowManager;
import com.feng.Database.MapDatabaseHelper;
import com.feng.Database.Workspace;
import com.feng.RSS.R;
import com.feng.Utils.L;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-11-24 上午8:45:37
 * @功能 设置界面-编辑地图的主要界面  排列出已有的工作区 点击工作区可以进入,长点击可以编辑工作区属性
 */
public class WorkspaceFragment extends Fragment {
    //编辑界面 支持长按进入编辑 操作界面只支持短按 进入地图Fragment
    private static boolean isEdit;  //在编辑界面为TRUE 在操作界面为FALSE
    private MapDatabaseHelper mDatabaseHelper = MapDatabaseHelper.getInstance();


    private GridView gridView;
    private List<Workspace> list;

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            //刷新UI
            if (msg.what == 120) {
                initGridView();
            }
        }
    };

    /**
     * Fragment的子类不推荐使用带参数的构造函数
     * 取而代之的,对象使用setArgument(),Fragment使用getArgument() 来传递参数
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workspace, container, false);
        // 确定 当前是否处于编辑
        initEditState();

        gridView = (GridView) view.findViewById(R.id.workspaceGridView);
        initGridView();
        return view;
    }

    private void initGridView() {
        list = getDataList();
        // 根据分辨率大小 ,设置一行显示几个 工作区...
        Point size = ((BaseActivity) getActivity()).getScreenSize();
        if (size.x > 900) {
            gridView.setNumColumns(2);
        } else {
            gridView.setNumColumns(1);
        }

        gridView.setAdapter(new GridViewAdapter(this.getActivity()));

        /**
         * 以下是 3个监听器的初始化, 编辑状态下的 短击和长点击, 以及 操作模式下的短点击
         */
        //短按  进入  设置界面的 mapFragment
        //initEditState 实时获取当前的 编辑状态
        if (initEditState()) {
            gridView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    //传递 工作区的ID 给 地图编辑页面,让其初始化 该界面
                    Intent intent = new Intent();
                    intent.putExtra("workspaceID", list.get(position).getId());
                    intent.setClass(WorkspaceFragment.this.getActivity(), EditMapActivity.class);
                    startActivity(intent);
                }
            });
            //长按编辑
            if (isEdit) {
                gridView.setOnItemLongClickListener(new OnItemLongClickListener() {

                    public boolean onItemLongClick(AdapterView<?> parent,
                                                   View view, final int position, long id) {
                        final PopupWindowManager pwm = new PopupWindowManager(WorkspaceFragment.this.getActivity());
                        pwm.showPopupWindowAsDropDown(view,
                                R.layout.popup_edit_workspace, 200, -120,
                                new int[]{R.id.popupEditData, R.id.popupEditMap, R.id.popupDelete},
                                //点击 编辑时
                                new OnClickListener() {
                                    public void onClick(View v) {
                                        //点击后就关闭 popup_path
                                        pwm.close();
                                        //并打开 编辑对话框
                                        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
                                        builder.setResourceID(R.layout.dialog_workspace)
                                                .setButtonText("修改", "取消")
                                                .setTitle("编辑工作区")
                                                .setOkBtnClick(R.id.dialogOKBtn, new CustomDialogCallback() {
                                                    public boolean onDialogBtnClick(List<View> viewList) {
                                                        Workspace newData = Workspace.loadWorkspace(viewList);
                                                        if (newData != null && mDatabaseHelper.updateData(newData)) {
                                                            //成功后刷新UI
                                                            handler.sendEmptyMessage(120);
                                                            return true;
                                                        } else {
                                                            return false;
                                                        }
                                                    }
                                                })
                                                .setCancelBtnClick(R.id.dialogCancelBtn, null)
                                                .create(new int[]{R.id.dialog_workspace_id, R.id.dialog_workspace_floor, R.id.dialog_workspace_name},
                                                        new Object[]{list.get(position).getId(), list.get(position).getFloor(), list.get(position).getName()})
                                                .show();
                                    }
                                },
                                //点击 选择平面图时
                                new OnClickListener() {
                                    public void onClick(View v) {
                                        pwm.close();
                                        //								L.e("点击了平面图"+wsHolder.name.getText().toString());
                                        /**
                                         * 1.让Activity获得平面图
                                         * 2.返回给fragment,fragment保存到数据库
                                         * 3.fragment设置到 缩略图 并刷新UI
                                         */
                                        //第1步
                                        ((EditWorkspaceActivity) getActivity()).getBackGroundPic(new ChangeMapCallback() {
                                            public void changeBmp(Bitmap map) {
                                                //第2步
                                                list.get(position).setMapPic(map);
                                                mDatabaseHelper.updateData(list.get(position));
                                                handler.sendEmptyMessage(120);
                                            }
                                        });
                                        //刷新UI

                                    }
                                },
                                //点击删除时
                                new OnClickListener() {
                                    public void onClick(View v) {
                                        pwm.close();
                                        mDatabaseHelper.delData(list.get(position));
                                        //刷新UI
                                        handler.sendEmptyMessage(120);
                                    }
                                });
                        return true;
                    }
                });
            }

        }//进入 主页的mapFragment
        else {
            //与上面不同,这是 fragment的切换
            gridView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    ((MainActivity) getActivity()).changeToDetailFragment(list.get(position).getId());
                }
            });

        }
    }

    /**
     * 获取数据库中的 工作区 数据
     */
    public List<Workspace> getDataList() {
        List<Workspace> dataList = new ArrayList<>();
        try {
            dataList = mDatabaseHelper.getAllWorkspace();
        } catch (Exception e) {
            L.e(e.toString());
        }
        return dataList;
    }

    class GridViewAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        GridViewAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return list.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public View getView(final int position, View convertView, ViewGroup parent) {
            final WorkspaceHolder wsHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.gridview_of_workspace, null);
                wsHolder = new WorkspaceHolder();
                //	wsHolder.imageBtn=(ImageButton)convertView.findViewById(R.id.girdViewOfWorkspaceImageBtn);
                wsHolder.id = (TextView) convertView.findViewById(R.id.gridViewOfWorkspaceId);
                wsHolder.name = (TextView) convertView.findViewById(R.id.gridViewOfWorkspaceName);
                wsHolder.floor = (TextView) convertView.findViewById(R.id.gridViewOfWorkspaceFloor);
                wsHolder.imageBtn = (ImageView) convertView.findViewById(R.id.girdViewOfWorkspaceImageBtn);
                wsHolder.nodeNum = (TextView) convertView.findViewById(R.id.gridViewOfWorkspaceNodeNum);
                convertView.setTag(wsHolder);
            } else {
                wsHolder = (WorkspaceHolder) convertView.getTag();
            }

            wsHolder.id.setText(String.valueOf(list.get(position).getId()));
            wsHolder.floor.setText(String.valueOf(list.get(position).getFloor()));
            wsHolder.name.setText(list.get(position).getName());
            wsHolder.nodeNum.setText(String.valueOf(mDatabaseHelper.getWorkspaceNodeCount(list.get(position).getId())));
            Bitmap bmp = list.get(position).getMapPic();
            if (bmp != null) {
                wsHolder.imageBtn.setBackground(new BitmapDrawable(getResources(), bmp));
            } else {
                wsHolder.imageBtn.setBackgroundResource(R.mipmap.default_map_background);
            }
            return convertView;
        }

        class WorkspaceHolder {
            public TextView id;
            public TextView name;
            public TextView floor;
            public TextView nodeNum;
            public ImageView imageBtn;
        }
    }

    public boolean initEditState() {
        if (this.getActivity() instanceof EditWorkspaceActivity) {
            isEdit = true;
            return true;
        } else if (this.getActivity() instanceof MainActivity) {
            isEdit = false;
            return false;
        } else {
            L.i("未知界面");
            return false;
        }
    }


}
