package com.sayzen.campfiresdk.app.develop

/*



    $ Заменить форумы чатами
        -   Создние (+событие)
        -   Удаление (+событие)
        -   Изменение (+событие)
        -   Основной чат сверху
        -   Кол-во чатов
        -   ссылки
        -   Ссылки в модераторских событиях
        -   Отображать инфо диалог при первом входе
        -   Возможность посмотреть инфо диалог через меню


    $ Лучшее/Хорошее



    При переходе на сообщение в чате открывть именно его
    Избавиться от дерганных анимаций
    Настройка выбор стикеров через паки
    Корзина для постов и черновиков
    В уведомлениях о реакциях выводить реакцию и коммент
    Переделать фильтры в ленте так чтоб все влазили

    Баг. После просмотра видео пропадает навигация
    Баг. Подвисает лента если в ней большой текст (не парсить теги и ссылки) (выводить тост при создании)
    Баг. В подписках и бездне разные результаты голосования в одно и тоже время (сделать адаптер для голосования)


----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            RELEASE
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Campfire 0.90 Beta

    Изменения:
    Убрать навигацию при создании рубрики
    В события где фэндом написан в скобках, заменить на гиперссылку
    Категория: Места
    Экран ананима
    Приходит уведомление о реакции на свйо комментарий
    В задании на привет дописать то в фэндоме
    Рубрика внутри поста
    Глочка не показывать подписки  в бездне
    Фразы
    Возможность анонимно оценить с экрана оценок
    Возможность ограничить голосование по уровню и карме

    Исправления:
    Баг. Ананимные оценки не ананимны в уведомлениях
    Баг. Если зайти в рбрики то на постах не отображается рубрика
    Баг. Если зайти в рбрики то на постах не отображается лучший коммент
    Баг. При отправке страницы с текстом не все иконки блокируется
    Баг. Есть кнопки при редактировании чужого поста
    Баг. Не скрывается клавиатура при открытии диалогов
    Баг. Нельзя поменть роль пользователя в чате пока его не адалишь

    Баг. При открытии поста с упоминаем, уведомление не читается

----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            BACK LOG
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Campfire Frotnite
        -   Уведомлния только Fortnite

    Примечания
       -   Выбор цвета
       -   Долгий тап на аватар
       -   Отображение увета на аватаре
       -   Возможность добавить примечанеи долгим тапом на аватар

    Информативность при удалении
        - Причина удаления фэндома
        - Причина удаления поста
        - Причина удаления фрума

    При блокировке стикера в профилях должно появлятсья событие и должно приходить уведомление

    HTTP - https://habr.com/ru/post/69136/
    Отображать фэндом при поиске по тегам
    Возможность цитировать аудио сообшения (+ свайп)
    Индикартор что есть новые уведомления при изпользовании драйвера
    При игноре - игнорить посты, сообщения
    При игноре - игнорить упоминаяния
    Система хештегов. Тренды. Поиск по хештегу.

    Баг. Очень часто сообщение которое установлено как последнее в чате - удалено
    Баг. Если один раз не удалось загрузить картинку, эту картинку не загрузин никогда
    Баг. ? Сломается завершение квеста по уведомлению!
    Баг. Если проскроллить к коментам и назад к посту то нельзя скопировать текст
    Баг. Иногда появляется навигациия при откратии диалогов в полноэкранном режиме
    Баг. При открытии диалога меняется цвет навигации
    Баг. При переходе к большому списку комментов через уведомления, не находит коммент
    Баг. Нет анимации клика на заоловке фэндома в посте
    Баг. Нельзя скопировать текст в описании профиля
    Баг. При создни поста нельзя копироть текст
    Баг. Белая-цветная: Иконки в меню
    Баг. Если открыть сообщение по уведомлению на счетчике оно не пропадает
    Баг. Не пропадают чаты при удалении фэндома
    Баг. Иногда в блокировках отображается пустой пост

    Вики
        -   Список объектов (+ заголовок)
        -   Избранное
        -   Объект ссылка на вики статью/раздел
        -   Возможность переключится на сетку
        -   Возможность востановить из удаленных
        -   Возможность откатить изменения элемента
        -   Возможность откатить изменения статьи
        -   Перенос разделов
        -   Возможность жаловаться
        -   Возможность переводить вики
        -   Перекрестные ссылки
        -   Юниты о публикации и редактировании в профиле и фэндоме
        -   Поиск
        -   Автоматически парсить текст на сервере на предмет ссылок на вики и добавлять форматирование с названиями (+ обновлять при пересохранении статьи на случай изменения)
        -   Над всеми языками нужна подпись
        -   ? Есл удалить кратинку, то она пропадет во всех ревизиях
        -   Сообщение что не заполнено на вашем языке
        -   Переключение на английский

    Картинки
        -   Дефолтная аватарка пользователя
        -   Аватарка для ананимной оценки
        -   Пустой вики список
        -   Пустой вики статья
        -   Картинка с флажками на фон профиля
        -   Список рубрик
        -   Уровни 10-15



----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            BACK LOG (LOW)
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Гибридные ЕЗ
    Оптимизировать EUnitsBlockGetAll
    Возможность выбрать стартовый экран лента/чаты/фэндом
    Блокнот
    Возожность просмотривать фэндомы сеткой.
    Возможность рассмотреть стикер

    Баг. При удалении старницы, не чистятся ресурсы
    Полезные публикации
    Возможность закреплять форумы
    Войс. Линия не отражает реальный звук
    Войс. Если поднести к уху - менять динамик (Если уубрать от уха - пауза) (Код написан, но не работает)
    Возможность пометить пост как фейк или сомнительный (отображать комментарий) + событие
    Возможность отключить загрузку гифок в настройках - Если у пользователя отключена загрузка гифок, нужно как-то давать ему возможность её загрузить кликом (+вес?)
    Разделть ленту на экраны (подписки/лучшее/хорошее/бездна)  -   В фильтрах возможность отключить или перемстить экран подписок
    Посты. Музыка
    Фэндом. Расширеннй поиск, с отображание скринштов
    Кнопка прачитано в шторке для сообщений
    Возможность подтвердить модерацию на экране её просмотра
    Отображать до 5 сообщений в уведомлениях
    Возможность передать черновики. (+ защита от спама (через чс?)) (+ возможность отключить)
    Админы. Возможность жаловаться на админа/модератора
    Админы. Экран жалоб на админов/модераторов
    Админы. Экран c админскими банами и комментариями
    Опрос. Опрос с нескольими вариантами
    Опрос. посмотреть результаты голосования без голосования (опционально)
    Опрос. Возможность посмотреть кто голосовал в опросах (открытый / закрытый опрос)
    Опрос. Опрос с картинками
    Улучшить защиту ресурсов
    Оптимизация. Сервер на час виснит ночью изза обновления кармы
    Посты. Переводы статей
    Почты. Репост статей с других сообществ
    Посты. Страница Твит
    Посты. Страница Куб
    Посты. Возможность делится файлами
    Чат. Ограничение ЛС (только те на кого подписан)
    Чат. Возможность отвечать на сообщеняи в уведомлении
    Чат. Возможность отключить уведомленяи от конкретного пользователя (на время / навсегда)
    Фэндом. WebView с wiki
    Фэндом. Кастомизация фэндомов цветом
    Фэндом. Подписка на теги
    Профиль. Функция вайп аккаунта - удлить ВСЁ связанное с аккаунтом, кроме id и GoogleId
    Возможность в течении короткого времени поменять оценку
    Возможность призвать админа/модератора (@Admin @Moderator)
    Ссылки на фендом по названию (Как быть с языком? Что если в названии есть _)
    Помечать топовых пользователей
    ? Показывать кол-во символов которое напечатал пользователь?


    Полезные посты
        -   отображать в профиле
        -   отображать в фендоме
        -   достижения

    Баг. Комментарий создается с лишней анимацией
    Баг. При расширении поста мерцает картинка
    Баг. Дергается экран если открывать спойлер в ленте
    Баг. При клике по уведомлению не всегда открывается пост
    Баг. Очень часто при переходе из уведомлений/ачивок в ленту зависает приложение
    Баг. Если из черновика перейти в чат, потом вернуться, клавиатура не открывается
    Баг. У некоторых пользователей время в приложении на час отличается от реального

----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            Dream
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Страница - генератор рандома. Задаешь срок. И в это время она генерирует случайное число / выбирает случайного пользователя / комментарий / текст

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

    Project created 10 apr 2016




 */
