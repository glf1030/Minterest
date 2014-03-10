package util;

import java.util.Arrays;
import java.util.HashSet;

public class Stopwords {
	public static HashSet<Character> getStoplist(){
		return stopwords;
	}
	private static final Character[] CH_STOPWORD = new Character[] { 
		'的',
		'一',
		'不',
		'在',
		'人',
		'有',
		'是',
		'为',
		'以',
		'于',
		'上',
		'他',
		'而',
		'后',
		'之',
		'来',
		'及',
		'了',
		'因',
		'下',
		'可',
		'到',
		'由',
		'这',
		'与',
		'也',
		'此',
		'但',
		'并',
		'个',
		'其',
		'已',
		'无',
		'小',
		'我',
		'们',
		'起',
		'最',
		'再',
		'今',
		'去',
		'好',
		'只',
		'又',
		'或',
		'很',
		'亦',
		'某',
		'把',
		'那',
		'你',
		'乃',
		'它',
		'吧',
		'被',
		'比',
		'别',
		'趁',
		'当',
		'从',
		'到',
		'得',
		'打',
		'凡',
		'儿',
		'尔',
		'该',
		'各',
		'给',
		'跟',
		'和',
		'何',
		'还',
		'即',
		'几',
		'既',
		'看',
		'据',
		'距',
		'靠',
		'啦',
		'了',
		'另',
		'么',
		'每',
		'们',
		'嘛',
		'拿',
		'哪',
		'那',
		'您',
		'凭',
		'且',
		'却',
		'让',
		'仍',
		'啥',
		'如',
		'若',
		'使',
		'谁',
		'虽',
		'随',
		'同',
		'所',
		'她',
		'哇',
		'嗡',
		'往',
		'哪',
		'些',
		'向',
		'沿',
		'哟',
		'用',
		'于',
		'咱',
		'则',
		'怎',
		'曾',
		'至',
		'致',
		'着',
		'诸',
		'自',
	};
	
	private static final HashSet<Character> stopwords = new HashSet<Character>(Arrays.asList(CH_STOPWORD));
	
}
