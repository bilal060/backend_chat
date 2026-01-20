package com.chats.controller.models

data class UICommand(
    val action: String,
    val parameters: Map<String, Any>
) {
    companion object {
        fun click(x: Float, y: Float, packageName: String? = null): UICommand {
            val params = mutableMapOf<String, Any>(
                "x" to x,
                "y" to y
            )
            packageName?.let { params["package"] = it }
            return UICommand(
                action = "ui_click",
                parameters = params
            )
        }
        
        fun findAndClick(text: String, packageName: String? = null): UICommand {
            val params = mutableMapOf<String, Any>(
                "text" to text
            )
            packageName?.let { params["package"] = it }
            return UICommand(
                action = "ui_find_and_click",
                parameters = params
            )
        }
        
        fun findAndClickById(viewId: String, packageName: String? = null): UICommand {
            val params = mutableMapOf<String, Any>(
                "view_id" to viewId
            )
            packageName?.let { params["package"] = it }
            return UICommand(
                action = "ui_find_and_click_by_id",
                parameters = params
            )
        }
        
        fun input(text: String, findText: String? = null, viewId: String? = null, packageName: String? = null): UICommand {
            val params = mutableMapOf<String, Any>(
                "text" to text
            )
            findText?.let { params["find_text"] = it }
            viewId?.let { params["view_id"] = it }
            packageName?.let { params["package"] = it }
            return UICommand(
                action = "ui_input",
                parameters = params
            )
        }
        
        fun scroll(direction: String, packageName: String? = null): UICommand {
            val params = mutableMapOf<String, Any>(
                "direction" to direction
            )
            packageName?.let { params["package"] = it }
            return UICommand(
                action = "ui_scroll",
                parameters = params
            )
        }
        
        fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 300): UICommand {
            return UICommand(
                action = "ui_swipe",
                parameters = mapOf(
                    "start_x" to startX,
                    "start_y" to startY,
                    "end_x" to endX,
                    "end_y" to endY,
                    "duration" to duration
                )
            )
        }
        
        fun launchApp(packageName: String): UICommand {
            return UICommand(
                action = "ui_launch_app",
                parameters = mapOf("package" to packageName)
            )
        }
    }
}
