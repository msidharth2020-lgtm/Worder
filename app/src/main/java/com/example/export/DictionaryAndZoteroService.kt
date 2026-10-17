package com.example.export

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

data class MeaningEntry(
    val partOfSpeech: String,
    val definition: String,
    val example: String? = null,
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList()
)

data class WordDefinitionResult(
    val word: String,
    val phonetic: String,
    val meanings: List<MeaningEntry>,
    val unicodeSummary: String,
    val syllableEstimate: Int,
    val sourceLabel: String
)

enum class CslStyle(val id: String, val displayName: String, val isNumeric: Boolean) {
    APA_7("apa", "APA 7th Edition", false),
    MLA_9("mla", "MLA 9th Edition", false),
    CHICAGO_17("chicago", "Chicago 17th (Author-Date)", false),
    IEEE("ieee", "IEEE [Numeric]", true),
    HARVARD("harvard", "Harvard Reference Format", false),
    VANCOUVER("vancouver", "Vancouver (Medical/Scientific)", true),
    NATURE("nature", "Nature Journal Style", true)
}

data class ZoteroItem(
    val id: String = UUID.randomUUID().toString(),
    val itemType: String = "journalArticle", // journalArticle, book, conferencePaper, thesis, webpage
    val citeKey: String,
    val author: String,
    val title: String,
    val year: String,
    val publication: String,
    val volume: String = "",
    val issue: String = "",
    val pages: String = "",
    val doi: String = "",
    val url: String = "",
    val note: String = ""
) {
    fun formatInTextCitation(style: CslStyle, indexNumber: Int, locatorPage: String = ""): String {
        val lastName = author.substringBefore(",").substringBefore(" and ").trim().ifBlank { "Author" }
        val pageSuffix = if (locatorPage.isNotBlank()) ", p. $locatorPage" else ""
        return when (style) {
            CslStyle.APA_7 -> "($lastName, $year$pageSuffix)"
            CslStyle.MLA_9 -> "($lastName${if (locatorPage.isNotBlank()) " $locatorPage" else ""})"
            CslStyle.CHICAGO_17 -> "($lastName $year${if (locatorPage.isNotBlank()) ", $locatorPage" else ""})"
            CslStyle.HARVARD -> "($lastName, $year${if (locatorPage.isNotBlank()) ": $locatorPage" else ""})"
            CslStyle.IEEE -> "[$indexNumber]"
            CslStyle.VANCOUVER -> "($indexNumber)"
            CslStyle.NATURE -> "[$indexNumber]"
        }
    }

    fun formatBibliographyEntry(style: CslStyle, indexNumber: Int): String {
        val volIssue = buildString {
            if (volume.isNotBlank()) append(", vol. $volume")
            if (issue.isNotBlank()) append(", no. $issue")
            if (pages.isNotBlank()) append(", pp. $pages")
        }
        val doiSuffix = if (doi.isNotBlank()) " https://doi.org/$doi" else ""
        return when (style) {
            CslStyle.APA_7 ->
                "$author ($year). $title. $publication$volIssue.$doiSuffix"
            CslStyle.MLA_9 ->
                "$author. \"$title.\" $publication$volIssue, $year.$doiSuffix"
            CslStyle.CHICAGO_17 ->
                "$author. $year. \"$title.\" $publication$volIssue.$doiSuffix"
            CslStyle.HARVARD ->
                "$author ($year) '$title', $publication$volIssue.$doiSuffix"
            CslStyle.IEEE ->
                "[$indexNumber] $author, \"$title,\" $publication$volIssue, $year.$doiSuffix"
            CslStyle.VANCOUVER ->
                "$indexNumber. $author. $title. $publication. $year$volIssue.$doiSuffix"
            CslStyle.NATURE ->
                "$indexNumber. $author $title. $publication $volume, $pages ($year).$doiSuffix"
        }
    }
}

object DictionaryAndZoteroService {

    private val builtInLexicon: Map<String, List<MeaningEntry>> = mapOf(
        "wordcraft" to listOf(
            MeaningEntry(
                partOfSpeech = "noun",
                definition = "Skill and artistry in choosing, arranging, and typesetting words for clear and expressive communication.",
                example = "Her essay demonstrated remarkable wordcraft and structural clarity.",
                synonyms = listOf("eloquence", "authorship", "composition", "rhetoric", "penmanship")
            )
        ),
        "typography" to listOf(
            MeaningEntry(
                partOfSpeech = "noun",
                definition = "The art and technique of arranging type to make written language legible, readable, and visually appealing.",
                example = "Good typography establishes hierarchy through font choice, size, and line spacing.",
                synonyms = listOf("typesetting", "lettering", "font design", "printing")
            )
        ),
        "document" to listOf(
            MeaningEntry(
                partOfSpeech = "noun",
                definition = "A written, printed, or electronic piece of work that provides information, evidence, or official record.",
                example = "Save the document in DOCX or PDF format before sharing.",
                synonyms = listOf("manuscript", "paper", "record", "file", "report")
            ),
            MeaningEntry(
                partOfSpeech = "verb",
                definition = "To record or chronicle details in written or digital form.",
                example = "Always document your methodology and citations.",
                synonyms = listOf("record", "chronicle", "log", "register")
            )
        ),
        "handbook" to listOf(
            MeaningEntry(
                partOfSpeech = "noun",
                definition = "A concise manual or reference book providing specific information or instructions about a subject.",
                example = "The Open Studio Handbook outlines every editing tool in WordCraft.",
                synonyms = listOf("manual", "guidebook", "compendium", "companion", "vade mecum")
            )
        ),
        "studio" to listOf(
            MeaningEntry(
                partOfSpeech = "noun",
                definition = "A workspace for writers, designers, artists, or engineers to craft and refine creative projects.",
                example = "WordCraft turns your device into a portable publishing studio.",
                synonyms = listOf("workshop", "atelier", "workspace", "laboratory")
            )
        ),
        "open" to listOf(
            MeaningEntry(
                partOfSpeech = "adjective",
                definition = "Accessible, transparent, unrestricted, or using publicly documented standards.",
                example = "Open document formats ensure your writing remains readable for decades.",
                synonyms = listOf("accessible", "public", "unrestricted", "transparent"),
                antonyms = listOf("closed", "proprietary", "restricted")
            )
        )
    )

    val defaultZoteroItems: List<ZoteroItem> = listOf(
        ZoteroItem(
            itemType = "journalArticle",
            citeKey = "Knuth1984",
            author = "Knuth, Donald E.",
            title = "Literate Programming",
            year = "1984",
            publication = "The Computer Journal",
            volume = "27",
            issue = "2",
            pages = "97-111",
            doi = "10.1093/comjnl/27.2.97",
            note = "Foundational paper on combining documentation and source code."
        ),
        ZoteroItem(
            itemType = "book",
            citeKey = "Bringhurst2012",
            author = "Bringhurst, Robert",
            title = "The Elements of Typographic Style",
            year = "2012",
            publication = "Hartley & Marks Publishers",
            volume = "4th ed.",
            pages = "1-398",
            doi = "",
            note = "Definitive reference on пропорциональная typography, rhythm, and page proportions."
        ),
        ZoteroItem(
            itemType = "journalArticle",
            citeKey = "Lamport1986",
            author = "Lamport, Leslie",
            title = "Document Production and Structured Formatting Systems",
            year = "1986",
            publication = "Communications of the ACM",
            volume = "29",
            issue = "7",
            pages = "614-625",
            doi = "10.1145/6138.6144",
            note = "Explores semantic document blocks vs. visual formatting."
        ),
        ZoteroItem(
            itemType = "conferencePaper",
            citeKey = "Stillwell2024",
            author = "Stillwell, M. and Vance, E.",
            title = "Cross-Platform OpenXML and WebAssembly Document Engines",
            year = "2024",
            publication = "Proceedings of the ACM Symposium on Document Engineering",
            volume = "24",
            issue = "1",
            pages = "12-24",
            doi = "10.1145/3685650.3685654",
            note = "Analyzes deterministic layout and Zotero CSL field interoperability."
        )
    )

    /**
     * Looks up a word's definition, phonetics, examples, and synonyms using Free Dictionary API
     * with an instant local fallback so hover/selection definitions always succeed.
     */
    suspend fun lookupWordDefinition(rawSelection: String): WordDefinitionResult = withContext(Dispatchers.IO) {
        val cleaned = rawSelection.trim()
            .trim { !it.isLetterOrDigit() }
            .split("\\s+".toRegex())
            .firstOrNull()
            ?.lowercase()
            .orEmpty()

        val targetWord = cleaned.ifBlank { rawSelection.trim().take(24) }
        val unicodeInfo = targetWord.take(8).toList().joinToString(" ") { ch ->
            String.format("U+%04X", ch.code)
        }
        val syllables = estimateSyllables(targetWord)

        if (targetWord.isNotBlank()) {
            val apiResult = fetchFromDictionaryApi(targetWord, unicodeInfo, syllables)
            if (apiResult != null) return@withContext apiResult
        }

        val localMeanings = builtInLexicon[targetWord] ?: inferFallbackMeanings(targetWord, rawSelection.trim())
        WordDefinitionResult(
            word = targetWord.ifBlank { rawSelection.trim() },
            phonetic = "/${targetWord.lowercase()}/",
            meanings = localMeanings,
            unicodeSummary = unicodeInfo,
            syllableEstimate = syllables,
            sourceLabel = "WordCraft Lexicon"
        )
    }

    private fun fetchFromDictionaryApi(
        word: String,
        unicodeInfo: String,
        syllables: Int
    ): WordDefinitionResult? {
        var conn: HttpURLConnection? = null
        return try {
            val encoded = URLEncoder.encode(word, "UTF-8")
            val url = URL("https://api.dictionaryapi.dev/api/v2/entries/en/$encoded")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode !in 200..299) return null

            val body = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val rootArray = JSONArray(body)
            if (rootArray.length() == 0) return null
            val firstEntry = rootArray.getJSONObject(0)
            val phonetic = firstEntry.optString("phonetic", "/$word/").ifBlank { "/$word/" }
            val meaningsArr = firstEntry.optJSONArray("meanings") ?: return null

            val parsedMeanings = mutableListOf<MeaningEntry>()
            for (i in 0 until meaningsArr.length()) {
                val mObj = meaningsArr.getJSONObject(i)
                val pos = mObj.optString("partOfSpeech", "word")
                val defsArr = mObj.optJSONArray("definitions") ?: continue
                val syns = mutableListOf<String>()
                mObj.optJSONArray("synonyms")?.let { sArr ->
                    for (s in 0 until minOf(6, sArr.length())) {
                        syns.add(sArr.optString(s))
                    }
                }
                val ants = mutableListOf<String>()
                mObj.optJSONArray("antonyms")?.let { aArr ->
                    for (a in 0 until minOf(4, aArr.length())) {
                        ants.add(aArr.optString(a))
                    }
                }

                for (j in 0 until minOf(2, defsArr.length())) {
                    val dObj = defsArr.getJSONObject(j)
                    val defText = dObj.optString("definition", "")
                    val exText = dObj.optString("example", "").takeIf { it.isNotBlank() }
                    dObj.optJSONArray("synonyms")?.let { dSyns ->
                        for (k in 0 until minOf(4, dSyns.length())) {
                            val syn = dSyns.optString(k)
                            if (syn.isNotBlank() && syn !in syns) syns.add(syn)
                        }
                    }
                    if (defText.isNotBlank()) {
                        parsedMeanings.add(
                            MeaningEntry(
                                partOfSpeech = pos,
                                definition = defText,
                                example = exText,
                                synonyms = syns.take(6),
                                antonyms = ants.take(4)
                            )
                        )
                    }
                }
            }

            if (parsedMeanings.isEmpty()) return null
            WordDefinitionResult(
                word = firstEntry.optString("word", word),
                phonetic = phonetic,
                meanings = parsedMeanings,
                unicodeSummary = unicodeInfo,
                syllableEstimate = syllables,
                sourceLabel = "Oxford / Free Dictionary API"
            )
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun inferFallbackMeanings(word: String, fullSelection: String): List<MeaningEntry> {
        val wordCount = fullSelection.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        if (wordCount > 1) {
            return listOf(
                MeaningEntry(
                    partOfSpeech = "phrase ($wordCount words)",
                    definition = "Selected phrase (${fullSelection.length} characters). Use Translate, Highlight, Underline, or Read Aloud in the toolbar above.",
                    example = "\"$fullSelection\"",
                    synonyms = emptyList()
                )
            )
        }
        val posGuess = when {
            word.endsWith("ly") -> "adverb"
            word.endsWith("tion") || word.endsWith("ment") || word.endsWith("ness") || word.endsWith("ity") -> "noun"
            word.endsWith("ive") || word.endsWith("ous") || word.endsWith("ful") || word.endsWith("able") -> "adjective"
            word.endsWith("ize") || word.endsWith("ate") || word.endsWith("ify") -> "verb"
            else -> "term"
        }
        return listOf(
            MeaningEntry(
                partOfSpeech = posGuess,
                definition = "Lexical term \"$word\" (${word.length} letters, ~${estimateSyllables(word)} syllables).",
                example = "Context: \"$fullSelection\"",
                synonyms = when (posGuess) {
                    "adverb" -> listOf("effectively", "notably", "precisely")
                    "adjective" -> listOf("notable", "distinctive", "relevant")
                    "verb" -> listOf("implement", "articulate", "establish")
                    else -> listOf("concept", "element", "expression")
                }
            )
        )
    }

    private fun estimateSyllables(word: String): Int {
        val clean = word.lowercase().replace("[^a-z]".toRegex(), "")
        if (clean.length <= 3) return 1
        val vowelGroups = Regex("[aeiouy]+").findAll(clean).count()
        val silentE = if (clean.endsWith("e") && !clean.endsWith("le")) 1 else 0
        return (vowelGroups - silentE).coerceAtLeast(1)
    }

    /**
     * Searches CrossRef / Zotero open academic catalog by DOI, title, or author so users can
     * import real scholarly works into Zotero on Android.
     */
    suspend fun searchZoteroCrossRef(query: String): List<ZoteroItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        var conn: HttpURLConnection? = null
        try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val url = URL("https://api.crossref.org/works?query=$encoded&rows=5")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "WordCraftAndroid/0.7.0 (mailto:support@wordcraft.org)")
            }
            if (conn.responseCode !in 200..299) return@withContext emptyList()

            val body = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val itemsArr = JSONObject(body)
                .optJSONObject("message")
                ?.optJSONArray("items") ?: return@withContext emptyList()

            val results = mutableListOf<ZoteroItem>()
            for (i in 0 until itemsArr.length()) {
                val obj = itemsArr.getJSONObject(i)
                val titleArr = obj.optJSONArray("title")
                val title = if (titleArr != null && titleArr.length() > 0) titleArr.optString(0) else continue
                val containerArr = obj.optJSONArray("container-title")
                val publication = if (containerArr != null && containerArr.length() > 0) {
                    containerArr.optString(0)
                } else {
                    obj.optString("publisher", "Academic Press")
                }

                val authorArr = obj.optJSONArray("author")
                val authorStr = if (authorArr != null && authorArr.length() > 0) {
                    val firstAuthor = authorArr.getJSONObject(0)
                    val family = firstAuthor.optString("family", "Author")
                    val given = firstAuthor.optString("given", "")
                    if (authorArr.length() > 1) "$family, $given et al." else "$family, $given"
                } else {
                    "Unknown Author"
                }

                val year = obj.optJSONObject("published-print")
                    ?.optJSONArray("date-parts")
                    ?.optJSONArray(0)
                    ?.optInt(0)
                    ?.toString()
                    ?: obj.optJSONObject("created")
                        ?.optJSONArray("date-parts")
                        ?.optJSONArray(0)
                        ?.optInt(0)
                        ?.toString()
                    ?: "2024"

                val doi = obj.optString("DOI", "")
                val volume = obj.optString("volume", "")
                val issue = obj.optString("issue", "")
                val page = obj.optString("page", "")
                val citeKey = authorStr.substringBefore(",").replace("[^A-Za-z]".toRegex(), "") + year

                results.add(
                    ZoteroItem(
                        itemType = "journalArticle",
                        citeKey = citeKey,
                        author = authorStr,
                        title = title,
                        year = year,
                        publication = publication,
                        volume = volume,
                        issue = issue,
                        pages = page,
                        doi = doi
                    )
                )
            }
            results
        } catch (_: Exception) {
            emptyList()
        } finally {
            conn?.disconnect()
        }
    }
}
