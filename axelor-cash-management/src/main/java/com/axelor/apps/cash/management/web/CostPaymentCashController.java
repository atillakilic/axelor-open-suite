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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.cash.management.db.CostPaymentCash;
import com.axelor.apps.cash.management.db.CostPaymentCashLine;
import com.axelor.apps.cash.management.service.NumToStrMoney;
import com.axelor.apps.hr.db.EmployeeRu;
import com.axelor.apps.hr.db.repo.EmployeeRuRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class CostPaymentCashController {

  public void setEmployee(ActionRequest request, ActionResponse response) {
    CostPaymentCash costPaymentCash = request.getContext().asType(CostPaymentCash.class);

    List<EmployeeRu> employeeList = new ArrayList<EmployeeRu>();

    employeeList =
        Beans.get(EmployeeRuRepository.class)
            .all()
            .filter("self.projectTeam = ?", costPaymentCash.getProjectTeam())
            .fetch();
    List<CostPaymentCashLine> costPaymentCashLineList = new ArrayList<CostPaymentCashLine>();
    int count = 0;
    for (EmployeeRu employee : employeeList) {
      count++;
      CostPaymentCashLine costPaymentCashLine = new CostPaymentCashLine();
      costPaymentCashLine.setEmployeeRu(employee);
      costPaymentCashLine.setSeqNumber(String.valueOf(count));
      costPaymentCashLineList.add(costPaymentCashLine);
    }
    response.setValue("costPaymentCashLine", costPaymentCashLineList);
  }

  public void printReport(ActionRequest request, ActionResponse response) {
    CostPaymentCash costPaymentCash = request.getContext().asType(CostPaymentCash.class);
    try {
      String fileLink =
          ReportFactory.createReport("CostPayment.rptdesign", "Cost Payment" + "-${date}")
              .addParam("id", costPaymentCash.getId())
              .generate()
              .getFileLink();

      response.setView(ActionView.define("Cost Payment").add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void calculate(ActionRequest request, ActionResponse response) {
    CostPaymentCash costPaymentCash = request.getContext().asType(CostPaymentCash.class);

    BigDecimal totalSum = new BigDecimal(0);
    for (CostPaymentCashLine costPaymentCashLine : costPaymentCash.getCostPaymentCashLine()) {

      totalSum = totalSum.add(costPaymentCashLine.getCostSum());
    }
    NumToStrMoney ntsm = new NumToStrMoney(totalSum.toString());
    String text = ntsm.num2str();

    response.setValue("fillText", text);
    response.setValue("fillSum", totalSum);
  }
}