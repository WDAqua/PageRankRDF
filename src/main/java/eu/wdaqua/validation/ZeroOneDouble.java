package eu.wdaqua.validation;


import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class ZeroOneDouble implements IParameterValidator {
    public void validate(String name, String value)
            throws ParameterException {
        double d = Double.parseDouble(value);
        if (d > 1.0D || d < 0.0D) {
            throw new ParameterException("Parameter " + name + " should be between zero and one (found " + value +")");
        }
    }
}