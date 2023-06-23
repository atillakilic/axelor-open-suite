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

import java.util.List;

import com.axelor.apps.hr.db.EmployeeContractRu;
import com.axelor.apps.hr.db.EmployeeDailyActivityLineRu;
import com.axelor.apps.hr.db.EmployeeRu;
import com.axelor.apps.hr.db.EmployeeSalaryRu;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class EmployeeDailyActivityLineController {

  public void setEmployeeRate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    EmployeeDailyActivityLineRu employeeDailyActivityLine =
        request.getContext().asType(EmployeeDailyActivityLineRu.class);
    EmployeeRu employee = employeeDailyActivityLine.getEmployee();

    if (employee == null) {
      return;
    }

    List<EmployeeContractRu> contractList = employee.getEmployeeContract();
    boolean employeeActiveContract = false;
    for (EmployeeContractRu contract : contractList) {
      if (contract.getStatus() == 2) { // contract is active
    	  employeeActiveContract = true;
        response.setValue("dailyWorkHoursOnContact", contract.getDailyWorkHours());
        for(EmployeeSalaryRu employeeSalary : contract.getEmployeeSalary()) {
        	if(employeeSalary.getCurrentlyActive()) {
        		response.setValue("salaryType", employeeSalary.getSalaryType());
        		response.setValue("employeeActiveSalaryContract", employeeSalary);
        	}
        }
      }
    }
    if(!employeeActiveContract) {
    	response.setAlert("Employee des not have any active contract Please add contract on Employee form!");
    }
  }
}
