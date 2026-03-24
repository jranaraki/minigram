package com.minigram

import android.content.Context
import android.util.Log
import org.pytorch.executorch.extension.llm.LlmModule
import org.pytorch.executorch.extension.llm.LlmCallback
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object GrammarService {

    private const val TAG = "MiniGram"
    private const val MAX_TOKENS = 128
    private const val TEMPERATURE = 0.0f
    private const val TIMEOUT_MS = 30000L

    private const val TEXT_MODEL = 1

    private var module: LlmModule? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return

        try {
            val modelFile = copyModelFromAssets(context, "qwen3_0.6B_model.pte", "model.pte")
            val tokenizerFile = copyModelFromAssets(context, "tokenizer.json", "tokenizer.json")

            module = LlmModule(
                TEXT_MODEL,
                modelFile.absolutePath,
                tokenizerFile.absolutePath,
                TEMPERATURE
            )

            module?.load()
            isInitialized = true
            Log.d(TAG, "Model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model", e)
            e.printStackTrace()
        }
    }

    private fun copyModelFromAssets(context: Context, assetName: String, outputName: String): File {
        val assetPath = "models/$assetName"
        val file = File(context.cacheDir, outputName)

        if (file.exists()) {
            return file
        }

        context.assets.open(assetPath).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return file
    }

    fun correctGrammar(text: String): String {
        Log.d(TAG, "Correcting grammar for: $text")

        if (!isInitialized || module == null) {
            Log.e(TAG, "Model not initialized!")
            return text
        }

        if (text.isBlank()) {
            return text
        }

        val cleanInput = text.removeSurrounding("[", "]")
        val prompt = "<|im_start|>system\nYou are a grammar correction assistant.\n- Correct grammatical errors.\n- Preserve meaning.\n- No explanations.\n- Return only the corrected sentence.\n- If the sentence is already correct, return it exactly as given. Do not say anything else.\n<|im_end|>\n<|im_start|>user\n$cleanInput<|im_end|>\n<|im_start|>assistant"

        return try {
            val latch = CountDownLatch(1)
            val resultBuilder = StringBuilder()
            var stopGeneration = false

            val callback = object : LlmCallback {
                override fun onResult(result: String) {
                    if (stopGeneration) {
                        return
                    }

                    resultBuilder.append(result)
                    Log.d(TAG, "Token: '$result'")

                    val current = resultBuilder.toString()
                    if (current.contains(".") && current.length < 128) {
                        //stopGeneration = true
                        //module?.stop()
                        //latch.countDown()
                        Log.d(TAG, "A period is detected!")
                    }

                    if (result == "<|im_end|>" || result == "</thinking>") {
                        stopGeneration = true
                        module?.stop()
                        latch.countDown()
                    }
                }

                override fun onStats(stats: String) {}
            }

            Log.d(TAG, "Starting generation...")
            module?.generate(prompt, MAX_TOKENS, callback, false)

            val completed = latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            if (!completed) {
                Log.w(TAG, "Generation timeout")
                module?.stop()
            }

            val result = resultBuilder.toString().replace("<|im_end|>", "").trim()
            Log.d(TAG, "Corrected: $result")
            val finalResult = if (result.contains("the sentence is grammatically correct", ignoreCase = true)) {
                text
            } else {
                result
            }
            finalResult.ifEmpty { text }
        } catch (e: Exception) {
            Log.e(TAG, "Error during generation", e)
            e.printStackTrace()
            text
        }
    }

    fun shutdown() {
        module?.stop()
        module = null
        isInitialized = false
    }
}
