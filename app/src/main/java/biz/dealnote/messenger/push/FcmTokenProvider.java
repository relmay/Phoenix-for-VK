package biz.dealnote.messenger.push;

import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public class FcmTokenProvider implements IFcmTokenProvider {

    public FcmTokenProvider() {
    }

    @Override
    public String getToken() {
        return FirebaseInstanceId.getInstance().getToken();
    }
}