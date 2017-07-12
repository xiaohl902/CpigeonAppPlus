package com.cpigeon.app.broadcastreceiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cpigeon.app.MainActivity;
import com.cpigeon.app.commonstandard.AppManager;
import com.cpigeon.app.modular.guide.view.SplashActivity;
import com.cpigeon.app.modular.matchlive.model.bean.MatchInfo;
import com.cpigeon.app.modular.matchlive.view.activity.RaceReportActivity;
import com.cpigeon.app.modular.matchlive.view.activity.RaceXunFangActivity;
import com.cpigeon.app.utils.CpigeonData;
import com.cpigeon.app.utils.SharedPreferencesTool;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by chenshuai on 2017/3/30.
 */

public class JPushBroadcastReceiver extends BroadcastReceiver {
    /**
     * 鸽友圈推送
     */
    private static final String TYPE_CIRCLE_MESSAGE = "circlemsg";
    /**
     * 今日比赛场次统计推送类型
     */
    public static final String TYPE_TODAY_RACE_COUNT = "TodayRaceCount";
    /**
     * 用户比赛关注推送
     */
    public static final String TYPE_USER_RACE_FOLLOW = "UserRaceFollow";

    private NotificationManager nm;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == nm) {
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        Bundle bundle = intent.getExtras();
//        Logger.d(TAG, "onReceive - " + intent.getAction() + ", extras: " + AndroidUtil.printBundle(bundle));
        Logger.d("onReceive - " + intent.getAction());

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            Logger.d("JPush用户注册成功");

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Logger.d("接受到推送下来的自定义消息");

            // Push Talk messages are push down by custom message format
            processCustomMessage(context, bundle);

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Logger.d("接受到推送下来的通知");

            receivingNotification(context, bundle);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Logger.d("用户点击打开了通知");

            openNotification(context, bundle);

        } else {
            Logger.d("Unhandled intent - " + intent.getAction());
        }
    }

    private void receivingNotification(Context context, Bundle bundle) {
        String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
        Logger.d(" title : " + title);
        String message = bundle.getString(JPushInterface.EXTRA_ALERT);
        Logger.d("message : " + message);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        Logger.d("extras : " + extras);
    }

    private void openNotification(Context context, Bundle bundle) {
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        Logger.d("extras : " + extras);
        String extraType = "", extraData = "";
        Intent intent;
        try {
            JSONObject extrasJson = new JSONObject(extras);
            extraType = extrasJson.optString("type");
            extraData = extrasJson.getString("data");
            Logger.d("data : " + extraData);
            if (TYPE_CIRCLE_MESSAGE.equals(extraType)) {
//            Intent mIntent = new Intent(context, GroupFriendActivity.class);
//            mIntent.putExtras(bundle);
//            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(mIntent);
            } else if (TYPE_TODAY_RACE_COUNT.equals((extraType))) {
                SharedPreferencesTool.Save(context.getApplicationContext(), MainActivity.APP_STATE_KEY_VIEWPAGER_SELECT_INDEX + CpigeonData.getInstance().getUserId(context.getApplicationContext()), 1, SharedPreferencesTool.SP_FILE_APPSTATE);
            } else if (TYPE_USER_RACE_FOLLOW.equals((extraType))) {
                MatchInfo matchInfo = JSON.parseObject(extraData, new TypeReference<MatchInfo>() {
                });
                SharedPreferencesTool.Save(context.getApplicationContext(), MainActivity.APP_STATE_KEY_VIEWPAGER_SELECT_INDEX + CpigeonData.getInstance().getUserId(context.getApplicationContext()), 1, SharedPreferencesTool.SP_FILE_APPSTATE);
                if (matchInfo != null && matchInfo.getRuid() == 0 && !"jg".equals(matchInfo.getDt())) {
                    if (matchInfo.isMatch()) {
                        intent = new Intent(context, RaceReportActivity.class);
                    } else {
                        intent = new Intent(context, RaceXunFangActivity.class);
                    }
                    Bundle bundle1 = new Bundle();                //创建Bundle对象
                    bundle1.putSerializable("matchinfo", matchInfo);     //装入数据
                    bundle1.putString("loadType", matchInfo.getLx());
                    intent.putExtras(bundle1);
                    context.startActivity(intent);
                    return;
                }
            }

        } catch (Exception e) {
            Logger.w("Unexpected: extras is not a valid json", e);
            return;
        }
        MainActivity mainActivity = (MainActivity) AppManager.getAppManager().getActivityByClass(MainActivity.class);
        Intent mIntent = null;
        if (mainActivity == null) {
            mIntent = new Intent(context, SplashActivity.class);
        } else {
            mIntent = new Intent(context, MainActivity.class);
        }
        mIntent.putExtras(bundle);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mIntent);
    }

    private void processCustomMessage(Context context, Bundle bundle) {
//        String title = bundle.getString(JPushInterface.EXTRA_TITLE);
//        String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
//        if (StringUtils.isEmpty(title)) {
//            Logger.w(TAG, "Unexpected: empty title (friend). Give up");
//            return;
//        }
//
//        boolean needIncreaseUnread = true;
//
//        if (title.equalsIgnoreCase(Config.myName)) {
//            Logger.d(TAG, "Message from myself. Give up");
//            needIncreaseUnread = false;
//            if (!Config.IS_TEST_MODE) {
//                return;
//            }
//        }
//
//        String channel = null;
//        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
//        try {
//            JSONObject extrasJson = new JSONObject(extras);
//            channel = extrasJson.optString(Constants.KEY_CHANNEL);
//        } catch (Exception e) {
//            Logger.w(TAG, "Unexpected: extras is not a valid json", e);
//        }
//
//        // Send message to UI (Webview) only when UI is up
//        if (!Config.isBackground) {
//            Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
//            msgIntent.putExtra(Constants.KEY_MESSAGE, message);
//            msgIntent.putExtra(Constants.KEY_TITLE, title);
//            if (null != channel) {
//                msgIntent.putExtra(Constants.KEY_CHANNEL, channel);
//            }
//
//            JSONObject all = new JSONObject();
//            try {
//                all.put(Constants.KEY_TITLE, title);
//                all.put(Constants.KEY_MESSAGE, message);
//                all.put(Constants.KEY_EXTRAS, new JSONObject(extras));
//            } catch (JSONException e) {
//            }
//            msgIntent.putExtra("all", all.toString());
//
//            context.sendBroadcast(msgIntent);
//        }
//
//        String chatting = title;
//        if (!StringUtils.isEmpty(channel)) {
//            chatting = channel;
//        }
//
//        String currentChatting = MyPreferenceManager.getString(Constants.PREF_CURRENT_CHATTING, null);
//        if (chatting.equalsIgnoreCase(currentChatting)) {
//            Logger.d(TAG, "Is now chatting with - " + chatting + ". Dont show notificaiton.");
//            needIncreaseUnread = false;
//            if (!Config.IS_TEST_MODE) {
//                return;
//            }
//        }
//
//        if (needIncreaseUnread) {
//            unreadMessage(title, channel);
//        }
//
//        NotificationHelper.showMessageNotification(context, nm, title, message, channel);
    }

    // When received message, increase unread number for Recent Chat
    private void unreadMessage(final String friend, final String channel) {
//        new Thread() {
//            public void run() {
//                String chattingFriend = null;
//                if (StringUtils.isEmpty(channel)) {
//                    chattingFriend = friend;
//                }
//
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("udid", Config.udid);
//                params.put("friend", chattingFriend);
//                params.put("channel_name", channel);
//
//                try {
//                    HttpHelper.post(Constants.PATH_UNREAD, params);
//                } catch (Exception e) {
//                    Logger.e(TAG, "Call pushtalk api to report unread error", e);
//                }
//            }
//        }.start();
    }
}
