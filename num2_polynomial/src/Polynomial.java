import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Objects;

public class Polynomial implements Comparable<Polynomial>, Cloneable {
    private final HashMap<Integer, Double> coefficients;

    public Polynomial() {
        coefficients = new HashMap<>();
    }

    public Polynomial(HashMap<Integer, Double> coeffs) {
        this();
        for (Map.Entry<Integer, Double> entry : coeffs.entrySet()) {
            if (entry.getValue() != 0.0) {
                coefficients.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void setCoefficient(int power, double coefficient) {
        if (coefficient == 0.0) {
            coefficients.remove(power);
        } else {
            coefficients.put(power, coefficient);
        }
    }

    public double getCoefficient(int power) {
        return coefficients.getOrDefault(power, 0.0);
    }

    public Polynomial add(Polynomial other) {
        Polynomial result = new Polynomial();

        for (Map.Entry<Integer, Double> entry : this.coefficients.entrySet()) {
            result.setCoefficient(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Integer, Double> entry : other.coefficients.entrySet()) {
            int power = entry.getKey();
            double newCoeff = result.getCoefficient(power) + entry.getValue();
            result.setCoefficient(power, newCoeff);
        }

        return result;
    }

    public Polynomial subtract(Polynomial other) {
        Polynomial result = new Polynomial();

        for (Map.Entry<Integer, Double> entry : this.coefficients.entrySet()) {
            result.setCoefficient(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Integer, Double> entry : other.coefficients.entrySet()) {
            int power = entry.getKey();
            double newCoeff = result.getCoefficient(power) - entry.getValue();
            result.setCoefficient(power, newCoeff);
        }

        return result;
    }

    public Polynomial multiply(Polynomial other) {
        Polynomial result = new Polynomial();

        for (Map.Entry<Integer, Double> thisEntry : this.coefficients.entrySet()) {
            for (Map.Entry<Integer, Double> otherEntry : other.coefficients.entrySet()) {
                int newPower = thisEntry.getKey() + otherEntry.getKey();
                double newCoeff = thisEntry.getValue() * otherEntry.getValue();
                double currentCoeff = result.getCoefficient(newPower);
                result.setCoefficient(newPower, currentCoeff + newCoeff);
            }
        }

        return result;
    }

    public Polynomial multiply(double scalar) {
        if (scalar == 0.0) {
            return new Polynomial();
        }

        Polynomial result = new Polynomial();
        for (Map.Entry<Integer, Double> entry : this.coefficients.entrySet()) {
            result.setCoefficient(entry.getKey(), entry.getValue() * scalar);
        }

        return result;
    }

    public Polynomial[] divide(Polynomial divisor) {
        if (divisor.isZero()) {
            throw new ArithmeticException("Division by zero polynomial");
        }

        Polynomial quotient = new Polynomial();
        Polynomial remainder = this.clone();

        int divisorDegree = divisor.degree();
        double divisorLeadingCoeff = divisor.getCoefficient(divisorDegree);

        while (!remainder.isZero() && remainder.degree() >= divisorDegree) {
            int currentDegree = remainder.degree();
            double currentLeadingCoeff = remainder.getCoefficient(currentDegree);

            int powerDiff = currentDegree - divisorDegree;
            double coeff = currentLeadingCoeff / divisorLeadingCoeff;

            Polynomial term = new Polynomial();
            term.setCoefficient(powerDiff, coeff);

            quotient = quotient.add(term);
            remainder = remainder.subtract(term.multiply(divisor));
        }

        return new Polynomial[]{quotient, remainder};
    }

    public Polynomial mod(Polynomial divisor) {
        Polynomial[] divisionResult = this.divide(divisor);
        return divisionResult[1];
    }

    public int degree() {
        if (coefficients.isEmpty()) {
            return -1;
        }
        return coefficients.keySet().stream().max(Integer::compare).orElse(0);
    }


    public boolean isZero() {
        return coefficients.isEmpty();
    }

    @Override
    public int compareTo(Polynomial other) {

        int degreeCompare = Integer.compare(this.degree(), other.degree());
        if (degreeCompare != 0) {
            return degreeCompare;
        }

        TreeMap<Integer, Double> thisSorted = new TreeMap<>(coefficients);
        TreeMap<Integer, Double> otherSorted = new TreeMap<>(other.coefficients);

        for (int power : thisSorted.descendingKeySet()) {
            double thisCoeff = this.getCoefficient(power);
            double otherCoeff = other.getCoefficient(power);

            int coeffCompare = Double.compare(thisCoeff, otherCoeff);
            if (coeffCompare != 0) {
                return coeffCompare;
            }
        }

        return 0;
    }

    @Override
    public Polynomial clone() {
        try {
            Polynomial cloned = (Polynomial) super.clone();
            HashMap<Integer, Double> newCoeffs = new HashMap<>(this.coefficients);
            return new Polynomial(newCoeffs);
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Polynomial other = (Polynomial) obj;
        return coefficients.equals(other.coefficients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coefficients);
    }

    @Override
    public String toString() {
        if (coefficients.isEmpty()) {
            return "0";
        }

        TreeMap<Integer, Double> sorted = new TreeMap<>(coefficients);
        StringBuilder sb = new StringBuilder();
        boolean firstTerm = true;

        for (Map.Entry<Integer, Double> entry : sorted.descendingMap().entrySet()) {
            int power = entry.getKey();
            double coeff = entry.getValue();

            if (!firstTerm) {
                sb.append(coeff > 0 ? " + " : " - ");
            } else {
                if (coeff < 0) {
                    sb.append("-");
                }
                firstTerm = false;
            }

            double absCoeff = Math.abs(coeff);
            if (power == 0) {
                sb.append(absCoeff);
            } else {
                if (absCoeff != 1.0) {
                    sb.append(absCoeff);
                    sb.append("*");
                }
                sb.append("x");
                if (power != 1) {
                    sb.append("^").append(power);
                }
            }
        }

        return sb.toString();
    }
}