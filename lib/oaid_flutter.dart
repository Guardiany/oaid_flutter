
import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class OaidFlutter {
  static const MethodChannel _channel =
      const MethodChannel('oaid_flutter');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<dynamic> get sdkVersion async {
    if (Platform.isIOS) {
      return null;
    }
    return await _channel.invokeMethod('getSdkVersion');
  }

  static Future<bool?> init({
    required String certFileName,
    bool isLogOn = false,
  }) async {
    if (Platform.isIOS) {
      return false;
    }
    return await _channel.invokeMethod('init', {
      'certFileName':certFileName,
      'isLogOn':isLogOn
    });
  }

  static Future<bool?> launchApp({
    required String packageName,
  }) async {
    return await _channel.invokeMethod('launch', {
      'packageName': packageName,
    });
  }

  static Future<String?> get imei async {
    if (Platform.isIOS) {
      return null;
    }
    return await _channel.invokeMethod('getImei');
  }

  static Future<String?> get ua async {
    if (Platform.isIOS) {
      return null;
    }
    return await _channel.invokeMethod('getUa');
  }

  static Future<String?> get oaid async {
    if (Platform.isIOS) {
      return null;
    }
    return await _channel.invokeMethod('getOaid');
  }

  static Future<String?> get vaid async {
    if (Platform.isIOS) {
      return null;
    }
    return await _channel.invokeMethod('getVaid');
  }

  static Future<String?> get aaid async {
    if (Platform.isIOS) {
      return null;
    }
    return await _channel.invokeMethod('getAaid');
  }
}
