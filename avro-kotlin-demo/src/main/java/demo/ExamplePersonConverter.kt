package demo

import main.KotlinAvroConverter


class ExamplePersonKotlinAvroConverter : KotlinAvroConverter<ExamplePersonKt, ExamplePerson> {
    override fun toAvroSpecificRecord(examplePersonKt: ExamplePersonKt): ExamplePerson {
        return ExamplePerson(examplePersonKt.id, examplePersonKt.username)
    }
    override fun fromAvroSpecificRecord(examplePerson: ExamplePerson): demo.ExamplePersonKt {
        return demo.ExamplePersonKt(id = examplePerson.id, username = if (examplePerson.username == null) null else examplePerson.username)
    }
}

