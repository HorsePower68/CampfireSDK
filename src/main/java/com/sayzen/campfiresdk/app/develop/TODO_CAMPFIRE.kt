package com.sayzen.campfiresdk.app.develop

/*


    Фон в чаты
    Комментарии со стикерами

    $ Квест
        -   Перейти на экарн квеста
        -   Поставить оценку
        -   Открыть экран достижений
        -   Написать чтонибудь в чате
        -   Открыть бездну
        -   Открыть фильтры
        -   Создать пост

    Окончательно переименовать unit в publication, обратная совместимость в JSON
    Приложение по анимэ

    Баг. Проголосовал но не окрасилось (1 голос)
    Баг. ? После первого входа после скачивания не приходят уведомления
    Баг. ? Пользователи не могут голосовать в опросах









    Фразы
    -   Не перепутайте дварфов с гномами!
    -   Вперёд, делать ежедневное задание!
    -   Уровень багов в приделах разрешенной нормы!
    -   Напиши о баге, получи конфетку.
    -   Любимый костер.
    -   Нудно больше дров!
    -   Астронавты не умирают по субботам.
    -   Боже, да я терпеть не могу опасность.
    -   Это один маленький шаг для человека и огромный скачок для человечества.

    Профиль
    -   Ловец багов
    -   Лунная королева
    -   Охотник на привидений



----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            RELEASE
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Сделал мелкий редизайн поста, стараюсь меньшить количество визуального мусора. В целом беру направление на упрощение интерфейса приложения. В битве идей победила возможность добавлять музыку в приложение, но это сейчас проблематично сделать. Музыка обязателньо будет но позже. Вторая победившая идея - выбирать количесто выставляемой кармы, такое реализовать невозможно, система не позволяет. Идея по поводу возможности посмотреть видео рекламу за нграды - реализована. Так-же реализована идея открытия конкретного сообщения в чте, даже если оно было очень давно. Очень много идей записаны и будут реализованы немного позже.

    Изменения:
    - Теперь не стандартная фраза в ленте будет отображаться всегда. (раньше первые 10 раз отображалась стандартная)
    Настройка ленты
    Перекрасить кнопки в навигации
    Сделать чтоб для лучших брались посты за вчера.
    Редизайн поста
    Реклама за награды
    Не беспокоить через долгий тап на уведомления
    Не начилсять модерскую карму на пост.
    Убрать правило о выпрашивании кармы
    Переход к сообщению в чате

    Исправления:
    - У некоторых пользователей не работали голосования
    Баг. Нелья выполнить квест на создание чата фэндома
    Баг. В посте про фразы коофицент (у комментариев не поменялся фэндом)
    Баг. Вижу ананимно на своём посте
    Баг. Мнимальный
    Баг. Спсика
    Баг. В демо нет выбора пола


----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            BACK LOG
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    $ Эстафеты

    Переделать фильтры в ленте так чтоб все влазили
    При блокировке стикера в профилях должно появлятсья событие и должно приходить уведомление
    HTTP - https://habr.com/ru/post/69136/ (Проблема с сертификатом)
    Возможность цитировать аудио сообшения (+ свайп)
    Индикартор что есть новые уведомления при изпользовании драйвера
    При игноре - игнорить посты, сообщения, упоминаяния
    Система хештегов. Тренды. Поиск по хештегу.
    Корзина для постов и черновиков (Удаление через 7 дней)
    В уведомлениях о реакциях выводить реакцию и коммент
    При переходе на сообщение в чате открывть именно его
    Настройка выбор стикеров через паки
    Гибридные ЕЗ
    Оптимизировать EUnitsBlockGetAll
    Возможность выбрать стартовый экран лента/чаты/фэндом
    Блокнот
    Возожность просмотривать фэндомы сеткой.
    Возможность рассмотреть стикер
    Войс. Линия не отражает реальный звук
    Войс. Если поднести к уху - менять динамик (Если уубрать от уха - пауза) (Код написан, но не работает)
    Возможность пометить пост как фейк или сомнительный (отображать комментарий) + событие
    Возможность отключить загрузку гифок в настройках - Если у пользователя отключена загрузка гифок, нужно как-то давать ему возможность её загрузить кликом (+вес?)
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
    Посты. Переводы статей
    Почты. Репост статей с других сообществ
    Посты. Страница Твит
    Посты. Страница Куб
    Посты. Возможность делится файлами
    Чат. Ограничение ЛС (только те на кого подписан)
    Чат. Возможность отвечать на сообщеняи в уведомлении
    Чат. Возможность отключить уведомленяи от конкретного пользователя (на время / навсегда)
    Фэндом. WebView с wiki
    Фэндом. Подписка на теги
    Профиль. Функция вайп аккаунта - удлить ВСЁ связанное с аккаунтом, кроме id и GoogleId
    Возможность в течении короткого времени поменять оценку
    Возможность призвать админа/модератора (@Admin @Moderator)
    Ссылки на фендом по названию (Как быть с языком? Что если в названии есть _)
    Показывать кол-во символов которое напечатал пользователь
    Переводы песен + приложение
    Секретные ачивки
    Холивары
    Инвентарь / валюта
    Оптимизация экранов под планшеты
    Вселенные
    Система званий
    Система коммитетов
    Страница - генератор рандома. Задаешь срок. И в это время она генерирует случайное число / выбирает случайного пользователя / комментарий / текст

    Баг. После просмотра видео пропадает навигация
    Баг. Если открыт ютюб и вернуться в риложение - бесконечная загрузка
    Баг. Подвисает лента если в ней большой текст (не парсить теги и ссылки) (выводить тост при создании)
    Баг. В подписках и бездне разные результаты голосования в одно и тоже время (сделать адаптер для голосования)
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
    Баг. При удалении старницы, не чистятся ресурсы
    Баг. Комментарий создается с лишней анимацией
    Баг. При расширении поста мерцает картинка
    Баг. Дергается экран если открывать спойлер в ленте
    Баг. При клике по уведомлению не всегда открывается пост
    Баг. Очень часто при переходе из уведомлений/ачивок в ленту зависает приложение
    Баг. Если из черновика перейти в чат, потом вернуться, клавиатура не открывается
    Баг. У некоторых пользователей время в приложении на час отличается от реального

    Campfire Frotnite
        -   Уведомлния только Fortnite

    Примечания
       -   Выбор цвета
       -   Долгий тап на аватар
       -   Отображение увета на аватаре
       -   Возможность добавить примечанеи долгим тапом на аватар

    Информативность при удалении
        -  Причина удаления фэндома
        -  Причина удаления поста
        -  Причина удаления фрума

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


----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            INFO
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Project was created 10 apr 2016




 */
