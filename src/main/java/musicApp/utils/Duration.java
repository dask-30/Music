package musicApp.utils;


public class Duration {

    private Integer numerator;
    private Integer denominator;

    public Duration() {
    }

    public Integer getNumerator() {
        return numerator;
    }

    public void setNumerator(Integer numerator) {
        this.numerator = numerator;
    }

    public Integer getDenominator() {
        return denominator;
    }

    public void setDenominator(Integer denominator) {
        this.denominator = denominator;
    }

    public Duration(Integer numerator, Integer denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }
}
