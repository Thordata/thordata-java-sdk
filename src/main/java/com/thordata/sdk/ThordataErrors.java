package com.thordata.sdk;

public final class ThordataErrors {

  public static class ThordataException extends RuntimeException {
    public ThordataException(String message) { super(message); }
  }

  public static class ThordataApiException extends ThordataException {
    public final Integer apiCode;
    public final Integer httpStatus;
    public final Object payload;

    public ThordataApiException(String message, Integer apiCode, Integer httpStatus, Object payload) {
      super(message);
      this.apiCode = apiCode;
      this.httpStatus = httpStatus;
      this.payload = payload;
    }
  }

  public static class ThordataAuthException extends ThordataApiException {
    public ThordataAuthException(String m, Integer c, Integer s, Object p) { super(m, c, s, p); }
  }

  public static class ThordataRateLimitException extends ThordataApiException {
    public ThordataRateLimitException(String m, Integer c, Integer s, Object p) { super(m, c, s, p); }
  }

  public static class ThordataServerException extends ThordataApiException {
    public ThordataServerException(String m, Integer c, Integer s, Object p) { super(m, c, s, p); }
  }

  public static class ThordataValidationException extends ThordataApiException {
    public ThordataValidationException(String m, Integer c, Integer s, Object p) { super(m, c, s, p); }
  }

  public static class ThordataNotCollectedException extends ThordataApiException {
    public ThordataNotCollectedException(String m, Integer c, Integer s, Object p) { super(m, c, s, p); }
  }
}