@OptIn(ExperimentalJsExport::class)
@JsExport
val jsCoreV = JSCoreT()

@OptIn(ExperimentalJsExport::class)
@JsExport
fun main() {
    globalThis.jsCoreV = jsCoreV
}