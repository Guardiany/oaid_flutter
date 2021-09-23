import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:oaid_flutter/oaid_flutter.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  dynamic _sdkVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    dynamic sdkVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await OaidFlutter.platformVersion ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }
    try {
      sdkVersion =
          await OaidFlutter.sdkVersion ?? 'Unknown sdk version';
    } on PlatformException {
      sdkVersion = 'Failed to get sdk version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
      _sdkVersion = sdkVersion;
    });
  }

  String? _oaid = '';
  String? _vaid = '';
  String? _aaid = '';

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Running on: $_platformVersion\nSdk version:$_sdkVersion'),
              Text('oaid: $_oaid \nvaid: $_vaid \naaid: $_aaid'),
              TextButton(onPressed: () async {
                bool? init = await OaidFlutter.init(certFileName: 'com.ahd.ahd_fun_camera.cert.pem');
                print(init);
              }, child: Text('初始化')),
              TextButton(onPressed: () async {
                _oaid = await OaidFlutter.oaid;
              }, child: Text('获取oaid')),
              TextButton(onPressed: () async {
                _vaid = await OaidFlutter.vaid;
              }, child: Text('获取vaid')),
              TextButton(onPressed: () async {
                _aaid = await OaidFlutter.aaid;
              }, child: Text('获取aaid')),
            ],
          ),
        ),
      ),
    );
  }
}
