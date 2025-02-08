# QR コード生成アプリ

このアプリは、入力された URL から ZXing を利用して QR コードを生成し、中央にアイコンを配置する Java Swing アプリケーションです。

## フォルダ構成

- **src/**: ソースコード
  - **com.example.Main.java**: アプリケーションエントリーポイント
  - **com.example.view.MainFrame.java**: Swing のメイン画面
  - **com.example.model.QRCodeGenerator.java**: ZXing を利用した QR コード生成クラス
  - **com.example.util.ImageUtil.java**: 画像加工用ユーティリティクラス（中央にアイコンを重ねる処理）
- **lib/**: ZXing の jar ファイル（例: zxing-core-3.4.1.jar）
- **resources/**: アイコン画像などのリソース
- **README.md**: プロジェクト概要

## 必要なライブラリ

- [ZXing](https://github.com/zxing/zxing) (zxing-core-3.4.1.jar 等)

## 実行方法

1. `lib` フォルダ内の ZXing ライブラリをクラスパスに追加してください。
2. プロジェクトルートからビルド＆実行してください。
