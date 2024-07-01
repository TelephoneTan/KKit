import KKit.shared.BuildConfig

@OptIn(ExperimentalJsExport::class)
@JsExport
class JSCoreT : JSI {
    override fun hello(res: (JSNothing) -> Unit) {
        world {
            console.log("hello, $it")
            res(null)
        }
    }

    override fun world(res: (String) -> Unit) {
        version {
            res("Powered by $jsCoreBrand $it ${getPlatform().name}")
        }
    }

    override fun version(res: (Int) -> Unit) {
        res(BuildConfig.JS_CORE_VERSION)
    }

    override fun ms(moneyStr: String, res: (String) -> Unit) {
        val money: Money = json.decodeFromString(moneyStr)
        res(money.symbol)
    }
}