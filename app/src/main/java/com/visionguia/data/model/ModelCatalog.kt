package com.visionguia.data.model

object ModelCatalog {
    val localLlm = ModelSpec(
        id = "qwen3-0.6b-int4",
        fileName = "qwen3-0.6b-int4.litertlm",
        url = "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/154c5de0fc0fc5002a7da570ba8c67edffa32ca2/Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm?download=true",
        sha256 = "e3e290109da4388d65a17510a0c66af91c8039f52d2c465868dbc43c09a776cf",
        minimumFreeBytes = 700_000_000,
    )
}
