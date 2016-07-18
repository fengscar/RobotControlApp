package com.feng.Activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.feng.Base.BaseActivity;
import com.feng.Constant.I_Parameters;
import com.feng.Database.MapDatabaseHelper;
import com.feng.Database.Workspace;
import com.feng.Fragments.WorkspaceFragment;
import com.feng.RSS.R;
import com.feng.Schedule.ScheduleClient;
import com.feng.CustomView.CustomDialog;
import com.feng.CustomView.CustomDialogCallback;
import com.feng.Utils.L;
import com.feng.RobotApplication;
import com.feng.Utils.SP;
import com.feng.Utils.T;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.List;

public class EditWorkspaceActivity extends BaseActivity implements I_Parameters {
    private final static String TAG = EditWorkspaceActivity.class.getSimpleName();
    public MyHandler handler = new MyHandler(this);
    private FloatingActionButton fabEditMapversion, fabUpdateMap, fabAddWorkspace, fabSaveMap;
    private FloatingActionsMenu fam;

    private static class MyHandler extends Handler {
        private WeakReference<EditWorkspaceActivity> activityWeakReference;

        public MyHandler(EditWorkspaceActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            EditWorkspaceActivity activity = activityWeakReference.get();
            if (activity != null) {
                if (msg.what == REFRESH) {
                    activity.initFragment();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stubj
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workspace);
        initView();
        initFragment();

    }

    @Override
    protected void onResume() {
        super.onResume();
        RobotApplication.getScheduleClient().putNotifier(EditWorkspaceActivity.class.getSimpleName(), new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ScheduleClient.MAP_UPDATE:
                        mapUpdate();
                        break;
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        RobotApplication.getScheduleClient().removeNotifier(EditWorkspaceActivity.class.getSimpleName());
    }

    private void initView() {
        EditWorkspaceClickListener listener = new EditWorkspaceClickListener();
        // 返回按键
        Button cancelBtn = (Button) findViewById(R.id.btnUniformTitleLeft);
        cancelBtn.setText(R.string.cancel);
        cancelBtn.setOnClickListener(listener);
        // 隐藏标题栏右侧按键
        findViewById(R.id.btnUniformTitleRight).setVisibility(View.INVISIBLE);
        // title
        ((TextView) findViewById(R.id.tvUniformTitleCenter)).setText(R.string.setting_edit_workspace);


        //悬浮 菜单
        fam = (FloatingActionsMenu) findViewById(R.id.fam);

        // 获取地图版本
        int mapVersion = (int) SP.get(this, I_Parameters.MAP_VERSION, 1);
        // 修改地图版本
        fabEditMapversion = (FloatingActionButton) findViewById(R.id.fabEditMapVersion);
        fabEditMapversion.setTitle("地图版本: " + mapVersion);
        fabEditMapversion.setOnClickListener(listener);

        // 保存地图到下位机
        fabSaveMap = (FloatingActionButton) findViewById(R.id.fabSaveMap);
        fabSaveMap.setOnClickListener(listener);

        // 从 调度端 更新地图
        fabUpdateMap = (FloatingActionButton) findViewById(R.id.fabUpdateMap);
        fabUpdateMap.setOnClickListener(listener);

        // 添加工作区
        fabAddWorkspace = (FloatingActionButton) findViewById(R.id.fabAddWorkspace);
        fabAddWorkspace.setOnClickListener(listener);

        fabAddWorkspace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                T.show("OK");
                return true;
            }
        });

    }

    // 当地图更新时...
    private void mapUpdate() {
        int mapVersion = (int) SP.get(this, I_Parameters.MAP_VERSION, 1);
        fabEditMapversion.setTitle("地图版本: " + mapVersion);

        initFragment();
    }

    public void initFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        WorkspaceFragment wFragment = new WorkspaceFragment();
        fragmentTransaction.remove(wFragment);
        fragmentTransaction.replace(R.id.edit_workspace_frame, wFragment);
        fragmentTransaction.commit();
    }

    public class EditWorkspaceClickListener implements android.view.View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                // 返回 按键
                case R.id.btnUniformTitleLeft:
                    EditWorkspaceActivity.this.finish();
                    break;

                //修改地图版本
                case R.id.fabEditMapVersion:
                    final View view = View.inflate(EditWorkspaceActivity.this, R.layout.dialog_input_oneline, null);
                    final EditText etInputMode = (EditText) view.findViewById(R.id.etInput);
                    int mapVersion = (int) SP.get(EditWorkspaceActivity.this, I_Parameters.MAP_VERSION, 1);
                    etInputMode.setText(String.valueOf(mapVersion));
                    etInputMode.setSelection(etInputMode.getText().length());

                    new AlertDialog.Builder(EditWorkspaceActivity.this)
                            .setTitle("编辑地图版本")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(view)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        int mapVersion = Integer.valueOf(etInputMode.getText().toString());
                                        SP.put(EditWorkspaceActivity.this, I_Parameters.MAP_VERSION, mapVersion);
                                        T.show("修改地图版本成功");

                                        mapUpdate();
                                    } catch (NumberFormatException e) {
                                        T.show("只接受数字格式的版本号");
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                    fam.collapse();
                    break;

                // 添加工作区
                case R.id.fabAddWorkspace:
                    final MapDatabaseHelper mDatabase = MapDatabaseHelper.getInstance();
                    int maxWorkspaceID = mDatabase.getMaxWorkspaceID();
                    CustomDialog.Builder builder = new CustomDialog.Builder(EditWorkspaceActivity.this);
                    builder.setResourceID(R.layout.dialog_workspace)
                            .setButtonText("添加", "取消")
                            .setTitle("添加工作区")
                            .setOkBtnClick(R.id.dialogOKBtn, new CustomDialogCallback() {
                                public boolean onDialogBtnClick(List<View> viewList) {
                                    Workspace wData = Workspace.loadWorkspace(viewList);
                                    if (wData != null && mDatabase.addData(wData)) {
                                        handler.sendEmptyMessage(REFRESH);
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            })
                            .setCancelBtnClick(R.id.dialogCancelBtn, null)
                            .create(new int[]{R.id.dialog_workspace_id, R.id.dialog_workspace_floor,
                                    R.id.dialog_workspace_name}, new Object[]{maxWorkspaceID + 1, null, null})
                            .show();
                    fam.collapse();
                    break;

                case R.id.fabUpdateMap:
                    RobotApplication.getScheduleClient().updateMap();
                    T.show("正在更新地图...");
                    fam.collapse();
                    break;

                case R.id.fabSaveMap:
                    RobotApplication.getArmUsbUtil().saveMap();
                    T.show("正在保存地图...");
                    fam.collapse();
                    break;
                default:
                    break;
            }
            handler.sendEmptyMessage(0);
        }

    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        handler.sendEmptyMessage(REFRESH);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.i(TAG, "onActivityResult: " + uri.toString());
            ContentResolver cr = this.getContentResolver();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                mCallback.changeBmp(bitmap);
            } catch (FileNotFoundException e) {
                L.e(e.toString());
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public interface ChangeMapCallback {
        void changeBmp(Bitmap map);
    }

    private ChangeMapCallback mCallback;

    public void getBackGroundPic(ChangeMapCallback cmcb) {
        Intent intent = new Intent();
        /* 开启Pictures画面Type设定为image */
        intent.setType("image/*");
        /* 使用Intent.ACTION_GET_CONTENT这个Action */
        intent.setAction(Intent.ACTION_GET_CONTENT);
        /* 取得相片后返回本画面 */
        startActivityForResult(intent, 1);
        this.mCallback = cmcb;
    }

}