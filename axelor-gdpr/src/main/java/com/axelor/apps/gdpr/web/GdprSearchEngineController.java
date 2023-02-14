package com.axelor.apps.gdpr.web;

import com.axelor.apps.gdpr.service.GdprSearchEngineService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GdprSearchEngineController {

  /**
   * search in Partner and Lead object with fields
   *
   * @param request
   * @param response
   */
  public void searchObject(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    // keep only filled fields
    Map<String, Object> searchParams = new HashMap<>();
    Optional.ofNullable(context.get("__firstName"))
        .map(Object::toString)
        .ifPresent(str -> searchParams.put("firstName", str));
    Optional.ofNullable(context.get("__lastName"))
        .map(Object::toString)
        .ifPresent(str -> searchParams.put("lastName", str));
    Optional.ofNullable(context.get("__email"))
        .map(Object::toString)
        .ifPresent(str -> searchParams.put("email", str));
    Optional.ofNullable(context.get("__phone"))
        .map(Object::toString)
        .ifPresent(str -> searchParams.put("phone", str));

    if (searchParams.isEmpty()) {
      response.setAlert(I18n.get("Please enter at least one field."));
    } else {
      List<Map<String, Object>> resultList = new ArrayList<>();
      try {
        resultList = Beans.get(GdprSearchEngineService.class).searchObject(searchParams);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
        response.setError(e.getMessage());
      }
      response.setValue("__searchResults", resultList);
    }
  }

  /**
   * fill reference field
   *
   * @param request
   * @param response
   */
  public void fillReferenceWithData(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      List<Map<String, Object>> resultList =
          (List<Map<String, Object>>) context.get("__searchResults");
      Map<String, Object> selectedObject =
          Beans.get(GdprSearchEngineService.class).checkSelectedObject(resultList);
      response.setValue("modelSelect", selectedObject.get("typeClass").toString());
      response.setValue("modelId", selectedObject.get("objectId"));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}