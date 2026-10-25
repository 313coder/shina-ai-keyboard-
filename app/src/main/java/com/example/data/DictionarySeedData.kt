package com.example.data

/**
 * Baseline curated Shina Dictionary dataset.
 * Contains authentic Shina words with Roman Shina transliteration, parts of speech,
 * Urdu & English definitions, synonyms (مترادفات), and example sentences.
 */
object DictionarySeedData {

    val INITIAL_ENTRIES: List<ShinaDictionaryEntryEntity> = listOf(
        ShinaDictionaryEntryEntity(
            wordShina = "سلام",
            romanized = "Salam",
            partOfSpeech = "Greeting (تحیہ)",
            definitionUrdu = "السلام علیکم / خوش آمدید / امن و سلامتی",
            definitionEnglish = "Hello / Peace be upon you / Greetings",
            synonyms = "جو, تسلیمات, بخیر",
            exampleSentenceShina = "سلام! تو کیچو ہنو؟",
            exampleTranslation = "Hello! How are you?",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "جو",
            romanized = "Jo",
            partOfSpeech = "Greeting & Affirmation (ادب و تحسین)",
            definitionUrdu = "جی ہاں / باادب سلام / محترم تحسین",
            definitionEnglish = "Yes / Respectful greeting / Acknowledgment",
            synonyms = "سلام, اوا, تسلیم",
            exampleSentenceShina = "جو بابو جی! بخیر ایلیت۔",
            exampleTranslation = "Greetings respected elder! Welcome.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "کرم",
            romanized = "Karam",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "مہربانی / احسان / شفقت / فضل",
            definitionEnglish = "Kindness / Favor / Grace / Generosity",
            synonyms = "احسان, شفقت, مہربانی, نوازش",
            exampleSentenceShina = "توئے بڑو کرم ہن۔",
            exampleTranslation = "It is your immense kindness.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "مزخ",
            romanized = "Mazakh",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "محبت / پیار / الفت / دلی لگاؤ",
            definitionEnglish = "Love / Affection / Fondness / Devotion",
            synonyms = "پیار, عشق, الفت, چاہت",
            exampleSentenceShina = "مئے دلس مجھ شینا زبانے مزخ ہن۔",
            exampleTranslation = "In my heart there is deep love for the Shina language.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "مست",
            romanized = "Mast",
            partOfSpeech = "Adjective (صفت)",
            definitionUrdu = "خوش / بے خود / پرمسرت / سرشار",
            definitionEnglish = "Joyful / Delighted / Carefree / High-spirited",
            synonyms = "خوش, سرشار, راضی, شادمان",
            exampleSentenceShina = "رو بیلوک خوشی مجھ مست ہن۔",
            exampleTranslation = "That boy is blissful in happiness.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "شیش",
            romanized = "Sheesh",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "سر / پہاڑ کی چوٹی / چوٹی",
            definitionEnglish = "Head / Mountain summit / Peak",
            synonyms = "کھور, چوٹی, سر, ݜیش",
            exampleSentenceShina = "راکاپوشیئے شیش سنوئے سئی ہن۔",
            exampleTranslation = "The summit of Rakaposhi is magnificent with snow.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "بوجو",
            romanized = "Bojo",
            partOfSpeech = "Verb (فعل)",
            definitionUrdu = "جانا / چلنا / روانہ ہونا",
            definitionEnglish = "To go / To leave / To depart",
            synonyms = "نکھسو, پٹھیجو, رانو",
            exampleSentenceShina = "مو گلگت بوجو ہنس۔",
            exampleTranslation = "I am going to Gilgit.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "ایو",
            romanized = "Ayo",
            partOfSpeech = "Verb (فعل)",
            definitionUrdu = "آنا / تشریف لانا / پہنچنا",
            definitionEnglish = "To come / To arrive / To visit",
            synonyms = "والیجو, حاضر بیو, پاو",
            exampleSentenceShina = "تو کہڑے مئے گھر ایو ہنو؟",
            exampleTranslation = "When are you coming to my home?",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "کھاو",
            romanized = "Khao",
            partOfSpeech = "Verb (فعل)",
            definitionUrdu = "کھانا / تناول کرنا",
            definitionEnglish = "To eat / To consume food",
            synonyms = "کھائینگ, نوش تھو",
            exampleSentenceShina = "تاتے تاتے روئی کھاو۔",
            exampleTranslation = "Eat warm, fresh bread.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "پیو",
            romanized = "Piyo",
            partOfSpeech = "Verb (فعل)",
            definitionUrdu = "پینا / نوش کرنا",
            definitionEnglish = "To drink / To sip",
            synonyms = "پوئینگ, چھیک تھو",
            exampleSentenceShina = "چشمئے تھڈو واائی پیو۔",
            exampleTranslation = "Drink cold spring water.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "روئی",
            romanized = "Rooi",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "روٹی / اناج کی خوراک",
            definitionEnglish = "Bread / Flatbread / Sustenance",
            synonyms = "کھانو, غذا, خوراک",
            exampleSentenceShina = "ملشئے ہتھے روئی لذیذ ہن۔",
            exampleTranslation = "Mother's handmade bread is delicious.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "واائی",
            romanized = "Waai",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "پانی / آب / چشمہ",
            definitionEnglish = "Water / Spring / Stream water",
            synonyms = "آب, چشمہ, جل",
            exampleSentenceShina = "نلتر مجھ صاف شیشہ واائی بوجین۔",
            exampleTranslation = "Crystal clear water flows in Naltar.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "دار",
            romanized = "Daar",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "دروازہ / مدخل / راہ",
            definitionEnglish = "Door / Gateway / Entrance",
            synonyms = "دروازہ, پٹ, باب",
            exampleSentenceShina = "دار بو کھولو۔",
            exampleTranslation = "Open the door please.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "گون",
            romanized = "Goon",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "رات / اندھیرا / شب",
            definitionEnglish = "Night / Nighttime / Darkness",
            synonyms = "رات, تریخی, شب",
            exampleSentenceShina = "گون تری بیلو تارے نکھتین۔",
            exampleTranslation = "The night fell and stars appeared.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "صار",
            romanized = "Saar",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "صبح / فجر / سویرا",
            definitionEnglish = "Morning / Dawn / Daybreak",
            synonyms = "سویرا, فجر, دی, صبح",
            exampleSentenceShina = "صار بیلو سورج نکھتو۔",
            exampleTranslation = "Morning broke and the sun rose.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "بش",
            romanized = "Bash",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "بارش / برسات / مینہ",
            definitionEnglish = "Rain / Rainfall / Shower",
            synonyms = "بارش, مینہ, برسات, جھڑی",
            exampleSentenceShina = "آستور مجھ تیز بش ولو۔",
            exampleTranslation = "Heavy rain fell in Astore.",
            dialect = "Astore"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "کاش",
            romanized = "Kaash",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "گھاس / چراگاہ کا سبزہ",
            definitionEnglish = "Grass / Green fodder / Pasture pasture",
            synonyms = "سبزہ, گھاس, ہریر",
            exampleSentenceShina = "ڈئیس مجھ ہریرو کاش بئی ہن۔",
            exampleTranslation = "There is green lush grass in the highland pasture.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "پوس",
            romanized = "Pus",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "پھول / شگوفہ / بہار کا پھول",
            definitionEnglish = "Flower / Blossom / Bloom",
            synonyms = "گل, کلی, شگوفہ",
            exampleSentenceShina = "بہار مجھ رنگ برنگ پوس نکھتین۔",
            exampleTranslation = "Colorful flowers bloom in spring.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "بٹس",
            romanized = "Bats",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "پتھر / چٹان / سنگ",
            definitionEnglish = "Stone / Rock / Boulder",
            synonyms = "چٹان, سنگ, کٹی",
            exampleSentenceShina = "پہاڑو سئی بٹس راکھسو۔",
            exampleTranslation = "The boulder fell from the mountain.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "سنو",
            romanized = "Sinu",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "برف / برفباری",
            definitionEnglish = "Snow / Snowfall",
            synonyms = "برف, یم, پالا",
            exampleSentenceShina = "پہاڑوں کچ سفید سنو ولو۔",
            exampleTranslation = "White snow fell upon the mountains.",
            dialect = "Diamer"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "شکریلا",
            romanized = "Shukrila",
            partOfSpeech = "Interjection (اظہار تشکر)",
            definitionUrdu = "شکریہ / احسان مند / نوازش",
            definitionEnglish = "Thank you / Many thanks / Grateful",
            synonyms = "مہربانی, احسان, شکریہ",
            exampleSentenceShina = "توئے مددے بڑو شکریلا!",
            exampleTranslation = "Thank you very much for your help!",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "سئی",
            romanized = "Seei",
            partOfSpeech = "Adjective (صفت)",
            definitionUrdu = "اچھا / خوبصورت / عمدہ / نیک",
            definitionEnglish = "Good / Beautiful / Fine / Excellent",
            synonyms = "خوبصورت, سونو, چنگو, بہترین",
            exampleSentenceShina = "انو کتاب بیلوک سئی ہن۔",
            exampleTranslation = "This book is very good.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "سونو",
            romanized = "Sono",
            partOfSpeech = "Adjective (صفت)",
            definitionUrdu = "سونے جیسا / پیارا / قیمتی",
            definitionEnglish = "Golden / Beloved / Precious",
            synonyms = "سئی, پیارو, قیمتی, لاڈلا",
            exampleSentenceShina = "مئے سونو گلگت!",
            exampleTranslation = "My golden Gilgit!",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "بھا",
            romanized = "Bha",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "بھائی / برادر / عزیز دوست",
            definitionEnglish = "Brother / Close friend",
            synonyms = "برادر, بھائی, یار, جگر",
            exampleSentenceShina = "مئے بھا بازارو والو۔",
            exampleTranslation = "My brother returned from the market.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "سوس",
            romanized = "Soos",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "بہن / ہمشیرہ",
            definitionEnglish = "Sister / Female sibling",
            synonyms = "ہمشیرہ, بہن",
            exampleSentenceShina = "سوس اسکول مجھ اول آئی۔",
            exampleTranslation = "Sister stood first in school.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "ملش",
            romanized = "Malash",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "ماں / والدہ / اماں",
            definitionEnglish = "Mother / Mom",
            synonyms = "والدہ, اماں, ممی, آئی",
            exampleSentenceShina = "ملشئے قدمن تھلے جنت ہن۔",
            exampleTranslation = "Paradise lies under mother's feet.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "بابو",
            romanized = "Babu",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "باپ / والد / بزرگ",
            definitionEnglish = "Father / Respected elder / Dad",
            synonyms = "والد, پاپا, بزرگ",
            exampleSentenceShina = "بابو نصحیت تھین۔",
            exampleTranslation = "Father is giving wise advice.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "بیلوک",
            romanized = "Beluk",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "لڑکا / بچہ / نوجوان",
            definitionEnglish = "Boy / Child / Young boy",
            synonyms = "چھوکرو, بالک, بچہ, جواں",
            exampleSentenceShina = "بیلوک اسکول بوجو ہن۔",
            exampleTranslation = "The boy is going to school.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "مولئی",
            romanized = "Molei",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "لڑکی / بچی / دوشیزہ",
            definitionEnglish = "Girl / Young girl / Maiden",
            synonyms = "بچی, کنیا, لڑکی",
            exampleSentenceShina = "مولئی شینا گیت گائین۔",
            exampleTranslation = "The girl is singing a Shina folk song.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "تھڈو",
            romanized = "Thado",
            partOfSpeech = "Adjective (صفت)",
            definitionUrdu = "ٹھنڈا / سرد / خنک",
            definitionEnglish = "Cold / Chilly / Cool",
            synonyms = "سرد, برفیل, ٹھنڈا, خنک",
            exampleSentenceShina = "شامے تھڈی ہوا والین۔",
            exampleTranslation = "In the evening, cold breeze blows.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "تاتو",
            romanized = "Tato",
            partOfSpeech = "Adjective (صفت)",
            definitionUrdu = "گرم / تپتا ہوا / حرارت والا",
            definitionEnglish = "Hot / Warm / Heated",
            synonyms = "گرم, تپتا, حرارت",
            exampleSentenceShina = "تاتو چائے پیو۔",
            exampleTranslation = "Drink warm tea.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "بڑو",
            romanized = "Baro",
            partOfSpeech = "Adjective (صفت)",
            definitionUrdu = "بڑا / عظیم / بزرگ / وسیع",
            definitionEnglish = "Big / Great / Large / Senior",
            synonyms = "عظیم, موٹو, بزرگ, وسیع",
            exampleSentenceShina = "نانگا پربت بڑو پہاڑ ہن۔",
            exampleTranslation = "Nanga Parbat is a magnificent huge mountain.",
            dialect = "Diamer"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "چونو",
            romanized = "Chuno",
            partOfSpeech = "Adjective (صفت)",
            definitionUrdu = "چھوٹا / ننھا / مختصر",
            definitionEnglish = "Small / Tiny / Little / Brief",
            synonyms = "چھوٹا, ننھا, کم, مختصر",
            exampleSentenceShina = "انو چونو کتابچہ ہن۔",
            exampleTranslation = "This is a small booklet.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "کیچو",
            romanized = "Kecho",
            partOfSpeech = "Adverb (متعلق فعل)",
            definitionUrdu = "کیسے / کس طرح / کیسا حال ہے",
            definitionEnglish = "How / In what manner / In what condition",
            synonyms = "کیسے, کی ہن, کی حالت",
            exampleSentenceShina = "تو کیچو ہنو بھائی؟",
            exampleTranslation = "How are you doing brother?",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "کانے",
            romanized = "Kaane",
            partOfSpeech = "Adverb (متعلق فعل)",
            definitionUrdu = "کہاں / کدھر / کس جگہ",
            definitionEnglish = "Where / To what destination",
            synonyms = "کہاں, کس طرف, کدھر",
            exampleSentenceShina = "تو کانے بوجو ہنو؟",
            exampleTranslation = "Where are you heading to?",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "کہڑے",
            romanized = "Kahray",
            partOfSpeech = "Adverb (متعلق فعل)",
            definitionUrdu = "کب / کس وقت / کس دن",
            definitionEnglish = "When / At what time",
            synonyms = "کب, کس وقت, کہڑے ٹائم",
            exampleSentenceShina = "تو کہڑے واپس ایو ہنو؟",
            exampleTranslation = "When are you returning?",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "کی",
            romanized = "Ki",
            partOfSpeech = "Pronoun (ضمیر استفہام)",
            definitionUrdu = "کیا / کیا چیز",
            definitionEnglish = "What",
            synonyms = "کیا, کی شے",
            exampleSentenceShina = "توئے نام کی ہن؟",
            exampleTranslation = "What is your name?",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "ݜینا",
            romanized = "Shina",
            partOfSpeech = "Proper Noun (اسم علم)",
            definitionUrdu = "شینا زبان اور گلگت بلتستان کی مادری قوم",
            definitionEnglish = "The Shina Language and Dardic cultural identity",
            synonyms = "شینا زباں, داردی قوم",
            exampleSentenceShina = "ݜینا مئے روح کچ سمائل ہن۔",
            exampleTranslation = "Shina is infused in my soul.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "ڇئے",
            romanized = "Chhaye",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "چائے / قہوہ / روایتی نمکین چائے",
            definitionEnglish = "Tea / Traditional salted tea / Brew",
            synonyms = "چائے, گرین ٹی, قہوہ",
            exampleSentenceShina = "نمکین ڇئے پیو گرم گرم۔",
            exampleTranslation = "Drink warm salted traditional tea.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "ݨار",
            romanized = "Naar",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "پہاڑی نالی / چشمہ / آبجو",
            definitionEnglish = "Mountain stream / Water brook / Channel",
            synonyms = "نالہ, چشمہ, کُوئی, جوئے",
            exampleSentenceShina = "ݨار مجھ ٹھنڈو واائی وگین۔",
            exampleTranslation = "Cold water streams through the mountain channel.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "پہچان",
            romanized = "Pehchan",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "شناخت / وقار / قومی وقار",
            definitionEnglish = "Identity / Heritage / Recognition",
            synonyms = "شناخت, وقار, نام, اعزاز",
            exampleSentenceShina = "شینا زبان مئے پہچان ہن۔",
            exampleTranslation = "Shina language is my proud identity.",
            dialect = "Gilgit"
        ),
        ShinaDictionaryEntryEntity(
            wordShina = "جاگیر",
            romanized = "Jageer",
            partOfSpeech = "Noun (اسم)",
            definitionUrdu = "وطن / سرزمین / آبائی دیس",
            definitionEnglish = "Homeland / Native soil / Ancestral heritage",
            synonyms = "وطن, دیس, دیار, مٹی",
            exampleSentenceShina = "گلگت بلتستان مئے سونی جاگیر ہن۔",
            exampleTranslation = "Gilgit-Baltistan is my beloved homeland.",
            dialect = "Gilgit"
        )
    )
}
