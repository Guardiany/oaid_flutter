# 移动安全联盟oaid sdk Flutter版本

## 简介
  oaid_flutter是一款集成了移动安全联盟oaid sdk的Flutter插件，只针对Android系统

## 官方文档
* [Android](http://www.msa-alliance.cn/)

## 集成步骤
#### 1、pubspec.yaml
```Dart
oaid_flutter:
  git: https://github.com/Guardiany/oaid_flutter.git
```

#### 2、Android
找到您的App⼯程下的libs⽂件夹，将oaid_sdk_1.0.27.aar拷⻉到该⽬录下；
将 supplierconfig.json 拷贝到项目 assets 目录下；
将证书文件（应用包名.cert.pem）拷贝到项目 assets 目录下；
在app的build.gradle⽂件中添加如下依赖:
```
dependencies {
    //oaid
    implementation files('libs/oaid_sdk_1.0.27.aar')
}
```

## 使用

#### 1、SDK初始化
```Dart
isInit = await OaidFlutter.init(certFileName: '证书文件名');
```
#### 2、获取SDK版本
```Dart
await oaid_flutter.sdkVersion;
```
#### 3、获取oaid
```Dart
///获取oaid
oaid = await OaidFlutter.oaid;

///获取vaid
vaid = await OaidFlutter.vaid;

///获取aaid
aaid = await OaidFlutter.aaid;
```

## 联系方式
* Email: 1204493146@qq.com
