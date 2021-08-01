package com.shkcodes.aurora.ui.create

enum class AttachmentType {
    IMAGE, VIDEO, GIF, OTHER;

    companion object {
        private const val ATTACHMENT_TYPE_IMAGE = "image"
        private const val ATTACHMENT_TYPE_VIDEO = "video"
        private const val ATTACHMENT_TYPE_GIF = "gif"

        fun from(type: String): AttachmentType {
            return when {
                type.contains(ATTACHMENT_TYPE_VIDEO) -> VIDEO
                type.contains(ATTACHMENT_TYPE_GIF) -> GIF
                type.contains(ATTACHMENT_TYPE_IMAGE) -> IMAGE
                else -> OTHER
            }
        }
    }
}
