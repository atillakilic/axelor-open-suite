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
package com.axelor.apps.cash.management.web;

import com.axelor.apps.cash.management.db.CostPaymentCash;
import com.axelor.apps.cash.management.db.CostPaymentCashLine;
import com.axelor.apps.cash.management.db.repo.CostPaymentCashRepository;
import com.axelor.apps.cash.management.service.NumToStrMoney;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;

@Singleton
public class CostPaymentCashLineController {

  public void convertNumberToText(ActionRequest request, ActionResponse response) {
    CostPaymentCashLine costPaymentCashline =
        request.getContext().asType(CostPaymentCashLine.class);
    BigDecimal costSum = costPaymentCashline.getCostSum();
    NumToStrMoney ntsm = new NumToStrMoney(costSum.toString());
    String text = ntsm.num2str();
    response.setValue("costText", text);
  }

  public void setSeqNumber(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    CostPaymentCash costPaymentCash = null;
    if (context.get("_parent") != null) {
      Map<String, Object> _parent = (Map<String, Object>) context.get("_parent");
      costPaymentCash =
          Beans.get(CostPaymentCashRepository.class)
              .find(Long.parseLong(_parent.get("id").toString()));
    }

    int listSize = 0;
    if (costPaymentCash != null) {
      listSize = costPaymentCash.getCostPaymentCashLine().size();
    }

    response.setValue("seqNumber", listSize + 1);
  }
}
