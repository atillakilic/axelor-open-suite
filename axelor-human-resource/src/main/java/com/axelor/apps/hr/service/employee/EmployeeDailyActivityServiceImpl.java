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
package com.axelor.apps.hr.service.employee;

import com.axelor.apps.hr.db.EmployeeDailyActivityLineRu;
import com.axelor.apps.hr.db.EmployeeDailyActivityRu;
import com.axelor.apps.hr.db.EmployeeSalaryRu;
import com.axelor.apps.hr.db.repo.EmployeeSalaryRuRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeDailyActivityServiceImpl implements EmployeeDailyActivityService {

  EmployeeSalaryRuRepository employeeSalaryRuRepo = Beans.get(EmployeeSalaryRuRepository.class);

  @Override
  @Transactional
  public boolean updateRecordOnSalary(EmployeeDailyActivityRu dailyActivity)
      throws AxelorException {

    LocalDate dailyDate = dailyActivity.getTodayDate();
    int day = dailyDate.getDayOfMonth();

    for (EmployeeDailyActivityLineRu activityLine : dailyActivity.getActivityRecord()) {
      if (activityLine.getEmployeeActiveSalaryContract() == null) {
        continue;
      }
      if (activityLine.getIsAbsence()) {
        if (activityLine.getSalaryType() == 1) {
          if (activityLine.getAbsenceReason() != null) {
            if (!activityLine.getAbsenceReason().getExcused()) {
              continue;
            }
          } else {
            continue;
          }
        }
      }

      EmployeeSalaryRu employeeSalary = activityLine.getEmployeeActiveSalaryContract();

      if (day == 1) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setOne(activityLine.getDailyWorkHours());
          employeeSalary.setOneSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setOneSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 2) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTwo(activityLine.getDailyWorkHours());
          employeeSalary.setTwoSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTwoSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 3) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setThree(activityLine.getDailyWorkHours());
          employeeSalary.setThreeSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setThreeSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 4) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setFour(activityLine.getDailyWorkHours());
          employeeSalary.setFourSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setFourSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 5) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setFive(activityLine.getDailyWorkHours());
          employeeSalary.setFiveSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setFiveSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 6) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setSix(activityLine.getDailyWorkHours());
          employeeSalary.setSixSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setSixSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 7) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setSeven(activityLine.getDailyWorkHours());
          employeeSalary.setSevenSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setSevenSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 8) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setEight(activityLine.getDailyWorkHours());
          employeeSalary.setEightSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setEightSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 9) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setNine(activityLine.getDailyWorkHours());
          employeeSalary.setNineSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setNineSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 10) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTen(activityLine.getDailyWorkHours());
          employeeSalary.setTenSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTenSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 11) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setEleven(activityLine.getDailyWorkHours());
          employeeSalary.setElevenSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setElevenSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 12) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTwelve(activityLine.getDailyWorkHours());
          employeeSalary.setTwelveSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTwelveSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 13) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setThriteen(activityLine.getDailyWorkHours());
          employeeSalary.setThriteenSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setThriteenSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 14) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setFourteen(activityLine.getDailyWorkHours());
          employeeSalary.setFourteenSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setFourteenSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 15) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setFifteen(activityLine.getDailyWorkHours());
          employeeSalary.setFifteenSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setFifteenSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 16) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setSixteen(activityLine.getDailyWorkHours());
          employeeSalary.setSixteenSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setSixteenSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 17) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setSeventeen(activityLine.getDailyWorkHours());
          employeeSalary.setSeventeenSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setSeventeenSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 18) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setEighteen(activityLine.getDailyWorkHours());
          employeeSalary.setEighteenSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setEighteenSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 19) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setNineteen(activityLine.getDailyWorkHours());
          employeeSalary.setNineteenSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setNineteenSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 20) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTwenty(activityLine.getDailyWorkHours());
          employeeSalary.setTwentySalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTwentySalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 21) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTwentyOne(activityLine.getDailyWorkHours());
          employeeSalary.setTwentyOneSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTwentyOneSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 22) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTwentyTwo(activityLine.getDailyWorkHours());
          employeeSalary.setTwentyTwoSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTwentyTwoSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 23) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTwentyThree(activityLine.getDailyWorkHours());
          employeeSalary.setTwentyThreeSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTwentyThreeSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 24) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTwentyFour(activityLine.getDailyWorkHours());
          employeeSalary.setTwentyFourSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTwentyFourSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 25) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTwentyFive(activityLine.getDailyWorkHours());
          employeeSalary.setTwentyFiveSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTwentyFiveSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 26) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTwentySix(activityLine.getDailyWorkHours());
          employeeSalary.setTwentySixSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTwentySixSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 27) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTwentySeven(activityLine.getDailyWorkHours());
          employeeSalary.setTwentySevenSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTwentySevenSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 28) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTwentyEight(activityLine.getDailyWorkHours());
          employeeSalary.setTwentyEightSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTwentyEightSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 29) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setTwentyNine(activityLine.getDailyWorkHours());
          employeeSalary.setTwentyNineSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setTwentyNineSalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 30) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setThirty(activityLine.getDailyWorkHours());
          employeeSalary.setThirtySalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setThirtySalaryType(activityLine.getSalaryType());
        }
      }

      if (day == 31) {
        if (activityLine.getSalaryType() == 2) { // hourly
          employeeSalary.setThirtyOne(activityLine.getDailyWorkHours());
          employeeSalary.setThirtyOneSalaryType(activityLine.getSalaryType());
        } else { // Monthly
          employeeSalary.setThirtyOneSalaryType(activityLine.getSalaryType());
        }
      }

      BigDecimal penaltyNotCame = new BigDecimal(0);
      BigDecimal penaltyCloths = new BigDecimal(0);
      BigDecimal penaltyWereHouse = new BigDecimal(0);
      BigDecimal penaltyCompany = new BigDecimal(0);

      for (EmployeeDailyActivityLineRu dailyActivityLine : employeeSalary.getDailyActivityLine()) {
        penaltyNotCame = penaltyNotCame.add(dailyActivityLine.getPenaltyNotCame());
        penaltyCloths = penaltyCloths.add(dailyActivityLine.getPenaltyCloths());
        penaltyWereHouse = penaltyWereHouse.add(dailyActivityLine.getPenaltyWereHouse());
        penaltyCompany = penaltyCompany.add(dailyActivityLine.getPenaltyCompany());
      }
      employeeSalary.setPenaltyNotCame(penaltyNotCame);
      employeeSalary.setPenaltyCloths(penaltyCloths);
      employeeSalary.setPenaltyWereHouse(penaltyWereHouse);
      employeeSalary.setPenaltyCompany(penaltyCompany);

      BigDecimal totalSalary =
          Beans.get(EmployeeSalaryService.class).calculateMonthlySalary(employeeSalary);
      employeeSalary.setTotalSalary(totalSalary);
      employeeSalaryRuRepo.save(employeeSalary);
    }

    return false;
  }
}
