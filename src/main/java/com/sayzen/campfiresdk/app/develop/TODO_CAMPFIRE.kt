package com.sayzen.campfiresdk.app.develop

/*



    ?  Баг. После редактирования опроса, слетают ограничения
    ?  Баг. Не передалась эстафета после того как нажал учстовть при жимвом ользователе

    Би. Кртакое описание пост (для уведомлений) (можно взять первый текст или картинку)
    Би. Кртакое описание комментария (для уведомлений) (можно взять первый текст или картинку)
    Подробная информация на экране блокировок (на сколько забанил, и т.д.)
    История тега

    Эстафеты
        -   Экрн эстафеты, список постов, участников, тех кто отказался и т.д.
        -   Возможность подписаться (Получать уведомления о новых постах)
        -   Автоматически подписывать участников
        -   Автоматически подписывать того кому передана
        -   Уведомлять что отказался и кому передана эстафета (если не кому, то тоже сообщать)


----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            RELEASE
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Campfire 0.93 Beta
    Решили посвятить релиз исправлению ошибок и оптимизации, а то жалоб на это море. В этом обновлении не появилось чего-то глобального, но все старое стало работать лучше. Ну... почти ничего. Я подностью переработал систему переходов между экранами с загрузкой, для пользователей это будет выглядеть просто как более плавная анимация, но для приложения это большое изменение, будем надеятся проблем с этим не вознинет.

    Изменения:
    - Дообавлена кое-какая коробочка.
    - Переработана система навигации между экранами с загрузкой, анимация будет плавнее и не будет отображаться вечный сервый экран с загрузкой.
    - Редизайн некоторых диалогов. (практически незаметный)
    - Убрана лишняя линия на экране активностей.
    - Теперь на этафете оранжевый счетсич. Для привлечения внимания.
    - Если вам не передавали эстафету, то счетчик не будет отображатсья в активностях.
    - В активности выведены рубрики. Это значит что если вы не делали пост в рубрике в течении 6 дней, то приложение вам об этом будет настойчево напоминать.
    - Доработан алгорит определения размера карточки с постом. Если пост совсем немного больше нужного размера, то карточка будет расширена автоматически.
    - В на карточку эстафеты в посте повешан лейбл, тчоб всем было понятно что это пост для эстафеты.
    - При выборе эстафет для поста зменена надпись в случае их отстутсвия, теперь пользователям будет очевиднее почему там ничего нет.
    - Немного измененно описание эпического квеста про настройку ленты.
    - Теперь у всех новых пользователей по умолчанию включена категория "другое".
    - Дорботан экран выбора пола при первом запуске приложения.
    - Куча новых сплешей в ленту.
    - Куча новых сплешей в профиль.
    - Убрана навигация с экранов ошибок.
    - На экранах ошибок изменен цвет статусбара и бара навигации(для тех, у кого он есть).
    - Теперь будет отображаться более подробная информация о удаленных и заблокированных публикациях при попытке перехода на них.

    Исправления:
    - Оптимизировал работу с памятью, надеюсь станет меньше вылетать на слабых телефонах.
    - Отображался плюс при выборе категории для поста.
    - У стартового экрана был прозрачный фон.
    - Иногда вылетало приложение при отображении диалогов загрузки.
    - Часто на постах отображалась кнопка "Показать" хотя она ничего не делала.
    - Приложение падало при попытке скачать картинку.
    - Нельзя было обновить экран подписок в ленте.
    - Невозможнобыло сделать пользователя модератором.
    - Не сразу отображался счетчик в навигации если передали эстафету.
    - При нажатии на уведомление о сообщени в чате, котрый уже открыт, небыло скролла
    - Если упомянули в беседе, то было видно только это сообщение и несколько выше.
    - Иногда вылетало приложение при разворачивании постов.
    - Иногда открывался лишнйи экран с сообщением о проблемах с сетью.
    - Иногда при частой смене аккаунта сетали настройки.
    - После создания коммента к нему небыло скролла. (Да, знаю что это уже в третий раз)
    - Иконка при свайпе карточек была без отступа
    - Можно было закрыть создание коммента кнопкой назад.
    - Не работали ссылки на чаты фэндома.
    - Некоторые ссылки в цитатах не форматировались.
    - Нельзя было использовать форматирование текста на ссылках кемпа.
    - У конференций попрежнему иногда оторажалась иконка с флагом.
    - После октаза от эстафеты не уменьшался счетчик.
    - Иногда вылетало при кадрировании изображения.
    - Иногда вылетало при отображени карточки Campfire объекта.
    - Иногда вылетало при загрузке данных из кеша.
    - Вылетало если оставить в ленте тольо экран подписок.
    - Вылетало при загрузке событий чата на английском языке.
    - Не подгружаются стикерпаки в профиле.


----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------
            BACK LOG
----------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------------------

    Регистрация по мылу
    HTTP - https://habr.com/ru/post/69136/ (Проблема с сертификатом)

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
