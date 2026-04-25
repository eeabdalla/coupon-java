package digits.schwarz.star.couponservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
  private Instant timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
  private List<FieldError> fieldErrors;

  @Data
  @Builder
  public static class FieldError {
    private String field;
    private String message;
    private Object rejectedValue;
  }
}
