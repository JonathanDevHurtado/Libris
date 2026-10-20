package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.model.*
import com.example.service.PdfRendererManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class BookRepository(
    private val context: Context,
    private val pdfRendererManager: PdfRendererManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _books = MutableStateFlow<List<BookItem>>(emptyList())
    val books: StateFlow<List<BookItem>> = _books.asStateFlow()

    private val _history = MutableStateFlow<List<ReadingHistoryItem>>(emptyList())
    val history: StateFlow<List<ReadingHistoryItem>> = _history.asStateFlow()

    private val _settings = MutableStateFlow(ReaderSettings())
    val settings: StateFlow<ReaderSettings> = _settings.asStateFlow()

    private val _googleAccount = MutableStateFlow(
        ConnectedAccount(
            provider = "Google",
            isConnected = true,
            username = "Lector de Libros",
            email = "lector@example.com",
            avatarLetter = "R",
            cloudBooksCount = 12,
            lastSyncTime = "Hoy, 14:15"
        )
    )
    val googleAccount: StateFlow<ConnectedAccount> = _googleAccount.asStateFlow()

    private val _wattpadAccount = MutableStateFlow(
        ConnectedAccount(
            provider = "Wattpad",
            isConnected = true,
            username = "@LectorEstelar",
            email = "usuario@wattpad.com",
            avatarLetter = "W",
            cloudBooksCount = 5,
            lastSyncTime = "Hace 10 min"
        )
    )
    val wattpadAccount: StateFlow<ConnectedAccount> = _wattpadAccount.asStateFlow()

    private val _isOfflineOnlyFilter = MutableStateFlow(false)
    val isOfflineOnlyFilter: StateFlow<Boolean> = _isOfflineOnlyFilter.asStateFlow()

    init {
        scope.launch {
            initSampleLibrary()
        }
    }

    private suspend fun initSampleLibrary() {
        // Generate real sample PDFs so PdfRenderer can open real files
        val pdfPrincipito = pdfRendererManager.createSampleLiteraryPdf(
            fileName = "el_principito.pdf",
            title = "El Principito",
            author = "Antoine de Saint-Exupéry",
            pagesText = listOf(
                "Pido perdón a los niños por haber dedicado este libro a una persona grande. Tengo una seria razón para ello: esta persona grande es el mejor amigo que tengo en el mundo. Viví así, solo, sin nadie con quien hablar verdaderamente, hasta un día en que tuve una avería en el desierto de Sahara, hace seis años. Algo se había roto en mi motor.",
                "La primera noche me dormí sobre la arena, a mil millas de toda tierra habitada. Estaba más aislado que un náufrago en una balsa en medio del océano. Imaginaos, pues, mi sorpresa cuando, al romper el día, me despertó una extraña vocecita que decía: —Por favor... ¡dibújame un cordero! —¡Eh! —Dibújame un cordero... Me puse en pie de un salto, como si me hubiera golpeado un rayo.",
                "—Las personas grandes nunca comprenden nada por sí solas, y es agotador para los niños tener que darles explicaciones una y otra vez. Aprendí así una segunda cosa muy importante: ¡su planeta de origen era apenas más grande que una casa! Tenía serias razones para creer que el planeta de donde venía el principito era el asteroide B 612.",
                "—No se ve bien sino con el corazón. Lo esencial es invisible a los ojos. —Lo esencial es invisible a los ojos —repitió el principito, a fin de acordarse. —El tiempo que perdiste por tu rosa hace que tu rosa sea tan importante. —Los hombres han olvidado esta verdad —dijo el zorro—. Pero tú no debes olvidarla."
            )
        )

        val pdfCiencia = pdfRendererManager.createSampleLiteraryPdf(
            fileName = "viaje_centro_tierra.pdf",
            title = "Viaje al Centro de la Tierra",
            author = "Julio Verne",
            pagesText = listOf(
                "El 24 de mayo de 1863, que era un domingo, mi tío, el profesor Lidenbrock, regresó precipitadamente a su modesta casa situada en el número 19 de la Königstrasse, una de las calles más antiguas del barrio de Hamburgo. Marta debió creerse muy atrasada, porque la comida empezaba apenas a hervir en el hornillo de la cocina.",
                "—¡Axel, ven aquí! —me gritó con voz imperiosa. No tuve tiempo de responder; ya me arrastraba hacia su despacho lleno de minerales, fósiles y mapas geológicos. Tenía en la mano un viejo manuscrito rúnico escrito sobre pergamino amarillento. —¡Mira este documento! ¡Es la clave de un enigma geológico universal!",
                "Descendimos por las entrañas del cráter del Sneffels, en Islandia. La sombra del Scartaris acariciaba las rocas antes de las calendas de julio. Nos adentramos en las profundidades de la corteza terrestre, donde la oscuridad y los ecos de aguas subterráneas anunciaban un mundo nuevo."
            )
        )

        val initialList = listOf(
            BookItem(
                id = "book_1",
                title = "El Principito",
                author = "Antoine de Saint-Exupéry",
                coverColorHex = 0xFF1A365D,
                coverAccentHex = 0xFFECC94B,
                format = BookFormat.PDF,
                filePath = pdfPrincipito.absolutePath,
                isDownloadedOffline = true,
                totalPages = 4,
                currentPage = 2,
                lastReadTimestamp = System.currentTimeMillis() - 1000 * 60 * 30, // 30 mins ago
                category = "Clásicos",
                description = "Una fábula poética sobre el sentido de la vida, el amor y la amistad. Edición en PDF con texto completo.",
                chapters = listOf(
                    ChapterContent("Capítulo I: El Desierto", listOf(
                        "Pido perdón a los niños por haber dedicado este libro a una persona grande. Tengo una seria razón para ello: esta persona grande es el mejor amigo que tengo en el mundo.",
                        "Viví así, solo, sin nadie con quien hablar verdaderamente, hasta un día en que tuve una avería en el desierto de Sahara, hace seis años.",
                        "La primera noche me dormí sobre la arena, a mil millas de toda tierra habitada. Estaba más aislado que un náufrago en medio del océano."
                    )),
                    ChapterContent("Capítulo II: El Encuentro", listOf(
                        "Imaginaos, pues, mi sorpresa cuando, al romper el día, me despertó una extraña vocecita que decía: —Por favor... ¡dibújame un cordero!",
                        "—Las personas grandes nunca comprenden nada por sí solas, y es agotador para los niños tener que darles explicaciones una y otra vez.",
                        "Aprendí así una segunda cosa muy importante: ¡su planeta de origen era apenas más grande que una casa!"
                    )),
                    ChapterContent("Capítulo XXI: El Secreto del Zorro", listOf(
                        "—Adiós —dijo el zorro—. He aquí mi secreto. Es muy simple: no se ve bien sino con el corazón. Lo esencial es invisible a los ojos.",
                        "—Lo esencial es invisible a los ojos —repitió el principito, a fin de acordarse.",
                        "—El tiempo que perdiste por tu rosa hace que tu rosa sea tan importante. Eres responsable para siempre de lo que has domesticado."
                    ))
                ),
                totalWords = 16500,
                readingTimeMinutes = 65,
                isFavorite = true
            ),
            BookItem(
                id = "book_2",
                title = "Don Quijote de la Mancha",
                author = "Miguel de Cervantes",
                coverColorHex = 0xFF5B3A29,
                coverAccentHex = 0xFFD4A373,
                format = BookFormat.CLASSIC,
                isDownloadedOffline = true,
                totalPages = 120,
                currentPage = 14,
                lastReadTimestamp = System.currentTimeMillis() - 1000 * 60 * 180, // 3 hours ago
                category = "Clásicos",
                description = "En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor.",
                chapters = listOf(
                    ChapterContent("Capítulo 1: Condición y ejercicio del hidalgo", listOf(
                        "En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor.",
                        "Una olla de algo más vaca que carnero, salpicón las más noches, duelos y quebrantos los sábados, lantejas los viernes, algún palomino de añadidura los domingos, consumían las tres partes de su hacienda.",
                        "Es, pues, de saber que este sobredicho hidalgo, los ratos que estaba ocioso —que eran los más del año—, se daba a leer libros de caballerías con tanta afición y gusto, que olvidó casi de todo punto el ejercicio de la caza y aun la administración de su hacienda."
                    )),
                    ChapterContent("Capítulo 8: De los molinos de viento", listOf(
                        "En esto, descubrieron treinta o cuarenta molinos de viento que hay en aquel campo; y, así como don Quijote los vio, dijo a su escudero:",
                        "—La ventura va guiando nuestras cosas mejor de lo que acertáramos a desear; porque ves allí, amigo Sancho Panza, donde se descubren treinta, o pocos más, desaforados gigantes, con quien pienso hacer batalla.",
                        "—Mire vuestra merced —respondió Sancho— que aquellos que allí se parecen no son gigantes, sino molinos de viento, y lo que en ellos parecen brazos son las aspas, que, volteadas del viento, hacen andar la piedra del molino."
                    ))
                ),
                totalWords = 380000,
                readingTimeMinutes = 540,
                isFavorite = true
            ),
            BookItem(
                id = "book_3",
                title = "El Silencio de las Estrellas",
                author = "Elena Valenti (Wattpad)",
                coverColorHex = 0xFF311B92,
                coverAccentHex = 0xFFFF500A,
                format = BookFormat.WATTPAD,
                isDownloadedOffline = true,
                totalPages = 38,
                currentPage = 8,
                lastReadTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24, // Yesterday
                category = "Romance & Fantasía",
                description = "Historia original de Wattpad con más de 1.8 millones de lecturas. Cuando las constelaciones comienzan a desaparecer del cielo nocturno, dos jóvenes descubren un pacto ancestral que desafía el tiempo.",
                chapters = listOf(
                    ChapterContent("Capítulo 1: Ecos en la Niebla", listOf(
                        "Dicen que antes de que una estrella caiga, el aire se llena de un aroma a lluvia y ozono.",
                        "Aria miraba a través del telescopio de bronce en la azotea de la academia. Esa noche, la constelación de Orión había perdido tres luceros.",
                        "—No deberías estar despierta a estas horas —susurró una voz a sus espaldas. Era Kaelen, con su chaqueta negra y esos ojos que parecían reflejar galaxias olvidadas.",
                        "—Algo está borrando el cielo, Kaelen. Y no creo que sea una ilusión óptica."
                    )),
                    ChapterContent("Capítulo 2: El Manuscrito Plateado", listOf(
                        "En el sótano secreto de la biblioteca, los tomos antiguos crujían con el tacto de sus dedos.",
                        "Encontraron un diario encuadernado en terciopelo azul marino. Cada página estaba escrita con tinta luminiscente que palpitaba al ritmo de la respiración.",
                        "—Este libro no fue escrito por un humano —dijo Aria con el corazón acelerado."
                    ))
                ),
                totalWords = 42000,
                readingTimeMinutes = 110,
                isFavorite = false
            ),
            BookItem(
                id = "book_4",
                title = "Viaje al Centro de la Tierra",
                author = "Julio Verne",
                coverColorHex = 0xFF1B4D3E,
                coverAccentHex = 0xFF81C784,
                format = BookFormat.PDF,
                filePath = pdfCiencia.absolutePath,
                isDownloadedOffline = true,
                totalPages = 3,
                currentPage = 1,
                lastReadTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 48,
                category = "Ciencia Ficción",
                description = "Una expedición científica liderada por el profesor Lidenbrock desciende por un volcán islandés hacia el corazón del planeta.",
                chapters = listOf(
                    ChapterContent("Capítulo 1: El Manuscrito Rúnico", listOf(
                        "El 24 de mayo de 1863, que era un domingo, mi tío, el profesor Lidenbrock, regresó precipitadamente a su modesta casa de Hamburgo.",
                        "Tenía en la mano un libro impreso en Copenhague en el siglo XII, escrito por el célebre alquimista islandés Arne Saknussemm."
                    ))
                ),
                totalWords = 65000,
                readingTimeMinutes = 190
            ),
            BookItem(
                id = "book_5",
                title = "Alicia en el País de las Maravillas",
                author = "Lewis Carroll",
                coverColorHex = 0xFF4A148C,
                coverAccentHex = 0xFFBA68C8,
                format = BookFormat.CLASSIC,
                isDownloadedOffline = false,
                totalPages = 24,
                currentPage = 1,
                lastReadTimestamp = 0,
                category = "Fantasía",
                description = "Alicia empezaba a cansarse de estar sentada junto a su hermana en la orilla del río, sin tener nada que hacer...",
                chapters = listOf(
                    ChapterContent("Capítulo 1: En la madriguera del conejo", listOf(
                        "Alicia empezaba a cansarse de estar sentada junto a su hermana a la orilla del río, y de no tener nada que hacer.",
                        "Había echado un par de miradas al libro que leía su hermana, pero no tenía dibujos ni conversaciones: «¿Y para qué sirve un libro sin dibujos ni conversaciones?», pensaba Alicia.",
                        "De pronto pasó corriendo cerca de ella un Conejo Blanco de ojos rosados, sacó un reloj del bolsillo de su chaleco y exclamó: —¡Dios mío, llegaré demasiado tarde!"
                    ))
                )
            ),
            BookItem(
                id = "book_6",
                title = "Academia de Sombras (Wattpad)",
                author = "Lucía Dark (Wattpad)",
                coverColorHex = 0xFF212121,
                coverAccentHex = 0xFFFF500A,
                format = BookFormat.WATTPAD,
                isDownloadedOffline = false,
                totalPages = 50,
                currentPage = 1,
                lastReadTimestamp = 0,
                category = "Misterio",
                description = "Nadie que cruza las rejas de la Academia Saint Claire vuelve a ser el mismo. Secretos, linajes mágicos y sombras que susurran al caer el sol."
            )
        )

        _books.value = initialList

        // Initial Reading History
        _history.value = listOf(
            ReadingHistoryItem(
                id = "hist_1",
                bookId = "book_1",
                bookTitle = "El Principito",
                author = "Antoine de Saint-Exupéry",
                pageReached = 2,
                totalPages = 4,
                progressPercent = 0.50f,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                durationMinutes = 18,
                coverColorHex = 0xFF1A365D
            ),
            ReadingHistoryItem(
                id = "hist_2",
                bookId = "book_2",
                bookTitle = "Don Quijote de la Mancha",
                author = "Miguel de Cervantes",
                pageReached = 14,
                totalPages = 120,
                progressPercent = 0.12f,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 180,
                durationMinutes = 32,
                coverColorHex = 0xFF5B3A29
            ),
            ReadingHistoryItem(
                id = "hist_3",
                bookId = "book_3",
                bookTitle = "El Silencio de las Estrellas",
                author = "Elena Valenti (Wattpad)",
                pageReached = 8,
                totalPages = 38,
                progressPercent = 0.21f,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                durationMinutes = 25,
                coverColorHex = 0xFF311B92
            )
        )
    }

    fun updateReadingProgress(bookId: String, newPage: Int, sessionDurationMinutes: Int = 5) {
        val currentList = _books.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookId }
        if (index != -1) {
            val book = currentList[index]
            val clampedPage = newPage.coerceIn(1, book.totalPages.coerceAtLeast(1))
            val updated = book.copy(
                currentPage = clampedPage,
                lastReadTimestamp = System.currentTimeMillis()
            )
            currentList[index] = updated
            _books.value = currentList

            // Add history entry
            val newHist = ReadingHistoryItem(
                id = "hist_${System.currentTimeMillis()}",
                bookId = book.id,
                bookTitle = book.title,
                author = book.author,
                pageReached = clampedPage,
                totalPages = book.totalPages,
                progressPercent = updated.progressPercent,
                timestamp = System.currentTimeMillis(),
                durationMinutes = sessionDurationMinutes,
                coverColorHex = book.coverColorHex
            )
            _history.value = listOf(newHist) + _history.value
        }
    }

    fun toggleOfflineDownload(bookId: String) {
        val currentList = _books.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookId }
        if (index != -1) {
            val book = currentList[index]
            val newState = !book.isDownloadedOffline
            currentList[index] = book.copy(isDownloadedOffline = newState)
            _books.value = currentList
        }
    }

    fun toggleFavorite(bookId: String) {
        val currentList = _books.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookId }
        if (index != -1) {
            val book = currentList[index]
            currentList[index] = book.copy(isFavorite = !book.isFavorite)
            _books.value = currentList
        }
    }

    suspend fun importPdfFromUri(uri: Uri, displayName: String): BookItem? {
        val file = pdfRendererManager.openPdfFromUri(uri) ?: return null
        val count = pdfRendererManager.pageCount.coerceAtLeast(1)

        val newBook = BookItem(
            id = "imported_${System.currentTimeMillis()}",
            title = displayName.removeSuffix(".pdf").replace("_", " "),
            author = "Documento Importado",
            coverColorHex = 0xFFBF360C,
            coverAccentHex = 0xFFFFAB91,
            format = BookFormat.PDF,
            filePath = file.absolutePath,
            isDownloadedOffline = true,
            totalPages = count,
            currentPage = 1,
            lastReadTimestamp = System.currentTimeMillis(),
            category = "Mis PDFs",
            description = "Archivo PDF importado desde el almacenamiento del dispositivo.",
            chapters = listOf(
                ChapterContent("Documento Completo", listOf("Archivo cargado localmente. Utilice el lector interactivo para visualizar sus páginas."))
            )
        )

        _books.value = listOf(newBook) + _books.value
        return newBook
    }

    fun updateSettings(newSettings: ReaderSettings) {
        _settings.value = newSettings
    }

    fun toggleGoogleAccount() {
        val current = _googleAccount.value
        _googleAccount.value = current.copy(
            isConnected = !current.isConnected,
            lastSyncTime = if (!current.isConnected) "Sincronizado ahora mismo" else "Desconectado"
        )
    }

    fun toggleWattpadAccount() {
        val current = _wattpadAccount.value
        _wattpadAccount.value = current.copy(
            isConnected = !current.isConnected,
            lastSyncTime = if (!current.isConnected) "Sincronizado ahora mismo" else "Desconectado"
        )
    }

    fun syncAllCloud() {
        val now = "Sincronizado ahora"
        _googleAccount.value = _googleAccount.value.copy(lastSyncTime = now)
        _wattpadAccount.value = _wattpadAccount.value.copy(lastSyncTime = now)
    }

    fun toggleOfflineOnlyFilter() {
        _isOfflineOnlyFilter.value = !_isOfflineOnlyFilter.value
    }
}
