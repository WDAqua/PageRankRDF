package eu.wdaqua.validation;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class FileCanBeOpenedValidator implements IParameterValidator {
    public void validate(String name, String value) throws ParameterException {
        File file =  new File(value);
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
        } catch (FileNotFoundException e) {
            throw new ParameterException("Parameter " + name + " should represent a file to write to. But it doesn't");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
