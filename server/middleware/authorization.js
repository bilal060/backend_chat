/**
 * Role-based Authorization Middleware
 */

/**
 * Require admin role
 */
const requireAdmin = (req, res, next) => {
    const user = req.user;

    if (!user) {
        return res.status(401).json({
            success: false,
            message: 'Authentication required'
        });
    }

    if (user.role !== 'admin') {
        return res.status(403).json({
            success: false,
            message: 'Access denied: Admin only'
        });
    }

    next();
};

/**
 * Require device owner role
 */
const requireDeviceOwner = (req, res, next) => {
    const user = req.user;

    if (!user) {
        return res.status(401).json({
            success: false,
            message: 'Authentication required'
        });
    }

    if (user.role !== 'device_owner') {
        return res.status(403).json({
            success: false,
            message: 'Access denied: Device owner only'
        });
    }

    next();
};

/**
 * Require admin or device owner
 */
const requireAdminOrDeviceOwner = (req, res, next) => {
    const user = req.user;

    if (!user) {
        return res.status(401).json({
            success: false,
            message: 'Authentication required'
        });
    }

    if (user.role !== 'admin' && user.role !== 'device_owner') {
        return res.status(403).json({
            success: false,
            message: 'Access denied'
        });
    }

    next();
};

module.exports = {
    requireAdmin,
    requireDeviceOwner,
    requireAdminOrDeviceOwner
};
