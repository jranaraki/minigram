package com.minigram

import android.util.Log
import android.view.accessibility.AccessibilityEvent

class GrammarAccessibilityService : android.accessibilityservice.AccessibilityService() {

    companion object {
        private const val TAG = "MiniGram"
    }
    private var overlayView: OverlayView? = null
    private var currentText: String? = null
    var sourceNode: android.view.accessibility.AccessibilityNodeInfo? = null
    private var selectionStart = 0
    private var selectionEnd = 0
    private var fullTextContent: String? = null
    private var isSelectionMode = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "AccessibilityService connected")
        GrammarService.initialize(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            when (it.eventType) {
                AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                    handleTextSelection(it)
                }
            }
        }
    }

    private fun handleTextSelection(event: AccessibilityEvent) {
        val node = event.source
        
        recycleSourceNode()
        
        val nodeText = node?.text?.toString() ?: ""
        val selectedText = if (nodeText.isNotEmpty() && event.fromIndex < event.toIndex) {
            nodeText.substring(
                (event.fromIndex).coerceAtLeast(0).coerceAtMost(nodeText.length),
                (event.toIndex).coerceAtLeast(0).coerceAtMost(nodeText.length)
            )
        } else {
            ""
        }

        sourceNode = node
        selectionStart = (event.fromIndex).coerceAtLeast(0).coerceAtMost(nodeText.length)
        selectionEnd = (event.toIndex).coerceAtLeast(0).coerceAtMost(nodeText.length)
        fullTextContent = nodeText
        isSelectionMode = true
        
        Log.d(TAG, "Text selection changed: from=${event.fromIndex}, to=${event.toIndex}, selected='$selectedText'")
        Log.d(TAG, "Full text length: ${fullTextContent?.length}, selectionStart=$selectionStart, selectionEnd=$selectionEnd")

        if (selectedText.isNotEmpty() && event.fromIndex != event.toIndex) {
            currentText = selectedText
            Log.d(TAG, "Showing overlay for: $selectedText")
            showOverlay()
        } else {
            Log.d(TAG, "Text deselected, dismissing overlay")
            overlayView?.dismiss()
            currentText = null
            isSelectionMode = false
            recycleSourceNode()
        }
    }

    private fun showOverlay() {
        overlayView?.dismiss()
        currentText?.let { text ->
            Log.d(TAG, "Creating and showing overlay")
            overlayView = OverlayView(this)
            overlayView?.show(text) { correctedText ->
                Log.d(TAG, "Overlay callback received. Original: $text, Corrected: $correctedText")
                Log.d(TAG, "Selection context: start=$selectionStart, end=$selectionEnd, mode=isSelectionMode=$isSelectionMode")
                try {
                    overlayView?.dismiss()
                    TextReplacer.replaceText(
                        service = this,
                        originalText = text,
                        correctedText = correctedText,
                        selectionStart = selectionStart,
                        selectionEnd = selectionEnd,
                        fullText = fullTextContent,
                        isSelectionMode = isSelectionMode
                    )
                    recycleSourceNode()
                } catch (e: Exception) {
                    Log.e(TAG, "Error during text replacement", e)
                }
            }
        }
    }

    private fun recycleSourceNode() {
        sourceNode?.recycle()
        sourceNode = null
    }

    override fun onInterrupt() {
        Log.d(TAG, "AccessibilityService interrupted")
        overlayView?.dismiss()
        recycleSourceNode()
        currentText = null
        fullTextContent = null
    }

    override fun onDestroy() {
        Log.d(TAG, "AccessibilityService destroyed")
        overlayView?.dismiss()
        recycleSourceNode()
        currentText = null
        fullTextContent = null
        GrammarService.shutdown()
        super.onDestroy()
    }
}