1. Регистрация
     1. Запрос:
       curl -X 'POST' \
       'http://localhost:9900/registration' \
       -H 'accept: */*' \
       -H 'Content-Type: application/json' \
       -d '{
               "username": "test2",
               "email": "test2@yandex.ru",
               "password": *******,
               "privacy": true
            }'
     2. Ответ со статусом 200, регистрация прошла успешно, пароля в ответе нет.
       {
           "id": 8,
           "username": "test2",
           "email": "test2@yandex.ru"
       }
     3. Регистрация с таким же емайлом:
      Запрос:
         curl -X 'POST' \
         'http://localhost:9900/registration' \
         -H 'accept: */*' \
         -H 'Content-Type: application/json' \
         -d '{
         "username": "test3",
         "email": "test2@yandex.ru",
         "password": ********,
         "privacy": true
         }'
     4. Ответ со статусом 409, регистрация не прошла, так как такой пользователь уже есть в системе.
      {
          "detail": "Адрес электронной почты уже зарегистрирован",
          "instance": "/registration",
          "status": 409,
          "title": "Conflict"
      }
     5. Регистрация с коротким паролем.
         Ответ 400, регистрация не прошла, так как пароль не прошёл проверку, по правилам регистрации.
            {
            "detail": "Проверьте данные запроса",
            "instance": "/registration",
            "status": 400,
            "title": "Bad Request",
            "fields": [
            "password: размер должен находиться в диапазоне от 8 до 72"
            ]
            }
2. Проверка авторизации:
    1. Запрос без авторизации
   curl -X 'GET' \
   'http://localhost:9900/user' \
   -H 'accept: */*'
    2. Ответ 401 мы не авторизованы у нас нет доступа к данному ендпоинту.

    3. Запрос с авторизацией:
            curl -X 'GET' \
            'http://localhost:9900/user' \
            -H 'accept: */*' \
            -H 'Authorization: Bearer *****

    4. Ответ 200, мы авторизовались и сервер нас узнал
   {
       "id": 8,
       "username": "test2",
       "email": "test2@yandex.ru",
       "roles": []
   }

    5. Выполнили логаут и вновь ответ 401 мы не авторизованы и сервис нас не пускает.
    6. Запрос с не правильным паролем
         curl -X 'POST' \
        'http://localhost:9900/login' \
        -H 'accept: */*' \
        -H 'Content-Type: application/json' \
        -d '{
        "email": "test2@yandex.ru",
        "password": ***
        }'
    
    7. Ответ 401 без access токена
    {
    "detail": "Неверный email или пароль",
    "instance": "/login",
    "status": 401,
    "title": "Unauthorized"
    }

    8. На запрос с не правильным переданным токеном возвращается ошибка 401

3.
Регистрация - мы создаём новую учетную запись пользователя.
Вход - пользователь проходит авторизацию и ему выдаётся токен авторизации, для дальнейшего использования в запросах на сервер.
Запрос к защищенному API может происходить только если пользователь авторизован, его токен валидный и еще действителен.