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

import com.axelor.apps.purchase.db.PurchaseOrderLineRu;
import com.axelor.apps.purchase.db.PurchaseOrderRu;
import com.axelor.apps.purchase.db.PurchaseRequestLineRu;
import com.axelor.apps.purchase.db.PurchaseRequestRu;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Singleton
public class PurchaseOrderRuController {

  @Transactional
  public void createPurchaseOrderLineRu(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderRu purchaseOrderRu = request.getContext().asType(PurchaseOrderRu.class);
      Set<PurchaseRequestRu> purchaseRequestRuSet = purchaseOrderRu.getPurchaseRequestSelect();

      List<PurchaseOrderLineRu> purchaseOrderLineListRu = new ArrayList<PurchaseOrderLineRu>();
      for (PurchaseRequestRu purchaseRequestRu : purchaseRequestRuSet) {

        for (PurchaseRequestLineRu purchaseRequestLineRu :
            purchaseRequestRu.getPurchaseRequestLineList()) {
          if (purchaseRequestLineRu.getRequestStatus() == 1) {
            PurchaseOrderLineRu purchaseOrderLine = new PurchaseOrderLineRu();
            purchaseOrderLine.setProductCategory(purchaseRequestLineRu.getProductCategory());
            purchaseOrderLine.setProduct(purchaseRequestLineRu.getProduct());
            purchaseOrderLine.setUnit(purchaseRequestLineRu.getUnit());
            purchaseOrderLine.setQuantity(purchaseRequestLineRu.getQuantity());
            purchaseOrderLine.setRequesterUser(purchaseRequestLineRu.getRequesterUser());
            purchaseOrderLine.setProjectStockLocation(
                purchaseRequestLineRu.getProjectStockLocation());
            purchaseOrderLine.setSpecialty(purchaseRequestLineRu.getSpecialty());
            purchaseOrderLine.setPurpose(purchaseRequestLineRu.getPurpose());

            purchaseOrderLineListRu.add(purchaseOrderLine);
          }
        }
      }
      System.err.println(purchaseOrderLineListRu);
      response.setValue("purchaseOrderLine", purchaseOrderLineListRu);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
