using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ototake.Edcw.OpenNlp
{
    #region 挿入物構造体
    /// <summary>
    /// 一つの文字列挿入物を表すクラス
    /// </summary>
    public class InsertSpan
    {
        /// <summary>
        /// 実際に挿入する文字列
        /// </summary>
        public string Value { get; set; }

        /// <summary>
        /// 挿入位置
        /// </summary>
        public int Begin { get; set; }

        public CharBasedPosition BeginChar { get; set; }
    }
    #endregion

    #region TagSpan構造体
    /// <summary>
    /// 文字列におけるタグ位置を表すクラス
    /// </summary>
    public class TagSpan
    {
        /// <summary>
        /// タグ名
        /// </summary>
        public string TagName { get; set; }

        /// <summary>
        /// 開始タグ位置
        /// </summary>
        public int Begin { get; set; }

        /// <summary>
        /// 終了タグ位置
        /// </summary>
        public int End { get; set; }

        /// <summary>
        /// 開始タグ位置CharBased
        /// </summary>
        public CharBasedPosition BeginChar { get; set; }

        /// <summary>
        /// 終了タグ位置CharBased
        /// </summary>
        public CharBasedPosition EndChar { get; set; }

        /// <summary>
        /// 開始タグと終了タグを表すInsertSpanを取得する
        /// </summary>
        /// <returns></returns>
        public InsertSpan[] GetInsertSpans()
        {
            if (this.Begin != this.End)
            {
                return new[]
                {
                    new InsertSpan {Begin = this.Begin, Value = string.Format("<{0}>", TagName), BeginChar = this.BeginChar },
                    new InsertSpan {Begin = this.End, Value = string.Format("</{0}>", TagName), BeginChar = this.EndChar },
                };
            }
            else
            {
                return new[] { new InsertSpan{
                    Begin = this.Begin, 
                    Value = string.Format("<{0}></{0}> ", TagName)
                }};
            }
        }

        /// <summary>
        /// 開始位置と終了位置の組合せからTagSpanを作る
        /// </summary>
        /// <param name="startEndPair"></param>
        public TagSpan(Tuple<int, int, CharBasedPosition, CharBasedPosition> beginEndPair, string tagName)
        {
            TagName = tagName;
            Begin = beginEndPair.Item1;
            End = beginEndPair.Item2;
            BeginChar = beginEndPair.Item3;
            EndChar = beginEndPair.Item4;
        }

        /// <summary>
        /// 開始位置と終了位置の組合せからTagSpanを作る
        /// </summary>
        /// <param name="startEndPair"></param>
        public TagSpan(Tuple<int, int> beginEndPair, string tagName)
        {
            TagName = tagName;
            Begin = beginEndPair.Item1;
            End = beginEndPair.Item2;
            BeginChar = default(CharBasedPosition);
            EndChar = default(CharBasedPosition);
        }
    }
    #endregion

    #region TagInsertableString
    /// <summary>
    /// タグの挿入が可能な文字列クラス
    /// </summary>
    public sealed class TagInsertableString
    {
        #region プロパティ
        /// <summary>
        /// 文字列本体
        /// </summary>
        public string BaseText { get; private set; }

        /// <summary>
        /// 挿入するTagSpan
        /// </summary>
        public List<TagSpan> TagSpans { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// 指定した文字列をベースとするインスタンスを生成する．
        /// </summary>
        /// <param name="baseString"></param>
        public TagInsertableString(string baseString)
        {
            BaseText = baseString;
            TagSpans = new List<TagSpan>();
        }
        #endregion

        #region メソッド
        /// <summary>
        /// タグの挿入を実行する．
        /// </summary>
        /// <returns></returns>
        public string ExecuteInsert()
        {
            // 挿入位置順に並べる
            var inspans = TagSpans.SelectMany(x => x.GetInsertSpans())
                                  .OrderBy(x => x.Begin)
                                  .ToArray();

            // 挿入していく
            var ret = new StringBuilder(BaseText);
            for (int i = 0; i < inspans.Length; i++)
            {
                Insert(ret, inspans, i);
            }

            return ret.ToString();
        }

        /// <summary>
        /// 文字列への暗黙型変換
        /// </summary>
        /// <param name="tstr"></param>
        /// <returns></returns>
        public static implicit operator string(TagInsertableString tstr)
        {
            return tstr.ExecuteInsert();
        }
        #endregion

        #region プライベートメソッド
        /// <summary>
        /// spans[currentIndex]が示す挿入をsbに対して行う．
        /// その際，後続するspansの位置調整を行う．
        /// </summary>
        /// <param name="sb"></param>
        /// <param name="spans"></param>
        /// <param name="currentIndex"></param>
        private void Insert(StringBuilder sb, InsertSpan[] spans, int currentIndex)
        {
            var span = spans[currentIndex];

            // 挿入操作
            try { sb.Insert(span.Begin, span.Value); }
            catch
            {
                Console.Error.WriteLine("--- span.Begin = {0}, Value = \"{1}\"", span.Begin, span.Value);
                Console.Error.WriteLine("----- char = '{0}', times = {1}", span.BeginChar.Letter, span.BeginChar.Times);
                Console.Error.WriteLine("----- BaseText: \"{0}\"", BaseText);
                Console.Error.WriteLine("----- StringBuilder: \"{0}\"", sb.ToString());
                throw;
            }

            // 後続スパンの調整
            for (int i = currentIndex + 1; i < spans.Length; i++)
            {
                // さっき挿入した文字列の長さだけずれる
                spans[i].Begin += span.Value.Length;
            }
        }
        #endregion
    }
    #endregion
}
