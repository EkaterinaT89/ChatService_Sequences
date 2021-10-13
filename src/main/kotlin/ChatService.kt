object ChatService {

    private val chat: HashMap<Int, MutableList<Message>> = hashMapOf()

    fun addUsers(user: User): User {
        chat
            .takeIf { !(chat.containsKey(user.userId)) }
            ?.put(user.userId, mutableListOf())
        return user
    }

    fun add(user: User, message: Message): Message {
        chat
            .takeIf { chat.containsKey(user.userId) }
            ?.get(user.userId)
            ?.add(message)
        return message
    }

    fun deleteChat(userIdForDelete: Int): Boolean {
        chat.forEach {
            if (chat.containsKey(userIdForDelete)) {
                chat.remove(userIdForDelete)
                return true
            }
        }
        throw UserNotFoundException("Такого пользователя нет!")
    }

    fun changeMessage(UID: Int, messageIdForChange: Int, message2: Message): Message {
        chat.forEach {
            if (chat.containsKey(UID)) {
                val messagesFromChats = chat.getValue(UID)
                for (message in messagesFromChats) {
                    if (message.messageId == messageIdForChange) {
                        messagesFromChats.add(message2)
                        messagesFromChats.remove(message)
                        return message2
                    }
                }
            }
        }
        throw MessageNotFoundException("Такого сообщения нет")
    }

    fun deleteMessage(userId: Int, messageIdForDelete: Int): Boolean {
        chat.forEach {
            if (chat.containsKey(userId)) {
                val messagesFromChats = chat.getValue(userId)
                for (message in messagesFromChats) {
                    if (message.messageId == messageIdForDelete && messagesFromChats.isNotEmpty()) {
                        messagesFromChats.remove(message)
                        return true
                    }
                    if (messagesFromChats.isEmpty()) {
                        deleteChat(userId)
                        return true
                    } else throw MessageNotFoundException("Такого сообщения нет")
                }
            }
        }
        throw UserNotFoundException("Пользователя с таким ID нет.")
    }

    fun print() {
        chat.forEach { (key, value) ->
            if (value.isNotEmpty()) println("Чат с пользователем с ID $key: \n $value") else
                println("Чат с пользователем с ID $key: \n нет сообщений.")
        }
    }

    fun getUnreadMessageCount(userId: Int): Int {
        (chat[userId] ?: throw UserNotFoundException("Пользователя с таким ID нет."))
        val unreadMessages = chat
            .getValue(userId)
            .filter { message -> return@filter !message.readStatus }
            .size
        println("Непрочитанных сообщений $unreadMessages")
        return unreadMessages
    }

fun getMessages(userId: Int): List<Message> {
    return (chat[userId] ?: throw UserNotFoundException("Пользователя с таким ID нет."))
        .ifEmptyAction { println("В чате с пользователем $userId сообщений нет!") }
        .ifNotEmptyAction { println("Чат с пользователем с ID $userId \n $it") }
}

    fun <T> List<T>.ifEmptyAction(lambda: (List<T>) -> Unit): List<T> {
    return ifEmpty { lambda(this); this }
}

    fun <T> T.ifNotEmptyAction(lambda: (T) -> Unit): T where T : Collection<*> {
    return if (isNotEmpty()) {
        lambda(this)
        this
    } else {
        this
    }
}

    fun getMessagesWithId(userId: Int, messageId: Int, countOfMessages: Int) {
        (chat[userId] ?: throw UserNotFoundException("Пользователя с таким ID нет."))
        chat
            .getValue(userId)
            .filter { message -> message.messageId > messageId }
            .take(countOfMessages)
            .ifEmptyAction { println("Нет сообщений, удовлетворяющих условиям!") }
            .ifNotEmptyAction { println("Чат с пользователем с ID $userId \n $it") }

    }

}



//Функция ifEmpty из стандартной библиотеки вам не подойдет,
// потому что она возвращает значение по умолчанию при пустой коллекции.
// В текущем варианте у вас внутри делается println, он возвращает Unit.
// Поэтому тип переменной messages получается Any.
//Мне кажется, что логически нам нужно вывести одно сообщение на экран,
// если коллекция сообщений пустая и другое сообщение, если коллекция не пустая.
// Для этого можем написать свои функции-расширения для списка
// (я предполагаю, что ваша функция getValue от chat возвращает List<Message>). Тогда можем сделать функцию-расширение:
//fun <T> List<T>.ifEmptyAction(lambda: (List<T>) -> Unit): List<T> {
//    return ifEmpty { lambda(this); this }
//}
//Это можно записать чуть короче, чтобы два раза не писать List<T>:
//fun <T> T.ifEmptyAction(lambda: (T) -> Unit): T where T : Collection<*> {
//    return ifEmpty { lambda(this); this }
//}
//То есть, мы будем вызывать лямбда-выражение, если коллекция пустая, а в качестве результата нашей функции-расширения вернется исходный объект коллекции.
//Аналогично объявим еще одну функцию:
//fun <T> T.ifNotEmptyAction(lambda: (T) -> Unit): T where T : Collection<*> {
//    return if (isNotEmpty()) {
//        lambda(this)
//        this
//    } else {
//        this
//    }
//}
//Она будет вызывать lambda, если коллекция не пустая. Тогда вызов наших функций будет таким:
//fun getMessages(userId: Int): List<Message> {
//    return chat
//        .getValue(userId)
//        .ifEmptyAction { println("В чате с пользователем $userId сообщений нет!") }
//        .ifNotEmptyAction { println("Чат с пользователем с ID $userId \n $it") }
//}
//А исключения в этой функции нет смысла выдавать. Лучше выкидывать его в функции getValue, потому что именно она получается сообщения для пользователя. Соответственно, и она же должна проверять плохие случаи.
//
//Я подумал, что у вас chat - это отдельный класс. Видимо он у вас хэш-таблица. Тогда исключение можно выдавать и здесь. Можно через Элвиса написать:
//fun getMessages(userId: Int): List<Message> {
//    return (chat[userId] ?: throw RuntimeException())
//        .ifEmptyAction { println("В чате с пользователем $userId сообщений нет!") }
//        .ifNotEmptyAction { println("Чат с пользователем с ID $userId \n $it") }
//}