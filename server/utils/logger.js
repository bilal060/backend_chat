const fs = require('fs');
const path = require('path');

/**
 * File logger utility for writing logs to .log files
 */
class FileLogger {
    constructor(logDir = path.join(__dirname, '../logs')) {
        this.logDir = logDir;
        this.ensureLogDirectory();
    }

    /**
     * Ensure log directory exists
     */
    ensureLogDirectory() {
        if (!fs.existsSync(this.logDir)) {
            fs.mkdirSync(this.logDir, { recursive: true });
        }
    }

    /**
     * Get log file path for today
     */
    getLogFilePath(filename) {
        const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD
        return path.join(this.logDir, `${filename}-${today}.log`);
    }

    /**
     * Write log entry to file
     */
    writeLog(filename, data) {
        try {
            const logFilePath = this.getLogFilePath(filename);
            const timestamp = new Date().toISOString();
            const logEntry = `[${timestamp}] ${JSON.stringify(data, null, 2)}\n\n`;
            
            fs.appendFileSync(logFilePath, logEntry, 'utf8');
        } catch (error) {
            console.error('Error writing to log file:', error);
        }
    }

    /**
     * Log chat payload
     */
    logChat(payload) {
        this.writeLog('chats', payload);
    }

    /**
     * Log any payload with custom filename
     */
    log(filename, payload) {
        this.writeLog(filename, payload);
    }
}

// Create singleton instance
const fileLogger = new FileLogger();

module.exports = fileLogger;
