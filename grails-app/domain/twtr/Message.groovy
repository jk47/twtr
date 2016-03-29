package twtr

class Message {
    String content
    Date dateCreated

    static constraints = {
        content blank: false, size: 1..40
    }
    static belongsTo = [ account : Account ]

}
