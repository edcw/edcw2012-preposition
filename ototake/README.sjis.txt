EDCW2012 前置詞・動詞誤り検出プログラム README
乙武 北斗 （OTOTAKE Hokuto）
2012/08/09：新規作成

========================
 概要
========================

  本システムは，Error Detection and Correction Workshop 2012 （EDCW2012）
における前置詞・動詞トラックの誤り検出を目的としたものです．
前置詞・動詞ともに，単一のプログラムで誤り検出を行います．

  本システムはC#によって書かれています．
動作環境はWindowsおよびLinuxで，以下の環境にて動作が正常に行えることを確認しています．
  ・Windows 7 x64 with .NET Framework 4.0
  ・Ubuntu Server 12.04 x64 with Mono 2.10.8.1

  以降の説明では，本システムのルートディレクトリを
    {project_root}/
  と記述します．


=========================================
 必要なサードパーティのモジュール・ライブラリ
=========================================

  本システムをビルドする前に，いくつかのサードパーティモジュールを準備する必要があります．
以下に必要なモジュールの詳細について述べます．


CLR またはその互換ランタイム
--------------------------
  Windowsの場合は，Windows Update等から .NET Framework 4.0以上をインストールしてください．
  Linuxの場合は，Mono (http://www.mono-project.com/) 2.10以上をインストールしてください．
  Ubuntuの場合，aptを用いて以下のようにインストールできます．

      apt-get install mono-complete


IKVM.NET (http://www.ikvm.net/)
--------
  後述する Apache OpenNLP をC#で使うために必要なモジュールです．
  公式サイトよりバイナリを含むZIPファイルをダウンロードしてください．
  展開して作成された bin ディレクトリに必要なモジュールがいくつかあります．
  以下のファイルを {project_root}/lib ディレクトリにコピーしてください．
    
	IKVM.OpenJDK.Beans.dll		IKVM.OpenJDK.Charsets.dll		IKVM.OpenJDK.Corba.dll
	IKVM.OpenJDK.Core.dll		IKVM.OpenJDK.Jdbc.dll			IKVM.OpenJDK.Management.dll
	IKVM.OpenJDK.Media.dll		IKVM.OpenJDK.Misc.dll			IKVM.OpenJDK.Naming.dll
	IKVM.OpenJDK.Remoting.dll	IKVM.OpenJDK.Security.dll		IKVM.OpenJDK.SwingAWT.dll
	IKVM.OpenJDK.Text.dll		IKVM.OpenJDK.Tools.dll			IKVM.OpenJDK.Util.dll
	IKVM.OpenJDK.XML.API.dll	IKVM.OpenJDK.XML.Bind.dll		IKVM.OpenJDK.XML.Crypto.dll
	IKVM.OpenJDK.XML.Parse.dll	IKVM.OpenJDK.XML.Transform.dll	IKVM.OpenJDK.XML.WebServices.dll
	IKVM.OpenJDK.XML.XPath.dll	IKVM.Reflection.dll				IKVM.Runtime.dll


Apache OpenNLP (http://opennlp.apache.org/)
--------------
  公式サイトより，バイナリパッケージをダウンロードしてください．
  (執筆時現在だと，apache-opennlp-1.5.2-incubating-bin)

  ダウンロードした後，圧縮ファイルを展開してください．
  すると，その中に lib ディレクトリが作成されます．
  以下，その lib ディレクトリを {opennlp_dir}/lib と表記します．

  Apache OpenNLPはJavaプログラムであるため，先ほど導入したIKVMを用いて.NETアセンブリに変換する必要があります．
  IKVMのbinディレクトリに移動する，もしくはパスを通した後に，以下のコマンドを実行してください．

  * Windows .NET Framework の場合
    ikvmc -out:OpenNlp.dll {opennlp_dir}/lib/*.jar

  * Linux Mono の場合
    mono ikvmc.exe -out:OpenNlp.dll {opennlp_dir}/lib/*.jar

  コマンド実行後，カレントディレクトリに OpenNlp.dll というファイルが作成されます．
  このファイルを {project_root}/lib ディレクトリにコピーしてください．


SGMLReader (https://github.com/MindTouch/SGMLReader)
----------
  githubより，バイナリパッケージをダウンロードしてください．
  その中に含まれる SgmlReaderDll.dll ファイルを {project_root}/lib ディレクトリにコピーしてください．


DotNetZip (http://dotnetzip.codeplex.com/)
---------
  公式サイトより，バイナリパッケージをダウンロードしてください．
  圧縮ファイルを展開した後，以下のファイルを {project_root}/lib ディレクトリにコピーしてください．

    {DotNetZip_dir}/Tools/Ionic.Zip.dll


ConsoleLib (https://bitbucket.org/shokai/consolelibnet)
----------
  上記URLのbitbucketにアクセスし，タブメニューから Source を選択してください．
  すると，プロジェクトのディレクトリ構成が表示されますので，以下のように辿り，
  ConsoleLib.dll ファイルをダウンロードし，{project_root}/lib ディレクトリにコピーしてください．

    ConsoleLib.NET/ConsoleLib/bin/Release/ConsoleLib.dll


Classias (http://www.chokkan.org/software/classias/index.html.ja)
--------
  本システムで前置詞誤りの検出を行う際に，機械学習実装のClassiasを用います．
  公式サイトよりダウンロードして，インストールを行ってください．


=========================================
 データ
=========================================

  本システムの動作に必要なデータは以下の通りです．
    
	・Apache OpenNLP 解析モデル
	・WordNet データベースファイル
	・前置詞分類モデルファイル（前置詞誤り検出時のみ）

  以降，それぞれについて詳細を説明します．


Apache OpenNLP 解析モデル (http://opennlp.sourceforge.net/models-1.5/)
------------------------
  上記URLから，以下の英語用解析モデルファイルをダウンロードし，
  任意のディレクトリにまとめて配置してください．

    en-token.bin		en-sent.bin				en-pos-maxent.bin
	en-chunker.bin		en-ner-date.bin			en-ner-location.bin
	en-ner-money.bin	en-ner-organization.bin	en-ner-percentage.bin
	en-ner-person.bin	en-ner-time.bin

  ただし，POSタグ付けモデルの en-pos-maxent.bin は，そのままでは不具合があって使用できません．
  以下の手順で修正を行ってください．

    1. en-pos-maxent.bin をZIPファイルとみなして展開する
	2. manifest.properties, tags.tagdict, pos.model の3つのファイルが展開される
	3. tags.tagdict を除く2ファイルを再度ZIP圧縮する（つまり含まれるファイルは manifest.properties, pos.model）
	4. 3.で作成したZIPファイルを en-pos-maxent-fix.bin という名前で，他のモデルファイルと同じディレクトリに保存する


WordNet データベースファイル (http://wordnet.princeton.edu/wordnet/download/current-version/)
----------------------
  上記URLより，WordNet 3.0 for UNIX-like systems の中にある，
  Download just database files: WNdb-3.0.tar.gz
  をダウンロードし，展開してください．

  展開後，作成されるすべてのファイルを含めてZIP圧縮を行ってください．
  最後に，そのZIPファイルを wn.zip という名前で保存し，
  OpenNLPの解析モデルファイルと同じディレクトリに配置してください．


前置詞分類モデルファイル（前置詞誤り検出時のみ）
----------------------------------------
  既に学習済みの前置詞モデルファイルを以下の場所に付属しています．

    {project_root}/prp_models/

  モデルファイルは，対象前置詞ごとに用意されており，以下の9ファイルです．

    about.train.model	at.train.model		by.train.model
	for.train.model		from.train.model	in.train.model
	of.train.model		on.train.model		to.train.model

  付属の前置詞モデルと同一のものを作成する場合，以下のリソースが必要となります．
    
	・英辞郎 Ver. 128 付属の例文集 「例辞郎」
	・EDR電子化辞書 Ver. 2.0 英語コーパス

  これらリソース，または他のリソースから前置詞モデルを構築する方法については，後述の「使い方」をご覧ください．


=========================================
 システムのビルド方法
=========================================

  各環境で本システムをビルドする方法を説明します．
  ビルドの際は，前述した事前に必要なモジュールを指定の場所に準備しておいてください．

Windows
-------
  Visual Studioを利用する方法と，MSBuildを利用する方法の2つがあります．

  Visual Studioのバージョン2010以上であれば，ソリューションファイル OtotakeEdcw2012.sln を直接開くことが可能です．
  ソリューションをReleaseビルドしてください．

  .NET Frameworkに付属するMSBuildを用いてビルドすることも可能です．
  .NET Framework 4.0 がインストール済みであれば，以下の場所にMSBuildの実行ファイルがあるはずです．

    C:\Windows\Microsoft.NET\Framework\v4.0.30319\MSBuild.exe

  上記のMSBuild.exeを任意の場所で実行できるよう，ディレクトリを環境変数PATHに追加してください．
  その後，本システムをビルドするには，コマンドプロンプトで以下の手順を踏んでください．

    cd {project_root}
	msbuild.exe /p:Configuration=Release

Linux
-----
  Monoに付属するMSBuild互換プログラムの xbuild を使用してビルドを行います．
  本システムをビルドするには，以下のコマンドを順に入力してください．

    cd {project_root}
	xbuild /p:Configuration=Release


=========================================
 使い方
=========================================

  前述した手順でビルドを完了すると，以下の場所に本システムの実行ファイルが作成されます．

    {project_root}/Console/bin/Release/Console.exe

  Windowsで.NET Frameworkの環境の場合は，上記実行ファイルは直接実行可能です．
  Monoを利用する場合は，実行の際は以下のように入力してください．

    mono Console.exe

  本プログラムは第一パラメータによって，複数の機能を提供します．
  パラメータと機能の関係は以下の通りです．

    Console.exe d :	KJ-Corpusを対象とした誤り検出
	Console.exe t :	前置詞用トレーニングデータ作成
	Console.exe r :	特定形式のデータの生テキスト変換

  以降，各機能の詳細を説明します．

KJ-Corpusを対象とした誤り検出
----------------------------
  * 実行方法

    Console.exe d [--target TYPE (prp | v_agr)] [--kjdir KJ-CORPUS-DIR]
                  [--formal] [--res RESOURCE-DIR]
                  [--out OUTPUT-FILE] [--model MODELS-DIR]
				  [--classias PATH-TO-CLASSIAS-TAG]

  * 各オプションについて
    
	--target TYPE
		誤り検出対象を指定する．prp (前置詞) もしくは，v_agr (動詞)

	--kjdir KJ-CORPUS-DIR
		誤り検出対象のKJ Corpusの corpus_data ディレクトリパスを指定する．

	--formal
		フォーマルラン用スイッチ．
		指定したKJ Corpusに .edc と .pos ファイルしか存在しない場合は，本オプションを指定する．

	--res RESOURCE-DIR
		OpenNLP，およびWordNetのリソースファイルがあるディレクトリを指定する．

	--out OUTPUT-FILE
		誤り検出結果の出力ファイル名を指定する．
		結果はZIPファイルとなる．

	--model MODELS-DIR
		(前置詞誤りのときのみ必要)
		前置詞分類モデルファイルがあるディレクトリを指定する．

	--classias PATH-TO-CLASSIAS-TAG
		機械学習実装であるClassiasの分類器プログラムである classias-tag のフルパスを指定します．
		省略された場合，/usr/local/bin/classias-tag とみなして実行します．


前置詞用トレーニングデータ作成
-------------------------
  生英文テキストを入力とし，各前置詞に対応したClassias二値分類用トレーニングデータを出力します．
  出力されるファイルは以下の通りです．

    about.train	at.train	by.train
	for.train	from.train	in.train  
	of.train	on.train	to.train

  * 実行方法

    Console.exe t [--out MODELS-OUTPUT-DIR] [--src SOURCE-TEXT-FILE]
                  [--res RESOURCE-DIR]

  * 各オプションについて

    --out DATA-OUTPUT-DIR
		各前置詞のトレーニングデータを出力するディレクトリを指定する．

	--src SOURCE-TEXT-FILE
		ソースとなる生テキストファイルを指定する．

	--res RESOURCE-DIR
		OpenNLP，およびWordNetのリソースファイルがあるディレクトリを指定する．

  * トレーニングデータから前置詞モデルを作成する方法
    
	作成したトレーニングデータ各々について，Classiasの学習器プログラムである classias-train を適用させることで，
	各前置詞の二値分類モデルを作成します．
	classias-train へのパスが通っていることを確認し，以下のコマンドを各トレーニングデータに対して行ってください．
	({prp} は 各前置詞に置き換えてください．)

	  classias-train -tb -a lbfgs.logistic -m {prp}.train.model {prp}.train

	結果として作成されるモデルファイルは，すべて同一のディレクトリに保存しておいてください．


特定形式のデータの生テキスト変換
---------------------------
  特定形式のデータを，トレーニングデータ作成用の生テキストに変換します．

  * 実行方法

    Console.exe r [--type DATA-TYPE (edr | reijiro)] [--file DATA-FILE]

  * 各オプションについて
    
	--type DATA-TYPE
		データ形式を指定する．
		現在対応可能な形式は以下の通り．
			edr:		EDR電子化辞書 Ver. 2.0 英語コーパス (ECO.DIC)
			reijiro:	英辞郎付属の例文集 「例辞郎」 (Ver.128であれば，REIJI128.TXT)
						（ただし，REIJIxxx.TXTがUTF-8以外の文字コードで記述されている場合，
						  UTF-8に変換してから，本プログラムを適用する必要がある．）

	--file DATA-FILE
		データファイルを指定する．