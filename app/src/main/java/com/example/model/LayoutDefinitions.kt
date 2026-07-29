package com.example.model

object LayoutDefinitions {

    val SHINA_DEFAULT_ROWS: List<List<KeyModel>> = listOf(
        listOf(
            KeyModel("ا", longPressVariants = listOf("آ", "إ", "أ", "ٱ")),
            KeyModel("ب", longPressVariants = listOf("پ", "ٻ")),
            KeyModel("پ", longPressVariants = listOf("پھ")),
            KeyModel("ت", longPressVariants = listOf("ٹ", "ٿ", "ۃ")),
            KeyModel("ٹ", longPressVariants = listOf("ٹھ")),
            KeyModel("ث"),
            KeyModel("ج", longPressVariants = listOf("چ", "ڃ")),
            KeyModel("چ", longPressVariants = listOf("ڇ", "څ"))
        ),
        listOf(
            KeyModel("ح"),
            KeyModel("خ"),
            KeyModel("د", longPressVariants = listOf("ڈ", "ذ")),
            KeyModel("ڈ", longPressVariants = listOf("ڈھ")),
            KeyModel("ذ"),
            KeyModel("ر", longPressVariants = listOf("ڑ", "ڙ")),
            KeyModel("ڑ", longPressVariants = listOf("ڙ")),
            KeyModel("ز", longPressVariants = listOf("ژ", "ݜ"))
        ),
        listOf(
            KeyModel("ژ"),
            KeyModel("س", longPressVariants = listOf("ش", "ݜ")),
            KeyModel("ش", longPressVariants = listOf("ݜ")),
            KeyModel("ص"),
            KeyModel("ض"),
            KeyModel("ط"),
            KeyModel("ظ"),
            KeyModel("ع", longPressVariants = listOf("غ"))
        ),
        listOf(
            KeyModel("غ"),
            KeyModel("ف"),
            KeyModel("ق"),
            KeyModel("ک", longPressVariants = listOf("گ", "کھ")),
            KeyModel("گ", longPressVariants = listOf("گھ")),
            KeyModel("ل"),
            KeyModel("م", longPressVariants = listOf("ݬ")),
            KeyModel("ن", longPressVariants = listOf("ں", "ݩ"))
        )
    )

    val SHINA_PLUS_ROWS: List<List<KeyModel>> = listOf(
        listOf(
            KeyModel("څ"),
            KeyModel("ڇ"),
            KeyModel("ڙ"),
            KeyModel("ݜ"),
            KeyModel("ݿ"),
            KeyModel("ݩ"),
            KeyModel("ݬ")
        ),
        listOf(
            KeyModel("پھ"),
            KeyModel("تھ"),
            KeyModel("ٹھ"),
            KeyModel("جھ"),
            KeyModel("چھ"),
            KeyModel("دھ")
        ),
        listOf(
            KeyModel("ڈھ"),
            KeyModel("کھ"),
            KeyModel("گھ"),
            KeyModel("ں"),
            KeyModel("و", longPressVariants = listOf("ؤ", "ۄ")),
            KeyModel("ہ", longPressVariants = listOf("ھ", "ۃ"))
        ),
        listOf(
            KeyModel("ھ"),
            KeyModel("ی", longPressVariants = listOf("ے", "ئ")),
            KeyModel("ے"),
            KeyModel("ء"),
            KeyModel("َ"),
            KeyModel("ِ"),
            KeyModel("ُ")
        )
    )

    val SYMBOLS_ROWS: List<List<KeyModel>> = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8").map { KeyModel(it) },
        listOf("9", "0", "،", "؛", "؟", "۔", "!").map { KeyModel(it) },
        listOf("«", "»", "(", ")", "{", "}", "[", "]").map { KeyModel(it) },
        listOf("+", "-", "*", "/", "=", "%", "@", "#", "$", "&").map { KeyModel(it) }
    )

    val ROMAN_SHINA_ROWS: List<List<KeyModel>> = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").map { KeyModel(it) },
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").map { KeyModel(it) },
        listOf("z", "x", "c", "v", "b", "n", "m", "sh").map { KeyModel(it) },
        listOf(
            KeyModel("ċ", longPressVariants = listOf("c", "č")),
            KeyModel("č", longPressVariants = listOf("ċ")),
            KeyModel("ǰ", longPressVariants = listOf("j")),
            KeyModel("ṣ", longPressVariants = listOf("s", "š")),
            KeyModel("ẓ", longPressVariants = listOf("z", "ž")),
            KeyModel("ṭ", longPressVariants = listOf("t")),
            KeyModel("ṛ", longPressVariants = listOf("r")),
            KeyModel("ŋ", longPressVariants = listOf("n")),
            KeyModel("rr", longPressVariants = listOf("r"))
        )
    )

    val EMOJI_ROWS: List<List<KeyModel>> = listOf(
        listOf("😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣").map { KeyModel(it) },
        listOf("😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰").map { KeyModel(it) },
        listOf("😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜").map { KeyModel(it) },
        listOf("🏔️", "🇵🇰", "🌸", "❤️", "👍", "🔥", "✨", "🎉").map { KeyModel(it) }
    )

    val GIF_ROWS: List<List<KeyModel>> = listOf(
        listOf("[GIF: Gilgit Valley]", "[GIF: Snow Karakoram]", "[GIF: Shina Dance]", "[GIF: Hunza Autumn]").map { KeyModel(it) },
        listOf("[GIF: Salam GB]", "[GIF: Karakoram Sunset]", "[GIF: Naltar Skiing]", "[GIF: Deosai Bears]").map { KeyModel(it) }
    )

    val INITIAL_SHINA_VOCABULARY = listOf(
        // Common Shina words across Gilgit-Baltistan
        "سلام", "جی", "شینا", "گلگت", "بلتستان", "آستور", "دیامر", "چلاس", "گوپس", "پنیال",
        "دنور", "کوہستان", "کرم", "مزخ", "جو", "میو", "تنو", "کاش", "بش", "بای",
        "صار", "گون", "روئی", "دار", "ہن", "ہنیس", "انو", "تری", "پوس", "بٹس",
        "مست", "شیش", "مو", "تو", "رو", "سی", "اسوس", "تسو", "تریخ", "دنو",
        "کھاو", "پیو", "بوجو", "ایو", "رائینگ", "کھائینگ", "پوئینگ", "ہنبو", "بسو",
        "شکریلا", "میلا", "جاگیر", "واائی", "شینایو", "زبان", "پاکستان", "پہچان"
    )
}
