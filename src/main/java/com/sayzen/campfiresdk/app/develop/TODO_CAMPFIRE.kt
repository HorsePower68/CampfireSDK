package com.sayzen.campfiresdk.app.develop

/*

    Добавить уведомление что проспал эстафету
    Убрать полоски в сообщениях и уведомлениях
    Переносить скрепку
    Баг. Тупит удаление сообщений
    Кнопка "Готово" при выборе доп опцый для поста
    Пометил пост как важный
    Баг. Дважды отображается один и тот же пост в закладках
    Вернуть рекламу
    Баг. Карточка обновления отвратительно выглядит (Отображать над квестом, но убедиться тчо работате обновление)


    Подробная информация на экране блокировок (на сколько забанил, и т.д.)

    Эстафеты
        -   Возможность подписаться (Получать уведомления о новых постах) (Автоматически подписывать участников)
        -   Уведомлять что отказался и кому передана эстафета (если не кому, то тоже сообщать)


----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            RELEASE
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

   !!! Нет скролла к комменту по уведомлению
   !!! Вылет при скачивании картинки
   !!! Баг с новой коробкой
   !!! Нет скролла к сообщению по уведомлению
   !!! Вылет при переходе через уведомление о упоминании в чате
   !!! Vulth передаётся эстафета Текущая схема назначения эстафет сдомаетс, нужно обнулять владельца если не нашлось кому передать



----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            BACK LOG
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Регистрация по мылу
    HTTP - https://habr.com/ru/post/69136/ (Проблема с сертификатом)

    История тега
    Фулскрин
    Описание в пользовательские чаты
    Переделать меню поста, часть функций вынести в иконки
    В жалобах нужно отображать фэндом иначе не понятно тчо это мултиязычный пост
    Би. Медальки
    Возможность поставить рубрику после публикации
    Би. Объединять комменты к посту в уведомлениях
    Комментарии со стикерами ?
     ? Челендж активности
    Переделать фильтры в ленте так чтоб все влазили
    При блокировке стикера в профилях должно появлятсья событие и должно приходить уведомление
    Возможность цитировать аудио сообшения (+ свайп)
    Индикартор что есть новые уведомления при изпользовании драйвера
    При игноре - игнорить посты, сообщения, упоминаяния
    Система хештегов. Тренды. Поиск по хештегу.
    Корзина для постов и черновиков (Удаление через 7 дней)
    В уведомлениях о реакциях выводить реакцию и коммент
    При переходе на сообщение в чате открывть именно его
    Переделать отзывы в рецензии
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
    Предложка при упоминании

    Баг. Проголосовал но не окрасилось (1 голос)
    Баг. ? После первого входа после скачивания не приходят уведомления
    Баг. ? Пользователи не могут голосовать в опросах
    Баг. После просмотра видео пропадает навигация
    Баг. Если открыт ютюб и вернуться в риложение - бесконечная загрузка
    Баг. Подвисает лента если в ней большой текст (не парсить теги и ссылки) (выводить тост при создании)
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
