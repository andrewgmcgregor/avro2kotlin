package demo

data class ExamplePersonKt(
    val id: Int,
    val username: String?) {

    fun toJava() = ExamplePerson(
        id,
        username,
        null,
        null)

    companion object ExamplePersonData {
        fun toKotlin(examplePerson: ExamplePerson) = ExamplePersonKt(
                id = examplePerson.getId(),
                username = examplePerson.getUsername()?.toString())
    }
}

