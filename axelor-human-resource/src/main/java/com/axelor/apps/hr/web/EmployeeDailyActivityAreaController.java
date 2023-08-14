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

import com.axelor.apps.hr.db.EmployeeDailyActivityAreaLineRu;
import com.axelor.apps.hr.db.EmployeeDailyActivityAreaRu;
import com.axelor.apps.hr.db.EmployeeRu;
import com.axelor.apps.hr.db.repo.EmployeeRuRepository;
import com.axelor.apps.project.db.ProjectAreaRu;
import com.axelor.apps.project.db.ProjectRu;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class EmployeeDailyActivityAreaController {

  public void getProjectAreaList(ActionRequest request, ActionResponse response)
      throws AxelorException {

    EmployeeDailyActivityAreaRu employeeDailyActivityArea =
        request.getContext().asType(EmployeeDailyActivityAreaRu.class);

    List<EmployeeDailyActivityAreaLineRu> activityAreaLineList =
        new ArrayList<EmployeeDailyActivityAreaLineRu>();

    ProjectRu project = employeeDailyActivityArea.getProject();

    List<EmployeeRu> employeeList =
        Beans.get(EmployeeRuRepository.class).all().filter("self.project = ?", project).fetch();

    for (ProjectAreaRu projectArea : project.getProjectArea()) {
      EmployeeDailyActivityAreaLineRu activityAreaLine = new EmployeeDailyActivityAreaLineRu();
      activityAreaLine.setProjectArea(projectArea);
      activityAreaLine.setTodayDate(employeeDailyActivityArea.getTodayDate());
      activityAreaLineList.add(activityAreaLine);
    }
    response.setValue("totalWorkerProject", employeeList.size());
    response.setValue("totalWorkedInArea", 0);
    response.setValue("totalEmpNotWorked", 0);
    response.setValue("dailyActivityAreaLine", activityAreaLineList);
  }

  public void calculateTotalEmployeeWorked(ActionRequest request, ActionResponse response)
      throws AxelorException {

    EmployeeDailyActivityAreaRu employeeDailyActivityArea =
        request.getContext().asType(EmployeeDailyActivityAreaRu.class);

    BigDecimal totalEmployeeWorked = new BigDecimal(0);
    for (EmployeeDailyActivityAreaLineRu activityAreaLine :
        employeeDailyActivityArea.getDailyActivityAreaLine()) {
      totalEmployeeWorked = totalEmployeeWorked.add(activityAreaLine.getEmployeeWorked());
    }

    response.setValue(
        "totalEmpNotWorked",
        employeeDailyActivityArea.getTotalWorkerProject().subtract(totalEmployeeWorked));
    response.setValue("totalWorkedInArea", totalEmployeeWorked);
  }
}
