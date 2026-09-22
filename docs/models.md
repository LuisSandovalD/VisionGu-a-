# Modelos locales

| Modelo | Uso/formato | Distribución | Tamaño exacto | SHA-256 | Licencia/fuente |
|---|---|---|---:|---|---|
| `vosk-model-small-es-0.42` | STT español, ZIP Vosk | Incluido en APK; extracción automática | 39,817,833 B | `09b239888f633ef2f0b4e09736e3d9936acfd810bc65d53fad45261762c6511f` | Apache-2.0, Alpha Cephei |
| `yolov8n.pt` | peso de origen | Solo trazabilidad de exportación | 6,549,796 B | `f59b3d833e2ff32e194b5bb8e08d211dc7c5bdf144b90d2c8412c47ccfc83b36` | AGPL-3.0, Ultralytics |
| `yolov8n.onnx` | detección COCO, ONNX FP32, 320×320 NCHW | Incluido en APK; instalación automática | 12,725,023 B | `8952e7feac460ea79b558c968f1bb1cce9cc8182adec963010e6a72a9e3d947d` | Derivado de YOLOv8n, AGPL-3.0 |
| `Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm` | conversación LiteRT-LM | Descarga automática al primer inicio | 344,437,808 B | `e3e290109da4388d65a17510a0c66af91c8039f52d2c465868dbc43c09a776cf` | Apache-2.0, LiteRT Community/Qwen |

El artefacto Qwen está fijado al commit `154c5de0fc0fc5002a7da570ba8c67edffa32ca2`; no se descarga una rama mutable. `ModelManager` exige HTTPS, espacio libre, soporta `Range`, descarga a `.part`, calcula SHA-256 y solo entonces hace el cambio atómico al nombre final.

Vosk y YOLO se comprueban también antes de extraerse/copiarse desde assets. Una prueba instrumentada inicializa Vosk y ejecuta una inferencia YOLO real en un bitmap.

No hay un modelo de moneda peruana incluido: sin un dataset, clases y métricas adecuados, la aplicación debe declarar incertidumbre. Esta restricción evita dar una denominación inventada.
