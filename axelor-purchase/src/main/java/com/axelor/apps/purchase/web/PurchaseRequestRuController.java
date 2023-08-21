/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.purchase.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.purchase.db.PurchaseRequestLineRu;
import com.axelor.apps.purchase.db.PurchaseRequestRu;
import com.axelor.apps.purchase.db.PurchaseRequestValidatorRu;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;

@Singleton
public class PurchaseRequestRuController {

  public void checkValidatorApproved(ActionRequest request, ActionResponse response) {
    try {
      PurchaseRequestRu purchaseRequest = request.getContext().asType(PurchaseRequestRu.class);
      List<PurchaseRequestValidatorRu> validatorList = purchaseRequest.getValidatores();

      if (purchaseRequest.getFileUpload() == null) {
        response.setError("Please Upload validator signature file.");
        return;
      }
      for (PurchaseRequestValidatorRu validator : validatorList) {
        if (!validator.getConfirm()) {
          response.setError("Please confirm from all validator.");
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void calculateTotal(ActionRequest request, ActionResponse response) {
    try {
      PurchaseRequestRu purchaseRequest = request.getContext().asType(PurchaseRequestRu.class);
      BigDecimal totalAmount = new BigDecimal(0);

      for (PurchaseRequestLineRu PurchaseRequestLine :
          purchaseRequest.getPurchaseRequestLineList()) {
        totalAmount = totalAmount.add(PurchaseRequestLine.getTotalCost());
      }
      response.setValue("totalCost", totalAmount);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printRequestForm(ActionRequest request, ActionResponse response) {
    try {
      PurchaseRequestRu purchaseRequest = request.getContext().asType(PurchaseRequestRu.class);

      String fileLink =
          ReportFactory.createReport("PurchaseRequest.rptdesign", "PurchaseReport" + "-${date}")
              .addParam("id", purchaseRequest.getId())
              .generate()
              .getFileLink();

      //    System.err.println(fileLink); debug
      response.setView(ActionView.define("Purchase Request").add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printSupplierQuoteForm(ActionRequest request, ActionResponse response) {
    try {
      PurchaseRequestRu purchaseRequest = request.getContext().asType(PurchaseRequestRu.class);

      String fileLink =
          ReportFactory.createReport("SupplierQuote.rptdesign", "Supplier Quote" + "-${date}")
              .addParam("id", purchaseRequest.getId())
              .generate()
              .getFileLink();

      response.setView(ActionView.define("Supplier Quote").add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateArrivalQty(ActionRequest request, ActionResponse response) {
    try {
      PurchaseRequestRu purchaseRequest = request.getContext().asType(PurchaseRequestRu.class);

      for (PurchaseRequestLineRu PurchaseRequestLine :
          purchaseRequest.getPurchaseRequestLineList()) {
        if (PurchaseRequestLine.getQuantity().compareTo(PurchaseRequestLine.getQuantityArrived())
            != 0) {
          response.setError("Please check all the qty is arrived or not.");
          return;
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setProductLineStatus(ActionRequest request, ActionResponse response) {
    try {
      PurchaseRequestRu purchaseRequest = request.getContext().asType(PurchaseRequestRu.class);

      List<PurchaseRequestLineRu> purchaseRequestLineList =
          purchaseRequest.getPurchaseRequestLineList();

      for (PurchaseRequestLineRu PurchaseRequestLine : purchaseRequestLineList) {
        PurchaseRequestLine.setStatusSelect(1);
      }

      response.setValue("purchaseRequestLineList", purchaseRequestLineList);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
