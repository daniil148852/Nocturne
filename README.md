# 🌑 Nocturne — AI Noir Detective

> Процедурный нуар-детектив на Android: каждое дело, подозреваемый и развязка сгенерированы Mixtral.

![Nocturne banner](docs/banner.svg)

## Что это

Nocturne — мобильная игра-расследование в эстетике 1940-х. Ты — детектив участка №7. Mixtral
создаёт дело (жертва, 4 подозреваемых, мотивы, улики, скрытая правда) при каждом запуске новой
игры. Ты осматриваешь места, допрашиваешь подозреваемых, ведёшь записки и, когда уверен,
выдвигаешь обвинение — а AI генерирует эпилог в стиле радиохроники 1947 года.

Ни одно дело не повторяется. Каждый диалог — живой. Каждая развязка — другая.

## Фишки

- 🌧️ Бесконечный дождь на фоне + глобальное мерцание янтарной лампы
- ⌨️ Эффект печатной машинки для всех AI-текстов (стриминг Mixtral)
- 🎺 JazzPulse — индикатор «думает» в стиле осциллографа
- 🧥 Процедурные силуэты подозреваемых (Canvas, без картинок)
- 🎬 Noir-скринсейвер, ночные тона, типографика как у Дэш Хэммета

## Стек

- **Язык**: Kotlin 1.9.22
- **UI**: Jetpack Compose (BOM 2024.02) + Material 3
- **AI**: Mistral Chat Completions (`mistral-medium`, fallback `open-mixtral-8x7b`)
- **Стриминг**: OkHttp + SSE, Flow-based typewriter
- **DI**: ручной ServiceLocator (`AppContainer`) — без Hilt/KSP, простота > магия
- **Хранение**: Jetpack DataStore (настройки + кейсы как JSON), EncryptedSharedPreferences для API-ключа
- **Min SDK**: 24 (Android 7+), target SDK 34

## Подготовка

1. **Android Studio Hedgehog** или новее.
2. JDK 17 (Android Studio ставит сама).
3. Установи Android SDK 34 через SDK Manager.
4. **Ключ Mixtral** — бесплатные кредиты дают на [console.mistral.ai](https://console.mistral.ai/):
   - Sign up → API Keys → Create new key
   - Скопируй ключ (формат: `xxxxxxxx`). Хранится в зашифрованном виде только на твоём устройстве.

## Сборка и запуск

```bash
# Клонировать/распаковать проект, затем:
cd Nocturne

# В Android Studio: File → Open → выбери папку
# Studio сама создаст gradle-wrapper.jar если его нет — Run ▶ App

# Или через gradle CLI (нужна Gradle 8.4+):
gradle wrapper --gradle-version 8.4      # если нет wrapper jar
./gradlew :app:assembleDebug
./gradlew :app:installDebug
adb shell am start -n com.nocturne.game/.MainActivity
```

При первом запуске приложение попросит ввести ключ Mixtral. После сохранения ключ шифруется
через Android Keystore (AES256_GCM) и сохраняется в `EncryptedSharedPreferences`.

## Как устроено AI

| Этап игры | Что генерирует Mixtral | Режим |
|---|---|---|
| Новое дело | `Case` (JSON): жертва, 4 подозреваемых, 6 улик, скрытая правда, эпилог | strict JSON |
| Допрос подозреваемого | Реплика от лица подозреваемого (стемит токены) | streaming |
| Осмотр места/улики | Внутренний монолог детектива (стемит) | streaming |
| Финал обвинения | Вердикт + что произошло + финальная фраза | strict JSON |

Все промпты живут в [`Prompts.kt`](app/src/main/java/com/nocturne/game/util/Prompts.kt).
Все ответы парсятся через `JsonExtract` (вытащит JSON даже если модель обернула его в prose).

## Структура проекта

```
app/src/main/java/com/nocturne/game/
├── NocturneApp.kt                Application (точка входа DI)
├── MainActivity.kt               Хост
├── AppContainer.kt               ServiceLocator: API, репозитории, шифрованные настройки
├── data/
│   ├── api/                      Mistral Chat Completions клиент + DTO + JSON-парсер
│   └── repository/               Settings (API key + active case) и Case (дело как JSON)
├── domain/model/                 Case · Suspect · Evidence · InterrogationTranscript
├── ui/
│   ├── theme/                    Noir-цвета и типографика
│   ├── noir/LanternFlicker.kt    Глобальная пульсация света
│   ├── components/               Дождь · Печатная машинка · JazzPulse · NoirCard · SuspectAvatar · Buttons
│   ├── navigation/               NavHost + Route sealed class
│   ├── common/                   UiState, фабрика ViewModel
│   └── screens/                  setup / menu / briefing / hub / location / interrogation / accusation / resolution
└── util/
    ├── Constants.kt              Mistral endpoints, model names, лимиты токенов
    └── Prompts.kt                Все промпты
```

## Известные ограничения / идеи для v2

- Нет сохранения прогресса допросов между сессиями (транскрипты not persisted yet)
- Нет звуков (дождь/джаз опциональны)
- Нет ачивок / дневного лимита дел
- Стоит перейти на `k-mistral` русскоязычный вариант, если качество отзывов покажется ниже

## Лицензия

Personal/educational project. Mistral API subject to its own ToS.

— Гуд luck, детектив. Город не ждёт.
