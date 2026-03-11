import SwiftUI
import ComposeApp
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()

        let analyticsBridge = SwiftAnalyticsBridge()
        let crashlyticsBridge = SwiftCrashlyticsBridge()
        let remoteConfigBridge = SwiftRemoteConfigBridge()

        MainViewControllerKt.doInitKoin(
            analyticsBridge: analyticsBridge,
            crashlyticsBridge: crashlyticsBridge,
            remoteConfigBridge: remoteConfigBridge
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
