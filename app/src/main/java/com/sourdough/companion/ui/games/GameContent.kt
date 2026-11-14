package com.sourdough.companion.ui.games

enum class TriviaCategory(val label: String) { BAKING("Baking"), HISTORY("History") }

data class TriviaQuestion(
    val question: String,
    val answers: List<String>,
    val correct: Int,
    val explanation: String,
    val category: TriviaCategory = TriviaCategory.BAKING,
)

data class ScrambleWord(val word: String, val clue: String)

/** Bread and sourdough trivia. The correct answer is always [TriviaQuestion.correct]; the game shuffles them. */
object Trivia {
    private val baking = listOf(
        TriviaQuestion(
            "In baker's percentages, which ingredient is always 100%?",
            listOf("Flour", "Water", "Starter", "Salt"), 0,
            "Everything else is measured as a percentage of the flour's weight.",
        ),
        TriviaQuestion(
            "A dough with 500 g flour and 375 g water has what hydration?",
            listOf("75%", "65%", "80%", "57%"), 0,
            "375 ÷ 500 = 0.75, so 75% hydration.",
        ),
        TriviaQuestion(
            "What is an autolyse?",
            listOf(
                "Resting flour and water before adding salt and levain",
                "Baking with the lid on",
                "Feeding the starter twice",
                "Chilling the shaped loaf",
            ),
            0,
            "The rest lets flour hydrate and gluten start forming on its own. French baker Raymond Calvel popularised it.",
        ),
        TriviaQuestion(
            "What is a levain?",
            listOf(
                "A portion of starter built up for a specific dough",
                "A type of French oven",
                "A proofing basket",
                "A scoring blade",
            ),
            0,
            "You take some starter, feed it, and use it at its peak to leaven one batch.",
        ),
        TriviaQuestion(
            "Which microbes give sourdough most of its tang?",
            listOf("Lactic acid bacteria", "Commercial yeast", "Mould spores", "E. coli"), 0,
            "Lactic acid bacteria in the starter produce lactic and acetic acids.",
        ),
        TriviaQuestion(
            "Which acid gives a sharper, vinegar-like sourness?",
            listOf("Acetic acid", "Lactic acid", "Citric acid", "Malic acid"), 0,
            "Acetic acid is the sharp one; lactic acid is milder and more yogurt-like.",
        ),
        TriviaQuestion(
            "The bacterium named after a famous sourdough city is…",
            listOf("sanfranciscensis", "parisiensis", "newyorkensis", "romanus"), 0,
            "Lactobacillus (now Fructilactobacillus) sanfranciscensis was first described from San Francisco sourdough.",
        ),
        TriviaQuestion(
            "Gold rush prospectors in Alaska and the Yukon were nicknamed…",
            listOf("Sourdoughs", "Bannetons", "Crumbs", "Levains"), 0,
            "They carried starter with them to bake bread on the trail.",
        ),
        TriviaQuestion(
            "What's the dark liquid that forms on a hungry starter called?",
            listOf("Hooch", "Whey", "Slurry", "Brine"), 0,
            "It's mostly alcohol from fermentation: a sign the starter needs feeding.",
        ),
        TriviaQuestion(
            "A 1:5:5 feed means…",
            listOf(
                "1 part starter, 5 parts flour, 5 parts water",
                "1 g salt per 5 g flour",
                "Feed once every 5 hours for 5 days",
                "1 part water, 5 parts flour, 5 parts starter",
            ),
            0,
            "Ratios are starter : flour : water, by weight. A bigger feed takes longer to peak.",
        ),
        TriviaQuestion(
            "Gluten forms when water meets which two wheat proteins?",
            listOf("Glutenin and gliadin", "Casein and whey", "Albumin and keratin", "Amylase and pectin"), 0,
            "Glutenin gives elasticity and gliadin gives extensibility.",
        ),
        TriviaQuestion(
            "Why do bakers do stretch and folds?",
            listOf(
                "To build gluten strength without kneading",
                "To cool the dough down",
                "To add more salt",
                "To stop fermentation",
            ),
            0,
            "Each set lines up and strengthens the gluten network.",
        ),
        TriviaQuestion(
            "Which flour usually has the most protein?",
            listOf("Bread flour", "Cake flour", "Pastry flour", "All-purpose flour"), 0,
            "More protein means more gluten, which suits chewy, open loaves.",
        ),
        TriviaQuestion(
            "How much salt is typical in a lean sourdough?",
            listOf("About 2% of the flour", "About 10% of the flour", "About 0.2% of the flour", "None"), 0,
            "Around 2% seasons the bread and tightens the gluten.",
        ),
        TriviaQuestion(
            "What is bulk fermentation?",
            listOf(
                "The first rise, after mixing and before shaping",
                "Feeding a large starter",
                "Baking several loaves at once",
                "The final proof in the fridge",
            ),
            0,
            "It's where most of the flavour and structure develop.",
        ),
        TriviaQuestion(
            "What's a banneton used for?",
            listOf("Holding shaped dough while it proofs", "Scoring the loaf", "Storing flour", "Measuring hydration"), 0,
            "The basket supports the shape and leaves those flour rings.",
        ),
        TriviaQuestion(
            "What is a lame?",
            listOf("A blade for scoring dough", "A lazy starter", "A loaf that didn't rise", "A linen cloth"), 0,
            "Usually a razor blade on a handle.",
        ),
        TriviaQuestion(
            "What does a couche do?",
            listOf(
                "Supports baguettes while they proof",
                "Covers the starter jar",
                "Measures dough temperature",
                "Holds the bread while it cools",
            ),
            0,
            "It's a heavy floured linen cloth, pleated between the loaves.",
        ),
        TriviaQuestion(
            "Why do bakers score the loaf before baking?",
            listOf(
                "To control where it expands in the oven",
                "To let the salt out",
                "To check if it's cooked",
                "To stop it browning",
            ),
            0,
            "Without a score the loaf bursts wherever its skin is weakest.",
        ),
        TriviaQuestion(
            "The raised flap along a well-opened score is called the…",
            listOf("Ear", "Lip", "Crown", "Fin"), 0,
            "A good ear comes from a shallow, angled cut and strong oven spring.",
        ),
        TriviaQuestion(
            "Why bake with the Dutch oven lid on at first?",
            listOf(
                "It traps steam so the crust stays soft and the loaf can rise",
                "It stops the bread burning on the bottom",
                "It keeps the oven door cool",
                "It makes the crumb denser",
            ),
            0,
            "Lid off later lets the crust dry out and brown.",
        ),
        TriviaQuestion(
            "The quick rise in the first minutes of baking is called…",
            listOf("Oven spring", "Bulk", "Bloom", "Retard"), 0,
            "Gas expands and the last burst of fermentation happens as the dough heats.",
        ),
        TriviaQuestion(
            "Which reaction gives the crust its brown colour and toasty flavour?",
            listOf("Maillard reaction", "Fermentation", "Oxidation", "Gelatinisation"), 0,
            "Amino acids and sugars react at high heat; caramelisation helps too.",
        ),
        TriviaQuestion(
            "Roughly what internal temperature means a lean loaf is baked?",
            listOf("About 96 °C (205 °F)", "About 60 °C (140 °F)", "About 75 °C (167 °F)", "About 120 °C (248 °F)"), 0,
            "Lean loaves are usually done around 93–99 °C inside.",
        ),
        TriviaQuestion(
            "Why let bread cool before slicing?",
            listOf(
                "The crumb is still setting from the steam inside",
                "Warm bread is bad for you",
                "So the crust gets softer",
                "To let the salt dissolve",
            ),
            0,
            "Cut it hot and the inside turns gummy.",
        ),
        TriviaQuestion(
            "In the poke test, a ready-to-bake dough…",
            listOf(
                "Springs back slowly and leaves a small dent",
                "Springs back instantly",
                "Never springs back",
                "Bursts open",
            ),
            0,
            "Instant spring-back means underproofed; no spring-back means overproofed.",
        ),
        TriviaQuestion(
            "What does a cold retard in the fridge do?",
            listOf(
                "Slows fermentation, deepens flavour and makes scoring easier",
                "Kills the yeast",
                "Removes all sourness",
                "Replaces the bulk ferment",
            ),
            0,
            "It also lets you bake on your own schedule.",
        ),
        TriviaQuestion(
            "Which is NOT in a basic sourdough loaf?",
            listOf("Commercial yeast", "Flour", "Water", "Salt"), 0,
            "The starter's wild yeast does all the rising.",
        ),
        TriviaQuestion(
            "Why are 100% rye loaves dense?",
            listOf(
                "Rye forms little gluten",
                "Rye has no starch",
                "Rye can't ferment",
                "Rye is always baked cold",
            ),
            0,
            "Rye's structure comes mostly from gummy pentosans rather than gluten.",
        ),
        TriviaQuestion(
            "\"Crumb\" means…",
            listOf("The inside texture of the bread", "A small burnt bit", "The bottom crust", "Leftover flour"), 0,
            "Open, closed, tight or custardy: bakers judge a loaf by its crumb.",
        ),
        TriviaQuestion(
            "\"Pain de campagne\" is French for…",
            listOf("Country bread", "Champagne bread", "Morning bread", "Bread of the fields"), 0,
            "A rustic loaf, often with some whole wheat or rye.",
        ),
        TriviaQuestion(
            "\"Ciabatta\" means what in Italian?",
            listOf("Slipper", "Pillow", "Brick", "Cloud"), 0,
            "Named for its flat, wide, slipper-like shape.",
        ),
    )

    private fun historyQ(question: String, answers: List<String>, explanation: String) =
        TriviaQuestion(question, answers, 0, explanation, TriviaCategory.HISTORY)

    /** Sourdough through the ages. The first answer is the correct one. */
    private val history = listOf(
        historyQ(
            "The oldest known bread, about 14,400 years old, was found in which country?",
            listOf("Jordan", "Egypt", "France", "China"),
            "Charred crumbs of a flatbread made by Natufian hunter-gatherers turned up at a site called Shubayqa 1, millennia before farming.",
        ),
        historyQ(
            "Which ancient civilisation is most often credited with the first risen, leavened bread?",
            listOf("Ancient Egypt", "Ancient Greece", "The Aztecs", "The Vikings"),
            "Egyptian bakers worked out that dough left to sit would ferment and rise, the first sourdough.",
        ),
        historyQ(
            "Workers who built the Egyptian pyramids were paid partly in…",
            listOf("Bread and beer", "Salt and olives", "Gold rings", "Wool"),
            "Daily rations of bread and beer were a staple wage, and both relied on the same wild yeasts.",
        ),
        historyQ(
            "Which Roman writer described Gauls and Iberians using beer froth to leaven bread?",
            listOf("Pliny the Elder", "Julius Caesar", "Cicero", "Virgil"),
            "In his Natural History he noted the froth made their bread lighter than other peoples'.",
        ),
        historyQ(
            "In medieval Europe, a \"trencher\" was…",
            listOf("A slab of stale bread used as a plate", "A bread knife", "A baker's apprentice", "A bread oven"),
            "After the meal the gravy-soaked trencher might be eaten or given to the poor or the dogs.",
        ),
        historyQ(
            "The \"baker's dozen\" of 13 is thought to come from bakers avoiding penalties under which English law?",
            listOf("The Assize of Bread and Ale", "Magna Carta", "The Corn Laws", "The Poor Law"),
            "From the 1260s the Assize fixed bread weights and prices, so an extra loaf protected bakers from being fined for short weight.",
        ),
        historyQ(
            "Before baker's yeast was sold, many European bakers leavened bread with sourdough or with…",
            listOf("Barm, the yeasty foam from brewing", "Baking soda", "Egg whites", "Vinegar"),
            "Bakeries and breweries often sat side by side, swapping barm back and forth.",
        ),
        historyQ(
            "Which scientist showed in the 1850s and 1860s that fermentation is caused by living yeast?",
            listOf("Louis Pasteur", "Isaac Newton", "Charles Darwin", "Marie Curie"),
            "Until then nobody knew for sure what made dough rise.",
        ),
        historyQ(
            "Commercial baker's yeast took off in the US after which brothers began selling it in 1868?",
            listOf("The Fleischmanns", "The Wright brothers", "The Kelloggs", "The Pillsburys"),
            "Reliable packaged yeast made baking faster, and sourdough became less common at home for a century.",
        ),
        historyQ(
            "San Francisco's Boudin Bakery says its sourdough mother dough dates back to…",
            listOf("1849", "1906", "1776", "1950"),
            "Isidore Boudin started baking during the California Gold Rush, and the bakery says it has kept feeding the same starter ever since.",
        ),
        historyQ(
            "In the Yukon during the Klondike Gold Rush, a \"sourdough\" was someone who…",
            listOf("Had lived through a whole northern winter", "Had just arrived", "Ran a bakery", "Had struck gold"),
            "Newcomers were called cheechakos; you earned \"sourdough\" by surviving a winter, starter jar and all.",
        ),
        historyQ(
            "The San Francisco sourdough bacterium was formally named in which decade?",
            listOf("The 1970s", "The 1850s", "The 1920s", "The 2000s"),
            "USDA scientists described it in 1971 while working out why San Francisco sourdough tasted the way it did.",
        ),
        historyQ(
            "Why did rye-growing regions like Germany and Scandinavia rely on sourdough?",
            listOf(
                "Its acidity stops rye's enzymes from turning the crumb gummy",
                "Rye can't hold water",
                "Commercial yeast was banned there",
                "Sourdough made the bread sweeter",
            ),
            "Rye flour is rich in starch-eating enzymes; the acid in sourdough slows them so the loaf sets properly.",
        ),
        historyQ(
            "Panettone, the Italian Christmas bread, is traditionally leavened with…",
            listOf("A sweet sourdough starter (lievito madre)", "Beer", "Baking powder", "Only eggs"),
            "Its tall, airy crumb takes days of careful fermentation with a stiff starter.",
        ),
        historyQ(
            "The Puratos \"Sourdough Library\", which keeps starters from around the world, is in which country?",
            listOf("Belgium", "Japan", "Canada", "Brazil"),
            "Opened in 2013, it stores over a hundred starters as a back-up of bread biodiversity.",
        ),
        historyQ(
            "Sourdough baking boomed worldwide in which year, when people were stuck at home?",
            listOf("2020", "2008", "1999", "2015"),
            "During COVID-19 lockdowns flour and yeast sold out in many countries, and a lot of people grew their first starter.",
        ),
    )

    val questions = baking + history + TriviaBank.baking + TriviaBank.history

    fun questions(category: TriviaCategory?): List<TriviaQuestion> =
        if (category == null) questions else questions.filter { it.category == category }
}

object Scramble {
    val words = listOf(
        ScrambleWord("LEVAIN", "Starter built up for one dough"),
        ScrambleWord("STARTER", "The wild yeast culture you feed"),
        ScrambleWord("GLUTEN", "Stretchy protein network"),
        ScrambleWord("CRUMB", "The inside of the loaf"),
        ScrambleWord("CRUST", "The browned outside"),
        ScrambleWord("SCORE", "Cut the top before baking"),
        ScrambleWord("PROOF", "The final rise before the oven"),
        ScrambleWord("KNEAD", "Work the dough by hand"),
        ScrambleWord("FLOUR", "Ground grain"),
        ScrambleWord("HYDRATION", "Water as a percentage of flour"),
        ScrambleWord("AUTOLYSE", "Flour and water resting together"),
        ScrambleWord("BANNETON", "Proofing basket"),
        ScrambleWord("DOUGH", "Flour and water, mixed"),
        ScrambleWord("YEAST", "What makes bread rise"),
        ScrambleWord("COUCHE", "Linen cloth for baguettes"),
        ScrambleWord("BOULE", "A round loaf"),
        ScrambleWord("BATARD", "An oval loaf"),
        ScrambleWord("BAGUETTE", "Long, thin French loaf"),
        ScrambleWord("SPELT", "An ancient kind of wheat"),
        ScrambleWord("HOOCH", "Liquid on a hungry starter"),
        ScrambleWord("FOCACCIA", "Flat, oily Italian bread"),
        ScrambleWord("CIABATTA", "Italian \"slipper\" bread"),
        ScrambleWord("STEAM", "Keeps the crust soft early in the bake"),
        ScrambleWord("RETARD", "A slow, cold rest in the fridge"),
        ScrambleWord("OVEN", "Where the magic happens"),
    )
}
