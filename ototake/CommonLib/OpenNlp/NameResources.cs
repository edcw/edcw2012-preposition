using System.Collections.Generic;
using System.IO;
using System.Linq;
using Ototake.Edcw.OpenNlp.Wrapper;

namespace Ototake.Edcw.OpenNlp
{
    /// <summary>
    /// エンティティ名解析のリソース群
    /// </summary>
    public class NameResources
    {
        #region 定数
        public const string DefaultDateModel = @"en-ner-date.bin";
        public const string DefaultLocationModel = @"en-ner-location.bin";
        public const string DefaultMoneyModel = @"en-ner-money.bin";
        public const string DefaultOrgModel = @"en-ner-organization.bin";
        public const string DefaultPercentageModel = @"en-ner-percentage.bin";
        public const string DefaultPersonModel = @"en-ner-person.bin";
        public const string DefaultTimeModel = @"en-ner-time.bin";
        #endregion

        #region プロパティ
        public NameFinderModel DateModel { get; private set; }
        public NameFinderModel LocationModel { get; private set; }
        public NameFinderModel MoneyModel { get; private set; }
        public NameFinderModel OrgModel { get; private set; }
        public NameFinderModel PercentageModel { get; private set; }
        public NameFinderModel PersonModel { get; private set; }
        public NameFinderModel TimeModel { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// 指定したディレクトリからデフォルトのファイルを使用してモデルを構築する．
        /// </summary>
        /// <param name="dir"></param>
        public NameResources(string dir)
        {
            DateModel = new NameFinderModel(Path.Combine(dir, DefaultDateModel));
            LocationModel = new NameFinderModel(Path.Combine(dir, DefaultLocationModel));
            MoneyModel = new NameFinderModel(Path.Combine(dir, DefaultMoneyModel));
            OrgModel = new NameFinderModel(Path.Combine(dir, DefaultOrgModel));
            PercentageModel = new NameFinderModel(Path.Combine(dir, DefaultPercentageModel));
            PersonModel = new NameFinderModel(Path.Combine(dir, DefaultPersonModel));
            TimeModel = new NameFinderModel(Path.Combine(dir, DefaultTimeModel));
        }
        #endregion
    }

    /// <summary>
    /// 名前解析器集合
    /// </summary>
    public class NameAnalyzer
    {
        #region プロパティ
        public NameFinder DateFinder { get; private set; }
        public NameFinder LocationFinder { get; private set; }
        public NameFinder MoneyFinder { get; private set; }
        public NameFinder OrgFinder { get; private set; }
        public NameFinder PercentageFinder { get; private set; }
        public NameFinder PersonFinder { get; private set; }
        public NameFinder TimeFinder { get; private set; }
        #endregion

        #region コンストラクタ
        public NameAnalyzer(NameResources resources)
        {
            DateFinder = new NameFinder(resources.DateModel);
            LocationFinder = new NameFinder(resources.LocationModel);
            MoneyFinder = new NameFinder(resources.MoneyModel);
            OrgFinder = new NameFinder(resources.OrgModel);
            PercentageFinder = new NameFinder(resources.PercentageModel);
            PersonFinder = new NameFinder(resources.PersonModel);
            TimeFinder = new NameFinder(resources.TimeModel);
        }
        #endregion

        #region メソッド
        /// <summary>
        /// すべての名前解析器を列挙
        /// </summary>
        /// <returns></returns>
        public IEnumerable<NameFinder> All()
        {
            yield return DateFinder;
            yield return LocationFinder;
            yield return MoneyFinder;
            yield return OrgFinder;
            yield return PercentageFinder;
            yield return PersonFinder;
            yield return TimeFinder;
        }

        /// <summary>
        /// 指定した1単語に対して名前解析をする
        /// </summary>
        /// <param name="w"></param>
        /// <returns></returns>
        public string[] Find(Word w)
        {
            var ret = new HashSet<string>();

            // 名詞かどうか
            if (w.Tag.Length >= 2 && w.Tag.Substring(0, 2) == "NN")
            {
                foreach (var a in All())
                {
                    var spans = a.Find(new string[] { w.Text });
                    foreach (var span in spans)
                    {
                        ret.Add(span.getType());
                    }
                }
            }

            return ret.ToArray();
        }
        #endregion
    }
}
