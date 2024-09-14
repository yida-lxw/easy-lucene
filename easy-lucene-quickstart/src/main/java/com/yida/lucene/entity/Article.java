package com.yida.lucene.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yida.lucene.annotation.AutoFill;
import com.yida.lucene.annotation.DocField;
import com.yida.lucene.annotation.DocId;
import com.yida.lucene.bean.LatLon;
import com.yida.lucene.constant.FieldType;
import com.yida.lucene.spring.annotation.ElEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * @author yida
 */
@Data
@ElEntity(value = "article", analyzerMappingJSONFilePath = "lucene/article.json")
public class Article {

	/**
	 * default data
	 */
	public static final List<Article> DATA = new ArrayList<>();

	static {
		Article article = new Article();
		article.setId(1L);
		article.setAuthor("新华每日电讯");
		article.setTitle("解放军无全面攻台能力？国防部驳斥");
		article.setAbContent("14日，国防部新闻发言人吴谦大校就“解放军尚未完全具备全面攻台能力”作出回应：中国人民解放军有可靠的手段让天...");
		article.setContent("中新社北京9月14日电 针对台湾防务部门近日言论，国防部新闻发言人吴谦14日回应指出，中国人民解放军有可靠的手段让天堑变通途，有强大的能力挫败“台独”武装的任何负隅顽抗。\n" +
				"\n" +
				"　　有记者提问，台防务部门近日称，解放军受限于台海的天然地理环境、登陆载具与后勤能力不足等因素，尚未完全具备全面攻台的正规作战能力。请问发言人有何评论？\n" +
				"\n" +
				"　　吴谦表示，民进党当局的说法荒唐可笑，纯属自欺欺人。祖国完全统一是历史必然、大势所趋，任何人任何势力都阻挡不了。中国人民解放军有可靠的手段让天堑变通途，有强大的能力挫败“台独”武装的任何负隅顽抗，坚决捍卫国家主权和领土完整。");
		article.setLatLon(LatLon.of(29.314684, 120.087596));
		DATA.add(article);

		article = new Article();
		article.setId(2L);
		article.setAuthor("红星新闻");
		article.setTitle("人民网评:“香港月饼香港买不到”,这该咋办");
		article.setAbContent("调查显示，这款名为香港美诚品牌的月饼，品牌运营方为广州市美诚食品有限公司，该公司注册于香港，但多名代理商均表示");
		article.setContent("近日，市场监管总局召开合规推进会，向主要网络交易平台作出五方面合规提示：严格落实平台主体责任，严格规范节日期间促销行为，严格规范直播营销行为，严格禁止销售违法违禁商品，妥善化解网络消费纠纷。其中明确提到，重点把控“网红”“私房”月饼、螃蟹等节令商品的质量，防范经营假冒伪劣商品行为。\n" +
				"\n" +
				"“小饼如嚼月，中有酥和饴。”随着中秋佳节渐近，市场上各类月饼如“月”而至，大显身手，有颜值有“内涵”，品种齐全，越来越具个性化。但是，一些消费者也在担心那些热销的月饼品质有保证吗？特别是在网络平台上热销的所谓“网红”月饼值得信赖吗？");
		article.setLatLon(LatLon.of(29.270803, 120.005879));
		DATA.add(article);

		article = new Article();
		article.setId(3L);
		article.setAuthor("人民网");
		article.setTitle("菲方宣称不会离开仙宾礁 国防部回应");
		article.setAbContent("近日，菲国家海事委员会发言人称，菲方不会离开仙宾礁相关海域。国防部回应：谁在南海兴风作浪，中方都将坚决反制。");
		article.setContent("中新网9月14日电 9月14日下午，国防部新闻局局长、国防部新闻发言人吴谦大校就近期涉军问题发布消息。\n" +
				"有记者问：据报道，菲国家海事委员会发言人称，菲方不会离开仙宾礁相关海域，将继续组织巡逻补给。此外，针对中国对菲在南海活动采取的行动，美防长奥斯汀在与菲防长通话时重申了美对菲的坚定承诺。请问发言人有何评论？\n" +
				"吴谦回应称，包括仙宾礁在内的南沙群岛是中国固有领土，中方依法在相关海域开展维权执法行动，合理合法、专业规范。美国不是南海问题的当事方，无权介入中菲涉海争议，更不得以双边条约为借口损害中国的领土主权和海洋权益。当前南海局势局部升温反复，其根源在于菲律宾一再侵权挑衅、冒险妄为，在于美国不断拱火浇油、挑动对抗。需要强调的是，无论谁在南海兴风作浪，侵犯中方领土主权和海洋权益，中方都将采取有力有效措施坚决反制。");
		article.setLatLon(LatLon.of(29.670547, 120.065771));
		DATA.add(article);

		article = new Article();
		article.setId(4L);
		article.setAuthor("新华网");
		article.setTitle("8月份中国经济运行总体平稳");
		article.setAbContent("9月14日，国家统计局发布数据显示，8月份国民经济保持总体平稳、稳中有进发展态势。");
		article.setContent("8月份，国民经济保持总体平稳、稳中有进发展态势。\n" +
				"全国规模以上工业增加值同比增长4.5%；环比增长0.32%。社会消费品零售总额38726亿元，同比增长2.1%；环比下降0.01%。\n" +
				"9月14日，中国国务院新闻办公室在北京举行新闻发布会。国家统计局新闻发言人、总经济师、国民经济综合统计司司长刘爱华介绍2024年8月份国民经济运行情况，并答记者问。 中新社记者 杨可佳 摄\n" +
				"会上，有记者提问：今年以来，我国经济总体运行平稳，延续向好态势，请问您如何评价8月的经济表现？\n" +
				"对此，刘爱华表示，刚刚过去的8月份，国内外环境更趋复杂严峻，同时高温天气和暴雨洪涝等自然灾害持续对经济活动造成影响，面对困难挑战，各地区各部门深入贯彻落实党中央、国务院决策部署，宏观政策成效继续显现。\n" +
				"从判断宏观经济常用的四大指标增长、就业、物价、国际收支这四方面来看，经济运行总体平稳。与此同时，转型升级稳步推进，高质量发展继续取得新成效，经济运行延续稳中有进发展态势。\n" +
				"（总台央视记者 陈茜）");
		article.setLatLon(LatLon.of(28.088612, 103.907233));
		DATA.add(article);
	}

	@DocId
	private Long id;

	@DocField(type = FieldType.TEXT)
	private String title;

	@DocField(type = FieldType.TEXT)
	private String content;

	@DocField(type = FieldType.TEXT)
	private String abContent;

	@DocField(type = FieldType.TEXT)
	private String author;

	@AutoFill
	@DocField(type = FieldType.DATE)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
	private LocalDateTime time;

	@DocField(type = FieldType.BOOL)
	private boolean deleted;

	@DocField(type = FieldType.LATLON)
	private LatLon latLon;

}
