# [cite_start]🛡️ Guía de Documentación: Lock-Chat [cite: 1]

> [cite_start]**Lock-Chat** es un sistema de mensajería móvil P2P (Peer-to-Peer) completamente descentralizado y enfocado en la privacidad extrema. [cite: 2] 
> [cite_start]Diseñado para operar de forma offline en entornos hostiles, de rescate o tácticos, utiliza una arquitectura de transporte híbrido combinando Bluetooth Low Energy (BLE) y radiofrecuencia de largo alcance LoRa mediante módulos de hardware ESP32 conectados por USB OTG. [cite: 3]

---

## [cite_start]📁 Estructura del Proyecto y Entregables [cite: 4]

[cite_start]Este directorio contiene la documentación técnica completa del proyecto dividida en archivos especializados según su contenido y formato ideal: [cite: 5]

| Archivo | Descripción |
| :--- | :--- |
| **`README.md`** | (Este archivo)[cite_start]: Resumen ejecutivo e índice general de navegación de la documentación. [cite: 6] |
| **`Arquitectura_y_Diseno.md`** | [cite_start]Descripción profunda de la arquitectura de la aplicación Android, patrones de diseño (MVVM, Clean Architecture, inyección de dependencias, base de datos local persistente y servicios en segundo plano) y configuración de compilación. [cite: 7] |
| **`Conceptos_Tecnicos.docx`** | [cite_start]Documento conceptual que detalla de forma educativa e ingenieril el funcionamiento de los Sockets de red, el Internet de las Cosas (IoT), la comunicación por puerto serie USB OTG, la física y protocolos detrás de la radiofrecuencia LoRa y los sistemas de topología en malla (mesh). [cite: 8] |

---

## [cite_start]⚙️ Casos de Uso (Diagramas PlantUML) [cite: 9]

[cite_start]Cuatro archivos independientes con código PlantUML que detallan las interacciones exactas entre componentes para los escenarios principales: [cite: 9]

| Archivo `.puml` | Escenario |
| :--- | :--- |
| `caso_uso_identidad.puml` | [cite_start]Creación descentralizada de identidad única local. [cite: 10] |
| `caso_uso_descubrimiento_qr.puml` | [cite_start]Descubrimiento offline de peers e intercambio de claves mediante código QR. [cite: 11] |
| `caso_uso_mensajeria_dual.puml` | [cite_start]Enrutamiento adaptativo de mensajes utilizando LoRa USB o BLE. [cite: 12] |
| `caso_uso_ping_pong_rtt.puml` | [cite_start]Protocolo de verificación de latencia en tiempo real (RTT) y estado de los contactos. [cite: 13] |

---

## [cite_start]💻 Módulos de Código Clave (Resaltado Sintáctico) [cite: 14]

[cite_start]Archivos HTML diseñados con estilos CSS inline que representan fragmentos clave del código fuente. [cite: 14] [cite_start]Están optimizados para que al abrirlos en tu navegador puedas seleccionarlos, copiarlos (**Ctrl+C**) y pegarlos directamente en un documento `.docx` de Microsoft Word o Google Docs manteniendo la estética oscura premium, colores de sintaxis, fuentes monospace y bordes de editor moderno: [cite: 15]

| Archivo `.html` | Función Clave |
| :--- | :--- |
| `codigo_ble_transport.html` | [cite_start]Coordinación del GATT Server y GATT Client bidireccional. [cite: 16] |
| `codigo_transport_manager.html` | [cite_start]Orquestador reactivo de rutas y flujo de Ping/Pong de RTT. [cite: 17] |
| `codigo_lora_usb.html` | [cite_start]Manejador del driver USB serial para la detección automática del ESP32. [cite: 18] |
| `codigo_foreground_service.html` | [cite_start]Foreground Service que mantiene el transporte activo en background. [cite: 19] |
| `codigo_identity_datastore.html` | [cite_start]Generación de identificador criptográfico local descentralizado. [cite: 20] |

---

## [cite_start]🎯 Objetivo de la Documentación [cite: 21]

[cite_start]El propósito de estos archivos es proveer un mapa mental claro y riguroso a ingenieros de desarrollo, diseñadores de hardware IoT y analistas de ciberseguridad sobre cómo opera una red inalámbrica descentralizada sin dependencia de infraestructuras celulares o de internet tradicional, mostrando la viabilidad de usar terminales móviles estándar como nodos activos de malla. [cite: 22]

---
[cite_start]*Documentación técnica de Lock-Chat © 2026. Todos los derechos reservados.* [cite: 23]
