package com.sms.demo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sms.demo.Util.GlobalContext;
import com.sms.demo.contact.sms.Config;
import com.sms.demo.contact.sms.MsgAdapter;
import com.sms.demo.contact.sms.SendActivity;
import com.sms.demo.contact.sms.Sms;
import com.sms.demo.contact.sms.bean.Msg;
import com.sms.demo.contact.sms.bean.ReceiverMsg;
import com.sms.demo.contact.sms.dialog.PopMainMenuDialog;
import com.sms.demo.contact.sms.dialog.PopMenuDialog;
import com.sms.demo.contact.sms.privatebox.bean.ThreadIdMsg;
import com.sms.demo.contact.sms.privatebox.db.DbUtil;
import com.sms.demo.contact.sms.send.SendListenerManager;
import com.sms.demo.contact.sms.util.CommonAsyncQuery;
import com.sms.demo.contact.sms.util.SmsUtil;
import com.sms.demo.contact.sms.view.XCRoundImageView;
import com.sms.demo.emotion.Main;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SendListenerManager.onSendListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ImageView ivEditMsg;
    private ListView listView;
    //    private Map<String, ArrayList<ReceiverMsg>> msgMap = new HashMap<>();
    private RelativeLayout rlToast;
    //    private List<ReceiverMsg> smsInPhone = new ArrayList<>();
//    private MsgAdapter msgadapter;
    private ConversationAdapter mAdapter;
    private RelativeLayout rlNoMessage;
    private ImageView ivMenu;
    private PopMainMenuDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        prepareData();
//        SmsUtil.queryContactPhoneNumber();
    }

    private void initView() {
        ivEditMsg = (ImageView) findViewById(R.id.editMsg);
        listView = (ListView) findViewById(R.id.listView);
        rlToast = (RelativeLayout) findViewById(R.id.rl_toast);
        rlNoMessage = (RelativeLayout) findViewById(R.id.rl_no_message);

        ivMenu = (ImageView) findViewById(R.id.iv_menu);
        dialog = new PopMainMenuDialog(this);

        ivEditMsg.setOnClickListener(this);
        rlToast.setOnClickListener(this);
        ivMenu.setOnClickListener(this);
//        getSms();
//        msgadapter = new MsgAdapter(this, msgMap);
//        listView.setAdapter(msgadapter);
        mAdapter = new ConversationAdapter(this, null);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        SendListenerManager.getInstance().addListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();
        if (!SmsUtil.isDefaultSms(this)) {
            rlToast.setVisibility(View.VISIBLE);
            // App is not default.
            // Show the "not currently set as the default SMS app" interface
        } else {
            rlToast.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SendListenerManager.getInstance().removeSendListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editMsg:
                startActivity(new Intent(MainActivity.this, SendActivity.class));
                break;
            case R.id.rl_toast:
                SmsUtil.setDefaultSms(this);
                break;
            case R.id.iv_menu:
                openOrCloseMenu();
                break;
        }
    }


    /**
     * 打开联系人详情页或者添加页
     */
    public void openOrCloseMenu() {
        if (this.dialog == null) {
            this.dialog = new PopMainMenuDialog(this);
        }
        if (this.dialog == null) {
            return;
        }
        if (this.dialog.isShowing()) {
            this.dialog.dismiss();
            return;
        }
        this.dialog.show(this.ivMenu.getRight(), 0);

    }

    @Override
    public void onSendChanged(String phoneNumber, ArrayList<ReceiverMsg> msg) {
        Log.d("wwq", "phoneNumber: " + phoneNumber + "  msg: " + msg.toString());
//        msgadapter.Refersh(phoneNumber, msg);
        rlNoMessage.setVisibility(View.GONE);
    }

    private final String[] CONVERSATION_PROJECTION = {
            "sms.thread_id AS _id",
            "sms.address AS address",
            "sms.date AS date",
            "sms.body AS body",
            "groups.msg_count AS count",
            "sms.type AS type",
            "sms.status AS status",
            "sms.read AS read",
            "sms.person AS person"


    };

    private final int THREAD_ID_COLUMN_INDEX = 0;
    private final int ADDRESS_COLUMN_INDEX = 1;
    private final int DATE_COLUMN_INDEX = 2;
    private final int BODY_COLUMN_INDEX = 3;
    private final int COUNT_COLUMN_INDEX = 4;
    private final int TYPE_COLUMN_INDEX = 5;
    private final int STATUS_COLUMN_INDEX = 6;
    private final int READ_COLUMN_INDEX = 7;

    private void prepareData() {
        CommonAsyncQuery asyncQuery = new CommonAsyncQuery(getContentResolver(), new CommonAsyncQuery.OnQueryCompleteListener() {
            @Override
            public void onPreQueryComplete(int token, Cursor cursor) {

                for (int m = 0; m < GlobalContext.getmMsg().size(); m++) {
                    DbUtil.getInstance().savePrivate(GlobalContext.getmMsg().get(m));
                }
                int count = cursor.getCount();
                if (count <= 0) {
                    rlNoMessage.setVisibility(View.VISIBLE);
                } else {
                    rlNoMessage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPostQueryComplete(int token, Cursor cursor) {

            }
        });
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String threadIDs = intent.getStringExtra("threadIDs");

        String selection = null;
        if (!TextUtils.isEmpty(title)) {
            // 是群组页面跳转过来
            selection = "thread_id in " + threadIDs;
            setTitle(title);
        }
        // 开始异步查询
        asyncQuery.startQuery(0, mAdapter, Sms.CONVERSATION_URI, CONVERSATION_PROJECTION, selection, null, "date desc");
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        int thread_id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
        // 跳转到会话详情页面
        String address = cursor.getString(ADDRESS_COLUMN_INDEX);
        Intent intent = new Intent(this, Main.class);
        intent.putExtra("address", address);
        intent.putExtra("thread_id", thread_id);
        Log.d("wwq", "address: " + address + "  thread_id: " + thread_id);
        startActivity(intent);
    }

    private boolean isSelect = false;

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//        isSelect=true;
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        int thread_id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
        Log.d("wwq", "thread_id: " + thread_id);
        Uri mUri = Uri.parse("content://sms/conversations/" + thread_id);
        int delete = getContentResolver().delete(mUri, null, null);
        Log.d("wwq", "delete: " + delete);
        mAdapter.notifyDataSetChanged();
        return true;
    }

    class ConversationAdapter extends CursorAdapter {

        private MsgHolder mHolder;

        public ConversationAdapter(Context context, Cursor c) {
            super(context, c);
        }

        /**
         * 当convertView等于null时, 调用, 返回一个item的View
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = View.inflate(context, R.layout.layout_sms_main_listview_item, null);
            mHolder = new MsgHolder();
            mHolder.ivIcon = (XCRoundImageView) view.findViewById(R.id.iv_icon);
            mHolder.tvName = (TextView) view.findViewById(R.id.tv_item_phone_or_nam);
            mHolder.tvBody = (TextView) view.findViewById(R.id.tv_item_body);
            mHolder.tvDate = (TextView) view.findViewById(R.id.tv_item_date);
            mHolder.ivWaring = (ImageView) view.findViewById(R.id.iv_item_waring);
            mHolder.ivEdit = (ImageView) view.findViewById(R.id.iv_item_eidt);
            mHolder.ivCheck = (ImageView) view.findViewById(R.id.iv_checkbox);
            view.setTag(mHolder);
            return view;
        }

        /**
         * 给item的布局绑定数据
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            mHolder = (MsgHolder) view.getTag();
            int thread_id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
            String address = cursor.getString(ADDRESS_COLUMN_INDEX);
            long date = cursor.getLong(DATE_COLUMN_INDEX);
            String body = cursor.getString(BODY_COLUMN_INDEX);
            int type = cursor.getInt(TYPE_COLUMN_INDEX);
            Log.d("wwq", "type:  " + type);
            int count = cursor.getInt(COUNT_COLUMN_INDEX);
            // 通过号码查询联系人的姓名
            if (isSelect) {
                mHolder.ivCheck.setVisibility(View.VISIBLE);
                view.setBackgroundResource(R.color.toolbar_btn_select);
            }
            if (type == 5) {
                mHolder.ivWaring.setVisibility(View.VISIBLE);
                mHolder.ivEdit.setVisibility(View.GONE);
            }
            String contactName = SmsUtil.getContactName(getContentResolver(), address);
            if (TextUtils.isEmpty(contactName)) {
                mHolder.tvName.setText(address + "(" + count + ")");
                mHolder.ivIcon.setImageResource(R.drawable.zerotheme_default_head);
            } else {
                // 通讯录中有此号码
                mHolder.tvName.setText(contactName + "(" + count + ")");
                Bitmap contactIcon = SmsUtil.getContactIcon(getContentResolver(), address);
                Log.d("wwq", "contactIcon: " + contactIcon + "  " + address);
                if (contactIcon != null) {
                    mHolder.ivIcon.setImageBitmap(contactIcon);
                } else {
                    mHolder.ivIcon.setImageResource(R.drawable.zerotheme_default_head);
                }
            }
            String strDate = null;
            if (DateUtils.isToday(date)) {
                // 显示时间
                strDate = DateFormat.getTimeFormat(context).format(date);
            } else {
                // 显示日期
                strDate = DateFormat.getDateFormat(context).format(date);
            }
            mHolder.tvDate.setText(strDate);
            mHolder.tvBody.setText(body);
        }
    }

    public class MsgHolder {
        private XCRoundImageView ivIcon;
        private TextView tvName;
        private TextView tvDate;
        private TextView tvBody;
        private ImageView ivWaring;
        private ImageView ivEdit;
        public ImageView ivCheck;
    }


}
