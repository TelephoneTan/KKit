package pub.telephone.kkit

import io.ktor.http.content.LastModifiedVersion
import io.ktor.util.AttributeKey

object AttributeKey {
    val LastModified = AttributeKey<LastModifiedVersion>("LastModified")
    val PrivateOrNoCDN = AttributeKey<Boolean>("PrivateOrNoCDN")
    val IsFile = AttributeKey<Boolean>("IsFile")
    val ToCDN = AttributeKey<String>("ToCDN")
}