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

import com.axelor.apps.hr.db.EmployeeDailyActivityLineRu;
import com.axelor.apps.hr.db.EmployeeDailyActivityRu;
import com.axelor.apps.hr.db.EmployeeDashboardCountRu;
import com.axelor.apps.hr.db.EmployeeDashboardEmpActivityRu;
import com.axelor.apps.hr.db.EmployeeDashboardEmpPenaltyRu;
import com.axelor.apps.hr.db.EmployeeDashboardRu;
import com.axelor.apps.hr.db.EmployeeDashboardWorkShiftRu;
import com.axelor.apps.hr.db.EmployeeRu;
import com.axelor.apps.hr.db.repo.EmployeeDailyActivityLineRuRepository;
import com.axelor.apps.hr.db.repo.EmployeeDailyActivityRuRepository;
import com.axelor.apps.project.db.ProjectAreaRu;
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
public class EmployeeDashboardController {

  public void onLoad(ActionRequest request, ActionResponse response) throws AxelorException {
    LocalDate today = LocalDate.now();
    LocalDate yesterDay = today.minusDays(1);
    response.setValue("employeeCountDate", yesterDay);
    response.setValue("employeeworkShift", yesterDay);
    response.setValue("employeeActivityFromDate", yesterDay);
    response.setValue("employeeActivityToDate", yesterDay);
    response.setValue("employeePenaltyFromDate", yesterDay);
    response.setValue("employeePenaltyToDate", yesterDay);
  }

  @Transactional
  public void getEmployeeCount(ActionRequest request, ActionResponse response)
      throws AxelorException {
    EmployeeDashboardRu dashboard = request.getContext().asType(EmployeeDashboardRu.class);
    LocalDate date = dashboard.getEmployeeCountDate();

    List<EmployeeDailyActivityRu> dailyActivityList =
        Beans.get(EmployeeDailyActivityRuRepository.class)
            .all()
            .filter("self.todayDate = ?", date)
            .fetch();

    List<EmployeeDashboardCountRu> employeeDashboardCountRu =
        new ArrayList<EmployeeDashboardCountRu>();

    for (EmployeeDailyActivityRu dailyActivity : dailyActivityList) {
      EmployeeDashboardCountRu employeeCount = new EmployeeDashboardCountRu();
      employeeCount.setTodayDate(dailyActivity.getTodayDate());
      employeeCount.setStatusShiftType(dailyActivity.getStatusShiftType());
      employeeCount.setProject(dailyActivity.getProject());
      employeeCount.setProjectTeam(dailyActivity.getProjectTeam());
      employeeCount.setTotalEmp(dailyActivity.getTotalEmp());
      employeeCount.setTotalEmpNotWorked(dailyActivity.getTotalEmpNotWorked());
      employeeCount.setTotalworked(dailyActivity.getTotalworked());
      employeeDashboardCountRu.add(employeeCount);
    }
    response.setValue("employeeCountList", employeeDashboardCountRu);
  }

  public void showTotalEmployeeShift(ActionRequest request, ActionResponse response)
      throws AxelorException {
    EmployeeDashboardRu dashboard = request.getContext().asType(EmployeeDashboardRu.class);
    LocalDate date = dashboard.getEmployeeworkShift();

    List<EmployeeDailyActivityLineRu> dailyActivityLineList =
        Beans.get(EmployeeDailyActivityLineRuRepository.class)
            .all()
            .filter("self.activityDate = ?", date)
            .fetch();

    List<EmployeeDashboardWorkShiftRu> employeeDashboarTotalShift =
        new ArrayList<EmployeeDashboardWorkShiftRu>();

    for (EmployeeDailyActivityLineRu dailyActivityLine : dailyActivityLineList) {
      ProjectAreaRu workAreaOnActivity = dailyActivityLine.getWorkArea();

      if (workAreaOnActivity == null) {
        continue;
      }

      boolean flegNewArea = true;
      for (EmployeeDashboardWorkShiftRu employeeDashboardWorkShiftRu : employeeDashboarTotalShift) {

        ProjectAreaRu workAreaAlreadyAdded = employeeDashboardWorkShiftRu.getWorkArea();
        if (workAreaAlreadyAdded.equals(workAreaOnActivity)) {
          employeeDashboardWorkShiftRu.setTotalDayNight(
              employeeDashboardWorkShiftRu.getTotalDayNight().add(new BigDecimal(1)));

          if (dailyActivityLine.getStatusShiftType() == 1) {
            employeeDashboardWorkShiftRu.setTotalWorkDay(
                employeeDashboardWorkShiftRu.getTotalWorkDay().add(new BigDecimal(1)));
          }
          if (dailyActivityLine.getStatusShiftType() == 2) {
            employeeDashboardWorkShiftRu.setTotalworkNight(
                employeeDashboardWorkShiftRu.getTotalworkNight().add(new BigDecimal(1)));
          }
          flegNewArea = false;
        }
      }

      if (flegNewArea) {
        EmployeeDashboardWorkShiftRu employeeshift = new EmployeeDashboardWorkShiftRu();

        employeeshift.setWorkArea(dailyActivityLine.getWorkArea());
        employeeshift.setTotalDayNight(new BigDecimal(1));
        if (dailyActivityLine.getStatusShiftType() == 1) {
          employeeshift.setTotalWorkDay(new BigDecimal(1));
          employeeshift.setTotalworkNight(new BigDecimal(0));
        }
        if (dailyActivityLine.getStatusShiftType() == 2) {
          employeeshift.setTotalworkNight(new BigDecimal(1));
          employeeshift.setTotalWorkDay(new BigDecimal(0));
        }

        employeeDashboarTotalShift.add(employeeshift);
      }
    }
    response.setValue("employeeWorkShift", employeeDashboarTotalShift);
  }

  public void employeeAxtivity(ActionRequest request, ActionResponse response)
      throws AxelorException {
    EmployeeDashboardRu dashboard = request.getContext().asType(EmployeeDashboardRu.class);

    LocalDate fromDate = dashboard.getEmployeeActivityFromDate();
    LocalDate toDate = dashboard.getEmployeeActivityToDate();

    if (toDate == null || fromDate == null) {
      return;
    }

    List<EmployeeDailyActivityLineRu> dailyActivityLineList =
        Beans.get(EmployeeDailyActivityLineRuRepository.class)
            .all()
            .filter("self.activityDate >= ? AND self.activityDate <= ?", fromDate, toDate)
            .fetch();

    List<EmployeeDashboardEmpActivityRu> employeeActivityList =
        new ArrayList<EmployeeDashboardEmpActivityRu>();

    for (EmployeeDailyActivityLineRu employeeDailyActivityLine : dailyActivityLineList) {
      boolean flagNewEmployee = true;
      EmployeeRu employeeOnDailyActivity = employeeDailyActivityLine.getEmployee();
      if (employeeOnDailyActivity == null) {
        continue;
      }

      for (EmployeeDashboardEmpActivityRu employeeDashboardEmpActivity : employeeActivityList) {
        EmployeeRu empAlredyfound = employeeDashboardEmpActivity.getEmployee();
        if (employeeOnDailyActivity.equals(empAlredyfound)) {
          if (employeeDailyActivityLine.getIsAbsence()) {
            employeeDashboardEmpActivity.setTotalDaysNotWorked(
                employeeDashboardEmpActivity.getTotalDaysNotWorked().add(new BigDecimal(1)));
          } else {
            employeeDashboardEmpActivity.setTotalDaysWorked(
                employeeDashboardEmpActivity.getTotalDaysWorked().add(new BigDecimal(1)));
            employeeDashboardEmpActivity.setTotalWorkHours(
                employeeDashboardEmpActivity
                    .getTotalWorkHours()
                    .add(employeeDailyActivityLine.getDailyWorkHours()));
          }
          flagNewEmployee = false;
        }
      }

      if (flagNewEmployee) {
        EmployeeDashboardEmpActivityRu employeeActivity = new EmployeeDashboardEmpActivityRu();
        employeeActivity.setEmployee(employeeDailyActivityLine.getEmployee());

        if (employeeDailyActivityLine.getIsAbsence()) {
          employeeActivity.setTotalDaysNotWorked(new BigDecimal(1));
          employeeActivity.setTotalDaysWorked(new BigDecimal(0));
        } else {
          employeeActivity.setTotalDaysWorked(new BigDecimal(1));
          employeeActivity.setTotalDaysNotWorked(new BigDecimal(0));
          employeeActivity.setTotalWorkHours(employeeDailyActivityLine.getDailyWorkHours());
        }

        employeeActivityList.add(employeeActivity);
      }
    }
    response.setValue("employeeActivity", employeeActivityList);
  }

  public void employeePenalty(ActionRequest request, ActionResponse response)
      throws AxelorException {
    EmployeeDashboardRu dashboard = request.getContext().asType(EmployeeDashboardRu.class);

    LocalDate fromDate = dashboard.getEmployeePenaltyFromDate();
    LocalDate toDate = dashboard.getEmployeePenaltyToDate();

    if (toDate == null || fromDate == null) {
      return;
    }

    List<EmployeeDailyActivityLineRu> dailyActivityLineList =
        Beans.get(EmployeeDailyActivityLineRuRepository.class)
            .all()
            .filter("self.activityDate >= ? AND self.activityDate <= ?", fromDate, toDate)
            .fetch();

    List<EmployeeDashboardEmpPenaltyRu> employeePenaltyList =
        new ArrayList<EmployeeDashboardEmpPenaltyRu>();

    for (EmployeeDailyActivityLineRu employeeDailyActivityLine : dailyActivityLineList) {
      boolean flagNewEmployee = true;
      EmployeeRu employeeOnDailyActivity = employeeDailyActivityLine.getEmployee();
      if (employeeOnDailyActivity == null) {
        continue;
      }

      for (EmployeeDashboardEmpPenaltyRu employeeDashboardEmpPenalty : employeePenaltyList) {
        EmployeeRu empAlredyfound = employeeDashboardEmpPenalty.getEmployee();
        if (employeeOnDailyActivity.equals(empAlredyfound)) {
        	
        	employeeDashboardEmpPenalty.setTotalPenaltyNotCame(employeeDashboardEmpPenalty.getTotalPenaltyNotCame().add( employeeDailyActivityLine.getPenaltyNotCame()));
        	employeeDashboardEmpPenalty.setTotalPenaltyCloths(employeeDashboardEmpPenalty.getTotalPenaltyCloths().add(employeeDailyActivityLine.getPenaltyCloths()));
        	employeeDashboardEmpPenalty.setTotalPenaltyWerehous(employeeDashboardEmpPenalty.getTotalPenaltyWerehous().add(employeeDailyActivityLine.getPenaltyWereHouse()));
        	employeeDashboardEmpPenalty.setTotalPenaltyCompany(employeeDashboardEmpPenalty.getTotalPenaltyCompany().add(employeeDailyActivityLine.getPenaltyCompany()));
        	
        	flagNewEmployee = false;
        }
      }

      if (flagNewEmployee) {
    	  EmployeeDashboardEmpPenaltyRu employeeDashboradPenalty = new EmployeeDashboardEmpPenaltyRu();
         
    	  employeeDashboradPenalty.setEmployee(employeeDailyActivityLine.getEmployee());

    	  employeeDashboradPenalty.setTotalPenaltyNotCame(employeeDailyActivityLine.getPenaltyNotCame());
    	  employeeDashboradPenalty.setTotalPenaltyCloths(employeeDailyActivityLine.getPenaltyCloths());
    	  employeeDashboradPenalty.setTotalPenaltyWerehous(employeeDailyActivityLine.getPenaltyWereHouse());
    	  employeeDashboradPenalty.setTotalPenaltyCompany(employeeDailyActivityLine.getPenaltyCompany());

    	  employeePenaltyList.add(employeeDashboradPenalty);
      }
    }
    response.setValue("employeePenalty", employeePenaltyList);
  }
}
