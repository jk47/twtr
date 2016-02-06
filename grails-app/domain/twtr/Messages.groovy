package twtr

class Message {
    String content
    Date dateCreated

    static constraints = {
        content blank: false
    }
    static belongsTo = [ account : Account ]

}
