# Zonda Push для Android

Библиотека подписывает установку приложения на Zonda Push и показывает уведомления FCM. Это отдельный репозиторий: `google-services.json` и ключ Firebase остаются в самом приложении.

Адрес и ключ берутся во вкладке Push в Zonda CRM, в блоке настройки приложения. Ключ начинается с `zp_`.

## Подключение

В `settings.gradle.kts` приложения укажите путь к клону этого репозитория:

```kotlin
includeBuild("/path/to/zonda-push-android")
```

В `build.gradle.kts` модуля приложения:

```kotlin
dependencies {
    implementation("com.zonda.push:zonda-push")
}
```

Плагин `com.google.gms.google-services` и файл `google-services.json` подключаются в приложении, не в этой библиотеке. Свой `FirebaseMessagingService` в приложении не нужен: его заменяет `ZondaMessagingService`.

## Старт

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ZondaPush.init(
            this,
            ZondaPushConfig(
                endpoint = "https://push.example.com",
                ingestKey = "zp_...",
            ),
        )
    }
}
```

`init` сохраняет постоянный `install_id`, создаёт канал `zonda_default` и отправляет установку на `POST /v1/users`. Повтор уходит при новом токене FCM и после ответа на разрешение уведомлений.

На Android 13 разрешение запрашивается отдельно, лучше после того, как пользователь уже в приложении:

```kotlin
ZondaPush.requestPermission(this)
```

В `onRequestPermissionsResult` вызовите `ZondaPush.onPermissionResult(this, granted)`.

## Сегменты

```kotlin
ZondaPush.addSegment(this, "payers")
ZondaPush.removeSegment(this, "payers")
```

Имя: строчные латинские буквы, цифры, `_` и `-`, до 40 символов. Отправка идёт на `POST /v1/users/segments` и повторяется, когда появится сеть.

## Нажатие

Сервер ставит `click_action` `com.zonda.push.OPEN`. По нажатию открывается `ZondaPushOpenActivity`. Если в данных пуша есть `url` с `http` или `https`, открывается ссылка. Иначе открывается приложение.

Свой обработчик задаётся в `Application.onCreate`, чтобы он был готов и при холодном старте:

```kotlin
ZondaPush.setOpenHandler { data ->
    // верните true, если переход обработан
    false
}
```

Канал по умолчанию — `zonda_default`. Его же подставляет сервер, если в рассылке канал не указан. Маленькая иконка в статус-баре — белый силуэт колокольчика. Свою иконку можно передать в `ZondaPushConfig.smallIconRes`. Большая картинка уведомления приходит с сервера.

## Требования

minSdk 23, compileSdk 35. Сеть — `HttpURLConnection`, очередь — WorkManager, сообщения — Firebase Messaging.
