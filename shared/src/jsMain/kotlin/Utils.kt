val isNodeJS: Boolean get() = globalThis.process != null
val isBrowser: Boolean get() = globalThis.self != null