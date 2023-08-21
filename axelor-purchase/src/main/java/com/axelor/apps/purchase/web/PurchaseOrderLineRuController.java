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

import com.axelor.apps.purchase.db.PurchaseRequestLineRu;
import com.axelor.apps.purchase.db.RequestSupplierListRu;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class PurchaseOrderLineRuController {

  public void calculateTotalSupplier(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    PurchaseRequestLineRu purchaseOrderLine = context.asType(PurchaseRequestLineRu.class);
    int totalSupplier = purchaseOrderLine.getSupplierLineList().size();
    response.setValue("totalSupplier", totalSupplier);
  }

  public void validateOffer(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    PurchaseRequestLineRu purchaseOrderLine = context.asType(PurchaseRequestLineRu.class);
    int totalSupplier = purchaseOrderLine.getSupplierLineList().size();
    int supplierOfferCount = 0;

    if (totalSupplier > 0) {
      for (RequestSupplierListRu requestSupplierList : purchaseOrderLine.getSupplierLineList()) {
        if (requestSupplierList.getOfferSupplier()) {
          response.setValue("supplier", requestSupplierList.getSupplierUser());
          supplierOfferCount = supplierOfferCount + 1;
        }
      }
    }

    if (supplierOfferCount > 1) {
      response.setAlert("Please select only one supplier!!");
    }
  }

  public void calculateTotal(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    PurchaseRequestLineRu purchaseOrderLine = context.asType(PurchaseRequestLineRu.class);
    BigDecimal totalAmount = new BigDecimal(0);

    for (RequestSupplierListRu requestSupplier : purchaseOrderLine.getSupplierLineList()) {
      if (requestSupplier.getOfferSupplier()) {
        totalAmount = totalAmount.add(requestSupplier.getTotalCost());
      }
    }
    response.setValue("totalCost", totalAmount);
  }
}
