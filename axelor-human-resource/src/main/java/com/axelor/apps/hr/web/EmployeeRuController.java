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
import com.axelor.apps.hr.db.EmployeeContractRu;
import com.axelor.apps.hr.db.EmployeeRu;
import com.axelor.apps.hr.db.EmployeeSalaryRu;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class EmployeeRuController {

  public void printEmpSalaryReport(ActionRequest request, ActionResponse response) {
    try {
      String idsStr = "";
      //		    if (request.getContext().get("_ids") == null) {
      //		      response.setAlert("Please select the grid.");
      //		      return;
      //		    }
      //		    List<Integer> selectedFiels = (List<Integer>) request.getContext().get("_ids");
      //		    idsStr = Joiner.on(",").join(selectedFiels);

      List<Long> empSalaryId = new ArrayList<Long>();

      EmployeeRu employee = request.getContext().asType(EmployeeRu.class);
      List<EmployeeContractRu> employeeContractList = employee.getEmployeeContract();

      for (EmployeeContractRu employeeContract : employeeContractList) {
        List<EmployeeSalaryRu> employeeSlaryList = employeeContract.getEmployeeSalary();

        for (EmployeeSalaryRu employeeSalary : employeeSlaryList) {
          empSalaryId.add(employeeSalary.getId());
        }
      }
      idsStr = Joiner.on(",").join(empSalaryId);
      String fileLink =
          ReportFactory.createReport("EmployeeSalaryEmpForm.rptdesign", "SalaryReport" + "-${date}")
              .addParam("ids", idsStr)
              .generate()
              .getFileLink();

      response.setView(ActionView.define("Salary Report").add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
