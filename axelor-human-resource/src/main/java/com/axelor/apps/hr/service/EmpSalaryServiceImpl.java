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
package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.EmployeeDailyActivity;
import com.axelor.apps.hr.db.EmployeeDailyActivityLine;
import com.axelor.apps.hr.db.EmployeeHr;
import com.axelor.apps.hr.db.EmployeeSalary;
import com.axelor.apps.hr.db.EmployeeSalaryLine;
import com.axelor.apps.hr.db.repo.EmployeeDailyActivityRepository;
import com.axelor.apps.hr.db.repo.EmployeeSalaryLineRepository;
import com.axelor.apps.hr.db.repo.EmployeeSalaryRepository;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpSalaryServiceImpl implements EmpSalaryService {

  @Override
  @Transactional
  public EmployeeSalary calculateSalary() {
    List<EmployeeDailyActivity> employeeDailyActivityList =
        (List<EmployeeDailyActivity>)
            Beans.get(EmployeeDailyActivityRepository.class)
                .all()
                .filter("self.statusSelect = 2")
                .fetch();
    if (employeeDailyActivityList.size() == 0) {
      return null;
    }

    EmployeeSalary empSalary = new EmployeeSalary();
    List<EmployeeSalaryLine> employeeSalaryLineList = new ArrayList<EmployeeSalaryLine>();

    for (EmployeeDailyActivity employeeDailyActivity : employeeDailyActivityList) {
      List<EmployeeDailyActivityLine> empDailyActivityLineList =
          employeeDailyActivity.getActivityRecord();

      for (EmployeeDailyActivityLine empDailyActivityLine : empDailyActivityLineList) {
        EmployeeHr empHrDailyActivityLine = empDailyActivityLine.getEmployee();

        boolean flag = false;
        EmployeeSalaryLine employeeSalaryNewLine = new EmployeeSalaryLine();

        for (EmployeeSalaryLine employeeSalaryLine : employeeSalaryLineList) {
          EmployeeHr empHrSalaryLine = employeeSalaryLine.getEmployee();
          if (empHrSalaryLine.equals(empHrDailyActivityLine)) {
            flag = true;
            employeeSalaryNewLine = employeeSalaryLine;
            break;
          }
        }

        if (flag) {
          // found in previous day
          BigDecimal totalHours = employeeSalaryNewLine.getTotalWorkHours();
          BigDecimal totalDays = employeeSalaryNewLine.getTotalWorkDays();
          BigDecimal penaltyOrReward = employeeSalaryNewLine.getTotalPenaltyOrReward();

          employeeSalaryNewLine.setTotalWorkHours(
              totalHours.add(empDailyActivityLine.getDailyWorkHours()));
          employeeSalaryNewLine.setTotalWorkDays(totalDays.add(new BigDecimal(1)));
          employeeSalaryNewLine.setTotalPenaltyOrReward(
              penaltyOrReward.add(empDailyActivityLine.getPenaltyOrReward()));

        } else {
          // not found in previous day Add new salary line
          employeeSalaryNewLine.setEmployee(empDailyActivityLine.getEmployee());
          employeeSalaryNewLine.setTotalWorkHours(empDailyActivityLine.getDailyWorkHours());
          employeeSalaryNewLine.setTotalWorkDays(new BigDecimal(1));
          employeeSalaryNewLine.setTotalPenaltyOrReward(empDailyActivityLine.getPenaltyOrReward());

          employeeSalaryLineList.add(employeeSalaryNewLine);
        }
      }
      employeeDailyActivity.setStatusSelect(3);
      Beans.get(EmployeeDailyActivityRepository.class).save(employeeDailyActivity);
    }

    empSalary.setStatusSelect(1);
    empSalary.setCreationDate(LocalDate.now());
    empSalary = Beans.get(EmployeeSalaryRepository.class).save(empSalary);

    for (EmployeeSalaryLine employeeSalaryLine : employeeSalaryLineList) {
      BigDecimal hourlyRate = employeeSalaryLine.getEmployee().getHourlyRate();
      BigDecimal totalSalary = employeeSalaryLine.getTotalWorkHours().multiply(hourlyRate);
      totalSalary = totalSalary.add(employeeSalaryLine.getTotalPenaltyOrReward());
      employeeSalaryLine.setTotalSalary(totalSalary);
      employeeSalaryLine.setEmployeeSalary(empSalary);

      employeeSalaryLine = Beans.get(EmployeeSalaryLineRepository.class).save(employeeSalaryLine);
    }
    return empSalary;
  }
}
