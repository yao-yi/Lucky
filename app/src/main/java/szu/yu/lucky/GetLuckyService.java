package szu.yu.lucky;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class GetLuckyService extends AccessibilityService {
    static final String LOG = "yu";
    static final String CHAT_UI_CLASSNAME = "com.tencent.mm.ui.LauncherUI";  //聊天的界面  E/yu(11965): com.tencent.mm.ui.LauncherUI
    static final String LUCKY_UI_CLASSNAME = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";  //开启红包界面 E/yu(11965): com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI
    static final String KEY = "[微信红包]";  //开启红包界面
    boolean flag = false;  //设定一个标志，为了避免重复抢红包吧。默认为false，当通知栏出现抢红包消息，并且成功进入请红包界面，为true；点击后设为false。


    public GetLuckyService() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        //Log.e(LOG,"收到通知" );

        /**
         * 判断事件类型，如果为通知时间，则打开通知消息
         */
        if(event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> textList =  event.getText();
            if(textList != null) {
                for(CharSequence s : textList) {
                    /**
                     * 打印通知消息
                     */
                    Log.e(LOG,s.toString() );
                    if(s.toString().contains(KEY)) {
                        if(event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                            Notification notification = (Notification) event.getParcelableData();
                            PendingIntent pd = notification.contentIntent;
                            try {
                                /**
                                 * 发送Intention，打开通知栏
                                 */
                                pd.send();
                                flag = true; //当通知栏出现抢红包消息，并且成功进入请红包界面，为true
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
            /**
             * 如果是窗口改变，应当对当前窗口进行遍历，获取红包子节点
             */
        }else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            /**
             * 获取当前窗口信息，用来判断是否为微信的窗口
             */
            String className = event.getClassName().toString();
            Log.e(LOG,className);

            if(className.equals(CHAT_UI_CLASSNAME)) {
                /**
                 * 如果当前窗口为聊天界面，则对模拟点击打开抢红包
                 */
                clickLucky();
            }else if (className.equals(LUCKY_UI_CLASSNAME)) {
                /**
                 * 如果当前窗口为红包界面，则开红包进入详细界面
                 */
                openLucky();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openLucky() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo != null) {
            int count = nodeInfo.getChildCount();
            for(int i = 0; i < count; i ++) {
                AccessibilityNodeInfo node = nodeInfo.getChild(i);
                if(node.isClickable()) {
                    Log.e(LOG, "找到啦");
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return;
                }
            }
        }
    }

    /**
     * 对红包进行模拟点击，进入拆红包界面
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void clickLucky() {
        /**
         * 对flag进行判断，为true就点击
         */
        if(flag) {
            /**
             * 获取当前活动节点（就是活动界面）
             */
            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
            Log.e(LOG,nodeInfo.getClassName().toString());
            /**
             * 通过根节点找到所有包含“领取红包”文本的子节点，返回一个链表
             */
            List<AccessibilityNodeInfo> nodeInfos = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
            /**
             * 遍历所有子节点，但是会有一个问题，就是当出现多个节点点击会混乱，我们要的是最新的红包节点，下面进行改造
             */
            for(AccessibilityNodeInfo node : nodeInfos) {
                if(node != null) {
                    Log.e(LOG, "找到啦");
                    node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    flag = false; //点击后设为false，知道下一次通知栏有红包消息为止
                }
            }
        }


    }

    @Override
    public void onInterrupt() {

    }

//
}
