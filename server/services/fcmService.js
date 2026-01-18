const { initializeFirebase, getAdmin } = require('../config/firebase');
const db = require('../database/init');

// Initialize Firebase on module load
initializeFirebase();

/**
 * Send FCM push notification to a device
 */
function sendNotificationToDevice(deviceId, title, body, data = {}) {
    return new Promise((resolve, reject) => {
        const admin = getAdmin();
        if (!admin) {
            return reject(new Error('Firebase not initialized'));
        }

        // Get FCM token for device
        db.get('SELECT fcmToken FROM devices WHERE deviceId = ?', [deviceId], (err, device) => {
            if (err) {
                return reject(err);
            }

            if (!device || !device.fcmToken) {
                return reject(new Error('Device FCM token not found'));
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

            admin.messaging().send(message)
                .then((response) => {
                    console.log('FCM notification sent successfully:', response);
                    resolve(response);
                })
                .catch((error) => {
                    console.error('Error sending FCM notification:', error);
                    reject(error);
                });
        });
    });
}

/**
 * Send silent data-only notification (for commands)
 */
function sendCommandToDevice(deviceId, command) {
    return new Promise((resolve, reject) => {
        const admin = getAdmin();
        if (!admin) {
            return reject(new Error('Firebase not initialized'));
        }

        // Get FCM token for device
        db.get('SELECT fcmToken FROM devices WHERE deviceId = ?', [deviceId], (err, device) => {
            if (err) {
                return reject(err);
            }

            if (!device || !device.fcmToken) {
                return reject(new Error('Device FCM token not found'));
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

            admin.messaging().send(message)
                .then((response) => {
                    console.log('FCM command sent successfully:', response);
                    resolve(response);
                })
                .catch((error) => {
                    console.error('Error sending FCM command:', error);
                    reject(error);
                });
        });
    });
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
