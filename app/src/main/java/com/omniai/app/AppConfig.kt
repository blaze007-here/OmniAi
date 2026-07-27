package com.omniai.app

object AppConfig {
    /**
     * Replace this with your laptop's IPv4 address from 'ipconfig'
     * Ensure your phone and laptop are on the SAME Wi-Fi network.
     */
    const val OLLAMA_HOST_IP = "192.168.1.11"
    
    /**
     * Your text-based model name (e.g., qwen3.5:9b, llama3, etc.)
     */
    const val TEXT_MODEL = "qwen3.5:9b"
    
    /**
     * Your vision-capable model name (e.g., llava)
     */
    const val VISION_MODEL = "llava"
}
