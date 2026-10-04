package silence.simsool.profileviewer.api.data;

/** Arithmetic used by the bundled skill-tree catalog; no executable expressions. */
public final class CatalogFormula {
	private final String source;
	private int index;

	private CatalogFormula(String source) { this.source = source; }

	public static double evaluate(String source, int level, int effectiveLevel) {
		CatalogFormula parser = new CatalogFormula(source.replace("effectiveLevel", Integer.toString(effectiveLevel)).replace("level", Integer.toString(level)).replace(" ", ""));
		double result = parser.sum();
		if (parser.index != parser.source.length()) throw new IllegalArgumentException("Invalid catalog formula: " + source);
		return result;
	}

	private boolean take(char token) {
		if (index < source.length() && source.charAt(index) == token) { index++; return true; }
		return false;
	}

	private double sum() {
		double value = product();
		while (true) {
			if (take('+')) value += product();
			else if (take('-')) value -= product();
			else return value;
		}
	}

	private double product() {
		double value = power();
		while (true) {
			if (take('*')) value *= power();
			else if (take('/')) value /= power();
			else return value;
		}
	}

	private double power() {
		double value = atom();
		return take('^') ? Math.pow(value, power()) : value;
	}

	private double atom() {
		if (take('-')) return -atom();
		if (source.startsWith("floor(", index)) {
			index += 6;
			double value = sum();
			if (!take(')')) throw new IllegalArgumentException(source);
			return Math.floor(value);
		}
		if (take('(')) {
			double value = sum();
			if (!take(')')) throw new IllegalArgumentException(source);
			return value;
		}
		int start = index;
		while (index < source.length() && (Character.isDigit(source.charAt(index)) || source.charAt(index) == '.')) index++;
		return Double.parseDouble(source.substring(start, index));
	}
}
