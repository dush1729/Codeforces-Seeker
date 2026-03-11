import Foundation
import ComposeApp
import FirebaseAnalytics
import FirebaseCrashlytics
import FirebaseRemoteConfig

class SwiftAnalyticsBridge: AnalyticsBridge {
    func logEvent(name: String, params: [String: Any]) {
        // Convert Kotlin numeric types to NSNumber for Firebase
        var firebaseParams: [String: Any] = [:]
        for (key, value) in params {
            if let number = value as? NSNumber {
                firebaseParams[key] = number
            } else if let string = value as? String {
                firebaseParams[key] = string
            } else {
                firebaseParams[key] = "\(value)"
            }
        }
        Analytics.logEvent(name, parameters: firebaseParams.isEmpty ? nil : firebaseParams)
    }
}

class SwiftCrashlyticsBridge: CrashlyticsBridge {
    func recordException(message: String, stackTrace: String) {
        let error = NSError(
            domain: "com.dush1729.cfseeker",
            code: 0,
            userInfo: [
                NSLocalizedDescriptionKey: message,
                "stack_trace": stackTrace
            ]
        )
        Crashlytics.crashlytics().record(error: error)
    }

    func log(message: String) {
        Crashlytics.crashlytics().log(message)
    }

    func setCustomKeyString(key: String, value: String) {
        Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }

    func setCustomKeyInt(key: String, value: Int32) {
        Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }

    func setCustomKeyBool(key: String, value: Bool) {
        Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }
}

class SwiftRemoteConfigBridge: RemoteConfigBridge {
    private let remoteConfig: RemoteConfig

    init() {
        remoteConfig = RemoteConfig.remoteConfig()

        // Set defaults matching Android remote_config_defaults.xml
        remoteConfig.setDefaults([
            "add_user_enabled": true as NSObject,
            "sync_all_users_enabled": true as NSObject,
            "sync_user_enabled": true as NSObject,
            "sync_all_cooldown_minutes": 5 as NSObject,
            "sync_all_user_delay_seconds": 2 as NSObject,
            "contest_refresh_interval_minutes": 30 as NSObject,
            "contest_standings_refresh_interval_minutes": 10 as NSObject,
            "users_info_refresh_interval_minutes": 30 as NSObject
        ])

        let settings = RemoteConfigSettings()
        #if DEBUG
        settings.minimumFetchInterval = 0
        #else
        settings.minimumFetchInterval = 3600 // 1 hour
        #endif
        remoteConfig.configSettings = settings
    }

    func fetchAndActivate(callback: any FetchCallback) {
        remoteConfig.fetchAndActivate { status, error in
            if let error = error {
                print("RemoteConfig: fetch error - \(error.localizedDescription)")
                callback.onResult(success: false)
            } else {
                callback.onResult(success: status != .error)
            }
        }
    }

    func getString(key: String) -> String {
        return remoteConfig.configValue(forKey: key).stringValue ?? ""
    }

    func getBoolean(key: String) -> Bool {
        return remoteConfig.configValue(forKey: key).boolValue
    }

    func getLong(key: String) -> Int64 {
        return remoteConfig.configValue(forKey: key).numberValue.int64Value
    }

    func getDouble(key: String) -> Double {
        return remoteConfig.configValue(forKey: key).numberValue.doubleValue
    }
}
