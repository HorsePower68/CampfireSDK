package com.sayzen.campfiresdk.app.develop

/*


    $ Больше тем

    Вики
    --- Элемент
    -   Создание статьи
    -   Изменение статьи
    -   Изменение заголовочника статьи

    -   Перенос разделов

    -   Возможность жаловаться
    -   Возможность переводить вики
    -   Перекрестные ссылки
    -   Юниты о публикации и редактировании в профиле и фэндоме
    -   Ссылки на элементы и списки
    -   Поиск
    -   Автоматически парсить текст на сервере на предмет ссылок на вики и добавлять форматирование с названиями (+ обновлять при пересохранении статьи на случай изменения)

    -   Над всеми языками нужна подпись
    -   Возможность востановить из удаленных
    -   Возможность откатить изменения элемента
    -   Возможность откатить изменения статьи

    Фразы в ленту
    - Не играйте с огнём.
    Фразы в профиль
    - Играет с огнём
    - Не играет с огнём
    - Протоадмин
    - Мяу

----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            RELEASE
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Отображать задание и обновление только после загрузки постов
    Оформить карточку с файлом как карточку
    Логировать запрсоы к бд
    Добавить идентификатор в запросы. + в выводе сервера
    Убрать свои посты из жалоб
    Добавить репорты на экран просмотра статьи
    XUnit - все унитовские адаптеры
    Вынести репорты в контроллер
    Рефактор карточек
    Хранить кол-во комментов в контроллере
    Скрывать карточку заданяи до первой загрузки
    Добавить кнопку смменить аватар в фэндоме
    Переименовать голосование в опрос
    Возможность добавлять ионку к тексту
    Редизайн встраиваемых карточек
    $ Обратная совместимость
    Фэндом по событиям
    Сделать отступ для иконки увеличенного текста
    Изменить цвет ачивки за стикеры
    - Поднят уровень для редактирования вики
    - Доработана система определеняи онлайна пользователя, теперь будет реже отображаться некорректный статус.
    - Поменял местами админскую и настройки.
    - Немного оптимизировано создание комментариев и сообщений, должно меньше фризить.

    Баг. Вылетело когда нажал на уведомление о достижении
    Баг. Из-за удаления карточек в ленте отображается картинка вместо загрузки
    Баг. По нажатию на уведомление блокировки поста по жалобе, нет перехода к модераторскому действию.
    Баг. Админская доступна не админам
    Баг. Сломан салют
    Баг. Лишним нули у ачивок
    Баг. Уровень не по порядку
    Баг. Карма не по порядку
    Баг. Осенний бокс работает рывками
    Баг. У ботов слишком большая дата жизни в приложении
    Баг. Нельзя убрать жалобы с форума
    Баг. Нельзя убрать жалобы с наборов стикеров
    - Иногда падало приложении при блокировке поста по жалобам.
    - Можно было пожаловаться на свой набор стикеров
    - При изменении сообщения в чате, нет отступа сверху.
    - Неправильно называлась осенняя коробка.
    - Исправлены некоторые проблемы с анимацией коробок.
    - Приложение больше не будет падать если вы используете устаревшую версию.
    - Иногда некорректно предлогался предпочтительный язык приложения.

----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            BACK LOG
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Оптимизировать EUnitsBlockGetAll
    Индикартор что есть новые уведомления при изпользовании драйвера
    Баг. Нельзя скопировать текст в описании профиля
    Баг. При создни поста нельзя копироть текст
    Возможность выбрать стартовый экран лента/чаты/фэндом
    Вики. Список объектов
    Вики. Избранное
    Вики. Возможность переключится на сетку
    !!! Баг. Куча нулов при регистрации
    % Редактор фото (возможность расширить фон)
    Блокнот
    Отображать пользователя что было заблокированно. Хоть как-то!!!

    При примечании к пользователю выбирать цвет для обводки аватара. Долгий тап по аватару показывает примечание.
    Полезные публикации
    При первом старте приложения настраивать ленту (выбирать интересные категории)
    Фэндом. Календарь с событиями
    Возможность закреплять форумы
    Войс. Линия не отражает реальный звук
    Войс. Если поднести к уху - менять динамик (Если уубрать от уха - пауза) (Код написан, но не работает)
    Возможность пометить пост как фейк или сомнительный (отображать комментарий) + событие
    Возможность отключить загрузку гифок в настройках - Если у пользователя отключена загрузка гифок, нужно как-то давать ему возможность её загрузить кликом (+вес?)
    Разделть ленту на экраны (подписки/лучшее/хорошее/бездна)  -   В фильтрах возможность отключить или перемстить экран подписок
    Посты. Музыка
    Посты. Страница Тест
    Посты. Страница Викторины
    Чат. Конфиренции (публичность)(управление людьми)
    Чат. Звонки
    Фэндом. Расширеннй поиск, с отображание скринштов
    При блокировке стикера в профилях должно появлятсья событие и должно приходить уведомление
    При блокировке набора стикеров в профилях должно появлятсья событие и должно приходить уведомление
    При игноре - игнорить посты, сообщения
    При игноре - игнорить упоминаяния
    Настройка - ставить оценку ананимно
    Скрывать навигацию при полном экране
    Кнопка прачитано в шторке для сообщений
    Возможность подтвердить модерацию на экране её просмотра
    Отображать до 5 сообщений в уведомлениях
    Программная потдержка эстафет
    Добавить пояснения к правилам (картинки)
    Возможность передать черновики. (+ защита от спама (через чс?)) (+ возможность отключить)
    Админы. Возможность жаловаться на админа/модератора
    Админы. Экран жалоб на админов/модераторов
    Админы. Экран c админскими банами и комментариями
    Админы. Возожноть протоадмину посмотреть заблокированные публикации пользователя
    Опрос. Опрос с нескольими вариантами
    Опрос. посмотреть результаты голосования без голосования (опционально)
    Опрос. Возможность посмотреть кто голосовал в опросах (открытый / закрытый опрос)
    Опрос. Опрос с картинками
    Улучшить защиту ресурсов
    Оптимизация. Очень долгий поиск по жанрам/платформам
    Оптимизация. Сервер на час виснит ночью изза обновления кармы
    Оптимизация. Кещирование страниц статьи
    Посты. Переводы статей
    Почты. Репост статей с других сообществ
    Посты. Страница Твит
    Посты. Страница Куб
    Посты. Возможность делится файлами
    Чат. Ограничение ЛС (только те на кого подписан)
    Чат. Возможность отвечать на сообщеняи в уведомлении
    Чат. Возможность отключить уведомленяи от конкретного пользователя (на время / навсегда)
    Фэндом. Возможность посмотреть подписчиков и их уровень подписки в фэндоме
    Фэндом. Поиск групп
    Фэндом. WebView с wiki
    Фэндом. Кастомизация фэндомов цветом
    Фэндом. Подписка на теги
    Профиль. Функция вайп аккаунта - удлить ВСЁ связанное с аккаунтом, кроме id и GoogleId
    Система хештегов. Тренды. Поиск по хештегу.
    Возможность в течении короткого времени поменять оценку
    Возможность призвать админа/модератора (@Admin @Moderator)
    Ссылки на фендом по названию (Как быть с языком? Что если в названии есть _)
    Помечать топовых пользователей
    Голосовой чат в обычных чатах

    Картинки
        -   Пустой вики список
        -   Пустой вики элемент
        -   Пустой список стикеров
        -   Пустой пакет стикеров

    Контроль работы сервера, медиа сервера, пушей
        -   Каждые 30 минут пытаться связаться с сервером, если не полчится выводить алерт
        -   Каждые 30 минут присылать пуши протоадмину в подпотоке, если пуша небыло 1 час выводить алерт в телефоне.
        -   При кадом пуше пытаться загрузить картинку, если не полчится выводить алерт

    Полезные посты
        -   отображать в профиле
        -   отображать в фендоме
        -   достижения

    Баг. При расширении поста мерцает картинка
    Баг. Дергается экран если открывать спойлер в ленте
    Баг. В модераторском событии о изменении тегов поста, нет информации о тегах
    Баг. Если открыть сообщение по уведомлению на счетчике оно не пропадает
    Баг. Не пропадают чаты при удалении фэндома
    Баг. При клике по уведомлению не всегда открывается пост
    Баг. Иногда в блокировках отображается пустой пост
    Баг. Если нажать на фильтр в уведомлениях то количество строк увеличивается
    Баг. Очень часто при переходе из уведомлений/ачивок в ленту зависает приложение
    Баг. Подвисает лента если в ней большой текст
    Баг. Уехала таблца 292845
    Баг. Неправильынй текст Модератор предупредил в фэндоме
    Баг. Если из черновика перейти в чат, потом вернуться, клавиатура не открывается
    Баг. У некоторых пользователей время в приложении на час отличается от реального

----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            Dream
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Гильдии
        -   Кнопки: Чат, Участники, Теги, Форумы, Фэндомы
        -   Описание
        -   Картинки
        -   Ссыли
        -   Посты
        -   Система заявок
        -   Система званий/привелегий
        -   Система меток (консты)
        -   Система контроля. (таблицы / налоги)

    Переводы песен + приложение
    Секретные ачивки
    Холивары
    Инвентарь / валюта
    Совместное создание статьи
    Потдержка (пользовательская (тикеты?))
    Оптимизация экранов под планшеты
    Вселенные
    Система званий
    Система коммитетов

----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            INFO
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Project created 7 oct 2015




 */
