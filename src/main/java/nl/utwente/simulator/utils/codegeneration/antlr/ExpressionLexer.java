package nl.utwente.simulator.utils.codegeneration.antlr;// Generated from E:/Google Drive/Final Project/simulator/src/main/resources\Expression.g4 by ANTLR 4.7
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ExpressionLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		DIVISION=1, LPAREN=2, MINUS=3, PI=4, SQRT=5, PLUS=6, POWER=7, RPAREN=8, 
		TIMES=9, NUMBER=10, VAR=11, WS=12;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"DIVISION", "LPAREN", "MINUS", "PI", "SQRT", "PLUS", "POWER", "RPAREN", 
		"TIMES", "NUMBER", "VAR", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'/'", "'('", "'-'", null, null, "'+'", "'^'", "')'", "'*'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "DIVISION", "LPAREN", "MINUS", "PI", "SQRT", "PLUS", "POWER", "RPAREN", 
		"TIMES", "NUMBER", "VAR", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public ExpressionLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Expression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\16R\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\5\5\5%\n\5\3\6\3"+
		"\6\3\6\3\6\3\6\5\6,\n\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\6\13\67\n"+
		"\13\r\13\16\138\3\13\3\13\6\13=\n\13\r\13\16\13>\5\13A\n\13\3\f\3\f\3"+
		"\f\3\f\3\f\3\f\3\f\5\fJ\n\f\3\r\6\rM\n\r\r\r\16\rN\3\r\3\r\2\2\16\3\3"+
		"\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\3\2\5\3\2\62;\4"+
		"\2C\\c|\5\2\13\f\17\17\"\"\2Z\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t"+
		"\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2"+
		"\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\3\33\3\2\2\2\5\35\3\2\2\2\7"+
		"\37\3\2\2\2\t$\3\2\2\2\13+\3\2\2\2\r-\3\2\2\2\17/\3\2\2\2\21\61\3\2\2"+
		"\2\23\63\3\2\2\2\25\66\3\2\2\2\27I\3\2\2\2\31L\3\2\2\2\33\34\7\61\2\2"+
		"\34\4\3\2\2\2\35\36\7*\2\2\36\6\3\2\2\2\37 \7/\2\2 \b\3\2\2\2!\"\7r\2"+
		"\2\"%\7k\2\2#%\7\u03c2\2\2$!\3\2\2\2$#\3\2\2\2%\n\3\2\2\2&\'\7u\2\2\'"+
		"(\7s\2\2()\7t\2\2),\7v\2\2*,\7\u221c\2\2+&\3\2\2\2+*\3\2\2\2,\f\3\2\2"+
		"\2-.\7-\2\2.\16\3\2\2\2/\60\7`\2\2\60\20\3\2\2\2\61\62\7+\2\2\62\22\3"+
		"\2\2\2\63\64\7,\2\2\64\24\3\2\2\2\65\67\t\2\2\2\66\65\3\2\2\2\678\3\2"+
		"\2\28\66\3\2\2\289\3\2\2\29@\3\2\2\2:<\7\60\2\2;=\t\2\2\2<;\3\2\2\2=>"+
		"\3\2\2\2><\3\2\2\2>?\3\2\2\2?A\3\2\2\2@:\3\2\2\2@A\3\2\2\2A\26\3\2\2\2"+
		"BC\7G\2\2CJ\7E\2\2DE\7G\2\2EJ\7P\2\2FG\7O\2\2GJ\7E\2\2HJ\t\3\2\2IB\3\2"+
		"\2\2ID\3\2\2\2IF\3\2\2\2IH\3\2\2\2J\30\3\2\2\2KM\t\4\2\2LK\3\2\2\2MN\3"+
		"\2\2\2NL\3\2\2\2NO\3\2\2\2OP\3\2\2\2PQ\b\r\2\2Q\32\3\2\2\2\n\2$+8>@IN"+
		"\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}