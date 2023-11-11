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

import com.axelor.apps.cash.management.db.AdvancePaymentCashLine;
import com.axelor.apps.cash.management.service.NumToStrMoney;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class AdvancePaymentCashLineController {

  public void convertNumberToText(ActionRequest request, ActionResponse response) {
    AdvancePaymentCashLine advancePaymentCashline =
        request.getContext().asType(AdvancePaymentCashLine.class);
    BigDecimal advanceSum = advancePaymentCashline.getAdvanceSum();
    NumToStrMoney ntsm = new NumToStrMoney(advanceSum.toString());
    String text = ntsm.num2str();
    response.setValue("advanceText", text);
  }
}
