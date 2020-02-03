package in.ecgcltd.erp.dms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "File empty or file not found.")
public class DMSException extends RuntimeException {

	public DMSException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
