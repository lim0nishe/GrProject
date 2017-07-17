# GrProject

RESTfull веб сервис для управления серверами FTP

Сервис подбирает подходящий сервер на основе разработанной мной метрики. Сервис предоставляет список доступных серверов, отсортированный
по значению метрики. Значение метрики зависит от требуемого для пользователя места, uptime серверов, load average серверов,
свободного места на сервере и ширины канала. Параметры для метрики могут быть легко добавлены или убраны, т.к. используется фабрика классов.
Также сервис легко перенастраивается для работы с другими FTP серверами, т.к. команды управления вынесены в отдельный файл. 

запросы:

/def/service/auth - авторизация
POST
content-type: application/json
raw body: 
{
  "login" : "admin",
  "password" : "admin"
}

/def/service/ - создание пользователя
POST
content-type: application/json
raw body:
{
  "sessionToken" : <session token>,
  "username" : <имя пользователя>,
  "password" : <пароль для доступа по ftp>,
  "quota" : <требуемое место>,
  "ServerId" : <id выбранного сервера>
}

/def/service/user - информация о пользователе
POST
content-type: application/json
raw body:
{
  "id" : <id пользователя>,
  "sessionToken" : <session token>
}

/def/service/serverRanks - получить список серверверов, отсортированный по значению метрики
POST
content-type: application/json
raw body:
{
  "sessionToken" : <session token>,
  "quota" : <требуемая квота>
}

/def/service/servers - список серверов
POST
content-type: application/json
raw body:
{
  "sessionToken" : <session token>
}

/def/service/server - информация о сервере
POST
content-type: application/json
raw body:
{
  "sessionToken" : <session token>,
  "serverId" : <id сервера>
}

/def/service/users - список пользователей
POST
content-type: application/json
raw body:
{
  "sessionToken" : <session token>
}
