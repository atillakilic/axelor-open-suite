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
package com.axelor.apps.hr.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.hr.db.EmployeeSalaryRu;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;

@Singleton
public class EmployeeSalaryController {

  public void calculateSalary(ActionRequest request, ActionResponse response) {
    EmployeeSalaryRu employeeSalaryRu = request.getContext().asType(EmployeeSalaryRu.class);
    BigDecimal totalSalary = new BigDecimal(0);
    BigDecimal hourlyRate = employeeSalaryRu.getHourlyRate();
    BigDecimal totalWorkHour = new BigDecimal(0);

    totalWorkHour =
        totalWorkHour
            .add(employeeSalaryRu.getOne())
            .add(employeeSalaryRu.getTwo())
            .add(employeeSalaryRu.getThree())
            .add(employeeSalaryRu.getFour())
            .add(employeeSalaryRu.getFive())
            .add(employeeSalaryRu.getSix())
            .add(employeeSalaryRu.getSeven())
            .add(employeeSalaryRu.getEight())
            .add(employeeSalaryRu.getNine())
            .add(employeeSalaryRu.getTen())
            .add(employeeSalaryRu.getEleven())
            .add(employeeSalaryRu.getTwelve())
            .add(employeeSalaryRu.getThriteen())
            .add(employeeSalaryRu.getFourteen())
            .add(employeeSalaryRu.getFifteen())
            .add(employeeSalaryRu.getSixteen())
            .add(employeeSalaryRu.getSeventeen())
            .add(employeeSalaryRu.getEighteen())
            .add(employeeSalaryRu.getNineteen())
            .add(employeeSalaryRu.getTwenty())
            .add(employeeSalaryRu.getTwentyOne())
            .add(employeeSalaryRu.getTwentyTwo())
            .add(employeeSalaryRu.getTwentyThree())
            .add(employeeSalaryRu.getTwentyFour())
            .add(employeeSalaryRu.getTwentyFive())
            .add(employeeSalaryRu.getTwentySix())
            .add(employeeSalaryRu.getTwentySeven())
            .add(employeeSalaryRu.getTwentyEight())
            .add(employeeSalaryRu.getTwentyNine())
            .add(employeeSalaryRu.getThirty())
            .add(employeeSalaryRu.getThirtyOne());

    totalSalary = hourlyRate.multiply(totalWorkHour);
    response.setValue("totalSalary", totalSalary);
  }

  public void printSalary(ActionRequest request, ActionResponse response) {
    try {
      String idsStr = "";
      if (request.getContext().get("_ids") == null) {
        response.setAlert("Please select the grid.");
        return;
      }
      List<Integer> selectedFiels = (List<Integer>) request.getContext().get("_ids");
      idsStr = Joiner.on(",").join(selectedFiels);
      System.err.println(idsStr);
      String fileLink =
          ReportFactory.createReport("EmployeeSalary.rptdesign", "SalaryReport" + "-${date}")
              .addParam("ids", idsStr)
              .generate()
              .getFileLink();

      response.setView(ActionView.define("Salary Report").add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
