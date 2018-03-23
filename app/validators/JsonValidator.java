package validators;

import play.data.validation.Constraints;
import play.libs.F;
import play.libs.Json;

import javax.validation.ConstraintValidator;

import static play.libs.F.Tuple;


public class JsonValidator extends Constraints.Validator<String> implements ConstraintValidator<Constraints.ValidateWith, String> {

    private final static String message = "error.json";
    @Override
    public void initialize(Constraints.ValidateWith constraintAnnotation) {

    }

    @Override
    public F.Tuple<String, Object[]> getErrorMessageKey() {
        return Tuple(message, new Object[] {});
    }

    @Override
    public boolean isValid(String object) {
        if (object == null || object.isEmpty()) {
            return true;
        }
        try {
            Json.parse(object);
            return true;
        } catch (RuntimeException e){
            return false;
        }
    }
}
