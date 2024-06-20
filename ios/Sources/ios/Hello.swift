import Foundation
import CryptoKit

@objc
public class Hello: NSObject {
    @objc(hello:) public class func hello(who: String) -> String? {
        guard let inputData = who.data(using: .utf8) else {
            return nil
        }
        let hash = SHA512.hash(data: inputData)
        let base64 = Data(hash).base64EncodedString()
        return "hello, " + base64
    }
}
