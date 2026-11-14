package com.sourdough.companion.ui.games

/**
 * The bulk of the trivia bank: bread and sourdough history through the ages, plus extra baking
 * questions. The right answer is always listed first; the game shuffles them.
 */
internal object TriviaBank {
    private fun hist(question: String, right: String, wrong1: String, wrong2: String, wrong3: String, why: String) =
        TriviaQuestion(question, listOf(right, wrong1, wrong2, wrong3), 0, why, TriviaCategory.HISTORY)

    private fun bake(question: String, right: String, wrong1: String, wrong2: String, wrong3: String, why: String) =
        TriviaQuestion(question, listOf(right, wrong1, wrong2, wrong3), 0, why, TriviaCategory.BAKING)

    val history = listOf(
        // --- The ancient world ---
        hist(
            "Ötzi the Iceman, frozen in the Alps about 5,300 years ago, had eaten which grain shortly before he died?",
            "Einkorn wheat", "Maize", "Rice", "Oats",
            "His last meals included einkorn, possibly as bread, along with ibex and deer meat.",
        ),
        hist(
            "Einkorn is famous for being…",
            "One of the first wheats ever domesticated", "A modern hybrid from the 1960s", "A type of rye", "A Roman bread oven",
            "It was farmed in the Fertile Crescent around 10,000 years ago.",
        ),
        hist(
            "Modern bread wheat came about when early wheat crossed with which wild plant?",
            "Goatgrass", "Oats", "Barley", "Rice",
            "A natural cross with goatgrass (Aegilops tauschii) gave bread wheat the extra genes behind its stretchy gluten.",
        ),
        hist(
            "Which wheat was the everyday grain of ancient Egyptian bakers?",
            "Emmer", "Spelt", "Durum", "Modern bread wheat",
            "Emmer, an ancient hulled wheat, was the staple of Egyptian bread and beer.",
        ),
        hist(
            "The Greek historian Herodotus was surprised that Egyptians kneaded dough with their…",
            "Feet", "Elbows", "Wooden paddles", "Knees",
            "He noted they kneaded dough with their feet but mixed clay with their hands.",
        ),
        hist(
            "The tomb of which Egyptian pharaoh has wall paintings of a royal bakery at work?",
            "Ramesses III", "Tutankhamun", "Cleopatra", "Khufu",
            "Scenes in his tomb in the Valley of the Kings show dough being kneaded and shaped loaves baked.",
        ),
        hist(
            "The 'Hymn to Ninkasi', a Sumerian song nearly 4,000 years old, is really a recipe for…",
            "Beer brewed from baked bread", "Honey cake", "Flatbread", "Bread soup",
            "It describes making beer from bappir, a twice-baked barley bread. Bread and beer shared their yeasts from the start.",
        ),
        hist(
            "Which Greek goddess ruled over grain and the harvest?",
            "Demeter", "Athena", "Aphrodite", "Hera",
            "Bread was her gift; her Roman counterpart was Ceres.",
        ),
        hist(
            "The word 'cereal' comes from the name of which Roman goddess?",
            "Ceres", "Venus", "Juno", "Minerva",
            "Ceres was the Roman goddess of grain and farming.",
        ),
        hist(
            "Pliny the Elder described Roman leaven made from millet mixed with what?",
            "Grape must", "Seawater", "Goat's milk", "Olive oil",
            "Millet and fresh grape juice were kneaded into cakes that kept for a year as a starter.",
        ),
        hist(
            "When Vesuvius erupted in AD 79, what was found preserved in a Pompeii bakery oven?",
            "Carbonised loaves of bread", "Gold coins", "A cookbook", "Live yeast",
            "Round loaves were found baked solid in the oven, almost 2,000 years later.",
        ),
        hist(
            "Roman loaves from Pompeii were round and scored into how many wedges?",
            "Eight", "Two", "Three", "Twelve",
            "The scoring made the panis quadratus easy to tear into portions.",
        ),
        hist(
            "What turned the millstones in many Pompeii bakeries?",
            "Donkeys or mules", "Waterwheels", "Windmills", "Steam engines",
            "Animals walked in circles around big hourglass-shaped lava-stone mills.",
        ),
        hist(
            "A carbonised loaf found at Herculaneum was stamped with…",
            "A name, showing whose bread it was", "A price", "The date", "A picture of the emperor",
            "The stamp names Celer, slave of Quintus Granius Verus: a 2,000-year-old maker's mark.",
        ),
        hist(
            "In Rome, the tomb of Eurysaces is decorated with scenes of his work as a…",
            "Baker", "Gladiator", "Senator", "Sailor",
            "His tomb near the Porta Maggiore shows grain being milled, dough kneaded and loaves baked.",
        ),
        hist(
            "Roman soldiers on the march carried a long-lasting twice-baked bread called…",
            "Buccellatum", "Focaccia", "Panettone", "Ciabatta",
            "Like later ship's biscuit, it was baked hard so it would keep.",
        ),
        hist(
            "Which Roman emperor replaced the free grain handout in Rome with a daily bread ration?",
            "Aurelian", "Nero", "Augustus", "Caligula",
            "In the 270s AD, Aurelian had baked bread handed out instead of grain.",
        ),
        hist(
            "The Roman poet Juvenal mocked a public that only wanted 'bread and…'",
            "Circuses", "Wine", "Gold", "Olives",
            "'Panem et circenses': free food and entertainment to keep people quiet.",
        ),
        hist(
            "Chinese mantou, eaten for around 2,000 years, are cooked how?",
            "Steamed", "Fried", "Baked on hot stones", "Boiled in broth",
            "Leavened wheat dough steamed in baskets: soft, white and crustless.",
        ),

        // --- Faith and tradition ---
        hist(
            "During Passover, Jewish tradition avoids leavened bread and eats which flatbread?",
            "Matzah", "Naan", "Focaccia", "Pita",
            "It recalls the Exodus, when there was no time to wait for the dough to rise.",
        ),
        hist(
            "In the Exodus story, what fell from the sky to feed the Israelites in the desert?",
            "Manna", "Matzah", "Barley", "Honey cakes",
            "It's described as bread from heaven, appearing each morning.",
        ),
        hist(
            "The name Bethlehem means what in Hebrew?",
            "House of bread", "City of kings", "Field of stars", "Well of water",
            "Beit Lechem: beit is house and lechem is bread.",
        ),
        hist(
            "In the Gospels, Jesus feeds a crowd of 5,000 with five loaves and how many fish?",
            "Two", "Five", "Twelve", "Seven",
            "It's one of the few miracles told in all four Gospels.",
        ),
        hist(
            "'Give us this day our daily bread' is a line from…",
            "The Lord's Prayer", "The Magna Carta", "A bakers' guild oath", "Shakespeare's Hamlet",
            "Bread stood for everything people needed to live.",
        ),
        hist(
            "Which church traditionally uses leavened bread for communion?",
            "Eastern Orthodox", "Roman Catholic", "Both use only matzah", "Neither uses bread",
            "The Latin West uses unleavened wafers; leavened versus unleavened was one of the arguments around the 1054 split.",
        ),
        hist(
            "Challah, the braided Jewish Sabbath bread, takes its name from…",
            "A portion of dough set aside as an offering", "The town where it was invented", "The Hebrew word for braid", "A famous rabbi",
            "Separating a small piece of dough, 'taking challah', is still part of making it.",
        ),
        hist(
            "Mexico's pan de muerto is baked for which celebration?",
            "Day of the Dead", "Christmas", "Independence Day", "Easter",
            "The sweet bun is decorated with dough 'bones' and left on family altars.",
        ),
        hist(
            "Greek tsoureki, the braided Easter bread, is often decorated with…",
            "Red-dyed eggs", "Olives", "Chocolate coins", "Sesame snaps",
            "The red eggs are baked right into the braid.",
        ),
        hist(
            "Lammas Day, 1 August, takes its name from the Old English for…",
            "Loaf-mass", "Lamb feast", "Land day", "Last harvest",
            "Bread baked from the first wheat of the harvest was blessed in church.",
        ),
        hist(
            "In many Slavic countries, honoured guests are welcomed with…",
            "Bread and salt", "Wine and cheese", "Honey and milk", "Tea and cake",
            "The round loaf with a salt cellar on top is offered at the door.",
        ),
        hist(
            "In the French galette des rois, eaten at Epiphany, whoever finds the fève becomes…",
            "King or queen for the day", "The next baker", "Responsible for the washing up", "Lucky in love",
            "The fève was originally a dried bean hidden inside; now it's usually a tiny porcelain charm.",
        ),
        hist(
            "Dresden's famous Christmas stollen got butter thanks to a 1490s letter from…",
            "The Pope", "The King of France", "Martin Luther", "A Venetian merchant",
            "Butter was banned during Advent fasting until Pope Innocent VIII's 'Butter Letter' allowed it.",
        ),
        hist(
            "In 1592, a London decree restricted spiced buns, like hot cross buns, to…",
            "Good Friday, Christmas and burials", "Royal weddings only", "Sundays", "Market days",
            "Bakers caught selling them on other days could have their buns confiscated and given to the poor.",
        ),

        // --- Words and sayings ---
        hist(
            "The English word 'lord' comes from the Old English for…",
            "Loaf-keeper", "Land-owner", "Sword-bearer", "House-builder",
            "Hlāfweard: the guardian of the loaves, the one who fed the household.",
        ),
        hist(
            "And 'lady' comes from the Old English for…",
            "Loaf-kneader", "Fair maiden", "Mistress of keys", "Weaver",
            "Hlǣfdīge: the one who kneaded the bread.",
        ),
        hist(
            "'Companion' literally means someone you share what with?",
            "Bread", "A house", "A boat", "A horse",
            "From Latin com (with) and panis (bread).",
        ),
        hist(
            "The French word 'copain' (pal, buddy) comes from the same roots, meaning…",
            "Someone you share bread with", "Someone you work with", "A neighbour", "A cousin",
            "Like 'companion', it's 'with' plus 'bread'.",
        ),
        hist(
            "'Leaven' and 'levain' both come from the Latin levare, meaning…",
            "To raise", "To sour", "To wait", "To knead",
            "Leaven is whatever makes the dough rise.",
        ),
        hist(
            "The word 'pantry' originally meant a room for storing…",
            "Bread", "Pans", "Pants", "Paint",
            "It comes through French from Latin panis, bread.",
        ),
        hist(
            "The surname Baxter originally meant…",
            "A baker", "A blacksmith", "A shepherd", "A soldier",
            "Old English bæcestre was a baker, at first a woman who baked.",
        ),
        hist(
            "'Biscuit' comes from Latin words meaning…",
            "Twice cooked", "Small bread", "Sweet crumb", "Sailor's food",
            "Bis coctus: hard-baked twice so it keeps, like ship's biscuit.",
        ),
        hist(
            "French toast is called 'pain perdu' in French, which means…",
            "Lost bread", "Sweet bread", "Morning bread", "Golden bread",
            "It rescues stale bread that would otherwise be 'lost'.",
        ),
        hist(
            "The German word 'pumpernickel' is usually explained as meaning roughly…",
            "Farting goblin", "Dark forest", "Little pump", "Nicholas's bread",
            "Pumpern was slang for breaking wind and Nickel a goblin, a cheeky nod to heavy rye bread.",
        ),
        hist(
            "The sandwich is named after an 18th-century…",
            "Earl", "Baker", "Ship", "Village festival",
            "The 4th Earl of Sandwich supposedly asked for meat between bread so he could eat without leaving the table.",
        ),
        hist(
            "Raising a glass to 'toast' someone comes from the old habit of…",
            "Putting spiced toast in drinks", "Toasting bread at weddings", "Clinking bread together", "Burning bread for luck",
            "A piece of spiced toast flavoured the wine, so a 'toast' came to mean the drink and then the tribute.",
        ),
        hist(
            "The words 'dough' and 'bread' both became slang for…",
            "Money", "Friends", "Food", "Hard work",
            "Bread was so basic that it stood for a living, and so did the money that bought it.",
        ),

        // --- Medieval and early modern Europe ---
        hist(
            "In medieval England, 'manchet' was…",
            "The finest white bread, for the wealthy", "Bread for horses", "A baker's shovel", "A Lent pastry",
            "Fine-sifted wheat was costly, so white bread was a sign of status.",
        ),
        hist(
            "Medieval 'maslin' bread was made from…",
            "Wheat and rye mixed together", "Barley only", "Acorns", "Chestnuts",
            "Wheat and rye were often sown together in the same field.",
        ),
        hist(
            "Medieval 'horse bread' was made from…",
            "Beans, peas and bran", "Hay", "Oats and honey", "Leftover pastry",
            "It fed horses, and in hard times poor people too.",
        ),
        hist(
            "In medieval France, a 'four banal' was…",
            "The lord's oven, which villagers had to pay to use", "A royal bakery", "A bread tax on nobles", "A bakers' guild hall",
            "Lords held monopolies over ovens and mills and charged a fee in bread or flour.",
        ),
        hist(
            "In medieval London, a baker caught selling short-weight bread could be…",
            "Dragged through the streets on a hurdle", "Made to eat it all", "Knighted", "Sent on crusade",
            "City rules had offenders pulled through the streets with the bad loaf hung round their neck.",
        ),
        hist(
            "Ergot, a fungus on rye, caused outbreaks of which medieval illness?",
            "St Anthony's Fire", "The Black Death", "Scurvy", "Smallpox",
            "Eating bread made with ergot-infected rye caused burning pains, hallucinations and gangrene.",
        ),
        hist(
            "The first known written mention of the bagel is in 1610 rules from which city?",
            "Kraków", "New York", "Vienna", "Montreal",
            "Kraków's Jewish community rules mention bagels as a gift for women after childbirth.",
        ),
        hist(
            "Sailors on long voyages lived on 'ship's biscuit'. Its biggest problem was…",
            "Weevils getting into it", "It went mouldy in a day", "It was too soft", "It had to be kept frozen",
            "It was baked so hard it lasted for months, often tapped on the table to knock the bugs out first.",
        ),
        hist(
            "Abraham Darby's 1707 patent made which baking tool cheap enough for ordinary homes?",
            "Cast-iron pots, like the Dutch oven", "The electric oven", "The bread knife", "The banneton",
            "He sand-cast iron pots using a Dutch method, hence the 'Dutch oven'.",
        ),
        hist(
            "Turin, in northern Italy, gave the world which crunchy bread?",
            "Grissini (breadsticks)", "Ciabatta", "Focaccia", "Panettone",
            "Thin, dry grissini were made there from the 1600s.",
        ),
        hist(
            "Sardinia's paper-thin pane carasau is nicknamed…",
            "Music paper", "Glass bread", "Shepherd's sheet", "Moon bread",
            "Carta da musica: it's thin and crisp enough to read through, and it kept for months on shepherds' travels.",
        ),
        hist(
            "Swedish knäckebröd (crispbread) was traditionally made with a hole in the middle so it could be…",
            "Hung on a pole to store", "Rolled like a wheel", "Worn as a necklace", "Baked faster",
            "Baked a few times a year, it was threaded on poles under the roof to keep dry.",
        ),

        // --- Revolutions and wars ---
        hist(
            "France's 'Flour War' of 1775 was a wave of riots over…",
            "The price of grain and bread", "A ban on baguettes", "Bakers going on strike", "A flour tax on nobles",
            "Grain prices shot up after the trade was deregulated, and crowds seized grain and flour.",
        ),
        hist(
            "'Let them eat cake': who actually wrote down the story first?",
            "Jean-Jacques Rousseau", "Marie Antoinette", "Napoleon", "Voltaire",
            "Rousseau told of a 'great princess' saying 'let them eat brioche' when Marie Antoinette was still a child in Austria.",
        ),
        hist(
            "In 1793, revolutionary Paris ordered bakers to make just one bread for everyone, called…",
            "The bread of equality", "The people's baguette", "Liberty loaf", "Republic rye",
            "Rich and poor were meant to eat the same loaf, the pain d'égalité.",
        ),
        hist(
            "Protests over bread shortages in Petrograd in 1917 helped start which revolution?",
            "The Russian February Revolution", "The French Revolution", "The American Revolution", "The Industrial Revolution",
            "On International Women's Day, women queuing for bread walked out in protest; within days the Tsar had abdicated.",
        ),
        hist(
            "Union soldiers in the American Civil War nicknamed their rock-hard hardtack…",
            "Sheet-iron crackers", "Cloud cakes", "Lucky loaves", "Yankee buns",
            "It was so hard it was often soaked in coffee or water before eating.",
        ),
        hist(
            "During the First World War, Americans were asked to observe…",
            "Wheatless days", "Breadless years", "Rye-only Sundays", "Flourless Fridays",
            "Skipping wheat on set days saved grain to send to the troops and allies.",
        ),
        hist(
            "Britain's wartime 'National Loaf', from 1942, was…",
            "A greyish wholemeal-style bread", "A sweet white bread", "A rye sourdough", "Made of potatoes only",
            "Milling more of the grain into the flour stretched scarce wheat. Many people hated it.",
        ),
        hist(
            "During the worst of the Siege of Leningrad in 1941, many people's daily bread ration fell to about…",
            "125 grams", "1 kilogram", "500 grams", "Nothing at all",
            "Barely a slice or two a day, often bulked out with sawdust and other fillers.",
        ),
        hist(
            "When did Britain ration bread?",
            "1946–1948, after the Second World War", "1914–1918, in the First World War", "Throughout the 1930s", "Never",
            "It wasn't rationed during the war itself, only afterwards when wheat ran short.",
        ),
        hist(
            "In 1943 the US government briefly banned…",
            "Sliced bread", "Sourdough", "Rye flour", "Bread ovens",
            "It was a wartime saving on wax paper and steel; the ban was so unpopular it ended within weeks.",
        ),

        // --- Science and discovery ---
        hist(
            "Who first saw yeast cells, through a microscope he made himself, in 1680?",
            "Antonie van Leeuwenhoek", "Louis Pasteur", "Galileo", "Robert Hooke",
            "He looked at a drop of fermenting beer and saw tiny round globules.",
        ),
        hist(
            "The baker's yeast name Saccharomyces cerevisiae means…",
            "Sugar fungus of beer", "Sour bread maker", "Little bubble", "Wheat spirit",
            "Saccharo is sugar, myces is fungus and cerevisia is Latin for beer.",
        ),
        hist(
            "In 1883, Emil Christian Hansen at the Carlsberg brewery was the first to…",
            "Grow a pure culture from a single yeast cell", "Bottle sourdough starter", "Invent the bread slicer", "Discover gluten",
            "Pure yeast strains changed brewing, and later baking, forever.",
        ),
        hist(
            "Eduard Buchner won the 1907 Nobel Prize for showing fermentation could happen…",
            "Without living yeast cells, using their juice", "Only at night", "Only in wooden barrels", "Without sugar",
            "Ground-up yeast juice still fermented sugar, which revealed the enzymes behind it.",
        ),
        hist(
            "In 1996, baker's yeast became the first organism of its kind to have its…",
            "Whole genome sequenced", "Picture taken from space", "Own museum", "Patent",
            "It was the first eukaryote (a cell with a nucleus) to be fully sequenced.",
        ),
        hist(
            "The browning of bread crust is named after Louis-Camille Maillard, who described the reaction in…",
            "1912", "1712", "1812", "1962",
            "He was studying how amino acids and sugars react when heated.",
        ),
        hist(
            "The word 'gluten' comes from the Latin for…",
            "Glue", "Grain", "Gold", "Belly",
            "Wet gluten is sticky, stretchy and glue-like.",
        ),
        hist(
            "A Dutch doctor linked coeliac disease to wheat after noticing sick children got better when…",
            "Bread ran out in the 1944–45 famine", "They moved to the seaside", "They ate more bread", "Ovens were banned",
            "Willem Dicke saw that children improved during the Hunger Winter and relapsed when bread came back.",
        ),
        hist(
            "Norman Borlaug won the 1970 Nobel Peace Prize for breeding…",
            "High-yield, short-stemmed wheat", "Gluten-free wheat", "The first rye hybrid", "Yeast for space travel",
            "His wheat helped feed millions in the Green Revolution.",
        ),
        hist(
            "The Svalbard Global Seed Vault, opened in 2008 to protect crops like wheat, is dug into a mountain in…",
            "Norway's Arctic islands", "The Swiss Alps", "Antarctica", "The Sahara",
            "Its permafrost keeps seed samples cold even if the power fails.",
        ),
        hist(
            "The Global Sourdough Project studied about 500 starters. What did it find about where a starter comes from?",
            "It barely predicted which microbes were in it", "It decided everything", "Only US starters had yeast", "Old starters were all identical",
            "Starters from the same city could be very different, and ones from far apart could be alike.",
        ),
        hist(
            "A 2020 study found bakers' hands carry more of which microbes than other people's hands?",
            "Sourdough yeasts and lactic acid bacteria", "Mould spores", "Salmonella", "None at all",
            "Baker and starter seem to swap microbes over time.",
        ),
        hist(
            "Which yeast is typical of San Francisco sourdough starters?",
            "Kazachstania humilis", "Brewer's yeast", "Penicillium", "Baker's soda",
            "It used to be called Candida milleri, and it happily lives alongside the souring bacteria.",
        ),
        hist(
            "San Francisco's tangy sourdough was long credited to the city's fog. The 'San Francisco' bacterium has since been found…",
            "In starters all over the world", "Only on the Golden Gate Bridge", "Only in Boudin's starter", "Nowhere outside California",
            "Its name stuck, but it turns out to be a global traveller.",
        ),
        hist(
            "In 2019, an Xbox co-creator made headlines by baking bread with yeast he collected from…",
            "Ancient Egyptian pottery", "A shipwreck", "A meteorite", "The Moon rocks",
            "Seamus Blackley gathered microbes from 4,500-year-old vessels in museums. Whether they were truly ancient was never proven.",
        ),

        // --- Mills, wheat and industry ---
        hist(
            "Who brought wheat to the Americas?",
            "Spanish colonists, from the late 1400s", "The Vikings, for good", "The Aztecs", "Chinese traders",
            "Wheat arrived with Columbus's voyages as part of the Columbian Exchange.",
        ),
        hist(
            "Hardy 'Turkey Red' wheat, which made Kansas a wheat state, was brought in the 1870s by…",
            "Mennonite immigrants from Russia", "Turkish sultans", "Gold rush miners", "British sailors",
            "They carried the seed from Ukraine's steppes, and it thrived on the prairie.",
        ),
        hist(
            "Steel roller mills, which made white flour cheap for everyone, were developed in the 1800s in…",
            "Hungary", "Japan", "Egypt", "Peru",
            "Budapest's roller mills replaced millstones and spread worldwide.",
        ),
        hist(
            "In the 1830s, Sylvester Graham preached that people should eat bread made from…",
            "Whole wheat flour", "Only white flour", "Rice", "Potatoes",
            "Graham flour, and later graham crackers, are named after him.",
        ),
        hist(
            "The tall, square 'Pullman loaf' is named after…",
            "Railway dining cars", "A bread inventor", "A London bakery", "A Scottish town",
            "Its lidded pan made compact square slices that suited the small kitchens on Pullman train cars.",
        ),
        hist(
            "Sliced bread was first sold in 1928 in which US town?",
            "Chillicothe, Missouri", "New York City", "San Francisco", "Chicago",
            "Otto Rohwedder's slicing machine went into service at the Chillicothe Baking Company.",
        ),
        hist(
            "The first pop-up toaster patent, granted in 1921, was invented by Charles Strite to stop…",
            "Burnt toast in a factory canteen", "House fires", "Bread going stale", "Butter melting",
            "He was fed up with burnt toast at work.",
        ),
        hist(
            "The little plastic clip that closes many bread bags was invented in 1952 by…",
            "Floyd Paxton, using a pocket knife", "NASA engineers", "Henry Ford", "A Danish toy company",
            "He whittled the first Kwik Lok clip from plastic on a plane trip.",
        ),
        hist(
            "From 1941, much US white flour was 'enriched' with…",
            "Vitamins and iron", "Extra gluten", "Sugar", "Rye",
            "Adding back B vitamins and iron helped wipe out deficiency diseases like pellagra.",
        ),
        hist(
            "The 1961 'Chorleywood process' changed British bread by…",
            "Making dough in minutes with high-speed mixing", "Bringing back sourdough", "Banning sliced bread", "Using only rye",
            "Intense mechanical mixing replaced long fermentation, and most UK bread is still made this way.",
        ),

        // --- Breads of the world ---
        hist(
            "The croissant's ancestor, the Austrian kipferl, was brought to Paris in the 1830s by…",
            "August Zang, who opened a Viennese bakery", "Napoleon's army", "Marie Antoinette", "A Swiss chocolatier",
            "Parisian bakers later laminated the dough with butter, and the croissant was born.",
        ),
        hist(
            "Ciabatta is younger than you might think. It was created in…",
            "1982", "1582", "1882", "1932",
            "Arnaldo Cavallari invented it in the Veneto as Italy's answer to the French baguette.",
        ),
        hist(
            "In 2003, which Italian sourdough became the first bread in Europe with protected origin (PDO) status?",
            "Pane di Altamura", "Ciabatta", "Focaccia di Recco", "Panettone",
            "It's made from durum wheat semolina in Puglia using sourdough.",
        ),
        hist(
            "Under a 1993 French law, bread sold as 'pain de tradition française' must be made with…",
            "Only flour, water, salt and leaven or yeast", "At least 50% rye", "Butter", "Imported flour",
            "The Décret Pain banned additives and freezing for traditional bread.",
        ),
        hist(
            "In 2022, UNESCO added which bread's craft and culture to its intangible heritage list?",
            "The French baguette", "The bagel", "Naan", "Pumpernickel",
            "It recognised the artisanal know-how and culture of baguette bread.",
        ),
        hist(
            "In 2014, UNESCO recognised the culture of which flatbread, baked against the walls of a clay oven?",
            "Armenian lavash", "Mexican tortilla", "Indian chapati", "Italian piadina",
            "Lavash is slapped onto the hot inside wall of a tonir oven.",
        ),
        hist(
            "Naan is traditionally baked…",
            "Stuck to the inside wall of a tandoor oven", "In a frying pan", "In a steamer", "Under hot sand",
            "The blistering heat of the clay tandoor puffs it up in a minute or two.",
        ),
        hist(
            "Paris's Poilâne bakery, famous for its big round sourdough miche, opened in…",
            "1932", "1789", "1870", "1999",
            "Pierre Poilâne started it, and son Lionel made the miche world-famous.",
        ),
        hist(
            "Roughly how many bread varieties are registered in Germany?",
            "Over 3,000", "About 50", "Around 300", "Exactly 100",
            "German bread culture has also been listed as national intangible cultural heritage.",
        ),
        hist(
            "The Museum of Bread Culture, one of the first museums devoted to bread, is in which German city?",
            "Ulm", "Berlin", "Munich", "Hamburg",
            "It was founded in the 1950s and traces 6,000 years of bread.",
        ),
        hist(
            "Traditional Westphalian pumpernickel is baked how?",
            "Slowly at a low temperature for up to a day", "In 10 minutes at a very high heat", "Fried", "Not baked at all",
            "The long, steamy bake turns the rye dark and sweet with no added colouring.",
        ),
        hist(
            "Danish smørrebrød are open sandwiches built on…",
            "Dark sourdough rye bread", "Croissants", "Pita", "White sandwich loaf",
            "Rugbrød, a dense sour rye, is the base for every topping.",
        ),
        hist(
            "In 2017, Finns voted which food their national dish?",
            "Rye bread", "Reindeer stew", "Salmon soup", "Karelian pies",
            "Sour rye bread (ruisleipä) won the vote for Finland's 100th birthday.",
        ),
        hist(
            "How is Iceland's traditional 'hot spring bread' baked?",
            "Buried in the ground near hot springs", "On a volcano's lava", "In a wood oven on a boat", "Under snow",
            "Rye dough in a pot is left underground for about a day, cooked by geothermal heat.",
        ),
        hist(
            "Kvass, a traditional drink in Russia and Eastern Europe, is made by fermenting…",
            "Rye bread", "Potatoes", "Apples", "Milk",
            "It's a lightly fizzy, slightly sour drink, a close cousin of sourdough.",
        ),
        hist(
            "Ethiopia's spongy flatbread injera gets its sour taste from…",
            "Fermenting teff batter for days", "Lemon juice", "Vinegar", "Yogurt",
            "Teff batter is left to ferment like a starter before it's cooked.",
        ),
        hist(
            "Japan's famous anpan (bean-paste bun), invented in 1874, was first leavened with…",
            "A starter made from sake yeast and rice", "Beer froth", "Baking powder", "Sourdough from San Francisco",
            "The Kimuraya bakery in Tokyo used sakadane, a rice-and-koji starter.",
        ),
        hist(
            "The Japanese word for bread, 'pan', comes from which language?",
            "Portuguese", "French", "English", "Dutch",
            "Portuguese traders brought bread (pão) to Japan in the 1500s.",
        ),
        hist(
            "Corn for Mexican tortillas is first cooked and soaked in…",
            "Lime water (an alkaline solution)", "Vinegar", "Sugar syrup", "Seawater",
            "This ancient step, nixtamalisation, makes the corn more nutritious and turns it into dough.",
        ),
        hist(
            "In 2012, a UK supermarket renamed its 'tiger bread' after a three-year-old wrote in. What's it called now?",
            "Giraffe bread", "Leopard loaf", "Zebra bread", "Cheetah crust",
            "She pointed out the crackled crust looks much more like a giraffe than a tiger.",
        ),

        // --- Gold rushes, frontiers and the North ---
        hist(
            "Which came first?",
            "The California Gold Rush", "The Klondike Gold Rush", "They were the same year", "Neither involved sourdough",
            "California's began in 1848–49; the Klondike rush in the Yukon was 1896–99. Sourdough went to both.",
        ),
        hist(
            "Why did prospectors carry sourdough starter instead of store-bought yeast?",
            "A starter could be fed and kept alive for ever", "Yeast was illegal in Alaska", "Starter was lighter than flour", "It doubled as glue",
            "A little saved dough plus flour and water meant bread for the whole trip.",
        ),
        hist(
            "The 1906 San Francisco earthquake and fire threatened Boudin's mother dough. According to the bakery, who saved it?",
            "Louise Boudin, carrying it out in a bucket", "The fire brigade", "A passing miner", "It was kept in a bank vault",
            "The bakery's story is that she rescued the starter from the burning bakery.",
        ),
        hist(
            "The San Francisco 49ers' mascot is called…",
            "Sourdough Sam", "Golden Gus", "Fog Freddie", "Miner Mike",
            "A bearded gold miner, named for the city's most famous bread.",
        ),
        hist(
            "Robert W. Service, 'the Bard of the Yukon', titled his first poetry book…",
            "Songs of a Sourdough", "Bread and Gold", "Northern Loaves", "The Starter Jar",
            "Published in 1907, it includes 'The Cremation of Sam McGee'.",
        ),
        hist(
            "What is the 'Sourdough Rendezvous' in Whitehorse, Yukon?",
            "A winter festival celebrating gold rush days", "A bread-baking contest only", "A mountain pass", "A bakery chain",
            "It's held every February, with costumes, games and plenty of sourdough.",
        ),
        hist(
            "On cattle drives in the American West, chuckwagon cooks were famous for which sourdough food?",
            "Biscuits", "Bagels", "Croissants", "Pretzels",
            "A crock of starter rode on the wagon, kept warm in the cook's bedroll on cold nights.",
        ),
        hist(
            "Australia's 'damper', the bushman's bread, is traditionally cooked…",
            "In the coals of a campfire", "In a steam oven", "In a tandoor", "On a hot rock in the sea",
            "Flour, water and a little salt, later with baking soda, buried in the embers.",
        ),
        hist(
            "Bannock, a skillet bread from Scotland, became a staple for which people in Canada?",
            "Many Indigenous communities", "Gold prospectors in Mexico", "French monks", "Dutch sailors",
            "Fur traders brought it, and it became part of many First Nations' and Métis cooking.",
        ),

        // --- Modern revival ---
        hist(
            "Why did Irish bakers take to soda bread in the 1800s?",
            "Irish wheat was soft and low in gluten, better suited to baking soda", "Yeast was banned", "Sourdough was too expensive", "Ovens were outlawed",
            "Once bread soda arrived around the 1840s, buttermilk and soft flour made a quick, tender loaf.",
        ),
        hist(
            "Who invented baking powder in England in 1843?",
            "Alfred Bird", "Louis Pasteur", "Isaac Newton", "Mrs Beeton",
            "He made it, along with egg-free custard, for his wife, who couldn't eat yeast or eggs.",
        ),
        hist(
            "Elizabeth David's influential 1977 book about British bread is called…",
            "English Bread and Yeast Cookery", "The Sourdough Bible", "Flour Water Salt Yeast", "Bread Alone",
            "It helped revive interest in real bread in Britain.",
        ),
        hist(
            "Acme Bread, founded in 1983 by Steve Sullivan, helped spark America's artisan bread revival from which city?",
            "Berkeley, California", "Boston", "Austin", "Seattle",
            "Sullivan had been a busboy at Chez Panisse and taught himself to bake from books.",
        ),
        hist(
            "Nancy Silverton opened which famous sourdough bakery in Los Angeles in 1989?",
            "La Brea Bakery", "Tartine", "Poilâne", "Acme",
            "Her rustic sourdough loaves helped bring artisan bread to American supermarkets.",
        ),
        hist(
            "The 'Bakery World Cup', the Coupe du Monde de la Boulangerie, was first held in Paris in…",
            "1992", "1892", "1952", "2012",
            "National teams compete in baguettes, specialty breads, viennoiserie and artistic bread pieces.",
        ),
        hist(
            "In 2006, which no-knead recipe in The New York Times got millions baking bread in a Dutch oven?",
            "Jim Lahey's no-knead bread", "Julia Child's baguette", "Chad Robertson's country loaf", "Paul Hollywood's cob",
            "Mark Bittman wrote about Lahey's slow, wet dough baked in a hot covered pot.",
        ),
        hist(
            "Chad Robertson's 2010 book 'Tartine Bread' is named after his bakery in…",
            "San Francisco", "Paris", "London", "Portland",
            "It made the tall, open-crumbed country loaf the dream of home sourdough bakers.",
        ),
        hist(
            "The Great British Bake Off first aired in…",
            "2010", "1995", "2016", "2005",
            "It started on BBC Two and its 'Bread Week' became a fixture.",
        ),
        hist(
            "Why do astronauts usually eat tortillas instead of bread?",
            "Crumbs float off and can get into equipment", "Bread is too heavy to launch", "Yeast stops working in space", "It's a NASA tradition from 1960",
            "Tortillas have been the space-sandwich solution since the 1980s.",
        ),
    )

    val baking = listOf(
        bake(
            "What dough temperature do many sourdough bakers aim for after mixing?",
            "About 24–26 °C (75–78 °F)", "About 10 °C (50 °F)", "About 37 °C (98 °F)", "About 50 °C (122 °F)",
            "Warm enough for a lively ferment, cool enough to keep it steady; bakers adjust the water temperature to hit it.",
        ),
        bake(
            "A poolish is…",
            "A wet preferment of equal flour and water with a little yeast", "A stiff Italian starter", "A Polish rye loaf", "A glaze",
            "Its name is usually linked to Polish bakers who brought the method to France.",
        ),
        bake(
            "An Italian biga is…",
            "A stiff preferment, drier than a poolish", "A type of oven", "A rye sour", "A baking stone",
            "It's mixed the day before and gives deep flavour to breads like ciabatta.",
        ),
        bake(
            "'Pâte fermentée' means…",
            "Old dough saved from the last batch", "A sourdough starter in a jar", "Fermented fruit filling", "Burnt crust",
            "A piece of yesterday's dough added to today's gives a head start on flavour.",
        ),
        bake(
            "Tangzhong (or yudane) is…",
            "A cooked flour-and-water paste that makes bread soft and keep longer", "A Chinese steamer", "A rye starter", "A scoring pattern",
            "The pre-cooked starch holds more water, giving fluffy milk-bread texture.",
        ),
        bake(
            "The 'windowpane test' checks…",
            "Whether the gluten is developed enough to stretch thin without tearing", "If the oven is hot", "If the starter is ready", "The crust colour",
            "A well-developed dough stretches into a translucent sheet.",
        ),
        bake(
            "'Bassinage' means…",
            "Adding extra water to the dough late in mixing", "Baking in a water bath", "Brushing with egg", "Cooling bread over a basin",
            "Holding some water back until the gluten forms makes very wet doughs easier to mix.",
        ),
        bake(
            "Laminating sourdough means…",
            "Stretching the dough out paper-thin on the bench, then folding it up", "Rolling butter into it", "Covering it in plastic", "Baking it twice",
            "It builds strength and is a neat way to add fillings evenly.",
        ),
        bake(
            "A coil fold is done by…",
            "Lifting the dough from the middle so it folds under itself", "Twisting it into a rope", "Rolling it with a pin", "Slapping it on the bench",
            "It's a gentle way to build strength in wet dough.",
        ),
        bake(
            "French flour types like T65 or T80 are numbered by…",
            "Their ash (mineral) content", "Protein percentage", "Price", "The mill's postcode",
            "The higher the number, the more of the whole grain is left in.",
        ),
        bake(
            "Diastatic malt powder is added to dough to…",
            "Help enzymes turn starch into sugar for the yeast", "Make it gluten-free", "Stop it rising", "Colour it green",
            "More sugar means livelier fermentation and a browner crust.",
        ),
        bake(
            "Why does bread go stale faster in the fridge?",
            "Starch recrystallises fastest at fridge temperatures", "Fridges are too dry", "The yeast wakes up", "Cold makes gluten dissolve",
            "Staling is mostly starch firming up. Freezing stops it; the fridge speeds it up.",
        ),
        bake(
            "Why do many bakers dust bannetons with rice flour?",
            "It absorbs less water, so the dough doesn't stick", "It tastes sweeter", "It makes the crust white", "It's cheaper",
            "Wheat flour soaks into the dough and acts like glue.",
        ),
        bake(
            "What does salt do to fermentation?",
            "Slows it down", "Speeds it up", "Stops it completely", "Has no effect",
            "Salt tightens gluten and slows yeast and bacteria, which is why it's often added after the autolyse.",
        ),
        bake(
            "A starter fed with whole grain or rye flour usually…",
            "Ferments faster than one fed white flour", "Never rises", "Can't be used for bread", "Has no bacteria",
            "Whole grains carry more nutrients and microbes to feed on.",
        ),
        bake(
            "A 'fermentolyse' is…",
            "An autolyse with the levain already mixed in", "A rest in the fridge", "A double bake", "A dough that failed",
            "Some bakers skip the separate step and let flour, water and levain rest together.",
        ),
        bake(
            "Roughly what is the pH of a finished sourdough loaf?",
            "About 4, mildly acidic like tomato juice", "About 7, neutral like water", "About 10, alkaline", "About 1, like stomach acid",
            "That acidity gives tang and helps sourdough keep longer.",
        ),
        bake(
            "French bakers call the opened, raised flap along a score the…",
            "Grigne", "Croûte", "Mie", "Baguette",
            "It's the French word for what English bakers call the ear.",
        ),
        bake(
            "A recipe with '20% levain' means the levain weighs…",
            "20% of the flour weight", "20% of the whole dough", "20 grams", "20% of the water",
            "Like everything in baker's percentages, it's measured against the flour.",
        ),
        bake(
            "Oven spring stops once the inside of the loaf is hot enough to kill the yeast, around…",
            "60 °C (140 °F)", "30 °C (86 °F)", "100 °C (212 °F)", "150 °C (300 °F)",
            "After that, rising comes only from expanding gas and steam until the crust sets.",
        ),
        bake(
            "What gives a bagel its chewy, shiny crust?",
            "Boiling it briefly before baking", "Brushing it with oil", "Baking it twice", "Freezing it overnight",
            "Boiling sets the outside starch before it ever reaches the oven.",
        ),
    )
}
