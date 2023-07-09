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

import com.axelor.apps.hr.db.EmployeeContractRu;
import com.axelor.apps.hr.db.EmployeeSalaryRu;
import com.axelor.apps.hr.db.ExpencesLineRu;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class EmployeeSalaryerviceImpl implements EmployeeSalaryService {

  @Override
  public BigDecimal calculateMonthlySalary(EmployeeSalaryRu employeeSalary) throws AxelorException {
    BigDecimal totalSalary = new BigDecimal(0);
    BigDecimal fixSalary = employeeSalary.getFixSalary();
    BigDecimal dalityFixSalary = fixSalary.divide(new BigDecimal(30), RoundingMode.HALF_UP);

    BigDecimal hourlyRate = employeeSalary.getHourlyRate();

    if (employeeSalary.getOneSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getOneSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getOne().multiply(hourlyRate));
    }

    if (employeeSalary.getTwoSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTwoSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTwo().multiply(hourlyRate));
    }

    if (employeeSalary.getThreeSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getThreeSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getThree().multiply(hourlyRate));
    }

    if (employeeSalary.getFourSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getFourSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getFour().multiply(hourlyRate));
    }

    if (employeeSalary.getFiveSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getFiveSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getFive().multiply(hourlyRate));
    }

    if (employeeSalary.getSixSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getSixSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getSix().multiply(hourlyRate));
    }

    if (employeeSalary.getSevenSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getSevenSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getSeven().multiply(hourlyRate));
    }

    if (employeeSalary.getEightSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getEightSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getEight().multiply(hourlyRate));
    }

    if (employeeSalary.getNineSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getNineSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getNine().multiply(hourlyRate));
    }

    if (employeeSalary.getTenSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTenSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTen().multiply(hourlyRate));
    }

    if (employeeSalary.getElevenSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getElevenSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getEleven().multiply(hourlyRate));
    }

    if (employeeSalary.getTwelveSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTwelveSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTwelve().multiply(hourlyRate));
    }

    if (employeeSalary.getThriteenSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getThriteenSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getThriteen().multiply(hourlyRate));
    }

    if (employeeSalary.getFourteenSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getFourteenSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getFourteen().multiply(hourlyRate));
    }

    if (employeeSalary.getFifteenSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getFifteenSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getFifteen().multiply(hourlyRate));
    }

    if (employeeSalary.getSixteenSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getSixteenSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getSixteen().multiply(hourlyRate));
    }

    if (employeeSalary.getSeventeenSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getSeventeenSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getSeventeen().multiply(hourlyRate));
    }

    if (employeeSalary.getEighteenSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getEighteenSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getEighteen().multiply(hourlyRate));
    }

    if (employeeSalary.getNineteenSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getNineteenSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getNineteen().multiply(hourlyRate));
    }

    if (employeeSalary.getTwentySalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTwentySalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTwenty().multiply(hourlyRate));
    }

    if (employeeSalary.getTwentyOneSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTwentyOneSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTwentyOne().multiply(hourlyRate));
    }

    if (employeeSalary.getTwentyTwoSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTwentyTwoSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTwentyTwo().multiply(hourlyRate));
    }

    if (employeeSalary.getTwentyThreeSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTwentyThreeSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTwentyThree().multiply(hourlyRate));
    }

    if (employeeSalary.getTwentyFourSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTwentyFourSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTwentyFour().multiply(hourlyRate));
    }

    if (employeeSalary.getTwentyFiveSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTwentyFiveSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTwentyFive().multiply(hourlyRate));
    }

    if (employeeSalary.getTwentySixSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTwentySixSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTwentySix().multiply(hourlyRate));
    }

    if (employeeSalary.getTwentySevenSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTwentySevenSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTwentySeven().multiply(hourlyRate));
    }

    if (employeeSalary.getTwentyEightSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTwentyEightSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTwentyEight().multiply(hourlyRate));
    }

    if (employeeSalary.getTwentyNineSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getTwentyNineSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getTwentyNine().multiply(hourlyRate));
    }

    if (employeeSalary.getThirtySalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getThirtySalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getThirty().multiply(hourlyRate));
    }

    if (employeeSalary.getThirtyOneSalaryType() == 1) { // monthly
      totalSalary = totalSalary.add(dalityFixSalary);
    } else if (employeeSalary.getThirtyOneSalaryType() == 2) { // hourly
      totalSalary = totalSalary.add(employeeSalary.getThirtyOne().multiply(hourlyRate));
    }

    totalSalary = totalSalary.subtract(employeeSalary.getPenaltyCloths());
    totalSalary = totalSalary.subtract(employeeSalary.getPenaltyCompany());
    totalSalary = totalSalary.subtract(employeeSalary.getPenaltyNotCame());
    totalSalary = totalSalary.subtract(employeeSalary.getPenaltyWereHouse());

    System.err.println(totalSalary);

    EmployeeContractRu employeeContractRu = employeeSalary.getEmployeeContract();

    BigDecimal totalExpense = new BigDecimal(0);

    for (ExpencesLineRu expencesLine : employeeContractRu.getEmployeeExpences()) {
      if (expencesLine.getIsActive()) {
        if (expencesLine.getExpencesType() != null) {
          BigDecimal percent = expencesLine.getPayFromCompanyPercent();
          BigDecimal amount = expencesLine.getExpencesType().getPrice();
          totalExpense =
              totalExpense.add(
                  (amount).multiply(percent).divide(new BigDecimal(100), RoundingMode.HALF_UP));
        }
      }
    }
    totalSalary = totalSalary.add(totalExpense);

    return totalSalary;
  }
}
