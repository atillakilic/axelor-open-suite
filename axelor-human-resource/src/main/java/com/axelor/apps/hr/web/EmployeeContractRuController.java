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
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.LocalDate;

@Singleton
public class EmployeeContractRuController {

  public void setCronJobDate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    LocalDate todayDate = LocalDate.now();
    LocalDate newDate = todayDate.plusYears(50);
    response.setValue("cronJobDate", newDate);
  }

  public void updateCronJobDate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    EmployeeContractRu employeeContract = request.getContext().asType(EmployeeContractRu.class);

    if (employeeContract.getEndDate() == null) {
      LocalDate todayDate = LocalDate.now();
      LocalDate newDate = todayDate.plusYears(50);
      response.setValue("cronJobDate", newDate);
    } else {
      response.setValue("cronJobDate", employeeContract.getEndDate());
    }
  }
}
