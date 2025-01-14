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

import com.axelor.apps.hr.db.EmployeeContractRu;
import com.axelor.apps.hr.db.EmployeeDailyActivityLineRu;
import com.axelor.apps.hr.db.EmployeeDailyActivityRu;
import com.axelor.apps.hr.db.EmployeeRu;
import com.axelor.apps.hr.db.EmployeeSalaryRu;
import com.axelor.apps.hr.db.repo.EmployeeRuRepository;
import com.axelor.apps.hr.service.employee.EmployeeDailyActivityService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class EmployeeDailyActivityController {

  @Transactional
  public void setEmployeeAndRate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    EmployeeDailyActivityRu employeeDailyActivity =
        request.getContext().asType(EmployeeDailyActivityRu.class);

    List<EmployeeRu> employeeList = new ArrayList<EmployeeRu>();

    if (employeeDailyActivity.getSelectActivityType() == 1) { // team of employee
      employeeList =
          Beans.get(EmployeeRuRepository.class)
              .all()
              .filter("self.projectTeam = ?", employeeDailyActivity.getProjectTeam())
              .fetch();
    }

    if (employeeDailyActivity.getSelectActivityType() == 3) { // project employee
      employeeList =
          Beans.get(EmployeeRuRepository.class)
              .all()
              .filter("self.project = ?", employeeDailyActivity.getProject())
              .fetch();
    }

    List<EmployeeDailyActivityLineRu> employeeDailyActivityLineRuList =
        new ArrayList<EmployeeDailyActivityLineRu>();
    for (EmployeeRu employee : employeeList) {

      List<EmployeeContractRu> contractList = employee.getEmployeeContract();
      boolean employeeActiveSalary = false;
      boolean employeeActiveSalaryMonthIsSameDailyActivity = false;

      for (EmployeeContractRu contract : contractList) {
        if (contract.getStatus() == 2) { // contract is active
          EmployeeDailyActivityLineRu dailyActivityLineRu = new EmployeeDailyActivityLineRu();
          dailyActivityLineRu.setActivityDate(employeeDailyActivity.getTodayDate());
          dailyActivityLineRu.setStatusShiftType(employeeDailyActivity.getStatusShiftType());
          dailyActivityLineRu.setEmployee(employee);
          dailyActivityLineRu.setDailyWorkHoursOnContact(contract.getDailyWorkHours());
          dailyActivityLineRu.setEmployeeDailyActivity(employeeDailyActivity);
          //          EmployeeDailyActivityLineRu employeeDailyActivityLineRu =
          //
          // Beans.get(EmployeeDailyActivityLineRuRepository.class).save(dailyActivityLineRu);

          for (EmployeeSalaryRu employeeSalary : contract.getEmployeeSalary()) {
            if (employeeSalary.getCurrentlyActive()) {
              employeeActiveSalary = true;
              LocalDate salaryMonthDate = employeeSalary.getDateOfStart();
              if (employeeDailyActivity.getTodayDate().getMonth() == salaryMonthDate.getMonth()) {
                employeeActiveSalaryMonthIsSameDailyActivity = true;
                dailyActivityLineRu.setSalaryType(employeeSalary.getSalaryType());
                dailyActivityLineRu.setEmployeeActiveSalaryContract(employeeSalary);
                dailyActivityLineRu.setComments("");
                dailyActivityLineRu.setIsAlert(false);
              }
            }
          }

          if (!employeeActiveSalary) {
            dailyActivityLineRu.setComments("Employee do not have active Salary on contract.");
            dailyActivityLineRu.setIsAlert(true);
          }

          if (!employeeActiveSalaryMonthIsSameDailyActivity) {
            dailyActivityLineRu.setComments(
                "Employee salary month is diffrent then current month, please correct on contract form.");
            dailyActivityLineRu.setIsAlert(true);
          }

          employeeDailyActivityLineRuList.add(dailyActivityLineRu);
          break;
        }
      }
    }
    response.setValue("activityRecord", employeeDailyActivityLineRuList);
  }

  public void calculateEmployeeWork(ActionRequest request, ActionResponse response)
      throws AxelorException {
    EmployeeDailyActivityRu employeeDailyActivity =
        request.getContext().asType(EmployeeDailyActivityRu.class);

    BigDecimal totalEmployee = new BigDecimal(0);
    BigDecimal totalEmployeeWorked = new BigDecimal(0);
    BigDecimal totalEmployeeNotWorked = new BigDecimal(0);

    totalEmployee = new BigDecimal(employeeDailyActivity.getActivityRecord().size());

    for (EmployeeDailyActivityLineRu activityList : employeeDailyActivity.getActivityRecord()) {
      if (activityList.getIsAbsence()) {
        totalEmployeeNotWorked = totalEmployeeNotWorked.add(new BigDecimal(1));
      } else {
        totalEmployeeWorked = totalEmployeeWorked.add(new BigDecimal(1));
        ;
      }
    }

    response.setValue("totalEmp", totalEmployee);
    response.setValue("totalworked", totalEmployeeWorked);
    response.setValue("totalEmpNotWorked", totalEmployeeNotWorked);
  }

  public void updateRecordOnSalary(ActionRequest request, ActionResponse response)
      throws AxelorException {

    EmployeeDailyActivityRu employeeDailyActivity =
        request.getContext().asType(EmployeeDailyActivityRu.class);
    Beans.get(EmployeeDailyActivityService.class).updateRecordOnSalary(employeeDailyActivity);
  }

  public void setEmployeeAbsentPlan(ActionRequest request, ActionResponse response)
      throws AxelorException {

    EmployeeDailyActivityRu employeeDailyActivity =
        request.getContext().asType(EmployeeDailyActivityRu.class);
    Beans.get(EmployeeDailyActivityService.class).setEmployeeAbsentPlan(employeeDailyActivity);
  }

  public void setEmployeeWorkhours(ActionRequest request, ActionResponse response)
      throws AxelorException {

    EmployeeDailyActivityRu employeeDailyActivity =
        request.getContext().asType(EmployeeDailyActivityRu.class);

    List<EmployeeDailyActivityLineRu> employeeDailyActivityLineList =
        new ArrayList<EmployeeDailyActivityLineRu>();
    for (EmployeeDailyActivityLineRu employeeDailyActivityLine :
        employeeDailyActivity.getActivityRecord()) {
      employeeDailyActivityLine.setDailyWorkHours(employeeDailyActivity.getDailyWorkHoursFill());
      employeeDailyActivityLineList.add(employeeDailyActivityLine);
    }

    response.setValue("activityRecord", employeeDailyActivityLineList);
  }
}
