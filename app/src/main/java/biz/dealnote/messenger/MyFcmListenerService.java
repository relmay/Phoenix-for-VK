package biz.dealnote.messenger;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.push.PushType;
import biz.dealnote.messenger.push.IPushRegistrationResolver;
import biz.dealnote.messenger.push.message.BirthdayFCMMessage;
import biz.dealnote.messenger.push.message.CommentFCMMessage;
import biz.dealnote.messenger.push.message.FriendAcceptedFCMMessage;
import biz.dealnote.messenger.push.message.FriendFCMMessage;
import biz.dealnote.messenger.push.message.FCMMessage;
import biz.dealnote.messenger.push.message.GroupInviteFCMMessage;
import biz.dealnote.messenger.push.message.LikeFCMMessage;
import biz.dealnote.messenger.push.message.NewPostPushMessage;
import biz.dealnote.messenger.push.message.ReplyFCMMessage;
import biz.dealnote.messenger.push.message.WallPostFCMMessage;
import biz.dealnote.messenger.push.message.WallPublishFCMMessage;
import biz.dealnote.messenger.realtime.Processors;
import biz.dealnote.messenger.realtime.QueueContainsException;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.PersistentLogger;

import static biz.dealnote.messenger.util.Utils.isEmpty;

public class MyFcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "MyFcmListenerService";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Context context = getApplicationContext();
        Logger.d(TAG, message.getData().size() > 0 ? "Data-notification" : "Notification-notification");

        String pushType = message.getData().get("type");

        Logger.d(TAG, "onMessage, from: " + message.getFrom() + ", pushType: " + pushType + ", data: " + message.getData());
        if (isEmpty(pushType)) {
            return;
        }

        StringBuilder bundleDump = new StringBuilder();
        for (Map.Entry<String, String> entry : message.getData().entrySet()) {
            try {
                String line = "key: " + entry.getKey() + ", value: " + entry.getValue() + ", class: " + (entry.getValue() == null ? "null" : entry.getValue().getClass());
                Logger.d(TAG, line);
                bundleDump.append("\n").append(line);
            } catch (Exception ignored) {
            }
        }

        int accountId = Settings.get()
                .accounts()
                .getCurrent();

        if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            return;
        }

        final IPushRegistrationResolver registrationResolver = Injection.providePushRegistrationResolver();

        if (!registrationResolver.canReceivePushNotification()) {
            Logger.d(TAG, "Invalid push registration on VK");
            return;
        }

        switch (pushType) {
            case PushType.MSG:
                //fireNewMessage(accountId, FCMMessage.fromRemoteMessage(message));
                FCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;
            case PushType.WALL_POST:
                WallPostFCMMessage.fromRemoteMessage(message).nofify(context, accountId);
                break;
            case PushType.REPLY:
                ReplyFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;
            case PushType.COMMENT:
                CommentFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;
            case PushType.WALL_PUBLISH:
                WallPublishFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;
            case PushType.FRIEND:
                FriendFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;
            case PushType.FRIEND_ACCEPTED:
                FriendAcceptedFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;
            case PushType.GROUP_INVITE:
                GroupInviteFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;
            case PushType.BIRTHDAY:
                BirthdayFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;

            case PushType.NEW_POST:
                new NewPostPushMessage(accountId, message).notifyIfNeed(context);
                break;
            case PushType.LIKE:
                new LikeFCMMessage(accountId, message).notifyIfNeed(context);
                break;
            default:
                PersistentLogger.logThrowable("Push issues", new Exception("Unespected Push event, collapse_key: " + pushType + ", dump: " + bundleDump));
                break;
        }
    }

//    private void fireNewMessage(int accountId, final @NonNull FCMMessage dto) {
//        try {
//            Processors.realtimeMessages().process(accountId, dto.message_id, true);
//        } catch (QueueContainsException ignored) {
//        }
//
//        if (dto.badge >= 0) {
//            Stores.getInstance()
//                    .dialogs()
//                    .setUnreadDialogsCount(accountId, dto.badge);
//        }
//    }
}