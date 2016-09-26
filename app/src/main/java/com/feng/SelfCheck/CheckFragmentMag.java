/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-3-22 上午9:57:37
 */
package com.feng.SelfCheck;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.feng.RSS.R;
import com.feng.Usb.ArmHandler.MagHandler;
import com.feng.Usb.ArmProtocol;
import com.feng.Usb.UsbData;
import com.feng.Utils.Verifier;

public class CheckFragmentMag extends CheckFragment {
    private static final String TAG = "CheckFragmentMag";
    private GridView gvMagFront, gvMagCenter;
    private MagGridViewAdapter mAdapterFront, mAdapterCenter;
    private final int MAG_COUNT_FRONT = 32, MAG_COUNT_CENTER = 26;
    private boolean[] magListFront = new boolean[MAG_COUNT_FRONT], magListCenter = new boolean[MAG_COUNT_CENTER];

    public static CheckFragment newInstance(int expRes, int imgRes) {
        CheckFragment f = new CheckFragmentMag();
        Bundle args = new Bundle();
        args.putInt(Explain_RES, expRes);
        args.putInt(Image_RES, imgRes);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_self_check_mag, null);
        initView();
        initValue();

        return view;
    }

    @Override
    protected void initView() {
        ivSignPic = (ImageView) view.findViewById(R.id.ivSignPic);
        tvExplain = (TextView) view.findViewById(R.id.tvExplain);

        tvExplain.setText(this.getArguments().getInt(Explain_RES));
        if (getArguments().getInt(Image_RES) != 0) {
            ivSignPic.setImageResource(this.getArguments().getInt(Image_RES));
        } else {
            ivSignPic.setVisibility(View.GONE);
        }


    }

    private void initValue() {
        gvMagFront = (GridView) view.findViewById(R.id.gvMagFront);
        mAdapterFront = new MagGridViewAdapter(magListFront);
        gvMagFront.setAdapter(mAdapterFront);

        gvMagCenter = (GridView) view.findViewById(R.id.gvMagCenter);
        mAdapterCenter = new MagGridViewAdapter(magListCenter);
        gvMagCenter.setAdapter(mAdapterCenter);
    }

    @Override
    public boolean startSelfCheck() {
        return MagHandler.getInstance().startSelfCheck();
    }

    //自检接收的数据有 可能是2个字节( 只读到前面的磁条),也有可能是4个字节( 前后磁条都读到)
    @Override
    public void onReceiveArmData(UsbData usbData) {
        if (new Verifier().compareHead(usbData.getDataReceive(), ArmProtocol.SelfCheckMagPosition)) {
            byte[] body = usbData.getReceiveBody();
            if (body == null || (body.length != 4 && body.length != 2)) {
                Log.e(TAG, "onReceiveArmData: 数据错误");
                return;
            }
            if (body[1] != 0x00) {
                magListFront[(int) body[1] - 1] = true;
                mAdapterFront.notifyDataSetChanged();
            }
            if (body.length == 4 && body[3] != 0x00) {
                magListCenter[(int) body[3] - 1] = true;
                mAdapterCenter.notifyDataSetChanged();
            }
//            // 前方传感器
//            if (body[0] == 0x01) {
//                for (byte b : body) {
//                    if ((int) b > 32 || (int) b < 1) {
//                        continue;
//                    }
//                    magListFront[(int) b - 1] = true;
//                }
//                mAdapterFront.notifyDataSetChanged();
//            }
//            //中间传感器
//            if (body[0] == 0x02) {
//                for (byte b : body) {
//                    if ((int) b > 26 || (int) b < 1) {
//                        continue;
//                    }
//                    magListCenter[(int) b - 1] = true;
//                }
//                mAdapterCenter.notifyDataSetChanged();
//            }
        }
    }


    class MagGridViewAdapter extends BaseAdapter {
        public MagGridViewAdapter(@NonNull boolean[] b) {
            mListData = b;
        }

        private boolean[] mListData;

        private void setDataSource(@NonNull boolean[] b) {
            mListData = b;
        }

        public int getCount() {
            return mListData.length;
        }

        public Object getItem(int position) {
            return mListData[position];
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getActivity()).inflate(R.layout.gridview_of_mag, null);
            Button tbMag = (Button) convertView.findViewById(R.id.btnMag);
            tbMag.setText(String.valueOf(position + 1));
            tbMag.setSelected(mListData[position]);

            return convertView;
        }
    }

}

