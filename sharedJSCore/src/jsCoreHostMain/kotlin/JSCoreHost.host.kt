import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.asyncFunction
import com.dokar.quickjs.converter.TypeConverter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlin.reflect.KType
import kotlin.reflect.typeOf

private data class ReqJSArgs(
    val resultChannel: Channel<JS> = Channel(1),
    val update: Boolean = false,
)

private val reqChannel: Channel<ReqJSArgs> = Channel()

private val jsCoreWorkerLaunched = Channel<Any?>(1)

internal typealias URLFetcher = suspend (url: String) -> String

private suspend fun loadQuickJS(
    version: Int,
    singleThreadDispatcher: CoroutineDispatcher,
    fetch: URLFetcher,
): QuickJs {
    return withContext(Dispatchers.IO) {
        fetch("${HTTPBases.SERVER}${URLSuffixes.jsCore(version)}")
    }.let {
        withContext(singleThreadDispatcher) {
            QuickJs.create(singleThreadDispatcher).apply {
                try {
                    addTypeConverters(object : TypeConverter<Long, Int> {
                        override val sourceType: KType
                            get() = typeOf<Long>()
                        override val targetType: KType
                            get() = typeOf<Int>()

                        override fun convertToTarget(value: Long): Int {
                            return value.toInt()
                        }
                    })
                    evaluate<Any?>(it)
                } catch (e: Throwable) {
                    close()
                    throw e
                }
            }
        }
    }
}

internal suspend fun jsCore(
    buildSingleThreadDispatcher: () -> CoroutineDispatcher,
    fetch: URLFetcher
): JS {
    if (jsCoreWorkerLaunched.trySend(null).isSuccess) {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Default) {
            var js: JS? = null
            var version: Int? = null
            for (req in reqChannel) {
                try {
                    req.apply {
                        req.resultChannel.trySend(
                            js?.takeIf { update.not() }
                                ?: withContext(Dispatchers.IO) {
                                    fetch(
                                        "${HTTPBases.SERVER}${URLSuffixes.JS_CORE_VERSION_LATEST}"
                                    )
                                }.toInt().let { latest ->
                                    js?.takeIf { version == latest }
                                        ?: buildSingleThreadDispatcher().let {
                                            loadQuickJS(latest, it, fetch).toJS(it).also {
                                                js = it
                                                version = latest
                                            }
                                        }
                                }
                        )
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
    return ReqJSArgs(update = true).let {
        reqChannel.send(it)
        it.resultChannel.receive()
    }
}

private class JSBridge<R> private constructor(
    val args: Channel<Any?>,
    val result: Channel<R>,
) {
    constructor(argNum: Int) : this(
        args = Channel(if (argNum <= 0) Channel.RENDEZVOUS else argNum),
        result = Channel(1),
    )
}

private abstract class JSO(
    val quickJS: QuickJs,
    val singleThreadDispatcher: CoroutineDispatcher,
) : JS {
    private val existence = mutableMapOf<String, JSBridge<*>>()
    suspend fun <T> ensureExist(name: String, create: (String) -> JSBridge<T>): JSBridge<T> {
        return withContext(singleThreadDispatcher) {
            existence.getOrPut(name) { create(name) } as JSBridge<T>
        }
    }
}

private const val coreName = "jsCoreV"

private class JSLiteral private constructor(
    private val methodName: String,
    private val argsMethodName: String,
    private val resMethodName: String,
    private vararg val args: Any?,
) {
    constructor(
        methodName: String,
        vararg args: Any?,
    ) : this(
        methodName = methodName,
        argsMethodName = "__${methodName}_args",
        resMethodName = "__${methodName}_res",
        *args
    )

    private val jsCode =
        """
        |{
            |$coreName.$methodName(
                |${List(args.size) { "await $argsMethodName()," }.joinToString("")}
                |$resMethodName,
            |)
        |}
        |""".trimMargin()

    suspend inline operator fun <reified T> JSO.invoke(): T {
        return quickJS.run {
            withContext(singleThreadDispatcher) {
                ensureExist(methodName) {
                    JSBridge<T>(args.size).also { bridge ->
                        asyncFunction(argsMethodName) {
                            bridge.args.receive()
                        }
                        asyncFunction<T, Unit>(resMethodName) {
                            bridge.result.send(it)
                        }
                    }
                }.also { bridge ->
                    for (it in args) {
                        bridge.args.send(it)
                    }
                    evaluate<Any?>(jsCode)
                }
            }.result.receive()
        }
    }
}

private fun QuickJs.toJS(singleThreadDispatcher: CoroutineDispatcher): JS = object : JSO(
    this,
    singleThreadDispatcher,
) {
    override suspend fun hello() {
        return run JSO@{
            JSLiteral(::hello.name).run {
                this@JSO()
            }
        }
    }

    override suspend fun world(): String {
        return run JSO@{
            JSLiteral(::world.name).run {
                this@JSO()
            }
        }
    }

    override suspend fun version(): Int {
        return run JSO@{
            JSLiteral(::version.name).run {
                this@JSO()
            }
        }
    }

    override suspend fun ms(money: Money): String {
        return run JSO@{
            JSLiteral(::ms.name, json.encodeToString(money)).run {
                this@JSO()
            }
        }
    }
}
