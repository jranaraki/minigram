package com.minigram

import android.view.accessibility.AccessibilityNodeInfo

object TextReplacer {

    fun replaceText(
        service: GrammarAccessibilityService,
        originalText: String,
        correctedText: String,
        selectionStart: Int = 0,
        selectionEnd: Int = 0,
        fullText: String? = null,
        isSelectionMode: Boolean = false
    ) {
        try {
            val textNode = service.sourceNode ?: run {
                android.util.Log.e("MiniGram", "No source node available")
                
                val nodeInfo = service.rootInActiveWindow ?: run {
                    android.util.Log.e("MiniGram", "No root node found")
                    return
                }

                val foundNode = findTextNode(nodeInfo)
                if (foundNode != null) {
                    attemptSetText(foundNode, correctedText, selectionStart, selectionEnd, fullText, isSelectionMode)
                }
                return
            }

            attemptSetText(textNode, correctedText, selectionStart, selectionEnd, fullText, isSelectionMode)
        } catch (e: Exception) {
            android.util.Log.e("MiniGram", "Error replacing text", e)
        }
    }

    private fun attemptSetText(
        node: android.view.accessibility.AccessibilityNodeInfo, 
        correctedText: String,
        selectionStart: Int = 0,
        selectionEnd: Int = 0,
        fullText: String? = null,
        isSelectionMode: Boolean = false
    ) {
        android.util.Log.d("MiniGram", "Node text: ${node.text}, editable: ${node.isEditable}")
        android.util.Log.d("MiniGram", "Selection mode: $isSelectionMode, start=$selectionStart, end=$selectionEnd")
        
        val finalText = if (isSelectionMode && fullText != null) {
            val textBefore = fullText.substring(0, selectionStart.coerceAtLeast(0))
            val textAfter = fullText.substring(selectionEnd.coerceAtMost(fullText.length))
            val reconstructed = textBefore + correctedText + textAfter
            android.util.Log.d("MiniGram", "Reconstructed text: '$textBefore' + '$correctedText' + '$textAfter'")
            android.util.Log.d("MiniGram", "Full text: $fullText")
            android.util.Log.d("MiniGram", "Result: $reconstructed")
            reconstructed
        } else {
            android.util.Log.d("MiniGram", "Full text mode, replacing entire text")
            correctedText
        }

        val arguments = android.os.Bundle()
        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            finalText
        )

        val result = node.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            arguments
        )

        android.util.Log.d("MiniGram", "ACTION_SET_TEXT result: $result")

        if (!result) {
            android.util.Log.d("MiniGram", "Trying ACTION_FOCUS first...")
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val retryResult = node.performAction(
                    AccessibilityNodeInfo.ACTION_SET_TEXT,
                    arguments
                )
                android.util.Log.d("MiniGram", "ACTION_SET_TEXT retry result: $retryResult")
            }, 100)
        }
    }

    private fun findTextNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.text?.toString()?.isNotEmpty() == true) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findTextNode(child)
            if (result != null) return result
        }

        return null
    }
}