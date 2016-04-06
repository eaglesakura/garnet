# Garnet

## 概要

 * 劣化Daggerだが。
 * 簡易的にオブジェクトの依存関係を構築する
 * テスト時に依存関係を上書きすることができる
 

## プロジェクト設定

```
// build.gradle

repositories {
    // add maven repository
    maven { url "http://eaglesakura.github.io/maven/" }
}

dependencies {
    // add library
    compile 'com.eaglesakura:garnet:0.1.+'
}
```

## LICENSE

プロジェクトの都合に応じて、下記のどちらかを選択してください。

* アプリ等の成果物で権利情報を表示可能な場合
	* 権利情報の表示を行う（行える）場合、MIT Licenseを使用してください。
	* [MIT License](LICENSE.txt)
* 何らかの理由で権利情報を表示不可能な場合
	* 何らかの事情によりライセンス表記を行えない場合、下記のライセンスで使用可能です。
	* ライブラリ内で依存している別なライブラリについては、必ずそのライブラリのライセンスに従ってください。
	* [NYSL(English)](LICENSE-NYSL-eng.txt)
	* [NYSL(日本語)](LICENSE-NYSL-jpn.txt)
