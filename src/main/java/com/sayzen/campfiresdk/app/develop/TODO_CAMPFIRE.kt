package com.sayzen.campfiresdk.app.develop

/*


----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            RELEASE
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    !!!!!! БЕКАП !!!!!
    !!!!!! БЕКАП !!!!!
    !!!!!! БЕКАП !!!!!
    !!!!!! БЕКАП !!!!!
    !!!!!! БЕКАП !!!!!

    !!!!!! МИГРАЦИЯ !!!!!
    !!!!!! МИГРАЦИЯ !!!!!
    !!!!!! МИГРАЦИЯ !!!!!

    Campfire 0.87 Beta
    В битве идей победили чсобственные чаты, так что вот они. Важно понимать, что мне пришлось очень серьезно переделать всю систему чатов, по этому будут ошибки. Скорее всего вечером выйдет еще одно обновление которое их исправляет. Теперь можно закрывать посты. Еще исправил кучу ошибок. Немного улчшен чат и добавлена пара полезных функций.

    Изменения:
    - Временно убран полноэкранный режим, из-за того что он работает очень нестабильно и как его починить я пока не знаю.
    - Теперь можно создавать чаты. Добавлять в них людей. Редактировать их. И не нарушать правила приложения, все ранво кто-то вас заложет админам.
    - Закрытые посты.
    - Доработано определение позиции для рекламного поста в ленте.
    - Изменено описание уровня на экарне достижений. Надеюсь новичкам теперь будет понятнее.
    - Доработан способ проверки актуальности версии приложения. Больше не будет открываться экран требующий обновить версию. (пока не выйдет версия для которой нет обратной совместимости)
    - Доработан алгорит загрузки рекламы, она больше не будет загружаться если не нужна.
    - Теперь можно настроить ленту на отображаение бездны первым экраном.
    - В сообщениях теперь выдеоен логин пользователя которому отвечают.
    - В цитатах теперь выделен логин пользователя сохдавшего цитируемое сообщение.
    - Немного почистил базу данных фэндомов.
    - Появилась возможность чистить историю личных сообщений.
    - Изменена дата рождения Campfire. (покапался в архивах)
    - Теперь сервер запоминает что вы согласились с предупреждением о закрытом фэндоме. (Больше не будет отображатсья при переустановке)
    - Доработана Аналитика

    Исправления:
    - Чато после первого входа в приложение не приходили уведомления.
    - С экраном приложения происходила магия при появлении клавиатуры.
    - Падали уведомления если сидеть на старой версии.
    - Вылетало при отображении события о изменении коофицэнтп.
    - Иногда не работали модераторские функции.
    - Иногда не получалось открыть экран фэндома.
    - Иногда мерцали изображения в чате.
    - Иногда не записывалась статистика на сервере.
    - Не у всех протоадминов отображаетсяэто в профиле.
    - По прежнему иногда не работало форматирование ссылок.
    - у некоторыехпользователей клавиатура скрывала поля ввода.
    - Иногда в заданиях на карму отображалось некорректное значение
    - Не работал экран избранных стикеров если их небыло.
    - Не работал экран стикеров если их небыло.
    - Не работал виджет стикеров если их не было.
    - Можно было цитировать комментарии в профиле.
    - При нажатии на сообщение в профиле открывался лишний диалог.
    - Можно было цитировать сообщение в профиле.
    - Иногда сами по себе выполнялись задания на карму.
    - При поиске фэндомов иногда не работал выбор категории.
    - При изменении собщения в чате пропадала циатта.
    - Иногад не загружались результаты опроса.
    - Иногда пользователю переставали приходить уведомления.
    - У пользователей с квадратными аватарками иногда появлялось скругление.
    - У некоторых пользователей в профиле отображались фантомные наборы стикеров.
    - В белой теме был серый колокол в чате.
    - Убрана комната, помыта посуда, внесен мусор.
    - В списке тем было две красных-белых темы.
    - Иногда при входе отображался серый экран.
    - В белой цветной теме не видно кнопок при просмотер картинки.
    - В белой цветной теме не видно навигацию.
    - При зуме на экарне просмотра картинок пропадают иконки.
    - Не работало приближение двойным тапом на экране прссмотра картинок.
    - В админском событии о блокировке в фэндоме былп кривая ссылка на фэндом.
    - Иногда не отображалось что ктото печатает в чате.
    - У некоторых фэндомов было некорректное кол-во подписчиков.
    - Ингда некорректно обновлялось состояние прочтения ЛС.
    - Нельзя было перейти к публикации по админскому действию.
    - Первое сообщение в ЛС становилось сразу прочитанным.
    - Если при переходе к чату возникала ошибка - была бесконечная загрузка.
    - Иногда не отображалась часть фэндомов в глобальном поиске.
    - Иногда не отображалось что пользователь модератор.
    - Нельзя было сделать админа модератором фэндома.
    - Гаснет колокол после появленяи клавиатуры в чате.
    - После удаления фэндома пропадали все карточки с экрана поиска фэндомов.

----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            MAST HAVE IN RELEASE
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Баг. Не выделен пользователь в обычном сообщении с ответом
    Баг. Если цитировать сообщение и его удалят о отправки выскачит повторить

    Конфирренции
        -   Возможность вернуться в беседу по ссылке если вышел и удалил историю
        -   Возможность запретить юзерам менять параметры беседы
        -   Возможность назначать модераторов
        -   Подписывать кто создатель/модератор/пользователь

    Информативность при удалении
        - Причина удаления фэндома
        - Причина удаления поста
        - Причина удаления фрума

    Campfire Hello

    При примечании к пользователю выбирать цвет для обводки аватара. Долгий тап по аватару показывает примечание.
    Программная потдержка рубрик
    Модернизировать систему блокировок
    Не информативное уведомление о блокировке
    При блокировке стикера в профилях должно появлятсья событие и должно приходить уведомление
    В блокировках отображать наказание и заблокированы ли последнии

    HTTP - https://habr.com/ru/post/69136/
    Категория: Места
    Отображать фэндом при поиске по тегам
    % Редактор фото (возможность расширить фон)
    % Редактор фото (возможность рисовать)
    Возможность цитировать аудио сообшения (+ свайп)
    Индикартор что есть новые уведомления при изпользовании драйвера
    Отображать пользователя что было заблокированно. Хоть как-то!!!
    При игноре - игнорить посты, сообщения
    При игноре - игнорить упоминаяния
    Настройка - ставить оценку ананимно
    Программная потдержка эстафет
    Система хештегов. Тренды. Поиск по хештегу.

    Баг. Если один раз не удалось загрузить картинку, эту картинку не загрузин никогда
    Баг. Иногда реклама отображается в самом начале ленты (а может и фича, кто знает)
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
    Баг. Подвисает лента если в ней большой текст

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

    Контроль работы сервера, медиа сервера, пушей
        -   Каждые 30 минут пытаться связаться с сервером, если не полчится выводить алерт
        -   Каждые 30 минут присылать пуши протоадмину в подпотоке, если пуша небыло 1 час выводить алерт в телефоне.
        -   При кадом пуше пытаться загрузить картинку, если не полчится выводить алерт

    Картинки
        -   Пустой вики список
        -   Пустой вики статья
        -   Уровни 10-15
        -   Картинка с флажками на фон профиля



----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            BACK LOG
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Гибридные ЕЗ
    Оптимизировать EUnitsBlockGetAll
    Возможность выбрать стартовый экран лента/чаты/фэндом
    Блокнот
    Возожность просмотривать фэндомы сеткой.
    Возможность рассмотреть стикер
    Реакция на комменты

    Баг. При удалении старницы, не чистятся ресурсы
    Полезные публикации
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
    Кнопка прачитано в шторке для сообщений
    Возможность подтвердить модерацию на экране её просмотра
    Отображать до 5 сообщений в уведомлениях
    Добавить пояснения к правилам (картинки)
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
    Возможность в течении короткого времени поменять оценку
    Возможность призвать админа/модератора (@Admin @Moderator)
    Ссылки на фендом по названию (Как быть с языком? Что если в названии есть _)
    Помечать топовых пользователей
    Голосовой чат в обычных чатах


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
