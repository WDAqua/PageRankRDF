package eu.wdaqua.validation;


import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class AllowedFormats implements IParameterValidator {
    public void validate(String name, String value)
            throws ParameterException {
        if (value.equals("tsv") || value.equals("nt")) {
            throw new ParameterException("Parameter " + name + " should be either \"tsv\" or \"nt\" (found " + value +")");
        }
    }
}