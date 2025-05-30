package sg.edu.nus.iss.edgp.notification.strategy;

import sg.edu.nus.iss.edgp.notification.dto.ValidationResult;

public interface IAPIHelperValidationStrategy<T>  {
	
	ValidationResult validateObject(T data);
}
