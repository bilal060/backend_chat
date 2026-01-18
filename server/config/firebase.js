// Firebase Admin SDK configuration
// This file will be configured with Firebase credentials
// For now, it's a placeholder that requires FIREBASE_CREDENTIALS environment variable

const admin = require('firebase-admin');

let initialized = false;

function initializeFirebase() {
    if (initialized) {
        return admin;
    }

    try {
        // Check if Firebase credentials are provided via environment variable
        if (process.env.FIREBASE_CREDENTIALS) {
            const serviceAccount = JSON.parse(process.env.FIREBASE_CREDENTIALS);
            admin.initializeApp({
                credential: admin.credential.cert(serviceAccount)
            });
        } else if (process.env.FIREBASE_CREDENTIALS_PATH) {
            // Or from a file path
            const serviceAccount = require(process.env.FIREBASE_CREDENTIALS_PATH);
            admin.initializeApp({
                credential: admin.credential.cert(serviceAccount)
            });
        } else {
            console.warn('Firebase credentials not provided. FCM will not work.');
            console.warn('Set FIREBASE_CREDENTIALS environment variable or FIREBASE_CREDENTIALS_PATH');
            return null;
        }

        initialized = true;
        console.log('Firebase Admin SDK initialized');
        return admin;
    } catch (error) {
        console.error('Error initializing Firebase:', error);
        return null;
    }
}

module.exports = {
    initializeFirebase,
    getAdmin: () => initialized ? admin : null
};
