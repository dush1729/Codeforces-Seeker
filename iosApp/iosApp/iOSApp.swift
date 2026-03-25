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
        let firestoreBridge = SwiftFirestoreBridge()

        MainViewControllerKt.doInitKoin(
            analyticsBridge: analyticsBridge,
            crashlyticsBridge: crashlyticsBridge,
            remoteConfigBridge: remoteConfigBridge,
            firestoreBridge: firestoreBridge
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
