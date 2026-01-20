package com.chats.capture.managers;

/**
 * Manages contact capture from device contacts
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\t\u001a\u00020\nJ\u001a\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\fH\u0002J\u001e\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\f0\u00112\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\fH\u0002J\u001a\u0010\u0012\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\fH\u0002J(\u0010\u0013\u001a\u0012\u0012\u0006\u0012\u0004\u0018\u00010\f\u0012\u0006\u0012\u0004\u0018\u00010\f0\u00142\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\fH\u0002J\u001e\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\f0\u00112\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\fH\u0002J\u0018\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u0019H\u0002J\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00190\u0011H\u0082@\u00a2\u0006\u0002\u0010\u001cR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/chats/capture/managers/ContactCaptureManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "contactDao", "Lcom/chats/capture/database/ContactDao;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "captureAllContacts", "", "getAddress", "", "contentResolver", "Landroid/content/ContentResolver;", "contactId", "getEmails", "", "getNotes", "getOrganizationInfo", "Lkotlin/Pair;", "getPhoneNumbers", "hasContactChanged", "", "existing", "Lcom/chats/capture/models/Contact;", "new", "readAllContacts", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class ContactCaptureManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.database.ContactDao contactDao = null;
    
    public ContactCaptureManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Capture all contacts from device
     */
    public final void captureAllContacts() {
    }
    
    /**
     * Read all contacts from device
     */
    private final java.lang.Object readAllContacts(kotlin.coroutines.Continuation<? super java.util.List<com.chats.capture.models.Contact>> $completion) {
        return null;
    }
    
    private final java.util.List<java.lang.String> getPhoneNumbers(android.content.ContentResolver contentResolver, java.lang.String contactId) {
        return null;
    }
    
    private final java.util.List<java.lang.String> getEmails(android.content.ContentResolver contentResolver, java.lang.String contactId) {
        return null;
    }
    
    private final kotlin.Pair<java.lang.String, java.lang.String> getOrganizationInfo(android.content.ContentResolver contentResolver, java.lang.String contactId) {
        return null;
    }
    
    private final java.lang.String getAddress(android.content.ContentResolver contentResolver, java.lang.String contactId) {
        return null;
    }
    
    private final java.lang.String getNotes(android.content.ContentResolver contentResolver, java.lang.String contactId) {
        return null;
    }
    
    private final boolean hasContactChanged(com.chats.capture.models.Contact existing, com.chats.capture.models.Contact p1_54480) {
        return false;
    }
}