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

import com.axelor.apps.hr.db.EmployeeSalary;
import com.axelor.apps.hr.service.EmpSalaryService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class EmployeeDailyActivityController {

  public void calculateSalary(ActionRequest request, ActionResponse response)
      throws AxelorException {
    EmployeeSalary employeeSalary = Beans.get(EmpSalaryService.class).calculateSalary();
    if (employeeSalary == null) {
      return;
    }
    response.setView(
        ActionView.define("Employee Salary")
            .model("com.axelor.apps.hr.db.EmployeeSalary")
            .add("grid", "hr-employee-salary-grid")
            .add("form", "hr-employee-salary-form")
            .map());
  }
}
