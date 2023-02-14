package com.axelor.apps.gdpr.exception;

public final class GdprExceptionMessage {

  public static final String FIELD_NOT_FOUND = /*$$(*/ "This field doesn't exist." /*)*/;

  public static final String NO_LINE_SELECTED = /*$$(*/ "Please select a line" /*)*/;

  public static final String TOO_MUCH_LINE_SELECTED = /*$$(*/ "Please select only one line" /*)*/;

  public static final String MISSING_ACCESS_REQUEST_RESPONSE_MAIL_TEMPLATE = /*$$(*/
      "Please configure a mail template for access request response." /*)*/;

  public static final String MISSING_ERASURE_REQUEST_RESPONSE_MAIL_TEMPLATE = /*$$(*/
      "Please configure a mail template for erasure request response." /*)*/;

  public static final String SENDING_MAIL_ERROR = /*$$(*/ "Error while sending the mail : %s" /*)*/;
}