# Zonda Push SDK

Android-библиотека пушей. У каждого приложения свой SDK-ключ `zp_…` из вкладки Push в Zonda CRM. Ключ другого приложения не подойдёт: сервер узнаёт приложение по этому ключу.

`google-services.json` в приложение класть не нужно. SDK-ключа достаточно, чтобы установка появилась в CRM и было видно, разрешил пользователь уведомления или нет. Пуши начнут доходить после того, как в CRM загрузите Firebase service account — обновлять приложение в сторе для этого не надо. Свой `FirebaseMessagingService` не нужен.

## Подключение

В `settings.gradle.kts`, рядом с `google()` и `mavenCentral()`:

```kotlin
maven { url = uri("https://jitpack.io") }
```

В модуле приложения только зависимость:

```kotlin
dependencies {
    implementation("com.github.zondaleadapps:push-sdk:1.1.2")
}
```

## Старт

Ключ берётся в CRM: Push → приложение → «Показать ключ».

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ZondaPush.init(this, "zp_ключ_этого_приложения")
    }
}
```

Либо без кода, через манифест — SDK стартует сам:

```xml
<meta-data
    android:name="com.zonda.push.sdk_key"
    android:value="zp_ключ_этого_приложения" />
```

Адрес сервера по умолчанию `https://push.android-crm.site`. Другой адрес: `ZondaPush.init(this, key, "https://...")` или meta-data `com.zonda.push.endpoint`.

`init` создаёт канал `zonda_default`, постоянный install id и отправляет установку на `POST /v1/users`.

На Android 13 разрешение спрашивайте отдельно, когда пользователь уже в приложении:

```kotlin
ZondaPush.requestPermission(this)
```

В `onRequestPermissionsResult` вызовите `ZondaPush.onPermissionResult(this, granted)`.

## Сегменты

```kotlin
ZondaPush.addSegment(this, "payers")
ZondaPush.removeSegment(this, "payers")
```

Имя: строчные латинские буквы, цифры, `_` и `-`, до 40 символов.

## Нажатие

Сервер ставит `click_action` `com.zonda.push.OPEN`. Если в данных пуша есть `url` с `http` или `https`, открывается ссылка. Иначе открывается приложение.

```kotlin
ZondaPush.setOpenHandler { data ->
    false
}
```

Обработчик задавайте в `Application.onCreate`, иначе холодный старт его не увидит.

minSdk 23, compileSdk 35.
