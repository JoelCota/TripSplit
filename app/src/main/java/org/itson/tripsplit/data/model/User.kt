package org.itson.tripsplit.data.model

data class User(
    var uid : String? = null,
    var email : String? = null,
    var name : String? = null
) {
    override fun toString(): String {
        return uid+"\t"+email+"\t"+name
    }
}
