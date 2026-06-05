# Sotark Play — Android

Android-клиент для магазина приложений Sotark Play.  
**Kotlin + Jetpack Compose + Hilt + Retrofit + Coil**

## Настройка

1. Клонируй репозиторий
2. Открой в **Android Studio Hedgehog** или новее
3. Укажи адрес сервера в `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"https://твой-сервер.railway.app/\"")
   ```
4. Нажми **Run**

## Структура

```
com.sotark.play
├── data
│   ├── api          — Retrofit интерфейс + DI
│   ├── model        — Data-классы
│   └── repository   — AppRepository
├── ui
│   ├── components   — Переиспользуемые Compose-компоненты
│   ├── screens      — HomeScreen, SearchScreen, AppDetailScreen
│   └── theme        — Цвета, типографика
└── viewmodel        — HomeVM, SearchVM, AppDetailVM
```

## Экраны

| Экран | Описание |
|-------|----------|
| Главная | Топ-10, новинки, категории |
| Поиск | Поиск с автоподсказками |
| Детали | Иконка, скриншоты, отзывы, кнопка скачать APK |
