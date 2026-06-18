<div align="center">

# 🛡️ LOCK-CHAT

### Red privada de mensajería P2P · Sin Internet · Sin servidores

[![Version](https://img.shields.io/badge/versión-1.2.2-00FF41?style=for-the-badge&logo=semver&logoColor=white&labelColor=0D0D0D)](https://github.com/tempMufld28/Lock-Chat/releases/tag/v1.2.2)
[![Android](https://img.shields.io/badge/Android-8.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=0D0D0D)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white&labelColor=0D0D0D)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.05-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white&labelColor=0D0D0D)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/licencia-MIT-00FF41?style=for-the-badge&labelColor=0D0D0D)](LICENSE)

[💬 Descargar APK](https://github.com/tempMufld28/Lock-Chat/releases/latest) ·
[🌐 Página oficial](https://tempmufld28.github.io/Lock-Chat/) ·
[📦 Releases](https://github.com/tempMufld28/Lock-Chat/releases)

</div>

---

> **Lock-Chat** es un sistema de mensajería móvil **P2P (Peer-to-Peer)** completamente descentralizado y enfocado en la privacidad extrema. Diseñado para operar **offline** en entornos hostiles, de rescate o tácticos, combina un transporte híbrido de **Bluetooth Low Energy (BLE)** y radiofrecuencia de largo alcance **LoRa** mediante módulos de hardware ESP32 conectados por **USB OTG**.

Sin torres. Sin SIM. Sin nube. Tu dispositivo se convierte en un **nodo activo** de una malla privada que encripta y transmite los mensajes directamente a otros usuarios.

---

## 📑 Tabla de contenidos

- [Características](#-características)
- [Arquitectura](#️-arquitectura)
- [Stack tecnológico](#-stack-tecnológico)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Pantallas y flujo](#-pantallas-y-flujo)
- [Compilación y ejecución](#-compilación-y-ejecución)
- [Descarga e instalación](#️-descarga-e-instalación)
- [Permisos](#-permisos)
- [Roadmap](#️-roadmap)
- [Contribuir](#-contribuir)
- [Licencia](#-licencia)

---

##  Características

| | Funcionalidad |
|:---:|---|
| 📡 | **Transporte híbrido BLE + LoRa** — enrutamiento adaptativo entre Bluetooth directo y radio de largo alcance vía ESP32 USB. |
| 🔑 | **Identidad descentralizada** — identificador criptográfico generado localmente, sin registro ni servidor central. |
| 🤝 | **Descubrimiento por QR** — intercambio offline de claves públicas escaneando un código QR con CameraX + ML Kit. |
| 💬 | **Mensajería bidireccional** — cola de escritura GATT con ACK, binding robusto `nodeId`↔peer vía `CHAR_INFO`. |
| 📥 | **Buzón de Solicitudes** — inbox para remitentes desconocidos con aceptar/rechazar antes de añadir contacto. |
| 🔔 | **Notificaciones locales** — avisos de mensajes entrantes con supresión cuando el chat está activo. |
| 🩺 | **Diagnóstico BLE** — pantalla con estado de adaptador, scanning, GATT server/client y conexiones activas. |
| ⚡ | **Ping/Pong RTT** — medición de latencia en tiempo real y estado de los contactos. |
| 🌙 | **Foreground Service** — transporte activo en segundo plano con notificación persistente. |
| 🔒 | **Cero telemetría** — sin analytics, sin backup, sin recolección de datos. |

---

##  Arquitectura

Lock-Chat sigue **Clean Architecture + MVVM** con inyección de dependencias mediante **Hilt**:

```
┌─────────────────────────────────────────────────────────┐
│                    UI (Jetpack Compose)                  │
│   Onboarding · Chats · ChatDetail · AddContact · Ping    │
│   Solicitudes · Diagnóstico · Profile                    │
└───────────────┬─────────────────────────┬───────────────┘
                │                         │
        ┌───────▼────────┐       ┌────────▼─────────┐
        │  ViewModels    │       │   Navigation     │
        │  (StateFlow)   │       │   (NavGraph)     │
        └───────┬────────┘       └──────────────────┘
                │
┌───────────────▼─────────────────────────────────────────┐
│              Domain (Models + Repositories)              │
│   Contact · Identity · Message · Transport               │
└───────────────┬─────────────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────────────┐
│                      Data Layer                          │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │   Room DB   │  │  DataStore   │  │   Transport    │  │
│  │ Contactos · │  │  Identity ·  │  │  BLE · LoRa    │  │
│  │ Mensajes ·  │  │  Theme       │  │  Manager       │  │
│  │ Solicitudes │  │              │  │                │  │
│  └─────────────┘  └──────────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

**Transporte:** `TransportManager` orquesta dos implementaciones de la interfaz `Transport`:
- **`BleTransport`** — GATT Server + Client bidireccional con `ConcurrentHashMap` de conexiones, `Mutex` por operación GATT y cola de escrituras con ACK.
- **`LoRaUsbTransport`** — driver USB serial (`usb-serial-for-android`) para detectar automáticamente módulos ESP32 (CP2102/CH340/FTDI).

---

##  Stack tecnológico

| Capa | Tecnología |
|:---|:---|
| **Lenguaje** | Kotlin 1.9.24 |
| **UI** | Jetpack Compose (BOM 2024.05) + Material 3 |
| **Arquitectura** | Clean Architecture + MVVM |
| **DI** | Hilt 2.51.1 |
| **Base de datos** | Room 2.6.1 (persistencia local) |
| **Preferencias** | DataStore 1.1.1 |
| **Background** | WorkManager 2.9.0 + Foreground Service |
| **BLE** | Android Bluetooth LE (GATT Server/Client) |
| **LoRa/USB** | usb-serial-for-android 3.7.2 |
| **QR** | ZXing 3.5.3 + CameraX 1.3.4 + ML Kit Barcode 17.3.0 |
| **Permisos** | Accompanist Permissions 0.34.0 |
| **Build** | AGP 8.13.2 · Gradle · KSP |
| **CI/CD** | GitHub Actions (build APK + deploy Pages) |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 35 (Android 15) |

---

##  Estructura del proyecto

```
Lock-Chat/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/                          # Recursos, temas, iconos, device_filter USB
│       └── java/com/lockchat/app/
│           ├── LockChatApp.kt            # Application (Hilt)
│           ├── MainActivity.kt
│           ├── data/
│           │   ├── local/                # Room DB, DAOs, Entities, DataStore
│           │   │   ├── AppDatabase.kt
│           │   │   ├── IdentityDataStore.kt
│           │   │   ├── ThemePreferences.kt
│           │   │   ├── dao/              # ContactoDao · MensajeDao · SolicitudDao
│           │   │   └── entity/           # Contacto · Mensaje · Solicitud
│           │   ├── notification/         # MessageNotifier (notificaciones locales)
│           │   ├── repository/           # Implementaciones de repositorios
│           │   └── transport/
│           │       ├── TransportManager.kt
│           │       ├── ble/BleTransport.kt       # GATT bidireccional + cola con ACK
│           │       └── lora/LoRaUsbTransport.kt  # Driver USB serial ESP32
│           ├── di/                       # AppModule · RepositoryModule (Hilt)
│           ├── domain/
│           │   ├── model/                # Contact · Identity · Message · Transport
│           │   └── repository/           # Interfaces de repositorios
│           ├── service/
│           │   └── MeshForegroundService.kt      # Transporte en background
│           ├── transport/
│           │   └── UsbEventReceiver.kt           # Detección attach/detach USB
│           └── ui/
│               ├── components/           # SignalWavesIcon y componentes reutilizables
│               ├── navigation/NavGraph.kt
│               ├── theme/                # Color · Theme · Type
│               └── screens/
│                   ├── onboarding/       # Creación de identidad
│                   ├── chats/            # Lista de conversaciones
│                   ├── chatdetail/       # Conversación individual
│                   ├── addcontact/       # Escáner QR
│                   ├── solicitudes/      # Buzón de remitentes desconocidos
│                   ├── diagnostico/      # Estado BLE en tiempo real
│                   ├── ping/             # Medición RTT
│                   └── profile/          # Perfil e identidad
├── Page/                                # Página de descarga (GitHub Pages)
│   ├── index.html
│   ├── style.css
│   └── script.js
├── .github/workflows/
│   ├── android.yml                      # Build + release APK (tag → release)
│   └── pages.yml                        # Deploy de Page/ a GitHub Pages
├── gradle/libs.versions.toml            # Catálogo de versiones
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

##  Pantallas y flujo

1. **Onboarding** — primera ejecución: genera tu identidad criptográfica local.
2. **Chats** — lista de conversaciones + estado online de contactos.
3. **ChatDetail** — conversación con envío/recepción en tiempo real.
4. **AddContact** — escanea el QR del peer para intercambiar claves.
5. **Solicitudes** — acepta o rechaza mensajes de remitentes desconocidos.
6. **Diagnóstico** — inspecciona el estado del BLE (advertiser, scanner, GATT).
7. **Ping** — mide la latencia RTT con un contacto.
8. **Profile** — tu identificador, handle y código QR.

---

## 🔧 Compilación y ejecución

**Requisitos:** Android Studio (Hedgehog o superior), JDK 17, Android SDK 35.

```bash
# Clonar el repositorio
git clone https://github.com/tempMufld28/Lock-Chat.git
cd Lock-Chat

# Build debug APK
./gradlew assembleDebug

# El APK se genera en:
# app/build/outputs/apk/debug/app-debug.apk
```

Para compilar el release firmado, configura tu keystore en `app/build.gradle.kts` o usa el workflow de GitHub Actions con un tag `v*`.

---

##  Descarga e instalación

La forma más sencilla es desde la página oficial:

> 🌐 **https://tempmufld28.github.io/Lock-Chat/**

O descarga directamente el APK desde el último release:

> 📦 **https://github.com/tempMufld28/Lock-Chat/releases/latest**

Pasos de instalación:

1. Pulsa **DESCARGAR APK** en la página.
2. Abre el archivo `Lock-Chat.apk` desde notificaciones o la carpeta Descargas.
3. Permite **"Instalar desde origen desconocido"** para tu navegador.
4. Pulsa **Instalar** y espera a que finalice.
5. Abre la app y concede los permisos de **Bluetooth**, **Ubicación** y **Notificaciones**.

---

##  Permisos

| Permiso | Uso |
|:---|:---|
| `BLUETOOTH_SCAN/CONNECT/ADVERTISE` | Descubrimiento, conexión y advertising BLE (Android 12+). |
| `BLUETOOTH` / `BLUETOOTH_ADMIN` | BLE en Android < 12 (fallback). |
| `ACCESS_FINE_LOCATION` | Requerido por BLE en Android < 12. |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_CONNECTED_DEVICE` | Mantener el transporte activo en background. |
| `POST_NOTIFICATIONS` | Notificaciones de mensajes entrantes (Android 13+). |
| `CAMERA` | Escáner de códigos QR. |
| `usb.host` (feature) | Comunicación con módulos ESP32 LoRa por USB OTG. |
| `INTERNET` | Reservado (sin tráfico de red; solo permiso del framework). |

---

##  Roadmap

- [x] Transporte BLE bidireccional con ACK
- [x] Identidad descentralizada + descubrimiento QR
- [x] Buzón de Solicitudes y notificaciones locales
- [x] Diagnóstico BLE en tiempo real
- [x] Driver USB serial para LoRa (ESP32)
- [x] Página de descarga + CI/CD con releases automáticos
- [x] Cifrado E2E con X3DH / Double Ratchet
- [x] SQLCipher para base de datos cifrada
- [x] Bloqueo biométrico de la app
- [x] Topología mesh multi-salto (mesh routing)

---

## 🤝 Contribuir

Las contribuciones son bienvenidas. Abre un issue para discutir cambios importantes antes de hacer un PR.

```bash
# Crea una rama desde main
git checkout -b feature/mi-mejora
# Haz commit de tus cambios
git commit -m "feat: descripción del cambio"
# Sube y abre un Pull Request
git push origin feature/mi-mejora
```

---

##  Licencia

Distribuido bajo licencia **GNU GPLv3**. Consulta el archivo `LICENSE` para más detalles.

---

<div align="center">

**Lock-Chat** © 2026 · Seguridad y simplicidad.

`> Chatea en privado. Estés donde estés._`

</div>
