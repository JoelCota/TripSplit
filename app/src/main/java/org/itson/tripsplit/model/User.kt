package org.itson.tripsplit.model

data class User(
    var uid : String? = null,
    var email : String? = null,
    var name : String? = null
) {
    override fun toString(): String {
        return uid+"\t"+email+"\t"+name
    }
}
