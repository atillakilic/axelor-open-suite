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
import com.axelor.apps.hr.db.EmployeeRu;
import com.axelor.apps.hr.db.EmployeeSalaryPaymentRu;
import com.axelor.apps.hr.db.EmployeeSalaryRu;
import com.axelor.apps.hr.db.repo.EmployeeRuRepository;
import com.axelor.apps.hr.db.repo.EmployeeSalaryRuRepository;
import com.axelor.apps.project.db.ProjectTeamRu;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Singleton
public class EmployeeSalaryPaymentRuController {

  public void getSalaryList(ActionRequest request, ActionResponse response) {
    EmployeeSalaryPaymentRu salaryPayment =
        request.getContext().asType(EmployeeSalaryPaymentRu.class);
    ProjectTeamRu ProjectTeamRu = null;
    EmployeeRu employeeRu = null;
    String idsStr = "";

    List<EmployeeSalaryRu> employeeSalary = new ArrayList<EmployeeSalaryRu>();

    if (salaryPayment.getSelectActivityType() == 1) {
      ProjectTeamRu = salaryPayment.getProjectTeam();
    } else {
      employeeRu = salaryPayment.getEmployee();
    }

    if (salaryPayment.getSelectActivityType() == 1) {
      List<EmployeeRu> employeeList =
          Beans.get(EmployeeRuRepository.class)
              .all()
              .filter("self.projectTeam = ?", ProjectTeamRu)
              .fetch();
      List<Long> selectedFiels = new ArrayList<Long>();
      for (EmployeeRu employee : employeeList) {
        selectedFiels.add(employee.getId());
      }
      if (selectedFiels.size() == 0) {
        response.setValue("employeeSalaryPayment", null);
        return;
      }
      idsStr = Joiner.on(",").join(selectedFiels);
      employeeSalary =
          Beans.get(EmployeeSalaryRuRepository.class)
              .all()
              .filter(
                  "self.employeeRu.id IN (" + idsStr + ") AND self.dateOfStart = ?",
                  salaryPayment.getStartDate())
              .fetch();

    } else {
      employeeSalary =
          Beans.get(EmployeeSalaryRuRepository.class)
              .all()
              .filter(
                  "self.employeeRu = ?  AND self.dateOfStart = ?",
                  employeeRu,
                  salaryPayment.getStartDate())
              .fetch();
    }

    for (EmployeeSalaryRu employeeSalaryRu : employeeSalary) {
      employeeSalaryRu.setLastPayAmount(
          employeeSalaryRu.getTotalSalary().subtract(employeeSalaryRu.getTotalPaidSalary()));
    }

    response.setValue("employeeSalaryPayment", employeeSalary);
  }

  public void setDates(ActionRequest request, ActionResponse response) {

    LocalDate today = LocalDate.now();
    LocalDate firstOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
    LocalDate lastDayOfMonthDate =
        today.withDayOfMonth(today.getMonth().length(today.isLeapYear()));

    response.setValue("startDate", firstOfCurrentMonth);
    response.setValue("endDate", lastDayOfMonthDate);
  }

  public void paySalary(ActionRequest request, ActionResponse response) {
    EmployeeSalaryPaymentRu salaryPayment =
        request.getContext().asType(EmployeeSalaryPaymentRu.class);
    // build ane total paid ma sarvalo karvano
    Set<EmployeeSalaryRu> employeeSalaryRuList = salaryPayment.getEmployeeSalaryPayment();
    for (EmployeeSalaryRu employeeSalaryRu : employeeSalaryRuList) {
      employeeSalaryRu.setTotalPaidSalary(
          employeeSalaryRu.getTotalPaidSalary().add(employeeSalaryRu.getLastPayAmount()));
      employeeSalaryRu.setLastPayAmount(new java.math.BigDecimal(0));

      if (employeeSalaryRu.getTotalSalary().compareTo(new java.math.BigDecimal(0)) == 0) {
        employeeSalaryRu.setSalaryStatus(1);
      }
      if (employeeSalaryRu.getTotalSalary().compareTo(employeeSalaryRu.getTotalPaidSalary()) == 0) {
        employeeSalaryRu.setSalaryStatus(3);
      } else if (employeeSalaryRu.getTotalSalary().compareTo(employeeSalaryRu.getTotalPaidSalary())
          == 1) { // hals paid
        employeeSalaryRu.setSalaryStatus(2);
      } else {
        employeeSalaryRu.setSalaryStatus(3);
      }

      if (salaryPayment.getSelectPayMethod() == 1) {
        employeeSalaryRu.setPaymentMethod("Bank");
      }
      if (salaryPayment.getSelectPayMethod() == 2) {
        employeeSalaryRu.setPaymentMethod("Cash");
      }
    }
    response.setValue("employeeSalaryPayment", employeeSalaryRuList);
  }

  public void printSalaryPaymentReport(ActionRequest request, ActionResponse response) {
    EmployeeSalaryPaymentRu salaryPayment =
        request.getContext().asType(EmployeeSalaryPaymentRu.class);
    String idsStr = "";
    List<Integer> selectedFiels = new ArrayList<Integer>();
    for (EmployeeSalaryRu employeeSalaryRu : salaryPayment.getEmployeeSalaryPayment()) {
      selectedFiels.add(employeeSalaryRu.getId().intValue());
    }

    idsStr = Joiner.on(",").join(selectedFiels);
    System.err.println(idsStr);
    try {
      String fileLink =
          ReportFactory.createReport("EmployeeSalaryPayment.rptdesign", "SalaryReport" + "-${date}")
              .addParam("id", salaryPayment.getId().toString())
              .addParam("ids", idsStr)
              .generate()
              .getFileLink();

      response.setView(ActionView.define("Salary Report").add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
