package biz.dealnote.messenger.push.message;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.MainActivity;
import biz.dealnote.messenger.longpoll.AppNotificationChannels;
import biz.dealnote.messenger.longpoll.NotificationHelper;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.push.NotificationScheduler;
import biz.dealnote.messenger.push.OwnerInfo;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.push.NotificationUtils.configOtherPushNotification;
import static biz.dealnote.messenger.push.NotificationUtils.optInt;
import static biz.dealnote.messenger.util.Utils.stringEmptyIfNull;

/**
 * Created by ruslan.kolbasa on 10.01.2017.
 * phoenix
 */
public class NewPostPushMessage {

 //04-14 13:00:42.166 1784-2485/ru.ezorrio.phoenix D/MyFcmListenerService: onMessage,
    // from: 652332232777, collapseKey: null, data: {image_type=user, from_id=280186075,
    // id=new_post_280186075_56, url=https://vk.com/wall280186075_56,
    // body=Добро пожаловать на мою страницу. Мы тестируем FCM, icon=followers_24,
    // time=1523700043, type=post, badge=1,
    // image=[{"width":200,"url":"https:\/\/pp.userapi.com\/c837424\/v837424529\/5c2cb\/OkkyraBZJCY.jpg","height":200},
    // {"width":100,"url":"https:\/\/pp.userapi.com\/c837424\/v837424529\/5c2cc\/dRPyhRW_dvU.jpg","height":100},
    // {"width":50,"url":"https:\/\/pp.userapi.com\/c837424\/v837424529\/5c2cd\/BB6tk_bcJ3U.jpg","height":50}],
    // sound=1, title=Yevgeni Polkin published a post, to_id=216143660, group_id=posts,
    // context={"item_id":"56","owner_id":280186075,"type":"post"}}

    private final int accountId;

    private final int fromId;

    private final int postId;

    private final String text;

    private final String groupName;

    private final String firstName;

    private final String lastName;

    //private String postType;

    public NewPostPushMessage(int accountId, RemoteMessage remote){
        this.accountId = accountId;
        this.fromId = optInt(remote, "from_id");
        this.postId = optInt(remote, "post_id");
        this.text = remote.getData().get("text");
        this.groupName = remote.getData().get("group_name");
        this.firstName = remote.getData().get("first_name");
        this.lastName = remote.getData().get("last_name");
        //this.postType = bundle.getString("post_type"); // group_status, status
    }

    public void notifyIfNeed(Context context){
        if(fromId == 0){
            Logger.wtf("NewPostPushMessage", "from_id is NULL!!!");
            return;
        }

        if (!Settings.get()
                .notifications()
                .isNewPostsNotificationEnabled()) {
            return;
        }

        OwnerInfo.getRx(context, accountId, fromId)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(ownerInfo -> notifyImpl(context, ownerInfo), ignored -> {});
    }

    private void notifyImpl(Context context, OwnerInfo info){
        String ownerName = fromId > 0 ? (stringEmptyIfNull(firstName) + " " + stringEmptyIfNull(lastName)) : groupName;
        final NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()){
            nManager.createNotificationChannel(AppNotificationChannels.getNewPostChannel(context));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.NEW_POST_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notify_statusbar)
                .setLargeIcon(info.getAvatar())
                .setContentTitle(context.getString(R.string.new_post_title))
                .setContentText(context.getString(R.string.new_post_was_published_in, ownerName))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, PlaceFactory.getPostPreviewPlace(accountId, postId, fromId));

        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, fromId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();

        configOtherPushNotification(notification);

        nManager.notify(String.valueOf(fromId), NotificationHelper.NOTIFICATION_NEW_POSTS_ID, notification);
    }
}