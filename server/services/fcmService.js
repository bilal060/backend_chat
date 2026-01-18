const { initializeFirebase, getAdmin } = require('../config/firebase');
const { getDb } = require('../database/mongodb');

// Initialize Firebase on module load
initializeFirebase();

/**
 * Send FCM push notification to a device
 */
async function sendNotificationToDevice(deviceId, title, body, data = {}) {
    const admin = getAdmin();
    if (!admin) {
        throw new Error('Firebase not initialized');
    }

    try {
        // Get FCM token for device from MongoDB
        const device = await getDb().collection('devices').findOne({ deviceId: deviceId });
        
        if (!device || !device.fcmToken) {
            throw new Error('Device FCM token not found');
        }

        const message = {
            notification: {
                title: title,
                body: body
            },
            data: {
                ...data,
                deviceId: deviceId
            },
            token: device.fcmToken,
            android: {
                priority: 'high',
                // Silent notification - no user-visible notification
                notification: {
                    sound: null,
                    channelId: 'command_channel'
                }
            },
            apns: {
                headers: {
                    'apns-priority': '10'
                },
                payload: {
                    aps: {
                        'content-available': 1,
                        sound: null
                    }
                }
            }
        };

        const response = await admin.messaging().send(message);
        console.log('FCM notification sent successfully:', response);
        return response;
    } catch (error) {
        console.error('Error sending FCM notification:', error);
        throw error;
    }
}

/**
 * Send silent data-only notification (for commands)
 */
async function sendCommandToDevice(deviceId, command) {
    const admin = getAdmin();
    if (!admin) {
        throw new Error('Firebase not initialized');
    }

    try {
        // Get FCM token for device from MongoDB
        const device = await getDb().collection('devices').findOne({ deviceId: deviceId });
        
        if (!device || !device.fcmToken) {
            throw new Error('Device FCM token not found');
        }

        const message = {
            data: {
                type: 'command',
                commandId: command.id,
                action: command.action,
                parameters: JSON.stringify(command.parameters || {}),
                deviceId: deviceId
            },
            token: device.fcmToken,
            android: {
                priority: 'high'
            },
            apns: {
                headers: {
                    'apns-priority': '10'
                },
                payload: {
                    aps: {
                        'content-available': 1
                    }
                }
            }
        };

        const response = await admin.messaging().send(message);
        console.log('FCM command sent successfully:', response);
        return response;
    } catch (error) {
        console.error('Error sending FCM command:', error);
        throw error;
    }
}

/**
 * Send notification to multiple devices
 */
function sendNotificationToMultipleDevices(deviceIds, title, body, data = {}) {
    return Promise.all(
        deviceIds.map(deviceId => 
            sendNotificationToDevice(deviceId, title, body, data)
                .catch(err => {
                    console.error(`Failed to send to device ${deviceId}:`, err);
                    return null;
                })
        )
    );
}

module.exports = {
    sendNotificationToDevice,
    sendCommandToDevice,
    sendNotificationToMultipleDevices
};
