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

import com.axelor.apps.hr.db.AdvanceRequestLineRu;
import com.axelor.apps.hr.db.AdvanceRequestRu;
import com.axelor.apps.hr.db.EmployeeSalaryRu;
import com.axelor.apps.hr.db.repo.EmployeeSalaryRuRepository;
import com.axelor.apps.hr.service.employee.EmployeeSalaryService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

@Singleton
public class EmployeeAdvancePaymentController {

  @Transactional
  public void confirmRequest(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AdvanceRequestRu employeeSalaryRu = request.getContext().asType(AdvanceRequestRu.class);

    for (AdvanceRequestLineRu advanceRequestLine : employeeSalaryRu.getAdvanceRequestLine()) {

      if (!advanceRequestLine.getConfirmCheckbox()) {
        continue;
      }
      BigDecimal deservedTotal = advanceRequestLine.getDeserveTotal();
      EmployeeSalaryRu empSalary = advanceRequestLine.getEmpSalaryMonth();
      empSalary.setTotalAdvancePay(empSalary.getTotalAdvancePay().add(deservedTotal));
      BigDecimal totalSalary =
          Beans.get(EmployeeSalaryService.class).calculateMonthlySalary(empSalary);
      empSalary.setTotalSalary(totalSalary);
      Beans.get(EmployeeSalaryRuRepository.class).save(empSalary);
    }
  }
}
