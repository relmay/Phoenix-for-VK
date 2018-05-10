package biz.dealnote.messenger.settings;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class VkPushRegistration {

    @SerializedName("userId")
    private final int userId;

    @SerializedName("deviceId")
    private final String deviceId;

    @SerializedName("vkToken")
    private final String vkToken;

    @SerializedName("pushProvider")
    private final String pushProvider;

    @SerializedName("fcmToken")
    private final String fcmToken;

    public VkPushRegistration(int userId, @NonNull String deviceId, @NonNull String vkToken,
                              @NonNull String pushProvider, @NonNull String fcmToken) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.vkToken = vkToken;
        this.pushProvider = pushProvider;
        this.fcmToken = fcmToken;
    }

    public int getUserId() {
        return userId;
    }

    public String getVkToken() {
        return vkToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getPushProvider() {
        return pushProvider;
    }
}