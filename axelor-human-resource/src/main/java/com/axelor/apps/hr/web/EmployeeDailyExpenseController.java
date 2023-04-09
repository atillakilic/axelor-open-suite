/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.web;

import com.axelor.apps.hr.db.EmployeeDailyExpense;
import com.axelor.apps.hr.db.EmployeeDailyExpenseLine;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;

@Singleton
public class EmployeeDailyExpenseController {

  public void calculateExpense(ActionRequest request, ActionResponse response)
      throws AxelorException {
    EmployeeDailyExpense empFile = request.getContext().asType(EmployeeDailyExpense.class);
    BigDecimal totalBank = new BigDecimal(0);
    BigDecimal totalCash = new BigDecimal(0);

    List<EmployeeDailyExpenseLine> expenseLine = empFile.getExpenseLine();

    for (EmployeeDailyExpenseLine employeeDailyExpenseLine : expenseLine) {
      if (employeeDailyExpenseLine.getPaymentMethod() == 1) { // Bank payment selection
        totalBank = totalBank.add(employeeDailyExpenseLine.getTotal());
      } else {
        totalCash = totalCash.add(employeeDailyExpenseLine.getTotal());
      }
    }
    System.err.println(totalBank);
    System.err.println(totalCash);
    System.err.println(totalBank);
    response.setValue("totalCash", totalCash);
    response.setValue("totalBank", totalBank);
    response.setValue("total", totalBank.add(totalCash));
  }
}
